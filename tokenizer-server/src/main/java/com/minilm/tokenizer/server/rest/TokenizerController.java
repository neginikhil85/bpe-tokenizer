package com.minilm.tokenizer.server.rest;

import com.minilm.tokenizer.core.tokenizers.model.TokenPiece;
import com.minilm.tokenizer.server.model.request.DecodeRequest;
import com.minilm.tokenizer.server.model.request.EncodeRequest;
import com.minilm.tokenizer.server.model.response.DecodeResponse;
import com.minilm.tokenizer.server.model.response.EncodeResponse;
import com.minilm.tokenizer.server.service.TokenizerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for Tokenizer REST API.
 * Delegates business logic to TokenizerService.
 */
@RestController
@RequestMapping("${tokenizer.dev-ui.path:/internal/tokenizer}")
@ConditionalOnProperty(name = "tokenizer.dev-ui.enabled", havingValue = "true")
public class TokenizerController {

    private final TokenizerService tokenizerService;

    @Value("classpath:static/index.html")
    private Resource indexHtml;

    public TokenizerController(TokenizerService tokenizerService) {
        this.tokenizerService = tokenizerService;
    }

    @GetMapping
    public ResponseEntity<Resource> devUi() {
        return ResponseEntity.ok(indexHtml);
    }

    @PostMapping("/encode")
    public ResponseEntity<EncodeResponse> encode(@RequestBody EncodeRequest request) {
        if (!tokenizerService.isLoaded()) {
            return ResponseEntity.internalServerError().build();
        }
        
        long start = System.currentTimeMillis();
        List<TokenPiece> pieces = tokenizerService.encode(request.getText());
        int[] tokens = pieces.stream().mapToInt(TokenPiece::getTokenId).toArray();
        List<String> textPieces = pieces.stream().map(TokenPiece::getText).collect(Collectors.toList());
        long timeMs = System.currentTimeMillis() - start;

        return ResponseEntity.ok(new EncodeResponse(tokens, textPieces, timeMs));
    }

    @PostMapping("/decode")
    public ResponseEntity<DecodeResponse> decode(@RequestBody DecodeRequest request) {
        if (!tokenizerService.isLoaded()) {
            return ResponseEntity.internalServerError().build();
        }
        
        String text = tokenizerService.decode(request.getTokens());
        return ResponseEntity.ok(new DecodeResponse(text));
    }
}
