package com.opentable.sampleapplication;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
import com.opentable.sampleapplication.model.CustomerReservationVipAdvancedReport;
import com.opentable.sampleapplication.service.RestaurantService;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class CustomerReservationVipAdvancedReportTest {

	@Autowired
	private RestaurantService restaurantService;

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate testRestTemplate;

	@ParameterizedTest
	@CsvFileSource(resources = "/GetCustomerReservationVipAdvancedReportThroughServiceTestData.csv", numLinesToSkip = 1)
	public void shouldBeAbleToGetCustomerReservationVipAdvancedReport(String startDateStr, String endDateStr,
			String reportsStr) throws JsonParseException, JsonMappingException, IOException {

		if (startDateStr.equals("null")) {
			startDateStr = null;
		}

		if (endDateStr.equals("null")) {
			endDateStr = null;
		}

		// Actual result
		List<CustomerReservationReport> resultList = restaurantService
				.getCustomerReservationReport("CustomerReservationVipAdvancedReport", startDateStr, endDateStr);

		// Expected result
		List<CustomerReservationReport> expectedList = createExpectedCustomerReservationVipAdvancedReportList(
				reportsStr);

		// Assertion
		assertEquals(expectedList, resultList);
	}

	@ParameterizedTest
	@CsvFileSource(resources = "/GetCustomerReservationVipAdvancedReportThroughAPITestData.csv", numLinesToSkip = 1)
	public void shouldBeAbleToGetCustomerReservationVipAdvancedReportThroughAPI(String reportName, String startDateStr,
			String endDateStr, String reportsStr) {

		// Actual result
		ResponseEntity<List<CustomerReservationVipAdvancedReport>> responseEntity;

		if (!startDateStr.equals("null") && !endDateStr.equals("null")) {
			// Both startDate and endDate criteria exist

			responseEntity = testRestTemplate.exchange(
					"http://localhost:" + port + "/api/reservation/report?reportName=" + reportName + "&startDate="
							+ startDateStr + "&endDate=" + endDateStr,
					HttpMethod.GET, null, new ParameterizedTypeReference<List<CustomerReservationVipAdvancedReport>>() {
					});
		} else if (!(startDateStr.equals("null"))) {
			// Only startDate criteria exist

			responseEntity = testRestTemplate.exchange(
					"http://localhost:" + port + "/api/reservation/report?reportName=" + reportName + "&startDate="
							+ startDateStr,
					HttpMethod.GET, null, new ParameterizedTypeReference<List<CustomerReservationVipAdvancedReport>>() {
					});
		} else if (!(endDateStr.equals("null"))) {
			// Only endDate criteria exist

			responseEntity = testRestTemplate.exchange(
					"http://localhost:" + port + "/api/reservation/report?reportName=" + reportName + "&endDate="
							+ endDateStr,
					HttpMethod.GET, null, new ParameterizedTypeReference<List<CustomerReservationVipAdvancedReport>>() {
					});
		} else {
			// No scheduled date search criteria

			responseEntity = testRestTemplate.exchange(
					"http://localhost:" + port + "/api/reservation/report?reportName=" + reportName, HttpMethod.GET,
					null, new ParameterizedTypeReference<List<CustomerReservationVipAdvancedReport>>() {
					});
		}

		List<CustomerReservationVipAdvancedReport> resultList = responseEntity.getBody();

		// Expected result
		List<CustomerReservationReport> expectedList = createExpectedCustomerReservationVipAdvancedReportList(
				reportsStr);

		// Assertion
		assertEquals(expectedList, resultList);
	}

	private List<CustomerReservationReport> createExpectedCustomerReservationVipAdvancedReportList(String reportsStr) {
		String[] reportList = reportsStr.split("\\|");

		List<CustomerReservationReport> expectedList = new ArrayList<>();

		if (reportList.length != 0) {
			// Not expecting empty list

			for (String reportStr : reportList) {
				String[] reportFields = reportStr.split("~");

				CustomerReservationVipAdvancedReport customerReservationVipAdvancedReport = new CustomerReservationVipAdvancedReport();

				customerReservationVipAdvancedReport.setName(reportFields[0]);
				customerReservationVipAdvancedReport.setNum_of_visit(Integer.parseInt(reportFields[1]));
				customerReservationVipAdvancedReport.setTotal_spend(new BigDecimal(reportFields[2]));
				customerReservationVipAdvancedReport.setMax_party_size(Integer.parseInt(reportFields[3]));
				customerReservationVipAdvancedReport.setVip_credit(Integer.parseInt(reportFields[4]));

				expectedList.add(customerReservationVipAdvancedReport);
			}
		}

		return expectedList;
	}
}
