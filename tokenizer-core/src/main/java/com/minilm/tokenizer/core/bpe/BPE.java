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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Facade for BPE model serialization and deserialization.
 */
public class BPE {
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Loads a tokenizer model from a directory on the filesystem.
     */
    public static BPETokenizer load(String modelDir) throws IOException {
        File dir = new File(modelDir);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new FileNotFoundException("Model directory not found: " + modelDir);
        }

        try (InputStream vocabStream = new FileInputStream(new File(dir, "vocab.json"));
             InputStream mergesStream = new FileInputStream(new File(dir, "merges.txt"))) {
            
            InputStream configStream = null;
            File configFile = new File(dir, "model_config.json");
            if (configFile.exists()) {
                configStream = new FileInputStream(configFile);
            }
            
            return loadFromStreams(vocabStream, mergesStream, configStream);
        }
    }

    /**
     * Loads a tokenizer model from the classpath resources.
     * @param resourcePath Path within resources (e.g., "/model")
     */
    public static BPETokenizer loadFromResources(String resourcePath) throws IOException {
        String base = resourcePath.endsWith("/") ? resourcePath : resourcePath + "/";
        
        try (InputStream vocabStream = BPE.class.getResourceAsStream(base + "vocab.json");
             InputStream mergesStream = BPE.class.getResourceAsStream(base + "merges.txt")) {
            
            if (vocabStream == null || mergesStream == null) {
                throw new FileNotFoundException("Model files not found in classpath: " + base);
            }

            InputStream configStream = BPE.class.getResourceAsStream(base + "model_config.json");
            return loadFromStreams(vocabStream, mergesStream, configStream);
        }
    }

    private static BPETokenizer loadFromStreams(InputStream vocabStream, InputStream mergesStream, InputStream configStream) throws IOException {
        // 1. Load Vocab
        Map<String, Integer> vocabMap = mapper.readValue(vocabStream, new TypeReference<Map<String, Integer>>() {});
        Vocabulary vocab = new Vocabulary(vocabMap);

        // 2. Load Merges
        List<MergeRule> merges = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(mergesStream, StandardCharsets.UTF_8))) {
            String line;
            int rank = 0;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length == 3) {
                    merges.add(new MergeRule(new Pair<>(Integer.parseInt(parts[0]), Integer.parseInt(parts[1])), 
                                            Integer.parseInt(parts[2]), rank++));
                }
            }
        }

        // 3. Load Config
        TokenizerConfig.Builder configBuilder = TokenizerConfig.builder();
        if (configStream != null) {
            Map<String, Object> configMap = mapper.readValue(configStream, new TypeReference<Map<String, Object>>() {});
            if (Boolean.TRUE.equals(configMap.get("codeAware"))) {
                configBuilder.preTokenizer(new CodeAwarePreTokenizer(
                        PreTokenizerConfig.builder().splitCamelCase(true).splitSnakeCase(true).build()));
            }
        }

        return new BPETokenizer(vocab, merges, configBuilder.build());
    }

    /**
     * Saves a tokenizer model to a directory.
     */
    public static void save(BPETokenizer tokenizer, String modelDir) throws IOException {
        Path dir = Paths.get(modelDir);
        if (!Files.exists(dir)) Files.createDirectories(dir);

        // Save Vocab
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(dir.resolve("vocab.json").toFile()), StandardCharsets.UTF_8)) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(writer, tokenizer.getVocabulary().getBase64ToIdMap());
        }

        // Save Merges
        try (BufferedWriter writer = Files.newBufferedWriter(dir.resolve("merges.txt"), StandardCharsets.UTF_8)) {
            for (MergeRule rule : tokenizer.getMerges()) {
                writer.write(rule.getPair().getLeft() + " " + rule.getPair().getRight() + " " + rule.getNewId());
                writer.newLine();
            }
        }

        // Save Config
        Map<String, Object> configMap = new java.util.HashMap<>();
        configMap.put("codeAware", tokenizer.getConfig().getPreTokenizer() instanceof CodeAwarePreTokenizer);
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(dir.resolve("model_config.json").toFile()), StandardCharsets.UTF_8)) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(writer, configMap);
        }
    }
}
