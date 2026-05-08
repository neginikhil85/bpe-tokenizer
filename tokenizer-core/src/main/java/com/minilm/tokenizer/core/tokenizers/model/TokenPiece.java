package com.minilm.tokenizer.core.tokenizers.model;

public class TokenPiece {
    private final int tokenId;
    private final String text;

    public TokenPiece(int tokenId, String text) {
        this.tokenId = tokenId;
        this.text = text;
    }

    public int getTokenId() { return tokenId; }
    public String getText() { return text; }

    @Override
    public String toString() {
        return String.format("[%d:\"%s\"]", tokenId, text);
    }
}
