package com.example.ucakbiletotamasyonu.service.impl;

import com.example.ucakbiletotamasyonu.dto.ChatResponse;
import com.example.ucakbiletotamasyonu.dto.FlightDto;
import com.example.ucakbiletotamasyonu.service.IChatService;
import com.example.ucakbiletotamasyonu.tool.FlightSearchTool;
import com.example.ucakbiletotamasyonu.enums.FlightStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class ChatServiceImpl implements IChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final ChatClient chatClient;
    private final FlightSearchTool flightSearchTool;
    private final ObjectMapper objectMapper;

    public ChatServiceImpl(ChatClient chatClient, FlightSearchTool flightSearchTool, ObjectMapper objectMapper) {
        this.chatClient = chatClient;
        this.flightSearchTool = flightSearchTool;
        this.objectMapper = objectMapper;
    }

    @Override
    public ChatResponse chat(String message) {
        log.info("chat request received, length={}, preview={}", message == null ? 0 : message.length(), preview(message));

        FlightQuery flightQuery = extractFlightQuery(message);
        if (flightQuery != null) {
            if (flightQuery.needsMoreInfo()) {
                log.info("flight search needs more info");
                return clarificationResponse();
            }
            return handleFlightSearch(flightQuery);
        }

        String assistantText = chatClient.prompt()
                .user(message)
                .call()
                .content();

        log.info("regular chat response generated, length={}", assistantText == null ? 0 : assistantText.length());
        return new ChatResponse("text", assistantText, List.of());
    }

    private ChatResponse handleFlightSearch(FlightQuery flightQuery) {
        log.info("flight search resolved, departure={}, arrival={}, departureDate={}",
                flightQuery.departure(), flightQuery.arrival(), flightQuery.departureDate());

        String rawResponse = flightSearchTool.searchFlights(
                flightQuery.departure(),
                flightQuery.arrival(),
                flightQuery.departureDate().toString()
        );

        log.info("flight api raw response={}", rawResponse);

        List<FlightDto> flights = parseFlights(rawResponse);
        log.info("parsed flights count={}", flights.size());

        String assistantText = buildAssistantText(flightQuery, flights);
        return new ChatResponse("flight_search_result", assistantText, flights);
    }

    private String buildAssistantText(FlightQuery flightQuery, List<FlightDto> flights) {
        if (flights.isEmpty()) {
            return String.format(
                    Locale.forLanguageTag("tr-TR"),
                    "%s tarihinde %s'den %s'ye uygun uçuş bulamadım.",
                    flightQuery.departureDate(),
                    flightQuery.departure(),
                    flightQuery.arrival()
            );
        }

        return String.format(
                Locale.forLanguageTag("tr-TR"),
                "%s tarihinde %s'den %s'ye %d uygun uçuş buldum. Kartlarda görebilirsin.",
                flightQuery.departureDate(),
                flightQuery.departure(),
                flightQuery.arrival(),
                flights.size()
        );
    }

    private List<FlightDto> parseFlights(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode dataNode = root.path("data");
            if (!dataNode.isArray()) {
                log.warn("flight api response does not contain data array");
                return List.of();
            }
            List<FlightDto> flights = new ArrayList<>();
            for (JsonNode flightNode : dataNode) {
                flights.add(toFlightDto(flightNode));
            }
            return flights;
        } catch (Exception ex) {
            log.warn("failed to parse flight api response", ex);
            return List.of();
        }
    }

    private FlightDto toFlightDto(JsonNode node) {
        return new FlightDto(
                intValue(node, "id"),
                textValue(node, "flightNo"),
                textValue(node, "airline"),
                textValue(node, "departure"),
                textValue(node, "arrival"),
                localDateTimeValue(node, "departureTime"),
                localDateTimeValue(node, "arrivalTime"),
                bigDecimalValue(node, "price"),
                intValue(node, "capacity"),
                intValue(node, "availableSeats"),
                flightStatusValue(node, "status")
        );
    }

    private FlightQuery extractFlightQuery(String message) {
        String extraction = chatClient.prompt()
                .system("""
                        You are a flight search slot extractor.
                        Return ONLY a JSON object with these keys:
                        - intent: "flight_search" or "chat"
                        - departure: string or null
                        - arrival: string or null
                        - departureDate: ISO-8601 date string yyyy-MM-dd or null
                        - needsMoreInfo: boolean
                        Use exact values from the user's message. Do not add markdown, code fences, or explanations.
                        If the user is not asking about flights, set intent to "chat" and needsMoreInfo to false.
                        If the user is asking about flights but a required field is missing, set intent to "flight_search" and needsMoreInfo to true.
                        """)
                .user(message)
                .call()
                .content();

        log.info("slot extractor raw output={}", extraction);

        if (extraction == null || extraction.isBlank()) {
            return null;
        }

        String json = stripCodeFences(extraction.trim());
        try {
            JsonNode root = objectMapper.readTree(json);
            String intent = textValue(root, "intent");
            log.info("slot extractor parsed intent={}", intent);

            if (!"flight_search".equalsIgnoreCase(intent)) {
                return null;
            }

            if (root.path("needsMoreInfo").asBoolean(false)) {
                return FlightQuery.needMoreInfo();
            }

            String departure = cleanSlot(textValue(root, "departure"));
            String arrival = cleanSlot(textValue(root, "arrival"));
            String departureDateText = cleanSlot(textValue(root, "departureDate"));

            log.info("slot extractor parsed departure={}, arrival={}, departureDate={}", departure, arrival, departureDateText);

            if (isBlank(departure) || isBlank(arrival) || isBlank(departureDateText)) {
                return FlightQuery.needMoreInfo();
            }

            LocalDate departureDate = LocalDate.parse(departureDateText);
            return new FlightQuery(departure, arrival, departureDate);
        } catch (Exception ex) {
            log.warn("failed to parse slot extractor output", ex);
            return null;
        }
    }

    private ChatResponse clarificationResponse() {
        return new ChatResponse(
                "text",
                "Uçuş araması için kalkış, varış ve tarih bilgisini paylaşır mısın?",
                List.of()
        );
    }

    private String stripCodeFences(String value) {
        String trimmed = value.trim();
        if (trimmed.startsWith("```")) {
            int firstLineBreak = trimmed.indexOf('\n');
            if (firstLineBreak >= 0) {
                trimmed = trimmed.substring(firstLineBreak + 1).trim();
            }
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3).trim();
            }
        }
        return trimmed;
    }

    private String textValue(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull()) {
            return null;
        }
        if (field.isTextual()) {
            return field.asText();
        }
        return field.toString();
    }

    private String cleanSlot(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return "null".equalsIgnoreCase(trimmed) ? null : trimmed;
    }

    private Integer intValue(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return field == null || field.isNull() ? null : field.asInt();
    }

    private BigDecimal bigDecimalValue(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        if (field == null || field.isNull()) {
            return null;
        }
        try {
            return field.decimalValue();
        } catch (Exception ex) {
            return null;
        }
    }

    private LocalDateTime localDateTimeValue(JsonNode node, String fieldName) {
        String value = textValue(node, fieldName);
        if (isBlank(value)) {
            return null;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private FlightStatus flightStatusValue(JsonNode node, String fieldName) {
        String value = textValue(node, fieldName);
        if (isBlank(value)) {
            return null;
        }
        try {
            return FlightStatus.valueOf(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String preview(String value) {
        if (value == null) {
            return "null";
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        return normalized.length() <= 80 ? normalized : normalized.substring(0, 80) + "...";
    }

    private record FlightQuery(String departure, String arrival, LocalDate departureDate) {
        static FlightQuery needMoreInfo() {
            return new FlightQuery("__NEED_MORE_INFO__", "__NEED_MORE_INFO__", null);
        }

        boolean needsMoreInfo() {
            return "__NEED_MORE_INFO__".equals(departure);
        }
    }
}
