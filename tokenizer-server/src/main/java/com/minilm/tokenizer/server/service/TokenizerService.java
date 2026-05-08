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
    
    @Value("${tokenizer.model-path:../model}")
    private String modelPath;

    private BPETokenizer tokenizer;

    @PostConstruct
    public void init() {
        try {
            logger.info("Initializing TokenizerService with model at: {}", modelPath);
            if (Files.exists(Paths.get(modelPath))) {
                tokenizer = BPE.load(modelPath);
                logger.info("Successfully loaded BPE model from {}", modelPath);
            } else {
                logger.warn("Model path {} does not exist. Tokenizer will not be available.", modelPath);
            }
        } catch (IOException e) {
            logger.error("Failed to load model from " + modelPath, e);
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
            throw new IllegalStateException("Tokenizer model is not loaded. Please check the model path: " + modelPath);
        }
    }

    public boolean isLoaded() {
        return tokenizer != null;
    }
}
