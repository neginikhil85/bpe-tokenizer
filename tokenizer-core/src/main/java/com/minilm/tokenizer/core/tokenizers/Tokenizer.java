package com.minilm.tokenizer.core.tokenizers;

import com.minilm.tokenizer.core.tokenizers.model.TokenPiece;
import java.util.List;

public interface Tokenizer {
    int[] encode(String text);
    List<TokenPiece> encodeAsPieces(String text);
    String decode(int[] tokens);
}
