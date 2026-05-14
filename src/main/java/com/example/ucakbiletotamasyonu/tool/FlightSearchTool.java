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
            Use this tool when the user asks to travel from one city to another on a specific date.
            The departure and arrival values should be city names.
            The departureDate must be in ISO-8601 date format: yyyy-MM-dd.
            Returns the exact JSON response from the flight search API.
            """)
    public String searchFlights(String departureCity, String arrivalCity, String departureDate) {
        return searchFlightsDetailed(departureCity, arrivalCity, departureDate, null, null);
    }

    @Tool(description = """
            Search available flights in the flight booking API with optional airport names.
            Use this tool when the user specifies both city and airport.
            The departure and arrival values should be city names.
            The optional departureAirport/arrivalAirport values should be airport names.
            The departureDate must be in ISO-8601 date format: yyyy-MM-dd.
            Returns the exact JSON response from the flight search API.
            """)
    public String searchFlightsDetailed(String departureCity, String arrivalCity, String departureDate,
                                        String departureAirport, String arrivalAirport) {
        if (isBlank(departureCity) || isBlank(arrivalCity) || isBlank(departureDate)) {
            log.info("flight search tool called with missing params departureCity={}, arrivalCity={}, departureDate={}",
                    departureCity, arrivalCity, departureDate);
            return """
                    {"message":"Uçuş araması için kalkış, varış ve tarih bilgisi gerekli.","data":[]}
                    """.trim();
        }

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(flightApiBaseUrl)
                .path("/api/flights/search")
                .queryParam("departure", departureCity.trim())
                .queryParam("arrival", arrivalCity.trim())
                .queryParam("departureDate", departureDate.trim());

        if (!isBlank(departureAirport)) {
            builder.queryParam("departureAirport", departureAirport.trim());
        }
        if (!isBlank(arrivalAirport)) {
            builder.queryParam("arrivalAirport", arrivalAirport.trim());
        }

        URI uri = builder.encode().build().toUri();
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

