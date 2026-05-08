package com.minilm.tokenizer.core.pretokenizers.impl;

import com.minilm.tokenizer.core.pretokenizers.PreTokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnicodePreTokenizer implements PreTokenizer {
    private static final Pattern PATTERN = Pattern.compile("'s|'t|'re|'ve|'m|'ll|'d| ?\\p{L}+| ?\\p{N}+| ?[^\\s\\p{L}\\p{N}]+|\\s+(?!\\S)|\\s+");

    @Override
    public List<String> preTokenize(String text) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = PATTERN.matcher(text);
        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        return tokens;
    }
}
