package com.minilm.tokenizer.server.model.response;

import java.util.List;

public class EncodeResponse {
    private int[] tokens;
    private List<String> pieces;
    private long timeMs;

    public EncodeResponse(int[] tokens, List<String> pieces, long timeMs) {
        this.tokens = tokens;
        this.pieces = pieces;
        this.timeMs = timeMs;
    }

    public int[] getTokens() {
        return tokens;
    }

    public List<String> getPieces() {
        return pieces;
    }

    public long getTimeMs() {
        return timeMs;
    }
}
