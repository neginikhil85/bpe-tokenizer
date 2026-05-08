package com.minilm.tokenizer.core.corpora;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CorpusMixer {
    public static String mix(List<String> corpora, List<Double> weights, int totalLines) {
        StringBuilder mixed = new StringBuilder();
        Random random = new Random();
        
        List<String[]> lines = new ArrayList<>();
        for (String corpus : corpora) {
            lines.add(corpus.split("\n"));
        }

        for (int i = 0; i < totalLines; i++) {
            double r = random.nextDouble();
            double cumulative = 0.0;
            for (int j = 0; j < weights.size(); j++) {
                cumulative += weights.get(j);
                if (r <= cumulative) {
                    String[] corpusLines = lines.get(j);
                    mixed.append(corpusLines[random.nextInt(corpusLines.length)]).append("\n");
                    break;
                }
            }
        }
        return mixed.toString();
    }
}
