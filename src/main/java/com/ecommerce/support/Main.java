package com.ecommerce.support;

import com.google.adk.agents.LlmAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.Runner;
import com.google.adk.sessions.InMemorySessionService;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.ecommerce.support.orchestrator.SupportOrchestratorAgent;
import com.ecommerce.support.repository.mock.MockOrderRepository;
import com.ecommerce.support.repository.mock.MockProductRepository;
import com.ecommerce.support.repository.mock.MockRefundRepository;
import com.ecommerce.support.tools.OrderTools;
import com.ecommerce.support.tools.ProductTools;
import com.ecommerce.support.tools.RefundTools;
import io.reactivex.rxjava3.core.Flowable;

import java.util.List;
import java.util.Scanner;

public class Main {

    private static final String APP_NAME = "ecommerce-support";
    private static final String USER_ID = "user-001";

    public static void main(String[] args) throws Exception {
        String apiKey = System.getenv("GOOGLE_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("Error: GOOGLE_API_KEY environment variable is not set.");
            System.exit(1);
        }

        // Wire up repositories
        MockOrderRepository orderRepo = new MockOrderRepository();
        MockRefundRepository refundRepo = new MockRefundRepository();
        MockProductRepository productRepo = new MockProductRepository();

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
        Runner runner = new Runner(orchestrator, APP_NAME, null, sessionService);

        System.out.println("E-Commerce Support Agent ready. Type 'quit' to exit.\n");

        Scanner scanner = new Scanner(System.in);
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
        scanner.close();
    }
}
