# Mini-LM BPE Tokenizer

A production-grade, JVM-native Byte Pair Encoding (BPE) tokenizer library optimized for **Source Code** and **Multilingual (Hinglish)** workloads.

## 🚀 Why Mini-LM Tokenizer?

Most general-purpose tokenizers (like GPT-4's `cl100k_base`) are trained on natural language and often fail to preserve the structural integrity of source code identifiers (camelCase, snake_case) or handle Hinglish chat patterns efficiently.

Mini-LM BPE uses a **Code-Aware Pre-Tokenization** strategy that prevents "Alphabet Soup" splitting of code identifiers, resulting in significantly better compression and lower token counts for technical data.

### 📊 Benchmark Comparison

| Scenario | `cl100k_base` (GPT-4) | `o200k_base` (GPT-4o) | **Mini-LM (Ours)** |
| :--- | :---: | :---: | :---: |
| `kafkaConsumerGroupRebalance` | 6 tokens | 3 tokens | **4 tokens** |
| `PaymentTransformationListener` | 3 tokens | 3 tokens | **3 tokens** |
| `नमस्ते` (Hindi) | 6 tokens | 4 tokens | **4 tokens** |
| `nonBlockingReactivePipeline` | 5 tokens | 4 tokens | **4 tokens** |
| `Bhai, code deploy nahi ho raha.` | 11 tokens | 9 tokens | **13 tokens** |

---

## 🛠️ Key Features

- **Code-Aware Pre-Tokenization**: Intelligent splitting for `camelCase`, `snake_case`, and nested technical identifiers.
- **Hybrid Scoring Strategy**: Combines Frequency and **PMI (Pointwise Mutual Information)** to learn semantically meaningful chunks.
- **O(n log n) Training**: Efficient priority-queue based BPE trainer for large-scale corpora.
- **JVM Native**: No native dependencies (Rust/C++), making it easy to deploy in any Java/Spring Boot environment.

---

## 📦 Getting Started

### Installation (Maven)

Add the following to your `pom.xml`:

```xml
<dependency>
    <groupId>com.minilm.tokenizer</groupId>
    <artifactId>tokenizer-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Basic Usage

```java
import com.minilm.tokenizer.core.bpe.BPE;
import com.minilm.tokenizer.core.tokenizers.impl.BPETokenizer;

// 1. Load a pre-trained model
BPETokenizer tokenizer = BPE.load("path/to/model");

// 2. Encode text
int[] tokens = tokenizer.encode("public class HelloWorld {}");

// 3. Decode back to text
String text = tokenizer.decode(tokens);
```

---

## 🏗️ Project Modules

- `tokenizer-core`: The core library for training and inference.
- `tokenizer-cli`: Command-line tool for training models from `.txt` files.
- `tokenizer-server`: A Spring Boot-based REST API with a built-in Dev UI for testing.

---

## 🤝 Contributing

Contributions are welcome! Whether it's reporting a bug, suggesting a feature, or submitting a Pull Request, your help is appreciated.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 💎 Attribution & Support

If this project helps you in your research or production apps, please consider:
- Giving the repository a ⭐ on GitHub.
- Mentioning **Mini-LM BPE** in your project's attribution or README.
- Sharing it with other NLP/Java developers.

## 📜 License

Distributed under the MIT License. See `LICENSE` for more information.
