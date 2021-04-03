package com.somecompany.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.somecompany.constant.RestaurantConstant;
import com.somecompany.constant.RestaurantConstant.CustomerReservationBasicReportField;
import com.somecompany.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.Map.Entry;

/**
 * The logic unit which carries out the request operation.
 *
 * @author patrick
 */
@Service
public class RestaurantService {

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${dataSource}")
    private String dataSource;

    /**
     * Get all reservation records from the datasource.
     *
     * @return A list of all reservations from the datasource
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public List<Reservation> getAllReservations() throws JsonParseException, JsonMappingException, IOException {

        List<Reservation> reservationList = new ArrayList<>();

        InputStream inputStream = TypeReference.class.getResourceAsStream(dataSource);

        reservationList = objectMapper.readValue(inputStream, new TypeReference<List<Reservation>>() {
        });

        return reservationList;
    }

    /**
     * Get all reservation records from the datasource within a time period.
     *
     * @param startDateStr
     * @param endDateStr
     * @return A list of all reservations from the datasource within a time period
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public List<Reservation> getReservationsByScheduledDate(String startDateStr, String endDateStr)
            throws JsonParseException, JsonMappingException, IOException {
        // The full list of reservations
        List<Reservation> rawList = getAllReservations();

        // The list of reservations with matching scheduledDate
        List<Reservation> filteredList = new ArrayList<>();

        for (Reservation reservation : rawList) {
            // Get date from the DB
            String scheduledDateStr = reservation.getScheduled_date();
            LocalDate scheduledDate = LocalDate.parse(scheduledDateStr);

            LocalDate startDate;
            LocalDate endDate;

            if (startDateStr != null && endDateStr != null) {
                // Both startDate and endDate search criteria exist

                startDate = LocalDate.parse(startDateStr);
                endDate = LocalDate.parse(endDateStr);

                if (scheduledDate.compareTo(startDate) >= 0 && scheduledDate.compareTo(endDate) <= 0) {
                    // scheduledDate is in between startDate(inclusive) and endDate(inclusive))
                    filteredList.add(reservation);
                }
            } else if (startDateStr != null && endDateStr == null) {
                // Only startDate exists

                startDate = LocalDate.parse(startDateStr);

                if (scheduledDate.compareTo(startDate) >= 0) {
                    // scheduledDate is after startDate(inlcusive)
                    filteredList.add(reservation);
                }
            } else if (startDateStr == null && endDateStr != null) {
                // Only endDate exists

                endDate = LocalDate.parse(endDateStr);

                if (scheduledDate.compareTo(endDate) <= 0) {
                    // scheduledDate is before endDate(inlcusive)
                    filteredList.add(reservation);
                }
            } else {
                // No startDate and endDate
                // No need to check scheduledDate

                filteredList.add(reservation);
            }
        }

        return filteredList;
    }

    /**
     * Get a report showing each customer's total restaurant visits and the respective total spending within a time
     * period.
     *
     * @param startDateStr
     * @param endDateStr
     * @return A list of report of each customer's total restaurant visits and the respective total spending within a
     * time period
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public List<CustomerReservationReport> getCustomerReservationReport(String reportName, String startDateStr,
                                                                        String endDateStr) throws JsonParseException, JsonMappingException, IOException {
        // The list of reservations with matching scheduledDate
        List<Reservation> rawList = getReservationsByScheduledDate(startDateStr, endDateStr);

        // A map storing unique customers as the key, with the value as another map (detailsMap) holding their
        // reservation statistics
        Map<String, Map<String, Object>> custStatisticsMap = getCustomerStatistics(rawList);

        // The final list
        List<CustomerReservationReport> reportList = new ArrayList<>();

        // CustomerReservationBasicReport
        if (reportName.toLowerCase().equals(RestaurantConstant.CUSTOMER_RESERVATION_BASIC_REPORT.toLowerCase())) {
            configureCustomerReservationBasicReport(reportList, custStatisticsMap);
        }

        // CustomerReservationAdvancedReport
        if (reportName.toLowerCase().equals(RestaurantConstant.CUSTOMER_RESERVATION_ADVANCED_REPORT.toLowerCase())) {
            configureCustomerReservationAdvancedReport(reportList, custStatisticsMap);
        }

        // CustomerReservationVIPAdvancedReport
        if (reportName.toLowerCase()
                .equals(RestaurantConstant.CUSTOMER_RESERVATION_VIP_ADVANCED_REPORT.toLowerCase())) {
            configureCustomerReservationVipAdvancedReport(reportList, custStatisticsMap);
        }

        // Sort the list in ascending order
        Collections.sort(reportList);

        return reportList;
    }

    /**
     * Generate a map storing customer reservation statistics.
     *
     * @param rawList
     * @return A map storing unique customers as the key, with the value as another map (detailsMap) holding their
     * reservation statistics
     */
    private Map<String, Map<String, Object>> getCustomerStatistics(List<Reservation> rawList) {

        // A map storing unique customers as the key, with the value as another map (detailsMap) holding their
        // reservation statistics
        // e.g.
        // {Felix Fish" | {[num_of_visit|10],[total_spend|999.99]}}
        Map<String, Map<String, Object>> custStatisticsMap = new HashMap<>();

        for (Reservation reservation : rawList) {
            if (custStatisticsMap.containsKey(reservation.getGuest().getName())) {
                // Already have this customer entry

                Map<String, Object> detailsMap = custStatisticsMap.get(reservation.getGuest().getName());
                // Update number of visit
                detailsMap.put(CustomerReservationBasicReportField.NUM_OF_VISIT.getField(),
                        (Integer) detailsMap.get(CustomerReservationBasicReportField.NUM_OF_VISIT.getField()) + 1);
                // Update total spend
                detailsMap.put("total_spend",
                        ((BigDecimal) detailsMap.get(CustomerReservationBasicReportField.TOTAL_SPEND.getField()))
                                .add(reservation.getTotal_spend()));
            } else {
                // Not yet have this customer entry

                Map<String, Object> detailsMap = new HashMap<>();
                // Update number of visit for the first time
                detailsMap.put(CustomerReservationBasicReportField.NUM_OF_VISIT.getField(), 1);
                // Update total spend for the first time
                detailsMap.put(CustomerReservationBasicReportField.TOTAL_SPEND.getField(),
                        reservation.getTotal_spend());

                custStatisticsMap.put(reservation.getGuest().getName(), detailsMap);
            }
        }

        return custStatisticsMap;
    }

    /**
     * Configure the report for CustomerReservationBasicReport type.
     *
     * @param reportList
     * @param custStatisticsMap
     */
    private void configureCustomerReservationBasicReport(List<CustomerReservationReport> reportList,
                                                         Map<String, Map<String, Object>> custStatisticsMap) {

        for (Entry<String, Map<String, Object>> entry : custStatisticsMap.entrySet()) {

            CustomerReservationBasicReport report = new CustomerReservationBasicReport();

            // Basic fields
            addCustomerReservationBasicReportFields(report, entry);

            reportList.add(report);
        }
    }

    /**
     * Configure the report for configureCustomerReservationAdvancedReport type.
     *
     * @param reportList
     * @param custStatisticsMap
     */
    private void configureCustomerReservationAdvancedReport(List<CustomerReservationReport> reportList,
                                                            Map<String, Map<String, Object>> custStatisticsMap) {

        for (Entry<String, Map<String, Object>> entry : custStatisticsMap.entrySet()) {
            CustomerReservationAdvancedReport report = new CustomerReservationAdvancedReport();

            // Basic fields
            addCustomerReservationBasicReportFields(report, entry);

            // CustomerReservationAdvancedReport fields
            addCustomerReservationAdvancedReportFields(report, entry);

            reportList.add(report);
        }
    }

    /**
     * Configure the report for configureCustomerReservationVipAdvancedReport type.
     *
     * @param reportList
     * @param custStatisticsMap
     */
    private void configureCustomerReservationVipAdvancedReport(List<CustomerReservationReport> reportList,
                                                               Map<String, Map<String, Object>> custStatisticsMap) {

        for (Entry<String, Map<String, Object>> entry : custStatisticsMap.entrySet()) {
            CustomerReservationVipAdvancedReport report = new CustomerReservationVipAdvancedReport();

            // Basic fields
            addCustomerReservationBasicReportFields(report, entry);

            // CustomerReservationAdvancedReport fields
            addCustomerReservationAdvancedReportFields(report, entry);

            // CustomerReservationVipAdvancedReport fields
            addCustomerReservationVipAdvancedReportFields(report, entry);

            reportList.add(report);
        }
    }

    /**
     * Add CustomerReservationBasicReport fields to the report.
     *
     * @param report
     * @param entry
     */
    private void addCustomerReservationBasicReportFields(CustomerReservationReport report,
                                                         Entry<String, Map<String, Object>> entry) {
        report.setName(entry.getKey());
        report.setNum_of_visit(
                (Integer) entry.getValue().get(CustomerReservationBasicReportField.NUM_OF_VISIT.getField()));
        report.setTotal_spend(
                (BigDecimal) (entry.getValue().get(CustomerReservationBasicReportField.TOTAL_SPEND.getField())));
    }

    /**
     * Add CustomerReservationAdvancedReport fields to the report.
     *
     * @param report
     * @param entry
     */
    private void addCustomerReservationAdvancedReportFields(CustomerReservationAdvancedReport report,
                                                            Entry<String, Map<String, Object>> entry) {
        report.setMax_party_size(100);
    }

    /**
     * Add CustomerReservationVipAdvancedReport fields to the report.
     *
     * @param report
     * @param entry
     */
    private void addCustomerReservationVipAdvancedReportFields(CustomerReservationVipAdvancedReport report,
                                                               Entry<String, Map<String, Object>> entry) {
        report.setVip_credit(200);
    }
}
