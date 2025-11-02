package org.travel.travelapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TripPlan {

    private String summary;
    private List<Attraction> attractions;
    private List<Flight> flights;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Attraction {
        private String name;
        private String description;
        private Double distanceFromCenter; // in kilometers
        private String address;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Flight {
        private String airline;
        private String price;
        private String departureTime;
        private String arrivalTime;
        private String duration;
        private Integer stops;
        private String bookingClass;
    }
}
