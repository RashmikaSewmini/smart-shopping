package com.smartshopping.aichat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.model}")
    private String model;

    public GeminiService(ObjectMapper objectMapper) {
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
        this.objectMapper = objectMapper;
    }

    public String ask(String message) {
        String instructions = """
            You are the Smart Shopping assistant.
            Help with shopping questions, general product information,
            and general educational information about medicines.

            Rules:
            - Never invent product prices, stock, or shop addresses.
            - If live product data is not provided, say you cannot verify it.
            - Do not diagnose medical conditions or prescribe medicines.
            - Do not provide personalized medicine dosages.
            - Encourage users to consult a pharmacist or doctor for
              medicine decisions and to seek emergency help for urgent symptoms.
            - Be clear, concise, and friendly.
            """;

        Map<String, Object> body = Map.of(
                "systemInstruction", Map.of(
                        "parts", List.of(Map.of("text", instructions))
                ),
                "contents", List.of(
                        Map.of(
                                "role", "user",
                                "parts", List.of(Map.of("text", message))
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", 0.3,
                        "maxOutputTokens", 700
                )
        );

        try {
            String json = restClient.post()
                    .uri("/v1beta/models/{model}:generateContent?key={key}",
                            model, apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(json);
            JsonNode text = root.at("/candidates/0/content/parts/0/text");

            if (text.isMissingNode() || text.isNull()) {
                throw new IllegalStateException(
                        "The AI provider returned no text."
                );
            }

            return text.asText();

        } catch (Exception ex) {
            throw new RuntimeException(
                    "Could not get a response from the AI provider.", ex
            );
        }
    }
}
