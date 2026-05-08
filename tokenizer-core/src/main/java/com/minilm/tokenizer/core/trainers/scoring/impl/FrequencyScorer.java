package com.minilm.tokenizer.core.trainers.scoring.impl;

import com.minilm.tokenizer.core.trainers.scoring.MergeScorer;
import com.minilm.tokenizer.core.util.Pair;
import java.util.Map;

public class FrequencyScorer implements MergeScorer {
    @Override
    public double score(Pair<Integer, Integer> pair, Map<Pair<Integer, Integer>, Integer> pairCounts, 
                       Map<Integer, Long> tokenCounts, long totalTokens) {
        return pairCounts.getOrDefault(pair, 0);
    }
}
