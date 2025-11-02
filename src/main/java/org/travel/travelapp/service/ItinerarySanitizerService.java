package org.travel.travelapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItinerarySanitizerService {

    private final ChatClient localGemmaChatClient;

    private static final String SYSTEM_PROMPT = """
            You are an AI assistant specialized in sanitizing travel itinerary descriptions.
            Your task is to:
            1. Remove any text that is NOT related to travel, destinations, activities, accommodation, or trip planning
            2. Remove inappropriate language, spam, or irrelevant content
            3. Keep only the relevant travel-related information
            4. If the entire description is unrelated to travel, return an empty string
            5. Preserve the original meaning and tone of valid travel content
            6. Return ONLY the sanitized text without any explanations or additional commentary
            7. If no sanitization is needed return text as is.
            """;


    public String sanitizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return description;
        }

        try {
            log.info("Sanitizing itinerary description: {}", description);

            String userPrompt = String.format(
                "Please sanitize this travel itinerary description by removing any text " +
                "unrelated to travel. Return only the sanitized text:\n\n%s",
                description
            );

            Prompt prompt = new Prompt(List.of(
                new SystemMessage(SYSTEM_PROMPT),
                new UserMessage(userPrompt)
            ));

            String sanitized = localGemmaChatClient.prompt(prompt)
                .call()
                .content();

            log.info("Sanitized description: {}", sanitized);

            // If the response is empty or just whitespace, return empty string
            return sanitized != null ? sanitized.trim() : "";

        } catch (Exception e) {
            log.error("Error sanitizing description with AI: {}", e.getMessage(), e);
            // Fallback: return original description if AI fails
            log.warn("Returning original description due to AI sanitization failure");
            return description;
        }
    }
}
