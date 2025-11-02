# Quick Start Guide

## Backend + Frontend Setup with CORS

### Current Setup

- **Backend**: Spring Boot running on port **8080**
- **Frontend**: React app running on port **3000**
- **CORS**: Configured to allow requests from `http://localhost:3000`

### Starting the Application

#### Terminal 1 - Backend

```bash
./mvnw spring-boot:run
```

Backend will start on: **http://localhost:8080**

#### Terminal 2 - Frontend

```bash
npm start
```

Frontend will start on: **http://localhost:3000** (opens automatically in browser)

### CORS Configuration

The backend has been configured with CORS to accept requests from the frontend:

**Files Updated:**
1. `src/main/java/org/travel/travelapp/config/CorsConfig.java` - Global CORS filter
2. `src/main/java/org/travel/travelapp/controller/ItineraryController.java` - Added @CrossOrigin annotation
3. `frontend/src/services/api.js` - Points to `http://localhost:8080/api`

**CORS Headers Enabled:**
- `Access-Control-Allow-Origin: http://localhost:3000`
- `Access-Control-Allow-Credentials: true`
- `Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH`
- `Access-Control-Allow-Headers: *`

### Testing the App

1. Open your browser to **http://localhost:3000**
2. Fill out the itinerary form:
   - **Destination**: Paris, France
   - **From**: London, UK
   - **Start Date**: 2025-11-01
   - **End Date**: 2025-11-07
   - **Number of Adults**: 2
   - **Description**: A week exploring Paris
3. Click "Create Itinerary"
4. You should see a success message with the created itinerary ID

### API Endpoint

**POST** `http://localhost:8080/api/itinerary`

**Request Body:**
```json
{
  "to": "Paris",
  "from": "London",
  "startDate": "2025-11-01",
  "endDate": "2025-11-07",
  "numberOfAdults": 2,
  "description": "A wonderful week exploring the City of Lights"
}
```

**Response:**
```json
{
  "id": "a09f6917-c6a5-499d-a73f-737187bc492d",
  "to": "Paris",
  "from": "London",
  "startDate": "2025-11-01",
  "endDate": "2025-11-07",
  "numberOfAdults": 2,
  "description": "A wonderful week exploring the City of Lights",
  "createdAt": "2025-10-26T16:13:18.214929",
  "status": "CREATED",
  "message": "Itinerary request received successfully"
}
```

### Troubleshooting

**CORS Error?**
- Make sure backend is running on port 8080
- Make sure frontend is running on port 3000
- Check browser console for CORS errors
- Verify CORS headers in Network tab

**Port Already in Use?**
```bash
# Kill process on port 8080
lsof -ti:8080 | xargs kill -9

# Kill process on port 3000
lsof -ti:3000 | xargs kill -9
```

**Frontend can't connect to backend?**
- Verify backend is running: `curl http://localhost:8080/api/itinerary`
- Check `frontend/src/services/api.js` has correct URL: `http://localhost:8080/api`

### Architecture

```
┌─────────────────┐          HTTP POST           ┌─────────────────┐
│   React App     │ ──────────────────────────► │  Spring Boot    │
│  (Port 3000)    │ ◄────────────────────────── │   (Port 8080)   │
│                 │        JSON Response         │                 │
└─────────────────┘                              └─────────────────┘
      Browser                                          Server
```

### What's Next?

- Add database persistence (H2, PostgreSQL)
- Implement AI-powered recommendations with Anthropic Claude
- Add authentication/authorization
- Create additional endpoints (GET, UPDATE, DELETE)
- Add more form fields (children, budget, preferences)
