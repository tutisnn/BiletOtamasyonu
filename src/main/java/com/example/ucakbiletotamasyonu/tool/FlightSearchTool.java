package com.example.ucakbiletotamasyonu.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
public class FlightSearchTool {

    private static final Logger log = LoggerFactory.getLogger(FlightSearchTool.class);

    private final RestClient restClient = RestClient.create();
    private final String flightApiBaseUrl;

    public FlightSearchTool(@Value("${app.flight-api-base-url:http://localhost:8080}") String flightApiBaseUrl) {
        this.flightApiBaseUrl = flightApiBaseUrl;
    }

    @Tool(description = """
            Search available flights in the flight booking API.
            Use this tool when the user asks to travel from one city/airport to another on a specific date.
            The departure and arrival values should be city names or airport/city names.
            The departureDate must be in ISO-8601 date format: yyyy-MM-dd.
            Returns the exact JSON response from the flight search API.
            """)
    public String searchFlights(String departure, String arrival, String departureDate) {
        if (isBlank(departure) || isBlank(arrival) || isBlank(departureDate)) {
            log.info("flight search tool called with missing params departure={}, arrival={}, departureDate={}", departure, arrival, departureDate);
            return """
                    {"message":"Uçuş araması için kalkış, varış ve tarih bilgisi gerekli.","data":[]}
                    """.trim();
        }

        URI uri = UriComponentsBuilder
                .fromUriString(flightApiBaseUrl)
                .path("/api/flights/search")
                .queryParam("departure", departure.trim())
                .queryParam("arrival", arrival.trim())
                .queryParam("departureDate", departureDate.trim())
                .encode()
                .build()
                .toUri();

        log.info("calling flight search api uri={}", uri);

        try {
            String response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);

            log.info("flight search api response received, length={}", response == null ? 0 : response.length());
            return response;
        } catch (Exception ex) {
            log.warn("flight search api call failed", ex);
            return String.format(
                    "{\"message\":\"flight search api call failed: %s\",\"data\":[]}",
                    escapeJson(ex.getMessage())
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "unknown error";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
