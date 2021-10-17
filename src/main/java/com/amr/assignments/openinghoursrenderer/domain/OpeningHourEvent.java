package com.amr.assignments.openinghoursrenderer.domain;


import java.time.LocalTime;

/**
 * Open/close hour time
 */
public class OpeningHourEvent implements Comparable<OpeningHourEvent> {

    private OpeningHourEventType type;
    private LocalTime value;

    public OpeningHourEvent() {}

    public OpeningHourEvent(OpeningHourEventType type, LocalTime value) {
        this.type = type;
        this.value = value;
    }

    public OpeningHourEventType getType() {
        return type;
    }

    public void setType(OpeningHourEventType type) {
        this.type = type;
    }

    public LocalTime getValue() {
        return value;
    }

    public void setValue(LocalTime value) {
        this.value = value;
    }

    @Override
    public int compareTo(OpeningHourEvent other) {
        return this.getValue().compareTo(other.getValue());
    }
}
