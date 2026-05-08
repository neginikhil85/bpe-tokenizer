package com.minilm.tokenizer.core.config;

/**
 * Configuration for the BPE Trainer.
 * Supports advanced scoring strategies like PMI and Hybrid frequency.
 */
public class TrainerConfig {
    public enum ScoringStrategy {
        FREQUENCY,
        PMI,
        HYBRID
    }

    private final int vocabSize;
    private final int minFrequency;
    private final ScoringStrategy strategy;
    private final double lambda; // for hybrid scoring: lambda * freq + (1-lambda) * PMI

    private TrainerConfig(Builder builder) {
        this.vocabSize = builder.vocabSize;
        this.minFrequency = builder.minFrequency;
        this.strategy = builder.strategy;
        this.lambda = builder.lambda;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getVocabSize() {
        return vocabSize;
    }

    public int getMinFrequency() {
        return minFrequency;
    }

    public ScoringStrategy getStrategy() {
        return strategy;
    }

    public double getLambda() {
        return lambda;
    }

    public static class Builder {
        private int vocabSize = 1000;
        private int minFrequency = 1;
        private ScoringStrategy strategy = ScoringStrategy.FREQUENCY;
        private double lambda = 0.7;

        public Builder vocabSize(int size) {
            this.vocabSize = size;
            return this;
        }

        public Builder minFrequency(int minFreq) {
            this.minFrequency = minFreq;
            return this;
        }

        public Builder strategy(ScoringStrategy strategy) {
            this.strategy = strategy;
            return this;
        }

        public Builder lambda(double lambda) {
            this.lambda = lambda;
            return this;
        }

        public TrainerConfig build() {
            return new TrainerConfig(this);
        }
    }
}
