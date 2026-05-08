package com.minilm.tokenizer.core.trainers.scoring.impl;

import com.minilm.tokenizer.core.trainers.scoring.MergeScorer;
import com.minilm.tokenizer.core.util.Pair;
import java.util.Map;

public class PMIScorer implements MergeScorer {
    @Override
    public double score(Pair<Integer, Integer> pair, Map<Pair<Integer, Integer>, Integer> pairCounts, 
                       Map<Integer, Long> tokenCounts, long totalTokens) {
        long countAB = pairCounts.getOrDefault(pair, 0);
        if (countAB == 0) return Double.NEGATIVE_INFINITY;

        long countA = tokenCounts.getOrDefault(pair.getLeft(), 0L);
        long countB = tokenCounts.getOrDefault(pair.getRight(), 0L);

        if (countA == 0 || countB == 0) return Double.NEGATIVE_INFINITY;

        // PMI = log( P(A,B) / (P(A) * P(B)) )
        // Using counts: log( (countAB / totalTokens) / ((countA / totalTokens) * (countB / totalTokens)) )
        // Simplified: log( (countAB * totalTokens) / (countA * countB) )
        return Math.log((double) (countAB * totalTokens) / (countA * countB));
    }
}
