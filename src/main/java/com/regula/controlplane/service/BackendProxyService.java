package com.regula.controlplane.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Service
public class BackendProxyService {

    private final String backendUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public BackendProxyService(
            @Value("${regula.backend.url}") String backendUrl,
            ObjectMapper objectMapper) {
        this.backendUrl = backendUrl;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    public Map<String, Object> get(String path, String bearerToken) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(backendUrl + path))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/json");
            if (bearerToken != null && !bearerToken.isBlank()) {
                builder.header("Authorization", "Bearer " + bearerToken);
            }
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            @SuppressWarnings("unchecked")
            Map<String, Object> body = objectMapper.readValue(response.body(), Map.class);
            return body;
        } catch (Exception e) {
            return Map.of("error", true, "mensaje", "No se pudo conectar con el backend: " + e.getMessage());
        }
    }
}
