package com.amr.assignments.openinghoursrenderer.service;

import com.amr.assignments.openinghoursrenderer.domain.DayOfWeek;
import com.amr.assignments.openinghoursrenderer.domain.OpeningHourEvent;
import com.amr.assignments.openinghoursrenderer.domain.OpeningHourEventType;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

@Service
public class OpeningHoursParser {

    private static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mm:ss a").withZone(ZoneId.of("UTC"));

    /**
     * Parse weekly opening hour events to a human readable format
     *
     * @param weeklyOpeningHours map that key is day of the week and value is a list of event times (open and/or close
     *                           hours) for that day
     * @return human readable representation of weekly opening hour period(s)
     */
    public String parse(final Map<DayOfWeek, List<OpeningHourEvent>> weeklyOpeningHours) {
        if (weeklyOpeningHours == null) {
            throw new IllegalArgumentException("no opening hours informed");
        }

        return weeklyOpeningHours.entrySet().stream()
                .filter(e -> Objects.nonNull(e.getValue()))
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(e -> formatOpeningHoursForDay(e.getKey(), e.getValue(), weeklyOpeningHours.get(e.getKey().tomorrow())))
                .filter(not(String::isBlank))// discard empty opening hours
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private String formatOpeningHoursForDay(final DayOfWeek today,
                                            final List<OpeningHourEvent> todayOpeningHours,
                                            final List<OpeningHourEvent> tomorrowOpeningHours) {
        final String formattedHours = formatOpeningHours(todayOpeningHours, tomorrowOpeningHours);
        return formattedHours.isEmpty() ? formattedHours : String.format("%s: %s", today.getName(), formattedHours);
    }

    /**
     * Format opening hours period(s) for a specified day of the week to a human readable format
     *
     * @param todayOpeningHours opening hours events list of the day
     * @param tomorrowOpeningHours opening hours events list of the next day (to be used in case of overnight period)
     * @return formatted opening hours period(s)
     */
    private String formatOpeningHours(final List<OpeningHourEvent> todayOpeningHours,
                                      final List<OpeningHourEvent> tomorrowOpeningHours) {
        if(todayOpeningHours == null) {
            throw new IllegalArgumentException("opening hours not informed");
        }

        if(todayOpeningHours.isEmpty()) {
            return "Closed";
        }

        final List<String> formattedOpeningHours = new ArrayList<>();

        List<OpeningHourEvent> todayOpenHours = tryGetOpeningHoursOfType(todayOpeningHours, OpeningHourEventType.OPEN);
        List<OpeningHourEvent> todayCloseHours = tryGetOpeningHoursOfType(todayOpeningHours, OpeningHourEventType.CLOSE);

        if(todayOpenHours.size() >= 1 || todayCloseHours.size() >= 1) {
            int numberOfPeriods = Math.min(todayOpenHours.size(), todayCloseHours.size());

            // discard first close hour because it is from previous day
            if(todayCloseHours.size() > todayOpenHours.size()) {
                todayCloseHours = todayCloseHours.subList(1, todayCloseHours.size());
            }

            for (int i = 0; i < numberOfPeriods; i++) {
                formattedOpeningHours.add(String.format("%s - %s",
                        formatTime(todayOpenHours.get(i).getValue()),
                        formatTime(todayCloseHours.get(i).getValue())));
            }

            if (todayOpenHours.size() > todayCloseHours.size()) {
                String overnightPeriod = calculateOvernightPeriod(todayOpeningHours, tomorrowOpeningHours);
                if(overnightPeriod != null) {
                    formattedOpeningHours.add(overnightPeriod);
                }
            }
        }

        return String.join(", ", formattedOpeningHours);
    }

    /**
     * Calculate opening hours overnight when open and close hours are split over two subsequent days
     *
     * @param todayOpeningHours today opening hour events list
     * @param tomorrowOpeningHours tomorrow opening hour events list
     * @return rendered opening hours overnight period for the day specified (today)
     */
    private String calculateOvernightPeriod(final List<OpeningHourEvent> todayOpeningHours,
                                            final List<OpeningHourEvent> tomorrowOpeningHours) {
        String overnightPeriod = null;

        List<OpeningHourEvent> todayOpenHours = tryGetOpeningHoursOfType(todayOpeningHours, OpeningHourEventType.OPEN);
        List<OpeningHourEvent> todayCloseHours = tryGetOpeningHoursOfType(todayOpeningHours, OpeningHourEventType.CLOSE);

        //if last open hour for the day is after last close hour, then last close hour could be in the next day
        LocalTime todayLastOpenHour = todayOpenHours.get(todayOpenHours.size() - 1).getValue();
        LocalTime todayLastCloseHour = todayCloseHours.isEmpty() ? null : todayCloseHours.get(todayCloseHours.size() - 1).getValue();
        if (todayLastCloseHour == null ||
                (todayLastOpenHour != null && todayLastOpenHour.isAfter(todayLastCloseHour))) {
            List<OpeningHourEvent> tomorrowOpenHours = tryGetOpeningHoursOfType(tomorrowOpeningHours, OpeningHourEventType.OPEN);
            List<OpeningHourEvent> tomorrowCloseHours = tryGetOpeningHoursOfType(tomorrowOpeningHours, OpeningHourEventType.CLOSE);
            LocalTime tomorrowFirstOpenHour = tomorrowOpenHours.isEmpty() ? null : tomorrowOpenHours.get(0).getValue();
            LocalTime tomorrowFirstCloseHour = tomorrowOpeningHours.isEmpty() ? null : tomorrowCloseHours.get(0).getValue();
            if (tomorrowFirstOpenHour == null ||
                    (tomorrowFirstCloseHour != null && tomorrowFirstCloseHour.isBefore(tomorrowFirstOpenHour))) {
                overnightPeriod = String.format("%s - %s", formatTime(todayLastOpenHour), formatTime(tomorrowFirstCloseHour));
            }
        } else {
            throw new IllegalArgumentException("Invalid period");
        }

        return overnightPeriod;
    }

    /**
     * Format time using date time format. If o'clock time, remove minutes and seconds
     *
     * @param time local time to be formatted
     * @return formatted time
     */
    private String formatTime(final LocalTime time) {
        if (time == null) {
            throw new IllegalArgumentException("time not informed");
        }

        return DATE_TIME_FORMATTER.format(time)
                .replaceAll(":00", "") // do not show minutes or seconds if o'clock time
                .toUpperCase();
    }

    /**
     * Filter opening hour events of specified type from given opening hour events list
     *
     * @param openingHourEvents opening hour events list
     * @param type opening hour event type
     * @return filtered opening hour events list
     */
    private List<OpeningHourEvent> tryGetOpeningHoursOfType(final List<OpeningHourEvent> openingHourEvents,
                                                            final OpeningHourEventType type) {
        return openingHourEvents.stream()
                .filter(Objects::nonNull)
                .filter(oh -> type.equals(oh.getType()))
                .sorted()
                .collect(Collectors.toList());
    }
}
