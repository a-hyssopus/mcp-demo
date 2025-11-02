package org.travel.travelapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.travel.travelapp.dto.ItineraryRequest;
import org.travel.travelapp.dto.TripPlan;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TripPlannerService {

    private final ChatClient claudeChatClient;
    private final ObjectMapper objectMapper;


    public TripPlan generateTripPlan(ItineraryRequest request, String sanitizedDescription) {
        log.info("Generating trip plan for {} to {} ({} to {})",
                request.getFrom(), request.getTo(),
                request.getStartDate(), request.getEndDate());

        try {
            // Build comprehensive prompt for Claude
            String systemPrompt = buildSystemPrompt();
            String userPrompt = buildUserPrompt(request, sanitizedDescription);

            // Call Claude with MCP tools available
            String claudeResponse = callClaudeWithMcp(systemPrompt, userPrompt);

            // Parse Claude's response into structured trip plan
            return parseTripPlan(claudeResponse, request);

        } catch (Exception e) {
            log.error("Error generating trip plan: {}", e.getMessage(), e);
            return createFallbackTripPlan(request);
        }
    }

    private String buildSystemPrompt() {
        return """
                You are an expert travel advisor with access to real-time data through MCP tools.
                
                Your task is to create a comprehensive trip plan using the following MCP tools:
                
                1. **Brave Search MCP**: Research the top 10 famous things to do/see in the destination city
                2. **Google Maps MCP**: Get distance from city center for each attraction
                3. **Amadeus MCP**: Find top 5 flight options with pricing
                
                **IMPORTANT**: You MUST return your response as a valid JSON object with this exact structure:
                
                {
                  "summary": "A compelling 2-3 sentence trip overview",
                  "attractions": [
                    {
                      "name": "Attraction name",
                      "description": "Brief description of the attraction",
                      "distanceFromCenter": 2.5,
                      "address": "Full address"
                    }
                  ],
                  "flights": [
                    {
                      "airline": "Airline name",
                      "price": "$XXX",
                      "departureTime": "HH:MM",
                      "arrivalTime": "HH:MM",
                      "duration": "Xh XXm",
                      "stops": 0,
                      "bookingClass": "Economy/Business"
                    }
                  ]
                }
                
                - Include exactly 10 attractions sorted by distance from city center (closest first)
                - Include exactly 5 flight options sorted by price (cheapest first)
                - Return ONLY the JSON object, no additional text or markdown formatting
                - Ensure distanceFromCenter is a number in kilometers
                """;
    }

    private String buildUserPrompt(ItineraryRequest request, String sanitizedDescription) {
        return String.format("""
                        Create a comprehensive trip plan for this itinerary:
                        
                        Destination: %s
                        Origin: %s
                        Travel Dates: %s to %s
                        Number of Adults: %d
                        Trip Details: %s
                        
                        Step-by-step instructions:
                        1. Use Brave Search MCP to research the top 10 famous attractions and things to do in %s
                        2. For each attraction, use Google Maps MCP to get the distance from the city center of %s
                        3. Sort the attractions by distance (closest to city center first)
                        4. Use Amadeus MCP with LIMIT of 5 flights to find the top 3 flight options from %s to %s on %s for %d adult(s)
                        5. Write a compelling 2-3 sentence trip summary
                        6. Return everything as a JSON object matching the schema provided in the system prompt
                        
                        Remember: Return ONLY the JSON object, no markdown code blocks or additional text.
                        """,
                request.getTo(),
                request.getFrom(),
                request.getStartDate(),
                request.getEndDate(),
                request.getNumberOfAdults(),
                sanitizedDescription != null && !sanitizedDescription.isBlank()
                        ? sanitizedDescription
                        : "General sightseeing and tourism",
                request.getTo(),
                request.getTo(),
                request.getFrom(),
                request.getTo(),
                request.getStartDate(),
                request.getNumberOfAdults()
        );
    }

    private String callClaudeWithMcp(String systemPrompt, String userPrompt) {
        log.info("Calling Claude with MCP tools enabled...");

        Prompt prompt = new Prompt(List.of(
                new SystemMessage(systemPrompt),
                new UserMessage(userPrompt)
        ));

        // ChatClient with MCP tools will automatically make them available to Claude
        String response = claudeChatClient
                .prompt(prompt)
                .call()
                .content();

        log.info("Claude response received: {} characters", response.length());
        return response;
    }

    private TripPlan parseTripPlan(String claudeResponse, ItineraryRequest request) {
        log.info("Parsing Claude's JSON response into structured trip plan");

        try {
            // Claude might wrap JSON in markdown code blocks, so clean it first
            String cleanedJson = claudeResponse.trim();
            if (cleanedJson.startsWith("```json")) {
                cleanedJson = cleanedJson.substring(7);
            }
            if (cleanedJson.startsWith("```")) {
                cleanedJson = cleanedJson.substring(3);
            }
            if (cleanedJson.endsWith("```")) {
                cleanedJson = cleanedJson.substring(0, cleanedJson.length() - 3);
            }
            cleanedJson = cleanedJson.trim();

            // Parse JSON directly into TripPlan object
            TripPlan tripPlan = objectMapper.readValue(cleanedJson, TripPlan.class);

            log.info("Successfully parsed trip plan with {} attractions and {} flights",
                    tripPlan.getAttractions() != null ? tripPlan.getAttractions().size() : 0,
                    tripPlan.getFlights() != null ? tripPlan.getFlights().size() : 0);

            return tripPlan;

        } catch (Exception e) {
            log.error("Error parsing Claude's JSON response: {}", e.getMessage(), e);
            log.debug("Raw response: {}", claudeResponse);

            // Return fallback trip plan
            return createFallbackTripPlan(request);
        }
    }

    private TripPlan createFallbackTripPlan(ItineraryRequest request) {
        log.warn("Using fallback trip plan due to AI error");

        TripPlan fallback = new TripPlan();

        fallback.setSummary(String.format(
                "Your %d-day trip from %s to %s promises an exciting adventure. " +
                        "Enjoy exploring new destinations and creating memorable experiences.",
                java.time.temporal.ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()),
                request.getFrom(),
                request.getTo()
        ));

        // Create fallback attractions
        fallback.setAttractions(new ArrayList<>());

        // Create fallback flights with generic pricing
        List<TripPlan.Flight> fallbackFlights = new ArrayList<>();
        fallbackFlights.add(new TripPlan.Flight(
                "Various Airlines",
                "Varies by season",
                "Multiple times available",
                "Multiple times available",
                "Varies",
                0,
                "Economy"
        ));
        fallback.setFlights(fallbackFlights);

        return fallback;
    }
}
