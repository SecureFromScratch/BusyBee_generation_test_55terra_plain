package com.securefromscratch.busybee.user;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class CurrentUserController {

    private final CurrentUserService currentUserService;

    CurrentUserController(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @GetMapping("/me")
    CurrentUserResponse currentUser(Authentication authentication) {
        return currentUserService.currentUser(authentication);
    }
}
