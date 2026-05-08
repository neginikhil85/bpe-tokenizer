package com.minilm.tokenizer.core;

import com.minilm.tokenizer.core.bpe.BPE;
import com.minilm.tokenizer.core.config.PreTokenizerConfig;
import com.minilm.tokenizer.core.config.TrainerConfig;
import com.minilm.tokenizer.core.config.TokenizerConfig;
import com.minilm.tokenizer.core.pretokenizers.impl.CodeAwarePreTokenizer;
import com.minilm.tokenizer.core.tokenizers.impl.BPETokenizer;
import com.minilm.tokenizer.core.trainers.BPETrainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class TokenizerTest {

    private static BPETokenizer tokenizer;

    @BeforeAll
    public static void setup() throws IOException {
        String corpus = "Hello world. Hello universe. This is a test. Testing BPE tokenization... " +
                "Wait, does it handle punctuation-heavy text? Yes!!! " +
                "What about emojis? 🚀🚀🚀 Yes it does 😊. " +
                "Multilingual: 你好世界! こんにちは世界! " +
                "Let's test with more text to ensure merges happen. Hello hello hello world world test test. " +
                "Also adding characters for punctuation test: ; ( Hopefully ) [ it works ] -";

        TrainerConfig trainerConfig = TrainerConfig.builder()
                .vocabSize(200)
                .strategy(TrainerConfig.ScoringStrategy.FREQUENCY)
                .build();
        
        PreTokenizerConfig preConfig = PreTokenizerConfig.builder()
                .build();

        BPETrainer trainer = new BPETrainer(new CodeAwarePreTokenizer(preConfig), trainerConfig);
        BPETrainer.TrainResult result = trainer.train(corpus);

        Path tempDir = Files.createTempDirectory("bpe-test-model");
        BPETokenizer trainerTokenizer = new BPETokenizer(result.vocabulary, result.merges, 
                TokenizerConfig.builder()
                        .preTokenizer(new CodeAwarePreTokenizer(preConfig))
                        .build());
        BPE.save(trainerTokenizer, tempDir.toString());

        tokenizer = BPE.load(tempDir.toString());
    }

    @Test
    public void testEncodeDecodeConsistency() {
        String text = "Hello world. Testing BPE...";
        int[] tokens = tokenizer.encode(text);
        String decoded = tokenizer.decode(tokens);
        assertEquals(text, decoded);
    }

    @Test
    public void testEmptyInput() {
        String text = "";
        int[] tokens = tokenizer.encode(text);
        assertArrayEquals(new int[0], tokens);
        String decoded = tokenizer.decode(tokens);
        assertEquals("", decoded);
    }

    @Test
    public void testMultilingualAndEmojis() {
        String text = "你好! こんにちは! 🚀🚀 😊 Hello.";
        int[] tokens = tokenizer.encode(text);
        String decoded = tokenizer.decode(tokens);
        assertEquals(text, decoded);
    }

    @Test
    public void testPunctuationHeavy() {
        String text = "Wait... what?!? Yes, this-is-a-test; right? (Hopefully) [it works]!!";
        int[] tokens = tokenizer.encode(text);
        String decoded = tokenizer.decode(tokens);
        assertEquals(text, decoded);
    }
}
