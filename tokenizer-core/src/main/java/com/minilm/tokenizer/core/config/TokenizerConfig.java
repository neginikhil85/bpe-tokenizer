package com.minilm.tokenizer.core.config;

import com.minilm.tokenizer.core.pretokenizers.PreTokenizer;
import com.minilm.tokenizer.core.pretokenizers.impl.UnicodePreTokenizer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * High-level configuration for the Tokenizer runtime.
 */
public class TokenizerConfig {
    private final PreTokenizer preTokenizer;
    private final boolean byteLevelFallback;
    private final Set<String> specialTokens;

    private TokenizerConfig(Builder builder) {
        this.preTokenizer = builder.preTokenizer != null ? builder.preTokenizer : new UnicodePreTokenizer();
        this.byteLevelFallback = builder.byteLevelFallback;
        this.specialTokens = Collections.unmodifiableSet(new HashSet<>(builder.specialTokens));
    }

    public static Builder builder() {
        return new Builder();
    }

    public PreTokenizer getPreTokenizer() {
        return preTokenizer;
    }

    public boolean isByteLevelFallback() {
        return byteLevelFallback;
    }

    public Set<String> getSpecialTokens() {
        return specialTokens;
    }

    public static class Builder {
        private PreTokenizer preTokenizer;
        private boolean byteLevelFallback = true;
        private final Set<String> specialTokens = new HashSet<>();

        public Builder preTokenizer(PreTokenizer preTokenizer) {
            this.preTokenizer = preTokenizer;
            return this;
        }

        public Builder byteLevelFallback(boolean enabled) {
            this.byteLevelFallback = enabled;
            return this;
        }

        public Builder addSpecialToken(String token) {
            this.specialTokens.add(token);
            return this;
        }

        public TokenizerConfig build() {
            return new TokenizerConfig(this);
        }
    }
}
