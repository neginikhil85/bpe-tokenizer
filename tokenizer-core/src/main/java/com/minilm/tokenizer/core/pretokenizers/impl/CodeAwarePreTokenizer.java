package com.minilm.tokenizer.core.pretokenizers.impl;

import com.minilm.tokenizer.core.config.PreTokenizerConfig;
import com.minilm.tokenizer.core.pretokenizers.PreTokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A production-grade pre-tokenizer optimized for Code (Java, Python, etc.)
 * Handles camelCase, snake_case, and standard GPT-style splits.
 */
public class CodeAwarePreTokenizer implements PreTokenizer {
    private final PreTokenizerConfig config;
    private final Pattern structuralPattern;
    private final Pattern gptPattern;

    public CodeAwarePreTokenizer(PreTokenizerConfig config) {
        this.config = config;
        this.structuralPattern = buildStructuralPattern();
        this.gptPattern = Pattern.compile("'s|'t|'re|'ve|'m|'ll|'d| ?\\p{L}+| ?\\p{N}+| ?[^\\s\\p{L}\\p{N}]+|\\s+(?!\\S)|\\s+");
    }

    private Pattern buildStructuralPattern() {
        // Complex splitting for Code (camelCase, snake_case)
        StringBuilder regexBuilder = new StringBuilder("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])");
        if (config.isSplitSnakeCase()) {
            regexBuilder.append("|(?<=_)|(?=_)");
        }
        return Pattern.compile(regexBuilder.toString());
    }

    @Override
    public List<String> preTokenize(String text) {
        List<String> tokens = new ArrayList<>();
        
        // 1. Structural Split (CamelCase, SnakeCase)
        String[] structuralParts = structuralPattern.split(text);
        
        // 2. Statistical Split (GPT-style) on each part
        for (String part : structuralParts) {
            if (part.isEmpty()) continue;
            Matcher matcher = gptPattern.matcher(part);
            while (matcher.find()) {
                String token = matcher.group();
                if (!token.isEmpty()) {
                    tokens.add(token);
                }
            }
        }
        return tokens;
    }
}
