package com.securefromscratch.busybee.functionality;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"mysql", "test"})
class AuthenticationFeatureTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registeredUserCanLogInAndReadCurrentUser() throws Exception {
        String username = "authentication-" + UUID.randomUUID();
        String password = "password-123";
        String registration = objectMapper.writeValueAsString(Map.of("username", username, "password", password));

        mvc.perform(post("/register").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(registration))
                .andExpect(status().isOk());

        MvcResult login = mvc.perform(formLogin().user(username).password(password))
                .andExpect(authenticated().withUsername(username))
                .andReturn();
        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);

        mvc.perform(get("/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.admin").value(false))
                .andExpect(jsonPath("$.entitlements").isEmpty())
                .andExpect(jsonPath("$.effectiveEntitlements").isEmpty());
    }
}
