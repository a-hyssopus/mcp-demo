package org.travel.travelapp.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.travel.travelapp.dto.ItineraryRequest;
import org.travel.travelapp.dto.ItineraryResponse;
import org.travel.travelapp.dto.TripPlan;
import org.travel.travelapp.service.ItinerarySanitizerService;
import org.travel.travelapp.service.TripPlannerService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/itinerary")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"})
@Slf4j
@RequiredArgsConstructor
public class ItineraryController {

    private final ItinerarySanitizerService sanitizerService;
    private final TripPlannerService tripPlannerService;

    @PostMapping
    public ResponseEntity<ItineraryResponse> createItinerary(@Valid @RequestBody ItineraryRequest request) {
        log.info("Received itinerary request: from {} to {}, dates: {} to {}",
                request.getFrom(), request.getTo(), request.getStartDate(), request.getEndDate());

        // Step 1: Sanitize description using local AI
        String sanitizedDescription = sanitizerService.sanitizeDescription(request.getDescription());

        log.info("Original description: {}", request.getDescription());
        log.info("Sanitized description: {}", sanitizedDescription);

        // Step 2: Generate trip plan using Claude with MCP tools
        log.info("Generating trip plan with Claude and MCP servers...");
        TripPlan tripPlan = tripPlannerService.generateTripPlan(request, sanitizedDescription);
        log.info("Trip plan generated successfully");

        // Step 3: Create response with generated ID, timestamp, and trip plan
        ItineraryResponse response = new ItineraryResponse(
                UUID.randomUUID().toString(),
                request.getTo(),
                request.getFrom(),
                request.getStartDate(),
                request.getEndDate(),
                request.getNumberOfAdults(),
                sanitizedDescription,  // Use sanitized description
                LocalDateTime.now(),
                "CREATED",
                "Itinerary created with AI-powered trip plan and suggestions"
        );

        // Add trip plan to response
        response.setTripPlan(tripPlan);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleException(Exception ex) {
        log.error("Error processing itinerary request", ex);
        return Map.of("error", ex.getMessage());
    }
}
