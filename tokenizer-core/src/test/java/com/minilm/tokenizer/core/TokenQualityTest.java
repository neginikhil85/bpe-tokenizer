package com.minilm.tokenizer.core;

import com.minilm.tokenizer.core.tokenizers.impl.BPETokenizer;
import com.minilm.tokenizer.core.config.PreTokenizerConfig;
import com.minilm.tokenizer.core.config.TrainerConfig;
import com.minilm.tokenizer.core.config.TokenizerConfig;
import com.minilm.tokenizer.core.pretokenizers.impl.CodeAwarePreTokenizer;
import com.minilm.tokenizer.core.trainers.BPETrainer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Quality tests to ensure the tokenizer learns meaningful subword chunks for code.
 */
public class TokenQualityTest {

    @Test
    public void testCodeVocabularyCompression() {
        // High-quality technical training data - repeat multiple times to boost statistics
        StringBuilder sb = new StringBuilder();
        String base = "distributedTracingSystem kafkaConsumerGroupRebalance nonBlockingReactivePipeline " +
                "idempotencyKeyValidationInterceptor GraphQLFederationGatewayResolver " +
                "distributed tracing system kafka consumer group rebalance non blocking reactive pipeline " +
                "idempotency key validation interceptor graphql federation gateway resolver ";
        for (int i = 0; i < 20; i++) sb.append(base);
        String corpus = sb.toString();

        TrainerConfig trainerConfig = TrainerConfig.builder()
                .vocabSize(1000)
                .strategy(TrainerConfig.ScoringStrategy.HYBRID)
                .lambda(0.3) // Favor PMI for semantic chunks
                .minFrequency(1)
                .build();

        PreTokenizerConfig preConfig = PreTokenizerConfig.builder()
                .splitCamelCase(true)
                .build();

        BPETrainer trainer = new BPETrainer(new CodeAwarePreTokenizer(preConfig), trainerConfig);
        BPETrainer.TrainResult result = trainer.train(corpus);
        
        BPETokenizer tokenizer = new BPETokenizer(result.vocabulary, result.merges, 
                TokenizerConfig.builder()
                        .preTokenizer(new CodeAwarePreTokenizer(preConfig))
                        .build());

        String input = "distributedTracingSystem";
        List<String> pieces = tokenizer.encodeAsPieces(input).stream()
                .map(p -> p.getText())
                .collect(Collectors.toList());

        System.out.println("Input: " + input + " -> Pieces: " + pieces);
        
        // With code-aware pre-tokenization and PMI scoring, it should split into 3 logical chunks
        // instead of random characters.
        assertTrue(pieces.size() <= 3, "Should have 3 or fewer tokens for " + input);
        assertTrue(pieces.contains("distributed"), "Should contain 'distributed'");
        assertTrue(pieces.contains("Tracing"), "Should contain 'Tracing'");
    }
}
