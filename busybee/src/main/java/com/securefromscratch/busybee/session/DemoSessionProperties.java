package com.securefromscratch.busybee.session;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("busybee.demo.sessions")
public class DemoSessionProperties {

    private boolean enabled;
    private Duration cookieMaxAge = Duration.ofHours(12);
    private Map<String, String> users = new HashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Duration getCookieMaxAge() {
        return cookieMaxAge;
    }

    public void setCookieMaxAge(Duration cookieMaxAge) {
        this.cookieMaxAge = cookieMaxAge;
    }

    public Map<String, String> getUsers() {
        return users;
    }

    public void setUsers(Map<String, String> users) {
        this.users = users == null ? new HashMap<>() : new HashMap<>(users);
    }
}
