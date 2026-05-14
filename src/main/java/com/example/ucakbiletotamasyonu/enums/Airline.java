package com.example.ucakbiletotamasyonu.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Airline {
    TURKISH_AIRLINES("Turkish Airlines"),
    PEGASUS_AIRLINES("Pegasus Airlines"),
    SUNEXPRESS("SunExpress"),
    AJET("AJet");

    private final String displayName;

    Airline(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getDisplayName() {
        return displayName;
    }

    @JsonCreator
    public static Airline fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        for (Airline airline : values()) {
            if (airline.displayName.equalsIgnoreCase(value)
                    || airline.name().equalsIgnoreCase(value)
                    || airline.displayName.replace(" ", "").equalsIgnoreCase(value.replace(" ", ""))) {
                return airline;
            }
        }

        throw new IllegalArgumentException("Unknown airline: " + value);
    }
}
