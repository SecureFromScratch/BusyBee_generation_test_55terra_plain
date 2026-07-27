package com.securefromscratch.busybee.ai;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
class VaultPersonalCredentialStore {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String vaultUri;
    private final String vaultToken;
    private final String mount;

    VaultPersonalCredentialStore(
            ObjectMapper objectMapper,
            @Value("${busybee.secrets.vault.uri:${VAULT_ADDR:http://192.168.48.1:8200}}") String vaultUri,
            @Value("${busybee.secrets.vault.token:${VAULT_TOKEN:root}}") String vaultToken,
            @Value("${busybee.secrets.vault.mount:secret}") String mount
    ) {
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
        this.objectMapper = objectMapper;
        this.vaultUri = vaultUri.replaceAll("/+$", "");
        this.vaultToken = vaultToken;
        this.mount = mount;
    }

    Optional<StoredPersonalCredential> find(String username) {
        HttpResponse<String> response = send(request("GET", username, HttpRequest.BodyPublishers.noBody()));
        if (response.statusCode() == 404) {
            return Optional.empty();
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new AiCredentialStoreException();
        }

        try {
            JsonNode data = objectMapper.readTree(response.body()).path("data").path("data");
            AiProvider provider = AiProvider.valueOf(data.path("provider").asText());
            String apiKey = data.path("apiKey").asText();
            if (apiKey.isBlank()) {
                throw new AiCredentialStoreException();
            }
            return Optional.of(new StoredPersonalCredential(provider, apiKey));
        } catch (JsonProcessingException | IllegalArgumentException exception) {
            throw new AiCredentialStoreException();
        }
    }

    void save(String username, StoredPersonalCredential credential) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "data", Map.of("provider", credential.provider().name(), "apiKey", credential.apiKey())
            ));
            HttpResponse<String> response = send(request("POST", username, HttpRequest.BodyPublishers.ofString(payload)));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new AiCredentialStoreException();
            }
        } catch (JsonProcessingException exception) {
            throw new AiCredentialStoreException();
        }
    }

    void delete(String username) {
        HttpResponse<String> response = send(request("DELETE", username, HttpRequest.BodyPublishers.noBody()));
        if (response.statusCode() != 404 && (response.statusCode() < 200 || response.statusCode() >= 300)) {
            throw new AiCredentialStoreException();
        }
    }

    private HttpRequest request(String method, String username, HttpRequest.BodyPublisher body) {
        return HttpRequest.newBuilder(secretUri(username))
                .timeout(Duration.ofSeconds(5))
                .header("X-Vault-Token", vaultToken)
                .header("Content-Type", "application/json")
                .method(method, body)
                .build();
    }

    private URI secretUri(String username) {
        String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8);
        return URI.create(vaultUri + "/v1/" + mount + "/data/busybee/ai/credentials/" + encodedUsername);
    }

    private HttpResponse<String> send(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException exception) {
            throw new AiCredentialStoreException();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AiCredentialStoreException();
        }
    }
}
