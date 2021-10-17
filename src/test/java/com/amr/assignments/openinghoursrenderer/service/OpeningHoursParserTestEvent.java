package com.amr.assignments.openinghoursrenderer.service;

import com.amr.assignments.openinghoursrenderer.domain.DayOfWeek;
import com.amr.assignments.openinghoursrenderer.domain.OpeningHourEvent;
import com.amr.assignments.openinghoursrenderer.domain.OpeningHourEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.amr.assignments.openinghoursrenderer.domain.OpeningHourEventType.CLOSE;
import static com.amr.assignments.openinghoursrenderer.domain.OpeningHourEventType.OPEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class OpeningHoursParserTestEvent {

    private OpeningHoursParser parser;

    @BeforeEach
    public void setup() {
        parser = new OpeningHoursParser();
    }

    @Test
    void givenNormalOpenAndCloseHoursForADay_whenParse_thenPeriodWithMinutesNotShown() {
        Map<DayOfWeek, List<OpeningHourEvent>> weeklyOpeningHours = Map.of(
                DayOfWeek.MONDAY,
                List.of(openingHourOf(OPEN,36000L), // 10:00
                        openingHourOf(CLOSE,64800L) // 18:00
                )
        );

        final String actual = parser.parse(weeklyOpeningHours);

        final String expected = "Monday: 10 AM - 6 PM";

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenNotOClockOpenAndCloseHoursForADay_whenParse_thenPeriodWithMinutesShown() {
        Map<DayOfWeek, List<OpeningHourEvent>> weeklyOpeningHours = Map.of(
                DayOfWeek.MONDAY,
                List.of(openingHourOf(OPEN,37800L), // 10:30
                        openingHourOf(CLOSE,66600L)  // 18:30
                )
        );

        final String actual = parser.parse(weeklyOpeningHours);

        final String expected = "Monday: 10:30 AM - 6:30 PM";

        assertThat(actual).isEqualTo(expected);
    }



    @Test
    void givenNeitherOpenNorCloseHours_whenParse_thenClosed() {
        Map<DayOfWeek, List<OpeningHourEvent>> weeklyOpeningHours = Map.of(DayOfWeek.MONDAY, Collections.emptyList());

        final String actual = parser.parse(weeklyOpeningHours);

        final String expected = "Monday: Closed";

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenCloseHourAtEarlyMorningOfNextDayButStillSpecifiedOnTheSameDay_whenParse_thenCloseHourAmAndOpenHourPm() {
        Map<DayOfWeek, List<OpeningHourEvent>> weeklyOpeningHours = Map.of(
                DayOfWeek.MONDAY,
                List.of(openingHourOf(OPEN,36000L), // 10:00
                        openingHourOf(CLOSE,3600L) // 01:00
                )
        );

        final String actual = parser.parse(weeklyOpeningHours);

        final String expected = "Monday: 10 AM - 1 AM";

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenMoreThanOneDayOpeningHours_whenParse_thenListOfDays() {
        Map<DayOfWeek, List<OpeningHourEvent>> weeklyOpeningHours = Map.of(
                DayOfWeek.MONDAY,
                List.of(openingHourOf(OPEN,36000L), // 10:00
                        openingHourOf(CLOSE,64800L) // 18:00
                ),

                DayOfWeek.TUESDAY,
                List.of(openingHourOf(OPEN,36000L), // 10:00
                        openingHourOf(CLOSE,64800L) // 18:00
                )
        );

        final String actual = parser.parse(weeklyOpeningHours);

        final String expected = "Monday: 10 AM - 6 PM" + System.lineSeparator() +
                                "Tuesday: 10 AM - 6 PM";

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenMoreThanOneDayOpeningHoursOutOfOrder_whenParse_thenListOfDaysOrderedByDayOfWeek() {
        Map<DayOfWeek, List<OpeningHourEvent>> weeklyOpeningHours = Map.of(
                DayOfWeek.FRIDAY,
                List.of(openingHourOf(OPEN,36000L), // 10:00
                        openingHourOf(CLOSE,64800L) // 18:00
                ),

                DayOfWeek.MONDAY,
                List.of(openingHourOf(OPEN,36000L), // 10:00
                        openingHourOf(CLOSE,64800L) // 18:00
                )
        );

        final String actual = parser.parse(weeklyOpeningHours);

        final String expected = "Monday: 10 AM - 6 PM" + System.lineSeparator() +
                                "Friday: 10 AM - 6 PM";

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenAnyNullOpeningHourValue_whenParse_thenIllegalArgumentException() {
        Map<DayOfWeek, List<OpeningHourEvent>> weeklyOpeningHours = Map.of(
                DayOfWeek.FRIDAY,
                List.of(new OpeningHourEvent(OPEN, null),
                        new OpeningHourEvent(CLOSE, null)
                )
        );

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> parser.parse(weeklyOpeningHours));
    }

    @Test
    void givenAnyNullOpeningHours_whenParse_thenEmptyString() {
        Map<DayOfWeek, List<OpeningHourEvent>> weeklyOpeningHours = new HashMap<>();
        weeklyOpeningHours.put(DayOfWeek.FRIDAY, null);

        final String actual = parser.parse(weeklyOpeningHours);

        assertThat(actual).isEmpty();
    }

    @Test
    void givenMinAndMaxTimeValues_whenParse_thenMaximumPeriodPossibleForSingleDay() {
        Map<DayOfWeek, List<OpeningHourEvent>> weeklyOpeningHours = Map.of(
                DayOfWeek.FRIDAY,
                List.of(openingHourOf(OPEN,0L), // 00:00
                        openingHourOf(CLOSE,86399L) // 23:59:59
                )
        );

        final String actual = parser.parse(weeklyOpeningHours);

        final String expected = "Friday: 12 AM - 11:59:59 PM";

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenMoreThanOnePeriodWithinADay_whenParse_thenCommaSeparatedListOfPeriods() {
        Map<DayOfWeek, List<OpeningHourEvent>> weeklyOpeningHours = Map.of(
                DayOfWeek.FRIDAY,
                List.of(openingHourOf(OPEN,0L), // 00:00
                        openingHourOf(CLOSE,3600L), // 01:00
                        openingHourOf(OPEN,7200L), // 02:00
                        openingHourOf(CLOSE,10800L) // 03:00
                )
        );

        final String actual = parser.parse(weeklyOpeningHours);

        final String expected = "Friday: 12 AM - 1 AM, 2 AM - 3 AM";

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenMoreThanOnePeriodWithinADayOutOfOrder_whenParse_thenCommaSeparatedOrderedListOfPeriods() {
        Map<DayOfWeek, List<OpeningHourEvent>> weeklyOpeningHours = Map.of(
                DayOfWeek.FRIDAY,
                List.of(openingHourOf(CLOSE,10800L), // 03:00
                        openingHourOf(CLOSE,3600L), // 01:00
                        openingHourOf(OPEN,7200L), // 02:00
                        openingHourOf(OPEN,0L) // 00:00
                )
        );

        final String actual = parser.parse(weeklyOpeningHours);

        final String expected = "Friday: 12 AM - 1 AM, 2 AM - 3 AM";

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenLessCloseHoursThanOpenHoursAndLastOpenHourIsBeforeLastCloseHour_whenParse_thenParseException() {
        Map<DayOfWeek, List<OpeningHourEvent>> weeklyOpeningHours = Map.of(
                DayOfWeek.THURSDAY,
                List.of(openingHourOf(OPEN,0L), // 00:00
                        openingHourOf(OPEN, 3600L), // 01:00
                        openingHourOf(CLOSE, 86399L) // 23:59:59
                )
        );

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> parser.parse(weeklyOpeningHours));
    }

    @Test
    void givenOpenHourInOneDayAndCloseHourInTheNextDay_whenParse_thenCloseHourMovedToFirstDay() {
        Map<DayOfWeek, List<OpeningHourEvent>> weeklyOpeningHours = Map.of(
                DayOfWeek.FRIDAY,
                List.of(openingHourOf(OPEN,72000L)), // 20:00

                DayOfWeek.SATURDAY,
                List.of(openingHourOf(CLOSE,3600L)) // 01:00
        );

        final String actual = parser.parse(weeklyOpeningHours);

        final String expected = "Friday: 8 PM - 1 AM";

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenOpenHourInOneDayAndCloseHourInTheNextDayWithOtherPeriodInTheFirstDay_whenParse_thenCloseHourMovedToFirstDay() {
        Map<DayOfWeek, List<OpeningHourEvent>> weeklyOpeningHours = Map.of(
                DayOfWeek.FRIDAY,
                List.of(openingHourOf(OPEN, 32400L), // 09:00
                        openingHourOf(CLOSE, 43200L), // 12:00
                        openingHourOf(OPEN,72000L) // 20:00
                ),

                DayOfWeek.SATURDAY,
                List.of(openingHourOf(CLOSE,3600L)) // 01:00
        );

        final String actual = parser.parse(weeklyOpeningHours);

        final String expected = "Friday: 9 AM - 12 PM, 8 PM - 1 AM";

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void givenOpenHourInOneDayAndCloseHourInTheNextDayWithOtherPeriodInTheSecondDay_whenParse_thenCloseHourMovedToFirstDay() {
        Map<DayOfWeek, List<OpeningHourEvent>> weeklyOpeningHours = Map.of(
                DayOfWeek.FRIDAY,
                List.of(openingHourOf(OPEN,72000L)), // 20:00

                DayOfWeek.SATURDAY,
                List.of(openingHourOf(CLOSE,3600L), // 01:00
                        openingHourOf(OPEN, 32400L), // 09:00
                        openingHourOf(CLOSE, 43200L) // 12:00
                )
        );

        final String actual = parser.parse(weeklyOpeningHours);

        final String expected = "Friday: 8 PM - 1 AM" + System.lineSeparator() +
                                "Saturday: 9 AM - 12 PM";

        assertThat(actual).isEqualTo(expected);
    }

    private OpeningHourEvent openingHourOf(final OpeningHourEventType type, final Long secondOfDay) {
        return new OpeningHourEvent(type, LocalTime.ofSecondOfDay(secondOfDay));
    }

}
