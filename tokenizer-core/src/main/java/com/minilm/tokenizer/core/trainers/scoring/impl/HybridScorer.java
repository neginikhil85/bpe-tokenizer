package com.minilm.tokenizer.core.trainers.scoring.impl;

import com.minilm.tokenizer.core.trainers.scoring.MergeScorer;
import com.minilm.tokenizer.core.util.Pair;
import java.util.Map;

public class HybridScorer implements MergeScorer {
    private final double lambda;
    private final PMIScorer pmiScorer = new PMIScorer();

    public HybridScorer(double lambda) {
        this.lambda = lambda;
    }

    @Override
    public double score(Pair<Integer, Integer> pair, Map<Pair<Integer, Integer>, Integer> pairCounts, 
                       Map<Integer, Long> tokenCounts, long totalTokens) {
        double freq = pairCounts.getOrDefault(pair, 0);
        double pmi = pmiScorer.score(pair, pairCounts, tokenCounts, totalTokens);
        return (lambda * freq) + ((1 - lambda) * pmi);
    }
}
