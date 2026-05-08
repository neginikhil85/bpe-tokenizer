package com.minilm.tokenizer.core.tokenizers.impl;

import com.minilm.tokenizer.core.bpe.MergeRule;
import com.minilm.tokenizer.core.config.TokenizerConfig;
import com.minilm.tokenizer.core.tokenizers.Tokenizer;
import com.minilm.tokenizer.core.tokenizers.model.TokenPiece;
import com.minilm.tokenizer.core.util.Pair;
import com.minilm.tokenizer.core.vocabs.Vocabulary;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Implementation of BPE Tokenizer following Clean Code standards.
 */
public class BPETokenizer implements Tokenizer {
    private final Vocabulary vocabulary;
    private final List<MergeRule> merges;
    private final TokenizerConfig config;
    private final Map<Pair<Integer, Integer>, Integer> mergeRanks;

    public BPETokenizer(Vocabulary vocabulary, List<MergeRule> merges, TokenizerConfig config) {
        this.vocabulary = vocabulary;
        this.merges = merges;
        this.config = config;
        this.mergeRanks = initializeMergeRanks(merges);
    }

    private Map<Pair<Integer, Integer>, Integer> initializeMergeRanks(List<MergeRule> merges) {
        Map<Pair<Integer, Integer>, Integer> ranks = new HashMap<>();
        for (int i = 0; i < merges.size(); i++) {
            ranks.put(merges.get(i).getPair(), i);
        }
        return ranks;
    }

    @Override
    public int[] encode(String text) {
        return encodeAsPieces(text).stream()
                .mapToInt(TokenPiece::getTokenId)
                .toArray();
    }

    @Override
    public List<TokenPiece> encodeAsPieces(String text) {
        List<String> preTokens = config.getPreTokenizer().preTokenize(text);
        List<TokenPiece> allPieces = new ArrayList<>();

        for (String preToken : preTokens) {
            allPieces.addAll(encodeWord(preToken));
        }
        return allPieces;
    }

    @Override
    public String decode(int[] tokens) {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        for (int id : tokens) {
            byte[] bytes = vocabulary.getBytes(id);
            if (bytes != null) {
                out.write(bytes, 0, bytes.length);
            }
        }
        return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }

    private List<TokenPiece> encodeWord(String word) {
        List<Integer> ids = convertToInitialByteIds(word);
        
        while (ids.size() >= 2) {
            Pair<Integer, Integer> bestPair = findBestMergePair(ids);
            if (bestPair == null) break;
            
            ids = applyMerge(ids, bestPair);
        }

        return convertToTokenPieces(ids);
    }

    private List<Integer> convertToInitialByteIds(String word) {
        byte[] bytes = word.getBytes(StandardCharsets.UTF_8);
        List<Integer> ids = new ArrayList<>();
        for (byte b : bytes) {
            ids.add(b & 0xFF);
        }
        return ids;
    }

    private Pair<Integer, Integer> findBestMergePair(List<Integer> ids) {
        Pair<Integer, Integer> bestPair = null;
        int bestRank = Integer.MAX_VALUE;

        for (int i = 0; i < ids.size() - 1; i++) {
            Pair<Integer, Integer> pair = new Pair<>(ids.get(i), ids.get(i + 1));
            Integer rank = mergeRanks.get(pair);
            if (rank != null && rank < bestRank) {
                bestRank = rank;
                bestPair = pair;
            }
        }
        return bestPair;
    }

    private List<Integer> applyMerge(List<Integer> ids, Pair<Integer, Integer> pair) {
        int newId = merges.get(mergeRanks.get(pair)).getNewId();
        List<Integer> nextIds = new ArrayList<>();
        int i = 0;
        while (i < ids.size()) {
            if (isMatchingPair(ids, i, pair)) {
                nextIds.add(newId);
                i += 2;
            } else {
                nextIds.add(ids.get(i));
                i++;
            }
        }
        return nextIds;
    }

    private boolean isMatchingPair(List<Integer> ids, int index, Pair<Integer, Integer> pair) {
        return index < ids.size() - 1 && ids.get(index).equals(pair.getLeft()) && ids.get(index + 1).equals(pair.getRight());
    }

    private List<TokenPiece> convertToTokenPieces(List<Integer> ids) {
        List<TokenPiece> pieces = new ArrayList<>();
        for (int id : ids) {
            pieces.add(new TokenPiece(id, vocabulary.decode(id)));
        }
        return pieces;
    }

    public Vocabulary getVocabulary() { return vocabulary; }
    public List<MergeRule> getMerges() { return merges; }
    public TokenizerConfig getConfig() { return config; }
}
