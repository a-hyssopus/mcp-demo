import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: false,
});

export const createItinerary = async (itineraryData) => {
  try {
    const response = await api.post('/itinerary', itineraryData);
    return response.data;
  } catch (error) {
    console.error('Error creating itinerary:', error);
    throw error;
  }
};

export default api;
