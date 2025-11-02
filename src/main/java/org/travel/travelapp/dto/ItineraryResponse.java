package org.travel.travelapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItineraryResponse {

    private String id;
    private String to;
    private String from;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer numberOfAdults;
    private String description;
    private LocalDateTime createdAt;
    private String status;
    private String message;
    private TripPlan tripPlan;

    // Constructor without tripPlan for backwards compatibility
    public ItineraryResponse(String id, String to, String from, LocalDate startDate,
                             LocalDate endDate, Integer numberOfAdults, String description,
                             LocalDateTime createdAt, String status, String message) {
        this.id = id;
        this.to = to;
        this.from = from;
        this.startDate = startDate;
        this.endDate = endDate;
        this.numberOfAdults = numberOfAdults;
        this.description = description;
        this.createdAt = createdAt;
        this.status = status;
        this.message = message;
    }
}
