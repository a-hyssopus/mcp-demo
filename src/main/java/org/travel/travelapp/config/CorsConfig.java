package org.travel.travelapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

// not relevant, only for local setup
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Allow credentials
        config.setAllowCredentials(true);

        // Allow frontend origin
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://127.0.0.1:3000"
        ));

        // Allow all headers
        config.addAllowedHeader("*");

        // Allow all HTTP methods
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // Apply configuration to all paths
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
