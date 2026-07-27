package com.securefromscratch.busybee.ai;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class AiCredentialController {

    private final AiCredentialService aiCredentialService;

    AiCredentialController(AiCredentialService aiCredentialService) {
        this.aiCredentialService = aiCredentialService;
    }

    @GetMapping("/ai/credential")
    AiCredentialStatusResponse get(Authentication authentication) {
        return aiCredentialService.get(authentication);
    }

    @PutMapping("/ai/credential")
    AiCredentialStatusResponse save(Authentication authentication, @RequestBody AiCredentialRequest request) {
        return aiCredentialService.save(authentication, request);
    }

    @DeleteMapping("/ai/credential")
    AiCredentialStatusResponse delete(Authentication authentication) {
        return aiCredentialService.delete(authentication);
    }

    @ExceptionHandler(InvalidAiCredentialException.class)
    ResponseEntity<Map<String, String>> invalidCredential(InvalidAiCredentialException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(AiCredentialStoreException.class)
    ResponseEntity<Map<String, String>> unavailableCredentialStore(AiCredentialStoreException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error", exception.getMessage()));
    }
}
