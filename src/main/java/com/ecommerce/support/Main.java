package com.ecommerce.support;

import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import com.ecommerce.support.config.DatabaseConfig;
import com.ecommerce.support.orchestrator.SupportOrchestratorAgent;
import com.ecommerce.support.repository.OrderRepository;
import com.ecommerce.support.repository.ProductRepository;
import com.ecommerce.support.repository.RefundRepository;
import com.ecommerce.support.repository.jdbc.JdbcOrderRepository;
import com.ecommerce.support.repository.jdbc.JdbcProductRepository;
import com.ecommerce.support.repository.jdbc.JdbcRefundRepository;
import com.ecommerce.support.repository.mock.MockOrderRepository;
import com.ecommerce.support.repository.mock.MockProductRepository;
import com.ecommerce.support.repository.mock.MockRefundRepository;
import com.ecommerce.support.tools.OrderTools;
import com.ecommerce.support.tools.ProductTools;
import com.ecommerce.support.tools.RefundTools;
import com.google.adk.agents.LlmAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.Runner;
import com.google.adk.sessions.InMemorySessionService;
import com.google.adk.sessions.Session;
import com.google.adk.web.AdkWebServer;
import com.google.genai.types.Content;
import com.google.genai.types.Part;

import io.reactivex.rxjava3.core.Flowable;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final String APP_NAME = "ecommerce-support";
    private static final String USER_ID = "user-001";

    public static void main(String[] args) {
        String apiKey = System.getenv("GOOGLE_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            LOGGER.severe("Error: GOOGLE_API_KEY environment variable is not set.");
            System.exit(1);
        }

        // Wire up repositories — PostgreSQL when DATABASE_URL is set, mocks otherwise
        final OrderRepository orderRepo;
        final RefundRepository refundRepo;
        final ProductRepository productRepo;

        if (System.getenv("DATABASE_URL") != null && System.getenv("DATABASE_USER") != null
          && System.getenv("DATABASE_PASSWORD") != null) {
            javax.sql.DataSource dataSource = DatabaseConfig.createDataSource();
            org.flywaydb.core.Flyway.configure()
                .dataSource(dataSource)
                .load()
                .migrate();
            orderRepo   = new JdbcOrderRepository(dataSource);
            refundRepo  = new JdbcRefundRepository(dataSource);
            productRepo = new JdbcProductRepository(dataSource);
        } else {
            orderRepo   = new MockOrderRepository();
            refundRepo  = new MockRefundRepository();
            productRepo = new MockProductRepository();
        }

        // Wire up tools
        OrderTools orderTools = new OrderTools(orderRepo);
        RefundTools refundTools = new RefundTools(refundRepo, orderRepo);
        ProductTools productTools = new ProductTools(productRepo);

        // Build agent graph
        LlmAgent orchestrator = SupportOrchestratorAgent.create(orderTools, refundTools, productTools);

        // Set up session service and create a session
        InMemorySessionService sessionService = new InMemorySessionService();
        Session session = sessionService
                .createSession(APP_NAME, USER_ID, null, null)
                .blockingGet();

        // Wire runner
        Runner runner = Runner.builder()
                .agent(orchestrator)
                .appName(APP_NAME)
                .sessionService(sessionService)
                .build();

        // Start Dev UI on a background (non-daemon) thread so the CLI loop can run alongside it
        Thread devUiThread = new Thread(() -> AdkWebServer.start(orchestrator), "adk-dev-ui");
        devUiThread.setDaemon(false);
        devUiThread.start();

        LOGGER.info("E-Commerce Support Agent ready...");
        LOGGER.info("Dev UI available at http://localhost:8080");
        LOGGER.info("E-Commerce Support Agent ready. Type 'quit' to exit.");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("You: ");
                String input = scanner.nextLine().trim();
                if (input.equalsIgnoreCase("quit") || input.equalsIgnoreCase("exit")) {
                    System.out.println("Goodbye!");
                    break;
                }
                if (input.isBlank()) continue;
                Content userMessage = Content.builder()
                        .role("user")
                        .parts(List.of(Part.fromText(input)))
                        .build();

                Flowable<Event> events = runner.runAsync(USER_ID, session.id(), userMessage);
                events.blockingForEach(event -> {
                    if (event.finalResponse()) {
                        String response = event.stringifyContent();
                        System.out.println("Agent: " + (response != null && !response.isBlank() ? response : "(no response)"));
                        System.out.println();
                    }
                });
            }
        }
    }
}
