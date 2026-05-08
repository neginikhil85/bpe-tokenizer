package com.minilm.tokenizer.cli;

import com.minilm.tokenizer.core.bpe.BPE;
import com.minilm.tokenizer.core.config.PreTokenizerConfig;
import com.minilm.tokenizer.core.config.TrainerConfig;
import com.minilm.tokenizer.core.pretokenizers.impl.CodeAwarePreTokenizer;
import com.minilm.tokenizer.core.tokenizers.impl.BPETokenizer;
import com.minilm.tokenizer.core.tokenizers.model.TokenPiece;
import com.minilm.tokenizer.core.trainers.BPETrainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Clean entry point for the Tokenizer CLI.
 * Delegates work to specialized command handlers to respect SRP.
 */
public class TokenizerCLI {
    private static final Logger logger = LoggerFactory.getLogger(TokenizerCLI.class);

    public static void main(String[] args) {
        if (args.length == 0) {
            printHelp();
            return;
        }

        String command = args[0];
        try {
            executeCommand(command, args);
        } catch (Exception e) {
            logger.error("Fatal Error during command execution", e);
        }
    }

    private static void executeCommand(String command, String[] args) throws IOException {
        switch (command) {
            case "train":
                handleTrain(args);
                break;
            case "encode":
                handleEncode(args);
                break;
            case "decode":
                handleDecode(args);
                break;
            default:
                logger.error("Unknown command: {}", command);
                printHelp();
        }
    }

    private static void handleTrain(String[] args) throws IOException {
        TrainCommand cmd = new TrainCommand(args);
        if (cmd.isValid()) {
            cmd.run();
        }
    }

    private static void handleEncode(String[] args) throws IOException {
        EncodeCommand cmd = new EncodeCommand(args);
        if (cmd.isValid()) {
            cmd.run();
        }
    }

    private static void handleDecode(String[] args) throws IOException {
        DecodeCommand cmd = new DecodeCommand(args);
        if (cmd.isValid()) {
            cmd.run();
        }
    }

    private static void printHelp() {
        System.out.println("Usage:");
        System.out.println("  train --input <corpus.txt> --output <model_dir> [--vocab-size 10000]");
        System.out.println("  encode --model <model_dir> --text \"Hello world\"");
        System.out.println("  decode --model <model_dir> --tokens \"1,45,23\"");
    }

    /**
     * Inner Command abstractions to separate parsing from logic.
     */
    static abstract class BaseCommand {
        abstract boolean isValid();
        abstract void run() throws IOException;
    }

    static class TrainCommand extends BaseCommand {
        private String input;
        private String output;
        private int vocabSize = 10000;
        private int minFreq = 1;
        private TrainerConfig.ScoringStrategy strategy = TrainerConfig.ScoringStrategy.FREQUENCY;

        TrainCommand(String[] args) {
            for (int i = 1; i < args.length; i++) {
                if ("--input".equals(args[i]) && i + 1 < args.length) input = args[++i];
                else if ("--output".equals(args[i]) && i + 1 < args.length) output = args[++i];
                else if ("--vocab-size".equals(args[i]) && i + 1 < args.length) vocabSize = Integer.parseInt(args[++i]);
                else if ("--min-freq".equals(args[i]) && i + 1 < args.length) minFreq = Integer.parseInt(args[++i]);
                else if ("--strategy".equals(args[i]) && i + 1 < args.length) 
                    strategy = TrainerConfig.ScoringStrategy.valueOf(args[++i].toUpperCase());
            }
        }

        @Override
        boolean isValid() {
            if (input == null || output == null) {
                logger.warn("Missing required parameters for 'train' command.");
                System.err.println("Usage: train --input <file> --output <dir> [--vocab-size <size>] [--strategy <freq|pmi|hybrid>]");
                return false;
            }
            return true;
        }

        @Override
        void run() throws IOException {
            logger.info("Training BPE model from: {}", input);
            String corpus = new String(Files.readAllBytes(Paths.get(input)), java.nio.charset.StandardCharsets.UTF_8);
            
            TrainerConfig config = TrainerConfig.builder()
                    .vocabSize(vocabSize).minFrequency(minFreq).strategy(strategy).build();
            
            PreTokenizerConfig preConfig = PreTokenizerConfig.builder()
                    .splitCamelCase(true).splitSnakeCase(true).build();

            BPETrainer trainer = new BPETrainer(new CodeAwarePreTokenizer(preConfig), config);
            BPETrainer.TrainResult result = trainer.train(corpus);

            BPETokenizer tokenizer = new BPETokenizer(result.vocabulary, result.merges, 
                    com.minilm.tokenizer.core.config.TokenizerConfig.builder()
                            .preTokenizer(new CodeAwarePreTokenizer(preConfig)).build());
            
            BPE.save(tokenizer, output);
            logger.info("Model saved successfully to: {}", output);
        }
    }

    static class EncodeCommand extends BaseCommand {
        private String modelPath;
        private String text;

        EncodeCommand(String[] args) {
            for (int i = 1; i < args.length; i++) {
                if ("--model".equals(args[i]) && i + 1 < args.length) modelPath = args[++i];
                else if ("--text".equals(args[i]) && i + 1 < args.length) text = args[++i];
            }
        }

        @Override
        boolean isValid() {
            return modelPath != null && text != null;
        }

        @Override
        void run() throws IOException {
            BPETokenizer tokenizer = BPE.load(modelPath);
            List<TokenPiece> pieces = tokenizer.encodeAsPieces(text);
            System.out.println("Tokens: " + Arrays.toString(tokenizer.encode(text)));
            System.out.println("Pieces: " + pieces);
        }
    }

    static class DecodeCommand extends BaseCommand {
        private String modelPath;
        private String tokensStr;

        DecodeCommand(String[] args) {
            for (int i = 1; i < args.length; i++) {
                if ("--model".equals(args[i]) && i + 1 < args.length) modelPath = args[++i];
                else if ("--tokens".equals(args[i]) && i + 1 < args.length) tokensStr = args[++i];
            }
        }

        @Override
        boolean isValid() {
            return modelPath != null && tokensStr != null;
        }

        @Override
        void run() throws IOException {
            BPETokenizer tokenizer = BPE.load(modelPath);
            int[] tokens = Arrays.stream(tokensStr.split(","))
                    .map(String::trim).mapToInt(Integer::parseInt).toArray();
            System.out.println("Decoded: " + tokenizer.decode(tokens));
        }
    }
}
