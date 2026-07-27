package com.securefromscratch.busybee.user;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class PasswordChangeController {

    private final PasswordChangeService passwordChangeService;

    PasswordChangeController(PasswordChangeService passwordChangeService) {
        this.passwordChangeService = passwordChangeService;
    }

    @PutMapping("/user/password")
    PasswordChangeResponse changePassword(Authentication authentication, @RequestBody PasswordChangeRequest request) {
        return passwordChangeService.changePassword(authentication.getName(), request);
    }

    @ExceptionHandler({IncorrectCurrentPasswordException.class, InvalidNewPasswordException.class})
    ResponseEntity<Map<String, String>> passwordChangeError(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", exception.getMessage()));
    }
}
