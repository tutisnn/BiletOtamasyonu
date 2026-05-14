package com.example.ucakbiletotamasyonu.converter;

import com.example.ucakbiletotamasyonu.enums.Airline;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AirlineConverter implements AttributeConverter<Airline, String> {

    @Override
    public String convertToDatabaseColumn(Airline attribute) {
        return attribute == null ? null : attribute.getDisplayName();
    }

    @Override
    public Airline convertToEntityAttribute(String dbData) {
        return Airline.fromValue(dbData);
    }
}
