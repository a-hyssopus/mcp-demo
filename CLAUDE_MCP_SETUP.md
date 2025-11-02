# Claude MCP Integration Setup

## Overview

This travel app integrates with Claude via Anthropic API and uses **Model Context Protocol (MCP)** servers to enhance trip planning capabilities:

1. **Local AI (Gemma3)** - Sanitizes user input
2. **Claude with MCP** - Generates trip summaries, suggestions, and pricing

## Architecture

```
User Input
    ↓
[1] Sanitization (Local Gemma3 Model)
    ↓
[2] Trip Planning (Claude + MCP Servers)
    ├── Docker MCP Server
    └── Amadeus MCP Server (Flight Prices)
    ↓
Enhanced Response (Summary + Suggestions + Pricing)
```

## Prerequisites

### 1. Anthropic API Key

Get your Claude API key:
1. Visit https://console.anthropic.com/
2. Create an account or sign in
3. Navigate to API Keys
4. Generate a new API key
5. Copy the key (starts with `sk-ant-...`)

### 2. MCP Servers

The app uses two MCP servers:

#### Docker MCP Server
- **Purpose**: Docker container management
- **Package**: `@modelcontextprotocol/server-docker`
- **Installation**: Automatic via `npx`

#### Amadeus MCP Server
- **Purpose**: Flight price lookups
- **Package**: `@modelcontextprotocol/server-amadeus`
- **Installation**: Automatic via `npx`

**Note**: MCP servers are launched automatically by Spring AI when the app starts.

### 3. Node.js & npx

Required for MCP servers:
```bash
# Check if installed
node --version  # Should be v16+
npx --version   # Should be installed with Node

# Install if needed (macOS)
brew install node

# Install if needed (Linux)
curl -fsSL https://deb.nodesource.com/setup_lts.x | sudo -E bash -
sudo apt-get install -y nodejs
```

## Configuration

### Step 1: Set Up Environment Variables

Create `.env` file in the project root:

```bash
cp .env.example .env
```

Edit `.env` and add your Anthropic API key:

```properties
ANTHROPIC_API_KEY=sk-ant-your-actual-api-key-here
```

### Step 2: Export Environment Variable

Before starting the app:

```bash
# macOS/Linux
export ANTHROPIC_API_KEY=sk-ant-your-api-key

# Windows (PowerShell)
$env:ANTHROPIC_API_KEY="sk-ant-your-api-key"

# Windows (CMD)
set ANTHROPIC_API_KEY=sk-ant-your-api-key
```

Alternatively, add to your shell profile:

```bash
# Add to ~/.zshrc or ~/.bashrc
echo 'export ANTHROPIC_API_KEY=sk-ant-your-api-key' >> ~/.zshrc
source ~/.zshrc
```

### Step 3: Verify Configuration

The `application.properties` already has the MCP configuration:

```properties
# Anthropic Claude API
spring.ai.anthropic.api-key=${ANTHROPIC_API_KEY:your-api-key-here}
spring.ai.anthropic.chat.options.model=claude-3-5-sonnet-20241022
spring.ai.anthropic.chat.options.temperature=0.7
spring.ai.anthropic.chat.options.max-tokens=4096

# MCP Servers
spring.ai.mcp.client.docker.command=npx
spring.ai.mcp.client.docker.args=-y,@modelcontextprotocol/server-docker
spring.ai.mcp.client.docker.transport=stdio

spring.ai.mcp.client.amadeus.command=npx
spring.ai.mcp.client.amadeus.args=-y,@modelcontextprotocol/server-amadeus
spring.ai.mcp.client.amadeus.transport=stdio
```

## How It Works

### Request Flow

1. **User submits itinerary** → Frontend sends POST to `/api/itinerary`
2. **Sanitization** → Local Gemma3 model removes spam/inappropriate content
3. **Trip Planning** → Claude analyzes sanitized data
4. **MCP Tools** → Claude calls MCP servers:
   - **Amadeus MCP**: Fetches flight price estimates
   - **Docker MCP**: Available for containerized tasks
5. **Response** → Combined summary, suggestions, and pricing returned

### Example Request

```json
POST /api/itinerary
{
  "to": "Paris",
  "from": "New York",
  "startDate": "2025-12-01",
  "endDate": "2025-12-10",
  "numberOfAdults": 2,
  "description": "Romantic getaway to Paris"
}
```

### Example Response

```json
{
  "id": "uuid-here",
  "to": "Paris",
  "from": "New York",
  "startDate": "2025-12-01",
  "endDate": "2025-12-10",
  "numberOfAdults": 2,
  "description": "Romantic getaway to Paris",
  "createdAt": "2025-10-26T16:30:00",
  "status": "CREATED",
  "message": "Itinerary created with AI-powered trip plan",
  "tripPlan": {
    "summary": "Experience the magic of Paris during winter with this 9-day romantic journey. From iconic landmarks to charming cafes, immerse yourself in French culture and cuisine.",
    "suggestions": [
      "Visit the Eiffel Tower at sunset for breathtaking views",
      "Explore the Louvre Museum - book tickets online to skip lines",
      "Stroll through Montmartre and visit Sacré-Cœur",
      "Enjoy a Seine river cruise for romantic evening views",
      "Try authentic croissants at local boulangeries",
      "Reserve dinner at a traditional French bistro",
      "December can be cold - pack warm layers and comfortable shoes"
    ],
    "flightInfo": {
      "estimatedPrice": "$650-$950 per person",
      "priceRange": "Economy class roundtrip",
      "bestTimeToBook": "6-8 weeks in advance",
      "notes": "Prices vary by airline and booking time. Consider connecting flights for better rates."
    }
  }
}
```

## MCP Server Details

### Docker MCP Server

**Capabilities**:
- List Docker containers
- Start/stop containers
- View container logs
- Execute commands in containers

**When Used**:
- Claude can use this for containerized tool execution
- Useful for running scripts or tools in isolated environments

**Configuration**:
```properties
spring.ai.mcp.client.docker.command=npx
spring.ai.mcp.client.docker.args=-y,@modelcontextprotocol/server-docker
spring.ai.mcp.client.docker.transport=stdio
```

### Amadeus MCP Server

**Capabilities**:
- Flight search and pricing
- Hotel availability
- Travel destination information
- Real-time pricing data

**When Used**:
- Claude calls this automatically when you ask for flight prices
- Provides current market rates for flight estimates

**Configuration**:
```properties
spring.ai.mcp.client.amadeus.command=npx
spring.ai.mcp.client.amadeus.args=-y,@modelcontextprotocol/server-amadeus
spring.ai.mcp.client.amadeus.transport=stdio
```

## Running the Application

### Start Backend with MCP

```bash
# 1. Set API key
export ANTHROPIC_API_KEY=sk-ant-your-key

# 2. Start backend (MCP servers launch automatically)
./mvnw spring-boot:run

# You'll see MCP server initialization in logs:
# INFO - Initializing MCP client: docker
# INFO - Initializing MCP client: amadeus
```

### Start Frontend

```bash
cd frontend
npm start
```

Visit `http://localhost:3000` to use the app.

## Troubleshooting

### Issue: "API key cannot be null"

**Solution**:
```bash
# Verify environment variable is set
echo $ANTHROPIC_API_KEY

# If empty, export it
export ANTHROPIC_API_KEY=sk-ant-your-key

# Restart the app
./mvnw spring-boot:run
```

### Issue: MCP Server Not Starting

**Symptoms**:
```
ERROR - Failed to start MCP client: docker
```

**Solutions**:

1. **Check Node.js/npx**:
```bash
node --version  # Should be v16+
npx --version
```

2. **Manual MCP Server Test**:
```bash
npx -y @modelcontextprotocol/server-docker
npx -y @modelcontextprotocol/server-amadeus
```

3. **Clear npx cache**:
```bash
rm -rf ~/.npm/_npx
```

### Issue: No Flight Prices in Response

**Possible Causes**:
1. Amadeus MCP server not connected
2. Claude didn't use the tool (depends on query)
3. API rate limits

**Solution**:
- Check logs for Amadeus MCP initialization
- Ensure query mentions pricing/flights explicitly
- Fallback pricing will be provided if MCP fails

### Issue: "Connection refused" for Local AI

**Solution**:
```bash
# Verify Gemma3 model is running
curl http://localhost:12434/engines/llama.cpp/v1/chat/completions

# If not running, start your Docker AI model
```

## Costs & Rate Limits

### Anthropic Claude API

**Pricing** (as of 2024):
- Claude 3.5 Sonnet: ~$3 per 1M input tokens, ~$15 per 1M output tokens
- Typical trip plan: ~100-200 tokens input, ~500-1000 tokens output
- **Cost per request**: ~$0.01-$0.02

**Rate Limits**:
- Tier 1: 50 requests/minute
- Tier 2: 1000 requests/minute (after usage threshold)

### MCP Servers

**Docker MCP**: Free (local)
**Amadeus MCP**: Depends on Amadeus API access (may require signup)

## Production Considerations

### 1. Caching

Implement caching to reduce API costs:

```java
@Cacheable(value = "tripPlans", key = "#request.to + #request.from + #request.startDate")
public TripPlan generateTripPlan(ItineraryRequest request, String description) {
    // ...
}
```

### 2. Async Processing

For better UX, generate trip plans asynchronously:

```java
@Async
public CompletableFuture<TripPlan> generateTripPlanAsync(...) {
    // ...
}
```

### 3. Rate Limiting

Add rate limiting to prevent API abuse:

```java
@RateLimiter(name = "claudeApi", fallbackMethod = "fallbackTripPlan")
public TripPlan generateTripPlan(...) {
    // ...
}
```

### 4. Error Handling

The service includes fallback responses if Claude/MCP fails:
- Basic trip suggestions
- Generic price estimates
- Graceful degradation

### 5. Monitoring

Monitor API usage and costs:
- Log all Claude API calls
- Track token usage
- Set up alerts for high usage
- Review Anthropic console regularly

## Development vs Production

### Development
```properties
# Use smaller model for cost savings
spring.ai.anthropic.chat.options.model=claude-3-haiku-20240307

# Lower token limits
spring.ai.anthropic.chat.options.max-tokens=1024
```

### Production
```properties
# Use best model for quality
spring.ai.anthropic.chat.options.model=claude-3-5-sonnet-20241022

# Higher token limits for detailed responses
spring.ai.anthropic.chat.options.max-tokens=4096
```

## Security Best Practices

1. **Never commit API keys** to Git
2. **Use environment variables** for all sensitive data
3. **Rotate API keys** periodically
4. **Monitor API usage** for anomalies
5. **Implement rate limiting** on endpoints
6. **Validate all user input** before sending to AI
7. **Sanitize AI responses** before displaying to users

## Additional Resources

- [Anthropic API Docs](https://docs.anthropic.com/)
- [MCP Protocol Spec](https://modelcontextprotocol.io/)
- [Spring AI MCP Docs](https://docs.spring.io/spring-ai/reference/api/clients/mcp-client.html)
- [Docker MCP Server](https://github.com/modelcontextprotocol/servers/tree/main/src/docker)
- [Amadeus Travel API](https://developers.amadeus.com/)

## Support

For issues:
1. Check logs for error messages
2. Verify all environment variables are set
3. Test MCP servers independently
4. Review API key permissions
5. Check Anthropic API status page

For questions about this integration, refer to the codebase:
- **TripPlannerService** (`src/main/java/org/travel/travelapp/service/TripPlannerService.java`)
- **Configuration** (`src/main/resources/application.properties`)
- **AI Integration Docs** (`AI_INTEGRATION.md`)
