# AI Integration - Itinerary Description Sanitization

## Overview

The Travel App now uses AI to automatically sanitize itinerary descriptions, removing any content unrelated to travel while preserving relevant information.

## Architecture

```
User Input → ItineraryController → ItinerarySanitizerService → Local AI Model (Gemma3)
                                                                        ↓
             Response ← Controller ← Service ← AI Response (Sanitized Text)
```

## Components

### 1. Local AI Model Setup

**Model**: Gemma3 (ai/gemma3:latest)
**Server**: Docker-based model runner
**Endpoint**: `http://localhost:12434/engines/llama.cpp/v1/chat/completions`
**API**: OpenAI-compatible API

### 2. Spring AI Configuration

**File**: `src/main/resources/application.properties`

```properties
# Custom OpenAI-compatible endpoint (Local Docker Model)
spring.ai.openai.api-key=not-needed
spring.ai.openai.base-url=http://localhost:12434/engines/llama.cpp
spring.ai.openai.chat.options.model=ai/gemma3:latest
spring.ai.openai.chat.options.temperature=0.7
```

**Key Points**:
- `base-url`: Points to the local model server (Spring AI appends `/v1/chat/completions`)
- `api-key`: Set to placeholder since local model doesn't require authentication
- `model`: Specifies which model to use
- `temperature`: Controls randomness (0.7 for balanced creativity/consistency)

### 3. Dependencies

**File**: `pom.xml`

Added Spring AI OpenAI starter:
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-openai</artifactId>
</dependency>
```

This dependency is managed by `spring-ai-bom` (version 1.0.3).

### 4. AI Sanitization Service

**File**: `src/main/java/org/travel/travelapp/service/ItinerarySanitizerService.java`

**Purpose**: Sanitizes travel itinerary descriptions by removing unrelated content.

**System Prompt**:
```
You are an AI assistant specialized in sanitizing travel itinerary descriptions.
Your task is to:
1. Remove any text that is NOT related to travel, destinations, activities, accommodation, or trip planning
2. Remove inappropriate language, spam, or irrelevant content
3. Keep only the relevant travel-related information
4. If the entire description is unrelated to travel, return an empty string
5. Preserve the original meaning and tone of valid travel content
6. Return ONLY the sanitized text without any explanations or additional commentary
```

**Key Features**:
- Uses Spring AI's `ChatClient` for communication with the model
- Constructs prompts with both system and user messages
- Handles errors gracefully with fallback to original description
- Logs all sanitization activity for debugging

**Error Handling**:
```java
try {
    // AI sanitization
} catch (Exception e) {
    log.error("Error sanitizing description with AI: {}", e.getMessage(), e);
    return description; // Fallback to original
}
```

### 5. Controller Integration

**File**: `src/main/java/org/travel/travelapp/controller/ItineraryController.java`

```java
@RequiredArgsConstructor
public class ItineraryController {

    private final ItinerarySanitizerService sanitizerService;

    @PostMapping
    public ResponseEntity<ItineraryResponse> createItinerary(@Valid @RequestBody ItineraryRequest request) {
        // Sanitize description using AI
        String sanitizedDescription = sanitizerService.sanitizeDescription(request.getDescription());

        // Use sanitized description in response
        ItineraryResponse response = new ItineraryResponse(..., sanitizedDescription, ...);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
```

## Testing

### Test Case 1: Mixed Content

**Input**:
```json
{
  "to": "Paris",
  "from": "New York",
  "startDate": "2025-12-01",
  "endDate": "2025-12-10",
  "numberOfAdults": 2,
  "description": "I want to visit the Eiffel Tower and Louvre Museum. By the way check out my crypto at scamlink.com! Also politicians ruin everything damn!"
}
```

**Output**:
```json
{
  "description": "Eiffel Tower\nLouvre Museum"
}
```

**Result**: ✅ AI successfully removed:
- Spam/scam content ("check out my crypto at scamlink.com")
- Political rants ("politicians ruin everything")
- Profanity ("damn")
- Unnecessary filler ("I want to", "By the way")

**Kept**: Only travel-related content (Eiffel Tower, Louvre Museum)

### Test Case 2: Normal Travel Description

**Input**: "Planning to explore Rome's Colosseum and Vatican Museums"

**Output**: "Rome's Colosseum and Vatican Museums"

**Result**: ✅ AI kept all relevant travel content

### Logs Example

```
INFO - Sanitizing itinerary description: I want to visit the Eiffel Tower...
INFO - Sanitized description: Eiffel Tower
Louvre Museum
INFO - Original description: I want to visit the Eiffel Tower...
INFO - Sanitized description: Eiffel Tower
Louvre Museum
```

## Performance

- **Average Response Time**: 1-2 seconds (depends on model and description length)
- **Token Usage**: ~35 prompt tokens + ~66 completion tokens per request
- **Caching**: Model server caches recent prompts for faster responses

## Benefits

1. **Content Moderation**: Automatically removes inappropriate content
2. **Spam Prevention**: Filters out promotional links and scams
3. **Data Quality**: Ensures only travel-related information is stored
4. **User Experience**: Users get clean, relevant itinerary descriptions
5. **Scalable**: Can be extended to other text fields (reviews, comments, etc.)

## Limitations

1. **Latency**: AI processing adds 1-2 seconds to response time
2. **Accuracy**: May occasionally remove valid content or keep spam (depends on model)
3. **Dependency**: Requires local AI model to be running
4. **Fallback**: On failure, returns original description (no sanitization)

## Future Enhancements

1. **Caching**: Cache sanitized results for identical descriptions
2. **Async Processing**: Sanitize in background for faster API response
3. **Batch Processing**: Sanitize multiple descriptions in one AI call
4. **Fine-tuning**: Train model specifically on travel content
5. **Additional Fields**: Apply sanitization to reviews, tips, recommendations
6. **Sentiment Analysis**: Add sentiment scoring to descriptions
7. **Language Detection**: Identify and handle multi-language content

## Troubleshooting

### AI Model Not Responding

**Symptom**: HTTP 404 or timeout errors

**Solution**:
1. Verify model server is running:
   ```bash
   curl http://localhost:12434/engines/llama.cpp/v1/chat/completions
   ```
2. Check `application.properties` base-url is correct
3. Ensure model name matches: `ai/gemma3:latest`

### Sanitization Not Working

**Symptom**: Original description returned unchanged

**Solution**:
1. Check logs for errors
2. Verify AI model is responding (test with curl)
3. Check if fallback is being triggered
4. Increase logging level to DEBUG

### Performance Issues

**Symptom**: Slow response times (>5 seconds)

**Solution**:
1. Check model server resources (CPU/Memory)
2. Consider caching strategy
3. Move to async processing
4. Use a faster model or GPU acceleration

## Configuration Reference

| Property | Value | Description |
|----------|-------|-------------|
| `spring.ai.openai.base-url` | `http://localhost:12434/engines/llama.cpp` | AI model endpoint base URL |
| `spring.ai.openai.api-key` | `not-needed` | Placeholder (local model) |
| `spring.ai.openai.chat.options.model` | `ai/gemma3:latest` | Model name |
| `spring.ai.openai.chat.options.temperature` | `0.7` | Response randomness (0-1) |

## Dependencies

- **Spring AI**: 1.0.3
- **Spring AI OpenAI Starter**: 1.0.3
- **Spring Boot**: 3.5.7
- **Java**: 22

## See Also

- [QUICKSTART.md](QUICKSTART.md) - How to run the app
- [README.md](README.md) - Project overview
- Spring AI Documentation: https://docs.spring.io/spring-ai/reference/
