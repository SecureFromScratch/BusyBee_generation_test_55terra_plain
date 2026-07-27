package com.securefromscratch.busybee.session;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class DemoSessionAuthenticationFilter extends OncePerRequestFilter {

    private final DemoSessionProperties demoSessionProperties;
    private final UserDetailsService userDetailsService;

    DemoSessionAuthenticationFilter(
            DemoSessionProperties demoSessionProperties,
            UserDetailsService userDetailsService
    ) {
        this.demoSessionProperties = demoSessionProperties;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String sessionId = configuredSessionId(request);
        String username = sessionId == null ? null : demoSessionProperties.getUsers().get(sessionId);
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails user = userDetailsService.loadUserByUsername(username);
            SecurityContextHolder.getContext().setAuthentication(
                    UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities())
            );
            response.addHeader("Set-Cookie", ResponseCookie.from("JSESSIONID", sessionId)
                    .path("/")
                    .httpOnly(true)
                    .maxAge(demoSessionProperties.getCookieMaxAge())
                    .build()
                    .toString());
        }
        filterChain.doFilter(request, response);
    }

    private String configuredSessionId(HttpServletRequest request) {
        if (!demoSessionProperties.isEnabled() || request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if ("JSESSIONID".equals(cookie.getName())
                    && demoSessionProperties.getUsers().containsKey(cookie.getValue())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
