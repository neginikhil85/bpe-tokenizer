package com.minilm.tokenizer.core.bpe;

import com.minilm.tokenizer.core.util.Pair;

/**
 * Represents a single merge rule in Byte-Level BPE.
 * Merges two token IDs into a new token ID.
 */
public class MergeRule implements Comparable<MergeRule> {
    private final Pair<Integer, Integer> pair;
    private final int newId;
    private final int rank;

    public MergeRule(Pair<Integer, Integer> pair, int newId, int rank) {
        this.pair = pair;
        this.newId = newId;
        this.rank = rank;
    }

    public Pair<Integer, Integer> getPair() {
        return pair;
    }

    public int getNewId() {
        return newId;
    }

    public int getRank() {
        return rank;
    }

    @Override
    public int compareTo(MergeRule o) {
        return Integer.compare(this.rank, o.rank);
    }
}
