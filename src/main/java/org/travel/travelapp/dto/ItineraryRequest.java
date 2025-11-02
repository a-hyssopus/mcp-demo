package org.travel.travelapp.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ItineraryRequest {

    @NotBlank(message = "Destination (to) is required")
    private String to;

    @NotBlank(message = "Origin (from) is required")
    private String from;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or in the future")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Number of adults is required")
    @Min(value = 1, message = "At least 1 adult is required")
    @Max(value = 20, message = "Maximum 20 adults allowed")
    private Integer numberOfAdults;

    @Size(max = 300, message = "Description must not exceed 300 characters")
    private String description;

    @AssertTrue(message = "End date must be after start date")
    private boolean isEndDateAfterStartDate() {
        if (startDate == null || endDate == null) {
            return true; // Let @NotNull handle null validation
        }
        return endDate.isAfter(startDate) || endDate.isEqual(startDate);
    }
}
