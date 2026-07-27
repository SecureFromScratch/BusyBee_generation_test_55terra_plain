package com.securefromscratch.busybee.session;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@Service
class SessionAccountService {

    private static final String PARKED_ACCOUNTS_ATTRIBUTE = "busybee.parked-accounts";

    private final UserDetailsService userDetailsService;
    private final HttpSessionSecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    SessionAccountService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    List<SessionAccount> accounts(Authentication authentication, HttpServletRequest request) {
        List<SessionAccount> accounts = new ArrayList<>();
        accounts.add(new SessionAccount("current", authentication.getName(), true));
        parkedAccounts(request.getSession(false)).forEach(
                (slot, username) -> accounts.add(new SessionAccount(slot, username, false))
        );
        return accounts;
    }

    AddedSessionAccount parkCurrentAccount(Authentication authentication, HttpServletRequest request) {
        Map<String, String> parkedAccounts = parkedAccounts(request.getSession(true));
        String slot = nextSlot(parkedAccounts);
        parkedAccounts.put(slot, authentication.getName());
        return new AddedSessionAccount(slot);
    }

    void switchAccount(
            Authentication authentication,
            SwitchSessionRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        if (request.slot() == null || request.slot().isBlank()) {
            throw new InvalidSessionSlotException("Session slot is required.");
        }

        Map<String, String> parkedAccounts = parkedAccounts(httpRequest.getSession(true));
        String requestedUsername = parkedAccounts.remove(request.slot());
        if (requestedUsername == null) {
            throw new InvalidSessionSlotException("Session slot was not found.");
        }
        parkedAccounts.put(nextSlot(parkedAccounts), authentication.getName());

        UserDetails user = userDetailsService.loadUserByUsername(requestedUsername);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities()));
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> parkedAccounts(HttpSession session) {
        if (session == null) {
            return Map.of();
        }
        Object existing = session.getAttribute(PARKED_ACCOUNTS_ATTRIBUTE);
        if (existing instanceof Map<?, ?>) {
            return (Map<String, String>) existing;
        }
        Map<String, String> parkedAccounts = new LinkedHashMap<>();
        session.setAttribute(PARKED_ACCOUNTS_ATTRIBUTE, parkedAccounts);
        return parkedAccounts;
    }

    private String nextSlot(Map<String, String> parkedAccounts) {
        int slot = 1;
        while (parkedAccounts.containsKey(Integer.toString(slot))) {
            slot++;
        }
        return Integer.toString(slot);
    }
}
