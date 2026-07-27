package com.securefromscratch.busybee.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    RegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    RegistrationResponse register(RegistrationRequest request) {
        if (request.username() == null || request.username().isBlank()
                || request.password() == null || request.password().isBlank()) {
            throw new InvalidRegistrationException();
        }

        userRepository.create(request.username().trim(), passwordEncoder.encode(request.password()));
        return new RegistrationResponse("/main/main.html");
    }
}
