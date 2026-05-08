package com.minilm.tokenizer.core.bpe;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minilm.tokenizer.core.config.PreTokenizerConfig;
import com.minilm.tokenizer.core.config.TokenizerConfig;
import com.minilm.tokenizer.core.pretokenizers.impl.CodeAwarePreTokenizer;
import com.minilm.tokenizer.core.tokenizers.impl.BPETokenizer;
import com.minilm.tokenizer.core.util.Pair;
import com.minilm.tokenizer.core.vocabs.Vocabulary;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BPE {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static BPETokenizer load(String modelDir) throws IOException {
        Path vocabPath = Paths.get(modelDir, "vocab.json");
        Path mergesPath = Paths.get(modelDir, "merges.txt");
        Path configPath = Paths.get(modelDir, "model_config.json");

        if (!Files.exists(vocabPath) || !Files.exists(mergesPath)) {
            throw new FileNotFoundException("Model files not found in " + modelDir);
        }

        Map<String, Integer> vocabMap;
        try (Reader jsonReader = new InputStreamReader(new FileInputStream(vocabPath.toFile()), java.nio.charset.StandardCharsets.UTF_8)) {
            vocabMap = mapper.readValue(jsonReader, new TypeReference<Map<String, Integer>>() {});
        }
        Vocabulary vocab = new Vocabulary(vocabMap);

        List<MergeRule> merges = new ArrayList<>();
        List<String> mergeLines = Files.readAllLines(mergesPath, java.nio.charset.StandardCharsets.UTF_8);
        int rank = 0;
        for (String line : mergeLines) {
            String[] parts = line.split(" ");
            if (parts.length == 3) {
                int left = Integer.parseInt(parts[0]);
                int right = Integer.parseInt(parts[1]);
                int newId = Integer.parseInt(parts[2]);
                Pair<Integer, Integer> pair = new Pair<>(left, right);
                merges.add(new MergeRule(pair, newId, rank++));
            }
        }

        TokenizerConfig.Builder configBuilder = TokenizerConfig.builder();
        if (Files.exists(configPath)) {
            Map<String, Object> configMap = mapper.readValue(configPath.toFile(), new TypeReference<Map<String, Object>>() {});
            if (Boolean.TRUE.equals(configMap.get("codeAware"))) {
                configBuilder.preTokenizer(new CodeAwarePreTokenizer(
                        PreTokenizerConfig.builder()
                                .splitCamelCase(true)
                                .splitSnakeCase(true)
                                .build()
                ));
            }
        }

        return new BPETokenizer(vocab, merges, configBuilder.build());
    }

    public static void save(BPETokenizer tokenizer, String modelDir) throws IOException {
        Path dir = Paths.get(modelDir);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        // Save Vocab
        Path vocabPath = dir.resolve("vocab.json");
        try (Writer jsonWriter = new OutputStreamWriter(new FileOutputStream(vocabPath.toFile()), java.nio.charset.StandardCharsets.UTF_8)) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonWriter, tokenizer.getVocabulary().getBase64ToIdMap());
        }

        // Save Merges
        Path mergesPath = dir.resolve("merges.txt");
        try (BufferedWriter writer = Files.newBufferedWriter(mergesPath, java.nio.charset.StandardCharsets.UTF_8)) {
            for (MergeRule rule : tokenizer.getMerges()) {
                writer.write(rule.getPair().getLeft() + " " + rule.getPair().getRight() + " " + rule.getNewId());
                writer.newLine();
            }
        }

        // Save Config Proxy
        Path configPath = dir.resolve("model_config.json");
        Map<String, Object> configMap = new java.util.HashMap<>();
        configMap.put("codeAware", tokenizer.getConfig().getPreTokenizer() instanceof CodeAwarePreTokenizer);
        try (Writer jsonWriter = new OutputStreamWriter(new FileOutputStream(configPath.toFile()), java.nio.charset.StandardCharsets.UTF_8)) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonWriter, configMap);
        }
    }
}
