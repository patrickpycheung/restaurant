package com.opentable.sampleapplication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.opentable.sampleapplication.model.CustomerReservationReport;
import com.opentable.sampleapplication.service.RestaurantService;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class CustomerReservationReportTest {

	@Autowired
	private RestaurantService restaurantService;

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate testRestTemplate;

	@ParameterizedTest
	@CsvFileSource(resources = "/GetCustomerReservationReportTestData.csv", numLinesToSkip = 1)
	public void shouldBeAbleToGetCustomerReservationReport(String startDateStr, String endDateStr, String reportsStr)
			throws JsonParseException, JsonMappingException, IOException {

		if (startDateStr.equals("null")) {
			startDateStr = null;
		}

		if (endDateStr.equals("null")) {
			endDateStr = null;
		}

		// Actual result
		List<CustomerReservationReport> resultList = restaurantService.getCustomerReservationReport(startDateStr,
				endDateStr);

		// Expected result
		List<CustomerReservationReport> expectedList = createExpectedCustomerReservationReportList(reportsStr);

		// Assertion
		assertEquals(expectedList, resultList);
	}

	@ParameterizedTest
	@CsvFileSource(resources = "/GetCustomerReservationReportAPITestData.csv", numLinesToSkip = 1)
	public void shouldBeAbleToGetCustomerReservationReportThroughAPICall(String startDateStr, String endDateStr,
			String reportsStr) {

		// Actual result
		ResponseEntity<List<CustomerReservationReport>> responseEntity;

		if (!startDateStr.equals("null") && !endDateStr.equals("null")) {
			// Both startDate and endDate criteria exist

			responseEntity = testRestTemplate.exchange(
					"http://localhost:" + port + "/api/reservation/report?startDate=" + startDateStr + "&endDate="
							+ endDateStr,
					HttpMethod.GET, null, new ParameterizedTypeReference<List<CustomerReservationReport>>() {
					});
		} else if (!(startDateStr.equals("null"))) {
			// Only startDate criteria exist

			responseEntity = testRestTemplate.exchange(
					"http://localhost:" + port + "/api/reservation/report?startDate=" + startDateStr, HttpMethod.GET,
					null, new ParameterizedTypeReference<List<CustomerReservationReport>>() {
					});
		} else if (!(endDateStr.equals("null"))) {
			// Only endDate criteria exist

			responseEntity = testRestTemplate.exchange(
					"http://localhost:" + port + "/api/reservation/report?endDate=" + endDateStr, HttpMethod.GET, null,
					new ParameterizedTypeReference<List<CustomerReservationReport>>() {
					});
		} else {
			// No scheduled date search criteria

			responseEntity = testRestTemplate.exchange("http://localhost:" + port + "/api/reservation/report",
					HttpMethod.GET, null, new ParameterizedTypeReference<List<CustomerReservationReport>>() {
					});
		}

		List<CustomerReservationReport> resultList = responseEntity.getBody();

		// Expected result
		List<CustomerReservationReport> expectedList = createExpectedCustomerReservationReportList(reportsStr);

		// Assertion
		assertEquals(expectedList, resultList);
	}

	private List<CustomerReservationReport> createExpectedCustomerReservationReportList(String reportsStr) {
		String[] reportList = reportsStr.split("\\|");

		List<CustomerReservationReport> expectedList = new ArrayList<>();

		if (reportList.length != 0) {
			// Not expecting empty list

			for (String reportStr : reportList) {
				String[] reportFields = reportStr.split("~");

				CustomerReservationReport customerReservationReport = new CustomerReservationReport();
				customerReservationReport.setName(reportFields[0]);
				customerReservationReport.setNum_of_visit(Integer.valueOf(reportFields[1]));
				customerReservationReport.setTotal_spend(new BigDecimal(reportFields[2]));

				expectedList.add(customerReservationReport);
			}
		}

		return expectedList;
	}

	@Test
	public void shouldBeAbleToGetCustomerReservationReportThroughAPICallWithQuestionMarkAppended() {
		ResponseEntity<List<CustomerReservationReport>> responseEntity = testRestTemplate.exchange(
				"http://localhost:" + port + "/api/reservation/report?", HttpMethod.GET, null,
				new ParameterizedTypeReference<List<CustomerReservationReport>>() {
				});

		// Assertion
		assertTrue(responseEntity.getBody().get(0) instanceof CustomerReservationReport);
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
