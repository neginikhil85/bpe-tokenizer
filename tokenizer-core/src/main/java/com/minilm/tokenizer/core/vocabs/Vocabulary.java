package com.minilm.tokenizer.core.vocabs;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Vocabulary {
    private final Map<String, Integer> base64ToId;
    private final Map<Integer, byte[]> idToBytes;

    public Vocabulary(Map<String, Integer> base64ToId) {
        this.base64ToId = base64ToId;
        this.idToBytes = new HashMap<>();
        for (Map.Entry<String, Integer> entry : base64ToId.entrySet()) {
            idToBytes.put(entry.getValue(), Base64.getDecoder().decode(entry.getKey()));
        }
    }

    public int getId(String base64) {
        return base64ToId.getOrDefault(base64, -1);
    }

    public byte[] getBytes(int id) {
        return idToBytes.get(id);
    }

    public String decode(int id) {
        byte[] bytes = idToBytes.get(id);
        if (bytes == null) return "";
        return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
    }

    public Map<String, Integer> getBase64ToIdMap() {
        return base64ToId;
    }

    public int size() {
        return base64ToId.size();
    }
}
