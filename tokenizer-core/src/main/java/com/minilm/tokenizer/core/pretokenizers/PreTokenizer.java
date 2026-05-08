package com.minilm.tokenizer.core.pretokenizers;

import java.util.List;

public interface PreTokenizer {
    List<String> preTokenize(String text);
}
