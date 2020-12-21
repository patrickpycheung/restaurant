package com.opentable.sampleapplication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class CustomerReservationReportTest {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate testRestTemplate;

	@Test
	public void shouldBeAbleToCatchValidationExceptionWhenGetCustomerReservationReportThroughAPICallWithQuestionMarkAppended() {
		ResponseEntity<Object> responseEntity = testRestTemplate.exchange(
				"http://localhost:" + port + "/api/reservation/report?", HttpMethod.GET, null,
				new ParameterizedTypeReference<Object>() {
				});

		// Assertion
		assertEquals("BAD_REQUEST", ((LinkedHashMap<String, String>) responseEntity.getBody()).get("status"));
		assertThat(((LinkedHashMap<String, String>) responseEntity.getBody()).get("message")
				.contains("Required String parameter 'reportName' is not present"));
	}

	@Test
	public void shouldBeAbleToCatchValidationExceptionWhenGetCustomerReservationReportThroughAPICallWhereStartDateIsIncomplete() {
		ResponseEntity<Object> responseEntity = testRestTemplate.exchange(
				"http://localhost:" + port + "/api/reservation/report?startDate", HttpMethod.GET, null,
				new ParameterizedTypeReference<Object>() {
				});

		// Assertion
		assertEquals("BAD_REQUEST", ((LinkedHashMap<String, String>) responseEntity.getBody()).get("status"));
		assertThat(((LinkedHashMap<String, String>) responseEntity.getBody()).get("message")
				.contains("Start date must have the pattern 'YYYY-MM-DD'"));
	}

	@Test
	public void shouldBeAbleToCatchValidationExceptionWhenGetCustomerReservationReportThroughAPICallWhereStartDateIsEmpty() {
		ResponseEntity<Object> responseEntity = testRestTemplate.exchange(
				"http://localhost:" + port + "/api/reservation/report?startDate=", HttpMethod.GET, null,
				new ParameterizedTypeReference<Object>() {
				});

		// Assertion
		assertEquals("BAD_REQUEST", ((LinkedHashMap<String, String>) responseEntity.getBody()).get("status"));
		assertThat(((LinkedHashMap<String, String>) responseEntity.getBody()).get("message")
				.contains("Start date must have the pattern 'YYYY-MM-DD'"));
	}

	@Test
	public void shouldBeAbleToCatchValidationExceptionWhenGetCustomerReservationReportThroughAPICallWhereEndDateIsIncomplete() {
		ResponseEntity<Object> responseEntity = testRestTemplate.exchange(
				"http://localhost:" + port + "/api/reservation/report?endDate", HttpMethod.GET, null,
				new ParameterizedTypeReference<Object>() {
				});

		// Assertion
		assertEquals("BAD_REQUEST", ((LinkedHashMap<String, String>) responseEntity.getBody()).get("status"));
		assertThat(((LinkedHashMap<String, String>) responseEntity.getBody()).get("message")
				.contains("End date must have the pattern 'YYYY-MM-DD'"));
	}

	@Test
	public void shouldBeAbleToCatchValidationExceptionWhenGetCustomerReservationReportThroughAPICallWhereEndDateIsEmpty() {
		ResponseEntity<Object> responseEntity = testRestTemplate.exchange(
				"http://localhost:" + port + "/api/reservation/report?endDate=", HttpMethod.GET, null,
				new ParameterizedTypeReference<Object>() {
				});

		// Assertion
		assertEquals("BAD_REQUEST", ((LinkedHashMap<String, String>) responseEntity.getBody()).get("status"));
		assertThat(((LinkedHashMap<String, String>) responseEntity.getBody()).get("message")
				.contains("End date must have the pattern 'YYYY-MM-DD'"));
	}

	@Test
	public void shouldBeAbleToCatchValidationExceptionWhenGetCustomerReservationReportThroughAPICallWhereStartDatePatternNotMatch() {
		ResponseEntity<Object> responseEntity = testRestTemplate.exchange(
				"http://localhost:" + port + "/api/reservation/report?startDate=20201106", HttpMethod.GET, null,
				new ParameterizedTypeReference<Object>() {
				});

		// Assertion
		assertEquals("BAD_REQUEST", ((LinkedHashMap<String, String>) responseEntity.getBody()).get("status"));
		assertThat(((LinkedHashMap<String, String>) responseEntity.getBody()).get("message")
				.contains("Start date must have the pattern 'YYYY-MM-DD'"));
	}

	@Test
	public void shouldBeAbleToCatchValidationExceptionWhenGetCustomerReservationReportThroughAPICallWhereEndDatePatternNotMatch() {
		ResponseEntity<Object> responseEntity = testRestTemplate.exchange(
				"http://localhost:" + port + "/api/reservation/report?endDate=20201106", HttpMethod.GET, null,
				new ParameterizedTypeReference<Object>() {
				});

		// Assertion
		assertEquals("BAD_REQUEST", ((LinkedHashMap<String, String>) responseEntity.getBody()).get("status"));
		assertThat(((LinkedHashMap<String, String>) responseEntity.getBody()).get("message")
				.contains("End date must have the pattern 'YYYY-MM-DD'"));
	}
}
