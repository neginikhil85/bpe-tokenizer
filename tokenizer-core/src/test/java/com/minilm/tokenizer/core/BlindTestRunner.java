package com.minilm.tokenizer.core;

import com.minilm.tokenizer.core.bpe.BPE;
import com.minilm.tokenizer.core.tokenizers.impl.BPETokenizer;
import com.minilm.tokenizer.core.benchmarks.TokenizerBenchmarkSuite;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BlindTestRunner {
    public static void main(String[] args) throws Exception {
        String modelDir = "model";
        if (!Files.exists(Paths.get(modelDir))) {
            modelDir = "../model";
        }
        
        String testFile = "massive_test_corpus.txt";
        if (!Files.exists(Paths.get(testFile))) {
            testFile = "tokenizer-core/massive_test_corpus.txt";
        }
        
        if (!Files.exists(Paths.get(modelDir))) {
            System.err.println("Model directory not found at " + Paths.get(modelDir).toAbsolutePath() + ". Please train the model first.");
            return;
        }
        
        System.out.println("Loading model from: " + modelDir);
        BPETokenizer tokenizer = BPE.load(modelDir);
        
        String testData = new String(Files.readAllBytes(Paths.get(testFile)), java.nio.charset.StandardCharsets.UTF_8);
        
        TokenizerBenchmarkSuite benchmark = new TokenizerBenchmarkSuite();
        TokenizerBenchmarkSuite.BenchmarkResult result = benchmark.run("Blind Test (Unseen Data)", testData, tokenizer);
        
        System.out.println("\n==========================================");
        System.out.println(result);
        System.out.println("==========================================\n");
        
        // Competitor Comparison Table
        System.out.println("--- COMPETITOR HEAD-TO-HEAD (tiktoken cl100k_base vs Ours) ---");
        System.out.printf("%-40s | %-10s | %-10s\n", "Test Case", "Tiktoken", "Ours");
        System.out.println("-----------------------------------------|------------|-----------");

        String[] benchmarkCases = {
            "PaymentTransformationListener",
            "kafkaConsumerGroupRebalance",
            "OutOfMemoryError",
            "nonBlockingReactivePipeline",
            "नमस्ते",
            "Bhai, code deploy nahi ho raha.",
            "{\"level\": \"ERROR\", \"message\": \"Failed to Authentication at 1715019632456\"}"
        };

        int[] tiktokenCounts = {3, 6, 3, 5, 6, 11, 21};

        for (int i = 0; i < benchmarkCases.length; i++) {
            String text = benchmarkCases[i];
            int oursCount = tokenizer.encode(text).length;
            System.out.printf("%-40s | %-10d | %-10d\n", 
                text.length() > 40 ? text.substring(0, 37) + "..." : text, 
                tiktokenCounts[i], oursCount);
        }
        System.out.println("------------------------------------------------------------------");
    }
}
