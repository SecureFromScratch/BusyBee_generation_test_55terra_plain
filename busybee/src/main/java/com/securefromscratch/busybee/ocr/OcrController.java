package com.securefromscratch.busybee.ocr;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class OcrController {

    private final OcrService ocrService;

    OcrController(OcrService ocrService) {
        this.ocrService = ocrService;
    }

    @PostMapping("/ocr/image")
    OcrDraft extractImage(Authentication authentication, @RequestBody OcrRequest request) {
        return ocrService.extractImage(authentication, request);
    }

    @ExceptionHandler(OcrException.class)
    ResponseEntity<Map<String, String>> ocrError(OcrException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", exception.getMessage()));
    }
}
