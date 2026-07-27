package com.securefromscratch.busybee.user;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class UserLookupController {

    private final UserLookupService userLookupService;

    UserLookupController(UserLookupService userLookupService) {
        this.userLookupService = userLookupService;
    }

    @GetMapping("/users/lookup")
    List<UserLookupResponse> findUsers(@RequestParam String query) {
        return userLookupService.findUsers(query);
    }
}
