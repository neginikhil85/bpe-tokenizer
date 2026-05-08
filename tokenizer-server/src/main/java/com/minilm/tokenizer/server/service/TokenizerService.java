package com.minilm.tokenizer.server.service;

import com.minilm.tokenizer.core.bpe.BPE;
import com.minilm.tokenizer.core.tokenizers.impl.BPETokenizer;
import com.minilm.tokenizer.core.tokenizers.model.TokenPiece;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private BPETokenizer tokenizer;

    @PostConstruct
    public void init() {
        try {
            // Default load path for local development
            if (Files.exists(Paths.get("model"))) {
                tokenizer = BPE.load("model");
            }
        } catch (IOException e) {
            logger.error("Failed to load default model during initialization", e);
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
            throw new IllegalStateException("Tokenizer model is not loaded.");
        }
    }

    public boolean isLoaded() {
        return tokenizer != null;
    }
}
