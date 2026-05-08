package com.minilm.tokenizer.core;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class MassiveTestGenerator {
    private static final String[] HINDI_WORDS = {"नमस्ते", "कैसे", "हैं", "आप", "धन्यवाद", "काम", "हो", "गया", "भैया"};
    private static final String[] TECH_WORDS = {"Middleware", "Service", "Controller", "Repository", "Handler", "Adapter", "Proxy", "Factory", "Manager", "Listener", "Emitter", "Stream", "Buffer", "Thread", "Mutex"};
    private static final String[] PREFIXES = {"User", "Order", "Product", "Payment", "Security", "Audit", "Health", "Metrics", "Database", "File", "S3", "Kafka", "Redis", "Elastic"};
    private static final String[] ACTIONS = {"Authentication", "Validation", "Processing", "Response", "Request", "Transformation", "Routing", "Filtering", "Aggregating"};

    public static void main(String[] args) throws IOException {
        String fileName = "massive_test_corpus.txt";
        Random random = new Random(42);

        try (FileWriter writer = new FileWriter(fileName)) {
            // 1. Generate 2000 Code Scenarios (More diverse)
            for (int i = 0; i < 2000; i++) {
                String camel = PREFIXES[random.nextInt(PREFIXES.length)] + 
                             ACTIONS[random.nextInt(ACTIONS.length)] + 
                             TECH_WORDS[random.nextInt(TECH_WORDS.length)];
                if (random.nextBoolean()) camel += random.nextInt(100); // Add numbers
                String snake = camel.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
                writer.write(camel + " " + snake + " " + camel.toUpperCase() + "\n");
            }

            // 2. Generate 1000 Hinglish Chat Scenarios
            for (int i = 0; i < 1000; i++) {
                writer.write("Bhai, " + camelToSpace(TECH_WORDS[random.nextInt(TECH_WORDS.length)]) + 
                             " me " + random.nextInt(500) + " error aa raha hai. Check " + ACTIONS[random.nextInt(ACTIONS.length)] + " urgent please.\n");
            }

            // 3. Generate 500 Multilingual snippets (Hindi/English mix)
            for (int i = 0; i < 500; i++) {
                writer.write(HINDI_WORDS[random.nextInt(HINDI_WORDS.length)] + " " + 
                             PREFIXES[random.nextInt(PREFIXES.length)] + " " +
                             TECH_WORDS[random.nextInt(TECH_WORDS.length)] + " process complete ho gaya.\n");
            }

            // 4. Generate 500 JSON/Log snippets
            for (int i = 0; i < 500; i++) {
                writer.write("{\"timestamp\": \"" + System.currentTimeMillis() + "\", \"level\": \"DEBUG\", \"component\": \"" + 
                             PREFIXES[random.nextInt(PREFIXES.length)] + "\", \"msg\": \"Successfully " + ACTIONS[random.nextInt(ACTIONS.length)] + "\"}\n");
            }
            
            // 5. Generate 500 Complex Nested Identifiers
            for (int i = 0; i < 500; i++) {
                String nested = PREFIXES[random.nextInt(PREFIXES.length)] + 
                                ACTIONS[random.nextInt(ACTIONS.length)] + 
                                PREFIXES[random.nextInt(PREFIXES.length)] + 
                                TECH_WORDS[random.nextInt(TECH_WORDS.length)];
                writer.write(nested + "\n");
            }
        }
        System.out.println("Massive test corpus generated: " + fileName);
    }

    private static String camelToSpace(String camel) {
        return camel.replaceAll("([a-z])([A-Z])", "$1 $2");
    }
}
