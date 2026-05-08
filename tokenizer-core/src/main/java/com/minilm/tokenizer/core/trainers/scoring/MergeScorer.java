package com.minilm.tokenizer.core.trainers.scoring;

import com.minilm.tokenizer.core.util.Pair;
import java.util.Map;

public interface MergeScorer {
    double score(Pair<Integer, Integer> pair, Map<Pair<Integer, Integer>, Integer> pairCounts, 
                Map<Integer, Long> tokenCounts, long totalTokens);
}
