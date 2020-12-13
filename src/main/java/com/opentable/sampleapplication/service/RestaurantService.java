package com.opentable.sampleapplication.service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opentable.sampleapplication.model.CustomerReservationReport;
import com.opentable.sampleapplication.model.Reservation;

/**
 * The logic unit which carries out the request operation.
 * 
 * @author patrick
 *
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
	 *         time period
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public List<CustomerReservationReport> getCustomerReservationReport(String startDateStr, String endDateStr)
			throws JsonParseException, JsonMappingException, IOException {
		// The list of reservations with matching scheduledDate
		List<Reservation> rawList = getReservationsByScheduledDate(startDateStr, endDateStr);

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
				detailsMap.put("num_of_visit", (Integer) detailsMap.get("num_of_visit") + 1);
				// Update total spend
				detailsMap.put("total_spend",
						((BigDecimal) detailsMap.get("total_spend")).add(reservation.getTotal_spend()));
			} else {
				// Not yet have this customer entry

				Map<String, Object> detailsMap = new HashMap<>();
				// Update number of visit for the first time
				detailsMap.put("num_of_visit", 1);
				// Update total spend for the first time
				detailsMap.put("total_spend", reservation.getTotal_spend());

				custStatisticsMap.put(reservation.getGuest().getName(), detailsMap);
			}
		}

		// Create the final list
		List<CustomerReservationReport> customerReservationReportList = new ArrayList<>();

		for (Entry<String, Map<String, Object>> entry : custStatisticsMap.entrySet()) {
			CustomerReservationReport customerReservationReport = new CustomerReservationReport();
			customerReservationReport.setName(entry.getKey());
			customerReservationReport.setNum_of_visit((Integer) entry.getValue().get("num_of_visit"));
			customerReservationReport.setTotal_spend((BigDecimal) entry.getValue().get("total_spend"));
			customerReservationReportList.add(customerReservationReport);
		}

		// Sort the list in ascending order
		Collections.sort(customerReservationReportList);

		return customerReservationReportList;
	}
}
