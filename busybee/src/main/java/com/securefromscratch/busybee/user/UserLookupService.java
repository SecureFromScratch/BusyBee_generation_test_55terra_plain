package com.securefromscratch.busybee.user;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
class UserLookupService {

    private static final int MAXIMUM_RESULTS = 20;

    private final UserRepository userRepository;

    UserLookupService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    List<UserLookupResponse> findUsers(String query) {
        String normalizedQuery = query.trim();
        if (normalizedQuery.isEmpty()) {
            return List.of();
        }

        return userRepository.findUsernamesContaining(normalizedQuery, MAXIMUM_RESULTS).stream()
                .map(UserLookupResponse::new)
                .toList();
    }
}
