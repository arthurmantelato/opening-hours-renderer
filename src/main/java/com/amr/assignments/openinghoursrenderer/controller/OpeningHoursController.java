package com.amr.assignments.openinghoursrenderer.controller;

import com.amr.assignments.openinghoursrenderer.domain.DayOfWeek;
import com.amr.assignments.openinghoursrenderer.domain.OpeningHourEvent;
import com.amr.assignments.openinghoursrenderer.service.OpeningHoursParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/opening-hours")
public class OpeningHoursController {

    private OpeningHoursParser openingHoursParser;

    @Autowired
    public OpeningHoursController(OpeningHoursParser openingHoursParser) {
        this.openingHoursParser = openingHoursParser;
    }

    /**
     * Get human readable representation of a weekly opening hours schedule of a restaurant
     *
     * @param weeklyOpeningHours map that key is day of the week and value is a list of event times (open and/or close
     *                           hours) for that day
     * @return rendered opening hours
     */
    @PostMapping
    public ResponseEntity<String> renderOpeningHours(
            @RequestBody final Map<DayOfWeek, List<OpeningHourEvent>> weeklyOpeningHours) {
        final String response = openingHoursParser.parse(weeklyOpeningHours);
        System.out.println(response);
        return ResponseEntity.ok(response);
    }

    /**
     * Global exception handler to reply with more friendly error message when an exception occurs
     *
     * @param ex Exception to be handled
     * @return error message with InternalServerError status code (500)
     */
    @ExceptionHandler({HttpMessageNotReadableException.class})
    public ResponseEntity handleJsonMappingException(final Exception ex) {
        log.error("Invalid data received", ex);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Wait, we've got something weird here. Invalid data received!");
    }

    /**
     * Global exception handler to reply with more friendly error message when an exception occurs
     *
     * @param ex Exception to be handled
     * @return error message with InternalServerError status code (500)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity handleRuntimeException(final RuntimeException ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Oops! Something went wrong!");
    }


}
