package com.amr.assignments.openinghoursrenderer.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.stream.Stream;

/**
 * Day of the week
 */
public enum DayOfWeek {

    MONDAY("Monday"),
    TUESDAY("Tuesday"),
    WEDNESDAY("Wednesday"),
    THURSDAY("Thursday"),
    FRIDAY("Friday"),
    SATURDAY("Saturday"),
    SUNDAY("Sunday");

    private String name;

    DayOfWeek(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @JsonCreator
    public static DayOfWeek of(final String name) {
        return Stream.of(values())
                .filter(v -> v.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid day of week"));
    }

    /**
     * Get next day of the week, i.e. tomorrow
     * @return tomorrow
     */
    public DayOfWeek tomorrow() {
        return values()[(this.ordinal() + 1) % values().length];
    }

}