package com.amr.assignments.openinghoursrenderer.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpeningHoursControllerTestEvent {

    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(), StandardCharsets.UTF_8);


    private MockMvc mockMvc;

    @Autowired
    OpeningHoursControllerTestEvent(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void shouldReturnCompleteResponse() throws Exception {
        final String requestJson = "{\"MONDAY\":[]," +
                "\"TUESDAY\":[{\"type\":\"OPEN\",\"value\":36000},{\"type\":\"CLOSE\",\"value\":64800}]," +
                "\"WEDNESDAY\":[]," +
                "\"THURSDAY\":[{\"type\":\"OPEN\",\"value\":37800},{\"type\":\"CLOSE\",\"value\":64800}]," +
                "\"FRIDAY\":[{\"type\":\"OPEN\",\"value\":36000}]," +
                "\"SATURDAY\":[{\"type\":\"CLOSE\",\"value\":3600},{\"type\":\"OPEN\",\"value\":36000}]," +
                "\"SUNDAY\":[{\"type\":\"CLOSE\",\"value\":3600},{\"type\":\"OPEN\",\"value\":43200},{\"type\":\"CLOSE\",\"value\":75600}]}";

        final String expectedResponse = "Monday: Closed" + System.lineSeparator() +
                                "Tuesday: 10 AM - 6 PM" + System.lineSeparator() +
                                "Wednesday: Closed" + System.lineSeparator() +
                                "Thursday: 10:30 AM - 6 PM" + System.lineSeparator() +
                                "Friday: 10 AM - 1 AM" + System.lineSeparator() +
                                "Saturday: 10 AM - 1 AM" + System.lineSeparator() +
                                "Sunday: 12 PM - 9 PM";

        assertOkAndResponse(requestJson, expectedResponse);
    }

    @Test
    void shouldBeCaseInsensitiveForEnumTypes() throws Exception {
        final String requestJson = "{\"MONDAY\":[]," +
                "\"Saturday\":[{\"type\":\"cLOSE\",\"value\":3600},{\"type\":\"OPEN\",\"value\":36000}]," +
                "\"sunday\":[{\"type\":\"close\",\"value\":3600},{\"type\":\"Open\",\"value\":43200},{\"type\":\"ClOsE\",\"value\":75600}]}";

        final String expectedResponse = "Saturday: 10 AM - 1 AM" + System.lineSeparator() +
                                "Sunday: 12 PM - 9 PM";

        assertOkAndResponse(requestJson, expectedResponse);
    }

    @Test
    void shouldNotAcceptInvalidDayOfWeek() throws Exception {
        final String requestJson = "{\"bla\":[]}";

        assertBadRequest(requestJson);
    }

    @Test
    void shouldNotAcceptInvalidOpeningHourType() throws Exception {
        final String requestJson = "{\"MONDAY\":[{\"type\":\"bla\",\"value\":3600}]}";

        assertBadRequest(requestJson);
    }

    @Test
    void shouldHandleNullOpeningHoursList() throws Exception {
        final String requestJson = "{\"MONDAY\": null}";

        String expectedResponse = "";

        assertOkAndResponse(requestJson, expectedResponse);
    }

    @Test
    void shouldHandleEmptyOpeningHours() throws Exception {
        final String requestJson = "{}";

        String expectedResponse = "";

        assertOkAndResponse(requestJson, expectedResponse);
    }


    @Test
    void shouldNotAcceptOutOfRangeSecondsOfDay() throws Exception {
        final String requestJson = "{\"MONDAY\":[{\"type\":\"open\",\"value\":-1}],{\"type\":\"close\",\"value\":999999999}]}";

        assertBadRequest(requestJson);
    }


    private void assertOkAndResponse(final String requestJson, final String expectedResponse) throws Exception {
        this.mockMvc.perform(post("/opening-hours").contentType(APPLICATION_JSON_UTF8).content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(expectedResponse)));
    }

    private void assertBadRequest(final String requestJson) throws Exception {
        this.mockMvc.perform(post("/opening-hours").contentType(APPLICATION_JSON_UTF8).content(requestJson))
                .andExpect(status().isBadRequest());
    }


    private void assertInternalServerError(String requestJson) throws Exception {
        this.mockMvc.perform(post("/opening-hours").contentType(APPLICATION_JSON_UTF8).content(requestJson))
                .andExpect(status().isInternalServerError());
    }

}