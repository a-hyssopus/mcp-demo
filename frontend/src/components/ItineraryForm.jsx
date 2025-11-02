import React, { useState } from 'react';
import './ItineraryForm.css';
import { createItinerary } from '../services/api';

const ItineraryForm = () => {
  const [formData, setFormData] = useState({
    to: '',
    from: '',
    startDate: '',
    endDate: '',
    numberOfAdults: 1,
    description: ''
  });

  const [errors, setErrors] = useState({});
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(false);
  const [response, setResponse] = useState(null);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: name === 'numberOfAdults' ? parseInt(value) || '' : value
    }));
    // Clear error for this field
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.to.trim()) newErrors.to = 'Destination is required';
    if (!formData.from.trim()) newErrors.from = 'Origin is required';
    if (!formData.startDate) newErrors.startDate = 'Start date is required';
    if (!formData.endDate) newErrors.endDate = 'End date is required';
    if (!formData.numberOfAdults || formData.numberOfAdults < 1) {
      newErrors.numberOfAdults = 'At least 1 adult required';
    }
    if (formData.numberOfAdults > 20) {
      newErrors.numberOfAdults = 'Maximum 20 adults allowed';
    }
    if (formData.description.length > 300) {
      newErrors.description = 'Description must not exceed 300 characters';
    }
    if (formData.startDate && formData.endDate && formData.endDate < formData.startDate) {
      newErrors.endDate = 'End date must be after start date';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSuccess(false);
    setResponse(null);

    if (!validateForm()) {
      return;
    }

    setLoading(true);

    try {
      const result = await createItinerary(formData);
      setResponse(result);
      setSuccess(true);
    } catch (error) {
      if (error.response?.data) {
        setErrors(error.response.data);
      } else {
        setErrors({ general: 'Failed to create itinerary. Please try again.' });
      }
    } finally {
      setLoading(false);
    }
  };

  const characterCount = formData.description.length;
  const characterLimit = 300;

  return (
    <div className="ios-container">
      <div className="ios-header">
        <h1 className="ios-title">Plan Your Trip</h1>
        <p className="ios-subtitle">Create your perfect itinerary</p>
      </div>

      <div className="ios-card">
        <form onSubmit={handleSubmit} className="ios-form">
          {/* Destination */}
          <div className="ios-form-group">
            <label className="ios-label">Destination</label>
            <input
              type="text"
              name="to"
              value={formData.to}
              onChange={handleChange}
              className={`ios-input ${errors.to ? 'ios-input-error' : ''}`}
              placeholder="Paris, France"
            />
            {errors.to && <span className="ios-error-text">{errors.to}</span>}
          </div>

          {/* Origin */}
          <div className="ios-form-group">
            <label className="ios-label">From</label>
            <input
              type="text"
              name="from"
              value={formData.from}
              onChange={handleChange}
              className={`ios-input ${errors.from ? 'ios-input-error' : ''}`}
              placeholder="London, UK"
            />
            {errors.from && <span className="ios-error-text">{errors.from}</span>}
          </div>

          {/* Dates Row */}
          <div className="ios-date-row">
            <div className="ios-form-group ios-form-group-half">
              <label className="ios-label">Start Date</label>
              <input
                type="date"
                name="startDate"
                value={formData.startDate}
                onChange={handleChange}
                className={`ios-input ios-input-date ${errors.startDate ? 'ios-input-error' : ''}`}
              />
              {errors.startDate && <span className="ios-error-text">{errors.startDate}</span>}
            </div>

            <div className="ios-form-group ios-form-group-half">
              <label className="ios-label">End Date</label>
              <input
                type="date"
                name="endDate"
                value={formData.endDate}
                onChange={handleChange}
                className={`ios-input ios-input-date ${errors.endDate ? 'ios-input-error' : ''}`}
              />
              {errors.endDate && <span className="ios-error-text">{errors.endDate}</span>}
            </div>
          </div>

          {/* Number of Adults */}
          <div className="ios-form-group">
            <label className="ios-label">Number of Adults</label>
            <div className="ios-stepper-container">
              <button
                type="button"
                className="ios-stepper-button"
                onClick={() => formData.numberOfAdults > 1 && handleChange({
                  target: { name: 'numberOfAdults', value: formData.numberOfAdults - 1 }
                })}
                disabled={formData.numberOfAdults <= 1}
              >
                −
              </button>
              <input
                type="number"
                name="numberOfAdults"
                value={formData.numberOfAdults}
                onChange={handleChange}
                className={`ios-input ios-input-number ${errors.numberOfAdults ? 'ios-input-error' : ''}`}
                min="1"
                max="20"
              />
              <button
                type="button"
                className="ios-stepper-button"
                onClick={() => formData.numberOfAdults < 20 && handleChange({
                  target: { name: 'numberOfAdults', value: formData.numberOfAdults + 1 }
                })}
                disabled={formData.numberOfAdults >= 20}
              >
                +
              </button>
            </div>
            {errors.numberOfAdults && <span className="ios-error-text">{errors.numberOfAdults}</span>}
          </div>

          {/* Description */}
          <div className="ios-form-group">
            <label className="ios-label">
              Description
              <span className="ios-char-count">
                {characterCount}/{characterLimit}
              </span>
            </label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleChange}
              className={`ios-textarea ${errors.description ? 'ios-input-error' : ''}`}
              placeholder="Tell us about your travel plans..."
              rows="4"
              maxLength="300"
            />
            {errors.description && <span className="ios-error-text">{errors.description}</span>}
          </div>

          {/* General Error */}
          {errors.general && (
            <div className="ios-alert ios-alert-error">
              {errors.general}
            </div>
          )}

          {/* Success Message with Trip Plan */}
          {success && response && (
            <div className="ios-alert ios-alert-success">
              <div className="ios-alert-title">Trip Created!</div>
              <div className="ios-alert-message">
                Your itinerary from {response.from} to {response.to} has been created successfully.
              </div>
              <div className="ios-alert-id">ID: {response.id}</div>

              {/* Trip Plan Details */}
              {response.tripPlan && (
                <div className="trip-plan-details">
                  {/* Summary */}
                  {response.tripPlan.summary && (
                    <div className="trip-section">
                      <h3 className="trip-section-title">Summary</h3>
                      <p className="trip-summary">{response.tripPlan.summary}</p>
                    </div>
                  )}

                  {/* Attractions */}
                  {response.tripPlan.attractions && response.tripPlan.attractions.length > 0 && (
                    <div className="trip-section">
                      <h3 className="trip-section-title">Top Attractions</h3>
                      <div className="attractions-list">
                        {response.tripPlan.attractions.map((attraction, index) => (
                          <div key={index} className="attraction-card">
                            <div className="attraction-header">
                              <span className="attraction-number">{index + 1}</span>
                              <h4 className="attraction-name">{attraction.name}</h4>
                            </div>
                            <p className="attraction-description">{attraction.description}</p>
                            {attraction.distanceFromCenter && (
                              <div className="attraction-distance">
                                📍 {attraction.distanceFromCenter.toFixed(1)} km from city center
                              </div>
                            )}
                            {attraction.address && (
                              <div className="attraction-address">{attraction.address}</div>
                            )}
                          </div>
                        ))}
                      </div>
                    </div>
                  )}

                  {/* Flights */}
                  {response.tripPlan.flights && response.tripPlan.flights.length > 0 && (
                    <div className="trip-section">
                      <h3 className="trip-section-title">Flight Options</h3>
                      <div className="flights-list">
                        {response.tripPlan.flights.map((flight, index) => (
                          <div key={index} className="flight-card">
                            <div className="flight-header">
                              <span className="flight-airline">{flight.airline}</span>
                              <span className="flight-price">{flight.price}</span>
                            </div>
                            <div className="flight-details">
                              <div className="flight-time">
                                <span>🛫 {flight.departureTime}</span>
                                <span className="flight-arrow">→</span>
                                <span>🛬 {flight.arrivalTime}</span>
                              </div>
                              <div className="flight-info">
                                <span>⏱ {flight.duration}</span>
                                <span>• {flight.stops === 0 ? 'Non-stop' : `${flight.stops} stop(s)`}</span>
                                <span>• {flight.bookingClass}</span>
                              </div>
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              )}
            </div>
          )}

          {/* Submit Button */}
          <button
            type="submit"
            className="ios-button ios-button-primary"
            disabled={loading}
          >
            {loading ? (
              <span className="ios-loading">
                <span className="ios-spinner"></span>
                Creating...
              </span>
            ) : (
              'Create Itinerary'
            )}
          </button>
        </form>
      </div>

      <div className="ios-footer">
        <p>Safe travels!</p>
      </div>
    </div>
  );
};

export default ItineraryForm;
