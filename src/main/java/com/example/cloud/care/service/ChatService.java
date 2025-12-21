package com.example.cloud.care.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class ChatService {

    @Value("${openai.api.key}")
    private String openaiApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getChatResponse(String userMessage) {
        try {
            String apiUrl = "https://api.openai.com/v1/responses";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + openaiApiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o-mini");

            requestBody.put("input", userMessage); // ✅ Correct parameter

            HttpEntity<String> entity = new HttpEntity<>(
                    objectMapper.writeValueAsString(requestBody),
                    headers);

            String response = restTemplate.postForObject(apiUrl, entity, String.class);
            JsonNode jsonNode = objectMapper.readTree(response);

            return jsonNode.get("output_text").asText(); // ✅ Correct field

        } catch (Exception e) {
            e.printStackTrace();
            return "⚠️ AI service is currently unavailable.\nPlease try again later.";
        }
    }
}