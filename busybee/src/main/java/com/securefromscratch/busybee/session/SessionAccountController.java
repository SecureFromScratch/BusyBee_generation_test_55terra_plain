package com.securefromscratch.busybee.session;

import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class SessionAccountController {

    private final SessionAccountService sessionAccountService;

    SessionAccountController(SessionAccountService sessionAccountService) {
        this.sessionAccountService = sessionAccountService;
    }

    @GetMapping("/session/accounts")
    List<SessionAccount> accounts(Authentication authentication, HttpServletRequest request) {
        return sessionAccountService.accounts(authentication, request);
    }

    @PostMapping("/session/add-user")
    AddedSessionAccount addUser(Authentication authentication, HttpServletRequest request) {
        return sessionAccountService.parkCurrentAccount(authentication, request);
    }

    @PostMapping("/session/switch")
    ResponseEntity<Void> switchAccount(
            Authentication authentication,
            @RequestBody SwitchSessionRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        sessionAccountService.switchAccount(authentication, request, httpRequest, httpResponse);
        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(InvalidSessionSlotException.class)
    ResponseEntity<Map<String, String>> invalidSlot(InvalidSessionSlotException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", exception.getMessage()));
    }
}
