package com.minilm.tokenizer.core;

import com.minilm.tokenizer.core.config.PreTokenizerConfig;
import com.minilm.tokenizer.core.pretokenizers.impl.CodeAwarePreTokenizer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PreTokenizerTest {
    @Test
    public void testCamelCaseSplitting() {
        PreTokenizerConfig config = PreTokenizerConfig.builder()
                .splitCamelCase(true)
                .build();
        CodeAwarePreTokenizer preTokenizer = new CodeAwarePreTokenizer(config);
        
        List<String> tokens = preTokenizer.preTokenize("distributedTracingSystem");
        System.out.println("Tokens: " + tokens);
        
        assertTrue(tokens.contains("distributed"));
        assertTrue(tokens.contains("Tracing"));
        assertTrue(tokens.contains("System"));
    }
}
