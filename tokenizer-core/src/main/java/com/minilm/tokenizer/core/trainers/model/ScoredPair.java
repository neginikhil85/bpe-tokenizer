package com.minilm.tokenizer.core.trainers.model;

import com.minilm.tokenizer.core.util.Pair;
import java.util.Objects;

public class ScoredPair implements Comparable<ScoredPair> {
    public final Pair<Integer, Integer> pair;
    public final double score;

    public ScoredPair(Pair<Integer, Integer> pair, double score) {
        this.pair = pair;
        this.score = score;
    }

    @Override
    public int compareTo(ScoredPair other) {
        int cmp = Double.compare(other.score, this.score);
        if (cmp != 0) return cmp;
        
        int leftCmp = Integer.compare(this.pair.getLeft(), other.pair.getLeft());
        if (leftCmp != 0) return leftCmp;
        return Integer.compare(this.pair.getRight(), other.pair.getRight());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScoredPair that = (ScoredPair) o;
        return Objects.equals(pair, that.pair);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pair);
    }
}
