package com.securefromscratch.busybee.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class PasswordChangeService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    PasswordChangeService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    PasswordChangeResponse changePassword(String username, PasswordChangeRequest request) {
        if (request.newPassword() == null || request.newPassword().isBlank()) {
            throw new InvalidNewPasswordException();
        }

        RegisteredUser user = userRepository.findByUsername(username)
                .orElseThrow(IncorrectCurrentPasswordException::new);
        if (request.currentPassword() == null || !passwordEncoder.matches(request.currentPassword(), user.passwordHash())) {
            throw new IncorrectCurrentPasswordException();
        }

        String replacementHash = passwordEncoder.encode(request.newPassword());
        if (userRepository.replacePasswordHash(username, user.passwordHash(), replacementHash) != 1) {
            throw new IncorrectCurrentPasswordException();
        }

        return new PasswordChangeResponse(true);
    }
}
