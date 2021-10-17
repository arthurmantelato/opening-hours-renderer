package com.amr.assignments.openinghoursrenderer.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.stream.Stream;

/**
 * Opening hour event type
 */
public enum OpeningHourEventType {

    OPEN("open"),
    CLOSE("close");

    private final String value;

    OpeningHourEventType(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @JsonCreator
    public static OpeningHourEventType of(final String value) {
        return Stream.of(values())
                .filter(v -> v.getValue().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
