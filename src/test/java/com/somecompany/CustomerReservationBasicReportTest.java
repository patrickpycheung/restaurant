package com.somecompany;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.somecompany.model.CustomerReservationBasicReport;
import com.somecompany.model.CustomerReservationReport;
import com.somecompany.service.RestaurantService;
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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class CustomerReservationBasicReportTest {

    @Autowired
    private RestaurantService restaurantService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @ParameterizedTest
    @CsvFileSource(resources = "/GetCustomerReservationBasicReportThroughServiceTestData.csv", numLinesToSkip = 1)
    public void shouldBeAbleToGetCustomerReservationBasicReport(String startDateStr, String endDateStr,
                                                                String reportsStr) throws JsonParseException, JsonMappingException, IOException {

        if (startDateStr.equals("null")) {
            startDateStr = null;
        }

        if (endDateStr.equals("null")) {
            endDateStr = null;
        }

        // Actual result
        List<CustomerReservationReport> resultList = restaurantService
                .getCustomerReservationReport("CustomerReservationBasicReport", startDateStr, endDateStr);

        // Expected result
        List<CustomerReservationReport> expectedList = createExpectedCustomerReservationReportList(reportsStr);

        // Assertion
        assertEquals(expectedList, resultList);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/GetCustomerReservationBasicReportThroughAPITestData.csv", numLinesToSkip = 1)
    public void shouldBeAbleToGetCustomerReservationBasicReportThroughAPI(String reportName, String startDateStr,
                                                                          String endDateStr, String reportsStr) {

        // Actual result
        ResponseEntity<List<CustomerReservationBasicReport>> responseEntity;

        if (!startDateStr.equals("null") && !endDateStr.equals("null")) {
            // Both startDate and endDate criteria exist

            responseEntity = testRestTemplate.exchange(
                    "http://localhost:" + port + "/api/reservation/report?reportName=" + reportName + "&startDate="
                            + startDateStr + "&endDate=" + endDateStr,
                    HttpMethod.GET, null, new ParameterizedTypeReference<List<CustomerReservationBasicReport>>() {
                    });
        } else if (!(startDateStr.equals("null"))) {
            // Only startDate criteria exist

            responseEntity = testRestTemplate.exchange(
                    "http://localhost:" + port + "/api/reservation/report?reportName=" + reportName + "&startDate="
                            + startDateStr,
                    HttpMethod.GET, null, new ParameterizedTypeReference<List<CustomerReservationBasicReport>>() {
                    });
        } else if (!(endDateStr.equals("null"))) {
            // Only endDate criteria exist

            responseEntity = testRestTemplate.exchange(
                    "http://localhost:" + port + "/api/reservation/report?reportName=" + reportName + "&endDate="
                            + endDateStr,
                    HttpMethod.GET, null, new ParameterizedTypeReference<List<CustomerReservationBasicReport>>() {
                    });
        } else {
            // No scheduled date search criteria

            responseEntity = testRestTemplate.exchange(
                    "http://localhost:" + port + "/api/reservation/report?reportName=" + reportName, HttpMethod.GET,
                    null, new ParameterizedTypeReference<List<CustomerReservationBasicReport>>() {
                    });
        }

        List<CustomerReservationBasicReport> resultList = responseEntity.getBody();

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

                CustomerReservationBasicReport customerReservationBasicReport = new CustomerReservationBasicReport();
                customerReservationBasicReport.setName(reportFields[0]);
                customerReservationBasicReport.setNum_of_visit(Integer.valueOf(reportFields[1]));
                customerReservationBasicReport.setTotal_spend(new BigDecimal(reportFields[2]));

                expectedList.add(customerReservationBasicReport);
            }
        }

        return expectedList;
    }
}
