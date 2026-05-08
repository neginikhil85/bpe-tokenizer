package com.minilm.tokenizer.core.config;

/**
 * Configuration for the Pre-Tokenizer behavior.
 */
public class PreTokenizerConfig {
    private final boolean splitCamelCase;
    private final boolean splitSnakeCase;
    private final boolean preserveWhitespace;

    private PreTokenizerConfig(Builder builder) {
        this.splitCamelCase = builder.splitCamelCase;
        this.splitSnakeCase = builder.splitSnakeCase;
        this.preserveWhitespace = builder.preserveWhitespace;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isSplitCamelCase() {
        return splitCamelCase;
    }

    public boolean isSplitSnakeCase() {
        return splitSnakeCase;
    }

    public boolean isPreserveWhitespace() {
        return preserveWhitespace;
    }

    public static class Builder {
        private boolean splitCamelCase = false;
        private boolean splitSnakeCase = false;
        private boolean preserveWhitespace = true;

        public Builder splitCamelCase(boolean enabled) {
            this.splitCamelCase = enabled;
            return this;
        }

        public Builder splitSnakeCase(boolean enabled) {
            this.splitSnakeCase = enabled;
            return this;
        }

        public Builder preserveWhitespace(boolean enabled) {
            this.preserveWhitespace = enabled;
            return this;
        }

        public PreTokenizerConfig build() {
            return new PreTokenizerConfig(this);
        }
    }
}
