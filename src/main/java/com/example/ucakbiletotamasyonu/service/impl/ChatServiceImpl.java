package com.example.ucakbiletotamasyonu.service.impl;

import com.example.ucakbiletotamasyonu.dto.AirportInfoDto;
import com.example.ucakbiletotamasyonu.dto.ChatResponse;
import com.example.ucakbiletotamasyonu.dto.FlightDto;
import com.example.ucakbiletotamasyonu.enums.Airline;
import com.example.ucakbiletotamasyonu.enums.FlightStatus;
import com.example.ucakbiletotamasyonu.service.IChatService;
import com.example.ucakbiletotamasyonu.tool.FlightSearchTool;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ChatServiceImpl implements IChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    private static final Locale TR = Locale.forLanguageTag("tr-TR");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // Minimal normalization for Turkish city names coming from natural language.
    // Prefer Turkish canonical spellings; we still try ASCII variants via fallback search.
    private static final Map<String, String> CITY_ALIASES = Map.ofEntries(
            Map.entry("istanbul", "\u0130stanbul"), // İstanbul
            Map.entry("izmir", "\u0130zmir"),       // İzmir
            Map.entry("adana", "Adana"),
            Map.entry("ankara", "Ankara"),
            Map.entry("antalya", "Antalya"),
            Map.entry("amsterdam", "Amsterdam"),
            Map.entry("londra", "Londra"),
            Map.entry("london", "London")
    );

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

        FlightSearchResult searchResult = searchFlightsWithFallback(
                flightQuery.departure(),
                flightQuery.arrival(),
                flightQuery.departureDate().toString()
        );

        log.info("flight api raw response={}", searchResult.rawResponse());

        List<FlightDto> flights = searchResult.flights();
        log.info("parsed flights count={}", flights.size());

        FlightQuery resolvedQuery = new FlightQuery(searchResult.departure(), searchResult.arrival(), flightQuery.departureDate());
        String assistantText = buildAssistantText(resolvedQuery, flights);
        log.info("flight_search_result assistantText preview={}", preview(assistantText));

        return new ChatResponse("flight_search_result", assistantText, flights);
    }

    private FlightSearchResult searchFlightsWithFallback(String departure, String arrival, String departureDateIso) {
        // DB city values might be stored as ASCII (Istanbul/Izmir) or Turkish (İstanbul/İzmir).
        // Try a small set of deterministic variants and stop at the first non-empty result.
        List<String> depCandidates = cityCandidates(departure);
        List<String> arrCandidates = cityCandidates(arrival);

        String firstRaw = null;
        for (String dep : depCandidates) {
            for (String arr : arrCandidates) {
                String raw = flightSearchTool.searchFlights(dep, arr, departureDateIso);
                if (firstRaw == null) {
                    firstRaw = raw;
                }
                List<FlightDto> flights = parseFlights(raw);
                if (!flights.isEmpty()) {
                    return new FlightSearchResult(dep, arr, raw, flights);
                }
            }
        }

        // Nothing found; return the first attempt (or an empty JSON if tool returned null).
        String raw = firstRaw == null ? "{\"data\":[],\"message\":\"İşlem başarılı\"}" : firstRaw;
        return new FlightSearchResult(departure, arrival, raw, parseFlights(raw));
    }

    private List<String> cityCandidates(String city) {
        if (isBlank(city)) {
            return List.of(city);
        }
        String c = city.trim();
        List<String> out = new ArrayList<>();
        out.add(c);

        // Prefer Turkish canonical variant first, then ASCII fallback.
        if ("Istanbul".equalsIgnoreCase(c) || "\u0130stanbul".equalsIgnoreCase(c)) {
            out.add("\u0130stanbul");
            out.add("Istanbul");
        }
        if ("Izmir".equalsIgnoreCase(c) || "\u0130zmir".equalsIgnoreCase(c)) {
            out.add("\u0130zmir");
            out.add("Izmir");
        }

        // De-dup while keeping order.
        List<String> dedup = new ArrayList<>();
        for (String s : out) {
            if (!dedup.contains(s)) {
                dedup.add(s);
            }
        }
        return dedup;
    }

    private String buildAssistantText(FlightQuery flightQuery, List<FlightDto> flights) {
        if (flights.isEmpty()) {
            return String.format(
                    TR,
                    "%s tarihinde %s'den %s'ye uygun uçuş bulamadım.",
                    flightQuery.departureDate(),
                    flightQuery.departure(),
                    flightQuery.arrival()
            );
        }

        StringBuilder sb = new StringBuilder();
        if (flights.size() == 1) {
            sb.append(String.format(
                    TR,
                    "%s tarihinde %s'den %s'ye 1 uygun uçuş buldum.",
                    flightQuery.departureDate(),
                    flightQuery.departure(),
                    flightQuery.arrival()
            ));
        } else {
            sb.append(String.format(
                    TR,
                    "%s tarihinde %s'den %s'ye %d uygun uçuş buldum.",
                    flightQuery.departureDate(),
                    flightQuery.departure(),
                    flightQuery.arrival(),
                    flights.size()
            ));
        }

        int max = Math.min(3, flights.size());
        sb.append("\n\n");
        for (int i = 0; i < max; i++) {
            sb.append(buildFlightSentence(flights.get(i)));
            if (i < max - 1) {
                sb.append("\n");
            }
        }

        if (flights.size() > max) {
            sb.append(String.format(TR, "\n\nİstersen diğer %d seçeneği de kartlardan görebilirsin.", flights.size() - max));
        }

        sb.append("\n\nİstersen en ucuzunu ya da en erken kalkışlı olanı birlikte seçelim.");
        return sb.toString();
    }

    private String buildFlightSentence(FlightDto f) {
        StringBuilder s = new StringBuilder();
        String flightNo = nullToDash(f.getFlightNo());
        String airline = f.getAirline() == null ? null : f.getAirline().toString();
        String dep = formatAirportShort(f.getDeparture());
        String arr = formatAirportShort(f.getArrival());
        String depTime = formatTime(f.getDepartureTime());
        String arrTime = formatTime(f.getArrivalTime());
        String price = formatTry(f.getPrice());

        if (airline != null && !airline.isBlank() && !"-".equals(airline)) {
            s.append(flightNo).append(" numaralı ").append(airline).append(" uçuşu var. ");
        } else {
            s.append(flightNo).append(" numaralı bir uçuş var. ");
        }

        s.append(dep).append("'dan ")
                .append(depTime)
                .append("'de kalkıyor, ")
                .append(arr)
                .append("'ye ")
                .append(arrTime)
                .append("'de varıyor. ");

        s.append("Fiyatı ").append(price).append(".");

        if (f.getAvailableSeats() != null) {
            s.append(" Şu an ").append(f.getAvailableSeats()).append(" boş koltuk görünüyor.");
        }
        return s.toString();
    }

    private String formatAirportShort(AirportInfoDto airport) {
        if (airport == null) {
            return "-";
        }
        String city = airport.getCity();
        String name = airport.getAirport();
        if (isBlank(city) && isBlank(name)) {
            return "-";
        }
        // Prefer the airport display name if present (often includes IATA like "(AMS)"), otherwise city.
        if (!isBlank(name) && !name.equalsIgnoreCase(city)) {
            return name;
        }
        return isBlank(city) ? "-" : city;
    }

    private String formatTime(LocalDateTime dt) {
        if (dt == null) {
            return "--:--";
        }
        try {
            return TIME_FMT.format(dt);
        } catch (Exception ex) {
            return "--:--";
        }
    }

    private String formatTry(BigDecimal amount) {
        if (amount == null) {
            return "-";
        }
        try {
            NumberFormat nf = NumberFormat.getNumberInstance(TR);
            nf.setMinimumFractionDigits(0);
            nf.setMaximumFractionDigits(2);
            return nf.format(amount) + " TL";
        } catch (Exception ex) {
            return amount.stripTrailingZeros().toPlainString() + " TL";
        }
    }

    private String nullToDash(String value) {
        return isBlank(value) ? "-" : value;
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
                airlineValue(node, "airline"),
                airportInfoValue(node, "departure"),
                airportInfoValue(node, "arrival"),
                localDateTimeValue(node, "departureTime"),
                localDateTimeValue(node, "arrivalTime"),
                bigDecimalValue(node, "price"),
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

            String departure = normalizeCity(cleanSlot(textValue(root, "departure")));
            String arrival = normalizeCity(cleanSlot(textValue(root, "arrival")));
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

    private String normalizeCity(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isBlank()) {
            return null;
        }

        // Lower with TR locale; "İ" can become "i\u0307" (i + combining dot). Remove the combining dot for stable matching.
        String key = trimmed.toLowerCase(TR).trim().replace("\u0307", "");
        key = stripTurkishLocationSuffix(key);

        String mapped = CITY_ALIASES.get(key);
        if (mapped != null) {
            return mapped;
        }

        // Fallback: title-case-ish for readability.
        if (key.length() == 1) {
            return key.toUpperCase(TR);
        }
        return key.substring(0, 1).toUpperCase(TR) + key.substring(1);
    }

    /**
     * Very small heuristic to handle common Turkish case suffixes when users say:
     * "izmir'e/izmire", "izmir'den/izmirden", "istanbul'a/istanbula", etc.
     */
    private String stripTurkishLocationSuffix(String key) {
        if (key == null) {
            return null;
        }
        String k = key
                .replace("’", "'")
                .replaceAll("[^\\p{L}' ]+", "")
                .trim();

        k = k.replace("'", "");
        k = k.replaceAll("\\s+", " ").trim();

        String[] suffixes = new String[] {
                "lerden", "lardan",
                "nden", "ndan",
                "den", "dan", "ten", "tan",
                "de", "da", "te", "ta",
                "ye", "ya",
                "e", "a"
        };

        for (String s : suffixes) {
            if (k.length() > s.length() + 2 && k.endsWith(s)) {
                return k.substring(0, k.length() - s.length()).trim();
            }
        }
        return k;
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

    private Airline airlineValue(JsonNode node, String fieldName) {
        String value = textValue(node, fieldName);
        if (isBlank(value)) {
            return null;
        }
        try {
            return Airline.fromValue(value);
        } catch (Exception ex) {
            return null;
        }
    }

    private AirportInfoDto airportInfoValue(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isTextual()) {
            // Backward compatible: some older responses may still be plain string.
            return new AirportInfoDto(value.asText(), value.asText());
        }
        String city = textValue(value, "city");
        String airport = textValue(value, "airport");
        if (isBlank(city) || isBlank(airport)) {
            return null;
        }
        return new AirportInfoDto(city, airport);
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

    private record FlightSearchResult(String departure, String arrival, String rawResponse, List<FlightDto> flights) {
    }
}
