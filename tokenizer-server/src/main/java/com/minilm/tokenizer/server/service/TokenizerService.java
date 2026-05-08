package com.minilm.tokenizer.server.service;

import com.minilm.tokenizer.core.bpe.BPE;
import com.minilm.tokenizer.core.tokenizers.impl.BPETokenizer;
import com.minilm.tokenizer.core.tokenizers.model.TokenPiece;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Service to handle tokenizer business logic and model lifecycle.
 */
@Service
public class TokenizerService {
    private static final Logger logger = LoggerFactory.getLogger(TokenizerService.class);
    
    @Value("${tokenizer.model-path:}")
    private String modelPath;

    private BPETokenizer tokenizer;

    @PostConstruct
    public void init() {
        try {
            // 1. Try loading from custom filesystem path if provided
            if (modelPath != null && !modelPath.isEmpty()) {
                logger.info("Attempting to load custom model from filesystem: {}", modelPath);
                if (Files.exists(Paths.get(modelPath))) {
                    tokenizer = BPE.load(modelPath);
                    logger.info("Successfully loaded custom BPE model from {}", modelPath);
                    return;
                }
                logger.warn("Custom model path {} not found. Falling back to default.", modelPath);
            }

            // 2. Fallback to default model bundled in resources
            logger.info("Loading default model from classpath resources (/model)");
            tokenizer = BPE.loadFromResources("/model");
            logger.info("Successfully loaded default BPE model from resources.");
            
        } catch (IOException e) {
            logger.error("Failed to load any BPE model. Tokenizer will be unavailable.", e);
        }
    }

    public List<TokenPiece> encode(String text) {
        validateModel();
        return tokenizer.encodeAsPieces(text);
    }

    public String decode(int[] tokens) {
        validateModel();
        return tokenizer.decode(tokens);
    }

    public int[] encodeToIds(String text) {
        validateModel();
        return tokenizer.encode(text);
    }

    private void validateModel() {
        if (tokenizer == null) {
            throw new IllegalStateException("Tokenizer model is not loaded. Please check your configuration.");
        }
    }

    public boolean isLoaded() {
        return tokenizer != null;
    }
}
