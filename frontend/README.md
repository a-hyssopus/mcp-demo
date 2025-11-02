# Travel App Frontend

A React frontend for the Travel Itinerary App.

### Prerequisites

- Node.js (v14 or higher)
- npm or yarn
- Backend server running on port 8080

### Installation

```bash
cd frontend
npm install
```

### Running the App

```bash
npm start
```

The app will open at [http://localhost:3000](http://localhost:3000)

### Building for Production

```bash
npm run build
```

## Features

### Itinerary Form

- **Destination & Origin**: Text input fields
- **Date Selection**: Start and end date pickers
- **Number of Adults**: Stepper control (1-20 adults)
- **Description**: Textarea with 300 character limit
- **Real-time Validation**: Instant feedback on form errors
- **Success Messages**: Confirmation when itinerary is created

### Validation Rules

- Destination and origin are required
- Start date must be today or in the future
- End date must be after or equal to start date
- Number of adults: minimum 1, maximum 20
- Description: optional, max 300 characters

## API Integration

The frontend communicates with the Spring Boot backend via REST API:

- **Endpoint**: `POST /api/itinerary`
- **Proxy**: Configured to proxy API requests to `http://localhost:8080`

## Styling

The app uses pure CSS with iOS 7 design principles:

- San Francisco font family (system default)
- iOS blue (#007AFF) for primary actions
- Rounded corners (10-14px)
- Thin, light typography
- Card-based layout
- Subtle hover and active states

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## License

MIT
