package com.securefromscratch.busybee.settings;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class UserSettingsController {

    private final UserSettingsService userSettingsService;

    UserSettingsController(UserSettingsService userSettingsService) {
        this.userSettingsService = userSettingsService;
    }

    @GetMapping("/user/settings")
    UserSettingsResponse getSettings(Authentication authentication) {
        return userSettingsService.getSettings(authentication.getName());
    }

    @PutMapping("/user/settings")
    UserSettingsResponse replaceSettings(Authentication authentication, @RequestBody UserSettingsRequest request) {
        return userSettingsService.replaceSettings(authentication.getName(), request);
    }

    @ExceptionHandler(InvalidSummaryThresholdException.class)
    ResponseEntity<Map<String, String>> invalidSummaryThreshold(InvalidSummaryThresholdException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", exception.getMessage()));
    }
}
