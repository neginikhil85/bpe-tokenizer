package com.minilm.tokenizer.core.benchmarks;

import com.minilm.tokenizer.core.tokenizers.Tokenizer;
import java.util.List;

/**
 * A benchmark suite to measure tokenizer performance and quality.
 */
public class TokenizerBenchmarkSuite {
    
    public static class BenchmarkResult {
        public final String datasetName;
        public final int totalTokens;
        public final double avgTokensPerWord;
        public final double compressionRatio; // Bytes per token
        public final long encodeTimeMs;

        public BenchmarkResult(String datasetName, int totalTokens, double avgTokensPerWord, double compressionRatio, long encodeTimeMs) {
            this.datasetName = datasetName;
            this.totalTokens = totalTokens;
            this.avgTokensPerWord = avgTokensPerWord;
            this.compressionRatio = compressionRatio;
            this.encodeTimeMs = encodeTimeMs;
        }

        @Override
        public String toString() {
            return String.format(
                "Benchmark [%s]:\n" +
                "  - Total Tokens: %d\n" +
                "  - Avg Tokens/Word: %.3f\n" +
                "  - Compression (Bytes/Token): %.3f\n" +
                "  - Latency: %d ms",
                datasetName, totalTokens, avgTokensPerWord, compressionRatio, encodeTimeMs
            );
        }
    }

    public BenchmarkResult run(String name, String text, Tokenizer tokenizer) {
        long start = System.currentTimeMillis();
        int[] tokens = tokenizer.encode(text);
        long end = System.currentTimeMillis();

        int totalBytes = text.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        int wordCount = text.split("\\s+").length;
        
        double avgTokensPerWord = (double) tokens.length / wordCount;
        double compressionRatio = (double) totalBytes / tokens.length;

        return new BenchmarkResult(name, tokens.length, avgTokensPerWord, compressionRatio, (end - start));
    }
}
