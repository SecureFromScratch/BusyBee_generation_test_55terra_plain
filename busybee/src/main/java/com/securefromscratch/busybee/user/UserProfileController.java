package com.securefromscratch.busybee.user;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class UserProfileController {

    private final UserProfileService userProfileService;

    UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping("/user/profile")
    UserProfileResponse profile(Authentication authentication) {
        return userProfileService.profile(authentication);
    }
}
