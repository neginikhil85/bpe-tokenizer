package com.minilm.tokenizer.core.trainers;

import com.minilm.tokenizer.core.bpe.MergeRule;
import com.minilm.tokenizer.core.config.TrainerConfig;
import com.minilm.tokenizer.core.pretokenizers.PreTokenizer;
import com.minilm.tokenizer.core.trainers.model.ScoredPair;
import com.minilm.tokenizer.core.trainers.scoring.MergeScorer;
import com.minilm.tokenizer.core.trainers.scoring.impl.FrequencyScorer;
import com.minilm.tokenizer.core.trainers.scoring.impl.HybridScorer;
import com.minilm.tokenizer.core.trainers.scoring.impl.PMIScorer;
import com.minilm.tokenizer.core.util.Pair;
import com.minilm.tokenizer.core.vocabs.Vocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Production-grade BPE Trainer with O(n log n) optimization.
 */
public class BPETrainer {
    private static final Logger logger = LoggerFactory.getLogger(BPETrainer.class);
    private final TrainerConfig config;
    private final PreTokenizer preTokenizer;
    private final MergeScorer scorer;

    // State maintained during training
    private final Map<Integer, byte[]> idToBytes = new HashMap<>();
    private final Map<Integer, Long> tokenCounts = new HashMap<>();
    private final Set<String> existingSequences = new HashSet<>();
    private final Map<Pair<Integer, Integer>, Integer> pairCounts = new HashMap<>();
    private final List<List<Integer>> corpus = new ArrayList<>();
    private final List<Integer> weights = new ArrayList<>();
    private final PriorityQueue<ScoredPair> pq = new PriorityQueue<>();
    private long totalTokens = 0;

    public BPETrainer(PreTokenizer preTokenizer, TrainerConfig config) {
        this.preTokenizer = preTokenizer;
        this.config = config;
        this.scorer = selectScorer(config);
    }

    private MergeScorer selectScorer(TrainerConfig config) {
        switch (config.getStrategy()) {
            case PMI: return new PMIScorer();
            case HYBRID: return new HybridScorer(config.getLambda());
            default: return new FrequencyScorer();
        }
    }

    public TrainResult train(String text) {
        clearState();
        
        initializeBaseVocabulary();
        prepareCorpusAndCounts(text);
        
        rebuildPQ(pq, pairCounts, tokenCounts, totalTokens);

        List<MergeRule> merges = performMerges();
        
        return finalizeTraining(merges);
    }

    private void clearState() {
        idToBytes.clear();
        tokenCounts.clear();
        existingSequences.clear();
        pairCounts.clear();
        corpus.clear();
        weights.clear();
        pq.clear();
        totalTokens = 0;
    }

    private void initializeBaseVocabulary() {
        for (int i = 0; i < 256; i++) {
            byte[] b = new byte[]{(byte) i};
            idToBytes.put(i, b);
            existingSequences.add(Base64.getEncoder().encodeToString(b));
            tokenCounts.put(i, 0L);
        }
    }

    private void prepareCorpusAndCounts(String text) {
        List<String> words = preTokenizer.preTokenize(text);
        Map<String, Integer> wordCounts = getWordFrequencies(words);

        for (Map.Entry<String, Integer> entry : wordCounts.entrySet()) {
            addWordToCorpus(entry.getKey(), entry.getValue());
        }
        calculateInitialPairCounts();
    }

    private Map<String, Integer> getWordFrequencies(List<String> words) {
        Map<String, Integer> counts = new HashMap<>();
        for (String word : words) {
            counts.put(word, counts.getOrDefault(word, 0) + 1);
        }
        return counts;
    }

    private void addWordToCorpus(String word, int weight) {
        byte[] bytes = word.getBytes(StandardCharsets.UTF_8);
        List<Integer> wordTokens = new ArrayList<>();
        for (byte b : bytes) {
            int id = b & 0xFF;
            wordTokens.add(id);
            tokenCounts.put(id, tokenCounts.getOrDefault(id, 0L) + weight);
            totalTokens += weight;
        }
        corpus.add(wordTokens);
        weights.add(weight);
    }

    private void calculateInitialPairCounts() {
        for (int i = 0; i < corpus.size(); i++) {
            List<Integer> tokens = corpus.get(i);
            int weight = weights.get(i);
            for (int j = 0; j < tokens.size() - 1; j++) {
                Pair<Integer, Integer> p = new Pair<>(tokens.get(j), tokens.get(j + 1));
                pairCounts.put(p, pairCounts.getOrDefault(p, 0) + weight);
            }
        }
    }

    private List<MergeRule> performMerges() {
        List<MergeRule> merges = new ArrayList<>();
        int nextId = 256;
        int mergeCount = 0;

        while (idToBytes.size() < config.getVocabSize() && !pq.isEmpty()) {
            ScoredPair best = pq.poll();
            if (!isValidCandidate(best)) continue;

            byte[] newBytes = concatenateBytes(best.pair);
            if (newBytes == null || isDuplicateSequence(newBytes)) {
                pairCounts.remove(best.pair);
                continue;
            }

            int newId = nextId++;
            idToBytes.put(newId, newBytes);
            existingSequences.add(Base64.getEncoder().encodeToString(newBytes));
            
            MergeRule rule = new MergeRule(best.pair, newId, mergeCount++);
            merges.add(rule);
            
            updateCorpusAndCounts(rule);
        }
        return merges;
    }

    private boolean isValidCandidate(ScoredPair best) {
        Integer currentCount = pairCounts.get(best.pair);
        if (currentCount == null || currentCount <= 0) return false;

        double currentScore = scorer.score(best.pair, pairCounts, tokenCounts, totalTokens);
        if (Math.abs(currentScore - best.score) > 1e-6) {
            if (currentScore > Double.NEGATIVE_INFINITY) pq.add(new ScoredPair(best.pair, currentScore));
            return false;
        }
        return true;
    }

    private byte[] concatenateBytes(Pair<Integer, Integer> pair) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            out.writeBytes(idToBytes.get(pair.getLeft()));
            out.writeBytes(idToBytes.get(pair.getRight()));
            return out.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isDuplicateSequence(byte[] bytes) {
        return existingSequences.contains(Base64.getEncoder().encodeToString(bytes));
    }

    private void updateCorpusAndCounts(MergeRule rule) {
        Set<Pair<Integer, Integer>> affected = new HashSet<>();
        long totalPairsMerged = 0;

        for (int i = 0; i < corpus.size(); i++) {
            totalPairsMerged += applyMergeToLine(i, rule, affected);
        }

        updateTokenStatistics(rule, totalPairsMerged);
        recalculateScores(affected);
    }

    private long applyMergeToLine(int lineIndex, MergeRule rule, Set<Pair<Integer, Integer>> affected) {
        List<Integer> oldTokens = corpus.get(lineIndex);
        int weight = weights.get(lineIndex);
        if (!containsPair(oldTokens, rule.getPair())) return 0;

        List<Integer> newTokens = new ArrayList<>();
        long mergesOnLine = 0;
        int j = 0;
        while (j < oldTokens.size()) {
            if (isTargetPair(oldTokens, j, rule.getPair())) {
                updateNeighbors(oldTokens, j, weight, affected);
                newTokens.add(rule.getNewId());
                mergesOnLine += weight;
                j += 2;
                if (j < oldTokens.size()) updatePairCount(new Pair<>(rule.getNewId(), oldTokens.get(j)), weight, affected);
                if (newTokens.size() > 1) updatePairCount(new Pair<>(newTokens.get(newTokens.size()-2), rule.getNewId()), weight, affected);
            } else {
                newTokens.add(oldTokens.get(j));
                j++;
            }
        }
        corpus.set(lineIndex, newTokens);
        return mergesOnLine;
    }

    private boolean containsPair(List<Integer> tokens, Pair<Integer, Integer> pair) {
        for (int i = 0; i < tokens.size() - 1; i++) {
            if (tokens.get(i).equals(pair.getLeft()) && tokens.get(i + 1).equals(pair.getRight())) return true;
        }
        return false;
    }

    private boolean isTargetPair(List<Integer> tokens, int index, Pair<Integer, Integer> pair) {
        return index < tokens.size() - 1 && tokens.get(index).equals(pair.getLeft()) && tokens.get(index + 1).equals(pair.getRight());
    }

    private void updateNeighbors(List<Integer> tokens, int index, int weight, Set<Pair<Integer, Integer>> affected) {
        if (index > 0) updatePairCount(new Pair<>(tokens.get(index - 1), tokens.get(index)), -weight, affected);
        if (index < tokens.size() - 2) {
            if (!isTargetPair(tokens, index + 1, tokens.get(index), tokens.get(index + 1))) { // Only if next isn't also the pair
                 updatePairCount(new Pair<>(tokens.get(index + 1), tokens.get(index + 2)), -weight, affected);
            }
        }
        updatePairCount(new Pair<>(tokens.get(index), tokens.get(index + 1)), -weight, affected);
    }

    private boolean isTargetPair(List<Integer> tokens, int index, int left, int right) {
        return index < tokens.size() - 1 && tokens.get(index) == left && tokens.get(index+1) == right;
    }

    private void updateTokenStatistics(MergeRule rule, long totalPairsMerged) {
        int left = rule.getPair().getLeft();
        int right = rule.getPair().getRight();
        tokenCounts.put(left, tokenCounts.get(left) - totalPairsMerged);
        tokenCounts.put(right, tokenCounts.get(right) - totalPairsMerged);
        tokenCounts.put(rule.getNewId(), totalPairsMerged);
        totalTokens -= totalPairsMerged;
        pairCounts.remove(rule.getPair());
    }

    private void recalculateScores(Set<Pair<Integer, Integer>> affected) {
        for (Pair<Integer, Integer> p : affected) {
            double s = scorer.score(p, pairCounts, tokenCounts, totalTokens);
            if (s > Double.NEGATIVE_INFINITY) pq.add(new ScoredPair(p, s));
        }
    }

    private void updatePairCount(Pair<Integer, Integer> pair, int delta, Set<Pair<Integer, Integer>> affectedPairs) {
        int newCount = pairCounts.getOrDefault(pair, 0) + delta;
        if (newCount <= 0) pairCounts.remove(pair);
        else pairCounts.put(pair, newCount);
        affectedPairs.add(pair);
    }

    private void rebuildPQ(PriorityQueue<ScoredPair> pq, Map<Pair<Integer, Integer>, Integer> pairCounts, 
                            Map<Integer, Long> tokenCounts, long totalTokens) {
        pq.clear();
        for (Map.Entry<Pair<Integer, Integer>, Integer> entry : pairCounts.entrySet()) {
            double score = scorer.score(entry.getKey(), pairCounts, tokenCounts, totalTokens);
            if (score > Double.NEGATIVE_INFINITY) {
                pq.add(new ScoredPair(entry.getKey(), score));
            }
        }
    }

    private TrainResult finalizeTraining(List<MergeRule> merges) {
        Map<String, Integer> base64ToId = new HashMap<>();
        for (Map.Entry<Integer, byte[]> entry : idToBytes.entrySet()) {
            base64ToId.put(Base64.getEncoder().encodeToString(entry.getValue()), entry.getKey());
        }
        return new TrainResult(new Vocabulary(base64ToId), merges);
    }

    public static class TrainResult {
        public final Vocabulary vocabulary;
        public final List<MergeRule> merges;

        public TrainResult(Vocabulary vocabulary, List<MergeRule> merges) {
            this.vocabulary = vocabulary;
            this.merges = merges;
        }
    }
}
