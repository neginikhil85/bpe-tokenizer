package com.minilm.tokenizer.server.model.response;

public class DecodeResponse {
    private String text;

    public DecodeResponse(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
