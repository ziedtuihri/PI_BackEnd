package tn.esprit.pi.api;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CohereQuizService {

    @Value("${cohere.token}")
    private String apiToken;

    private final ObjectMapper objectMapper;

    public List<tn.esprit.pi.api.AIQuizDTO> generateQuestions(String description, String prompt, int count) {
        try {
            String modelUrl = "https://api.cohere.ai/v1/chat";
            HttpClient client = HttpClient.newHttpClient();

            String userPrompt = """
                    Generate %d multiple-choice questions in this JSON format:

                    [
                      {
                        "question": "...",
                        "options": ["A", "B", "C", "D"],
                        "correct": "..."
                      }
                    ]

                    Use this job description: %s
                    """.formatted(count, prompt != null && !prompt.isBlank() ? prompt : description);

            log.info("Sending prompt to Cohere:\n{}", userPrompt);

            String jsonBody = objectMapper.writeValueAsString(Map.of(
                    "model", "command-r",
                    "message", userPrompt,
                    "temperature", 0.7
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(modelUrl))
                    .header("Authorization", "Bearer " + apiToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Cohere response status: {}", response.statusCode());
            log.info("Cohere raw response: {}", response.body());

            if (response.statusCode() >= 400) {
                throw new RuntimeException("Cohere API returned error: " + response.body());
            }

            // Step 1: Get "text" field from "chat" response
            Map<String, Object> parsed = objectMapper.readValue(response.body(), new TypeReference<>() {});
            String text = (String) parsed.get("text");

            // Step 2: Strip code markdown (optional)
            String cleaned = text.replaceAll("(?s)```json\\s*|```", "").trim();

            // Step 3: Parse JSON string to list
            return objectMapper.readValue(cleaned, new TypeReference<List<tn.esprit.pi.api.AIQuizDTO>>() {});

        } catch (Exception e) {
            log.error("Failed to generate quiz using Cohere", e);
            throw new RuntimeException("Failed to generate quiz using Cohere", e);
        }
    }
}
