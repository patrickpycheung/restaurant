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
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.opentable.sampleapplication.model.Guest;
import com.opentable.sampleapplication.model.Reservation;
import com.opentable.sampleapplication.service.RestaurantService;

@SpringBootTest
@ActiveProfiles("dev")
public class ReservationTest {

	@Autowired
	private RestaurantService restaurantService;

	@ParameterizedTest
	@CsvFileSource(resources = "/GetAllReservationsTestData.csv", numLinesToSkip = 1)
	public void shouldBeAbleToGetAllReservations(String reservationsStr)
			throws JsonParseException, JsonMappingException, IOException {
		// Actual result
		List<Reservation> resultList = restaurantService.getAllReservations();

		// Expected result
		List<Reservation> expectedList = createExpectedReservationList(reservationsStr);

		// Assertion
		assertEquals(expectedList, resultList);
	}

	@ParameterizedTest
	@CsvFileSource(resources = "/GetReservationsByScheduledDateTestData.csv", numLinesToSkip = 1)
	public void shouldBeAbleToGetReservationsByScheduledDate(String startDateStr, String endDateStr,
			String reservationsStr) throws JsonParseException, JsonMappingException, IOException {

		if (startDateStr.equals("null")) {
			startDateStr = null;
		}

		if (endDateStr.equals("null")) {
			endDateStr = null;
		}

		// Actual result
		List<Reservation> resultList = restaurantService.getReservationsByScheduledDate(startDateStr, endDateStr);

		// Expected result
		List<Reservation> expectedList = createExpectedReservationList(reservationsStr);

		// Assertion
		assertEquals(expectedList, resultList);
	}

	private List<Reservation> createExpectedReservationList(String reservationsStr) {
		String[] reservationList = reservationsStr.split("\\|");

		List<Reservation> expectedList = new ArrayList<>();

		if (reservationList.length != 0) {
			// Not expecting empty list

			for (String reservationStr : reservationList) {
				String[] reservationFields = reservationStr.split("~");

				Guest guest = new Guest();
				guest.setId(reservationFields[4]);
				guest.setName(reservationFields[5]);

				Reservation reservation = new Reservation();
				reservation.setReservation_id(reservationFields[0]);
				reservation.setParty_size(Integer.valueOf(reservationFields[1]));
				reservation.setScheduled_date(reservationFields[2]);
				reservation.setTotal_spend(new BigDecimal(reservationFields[3]));
				reservation.setGuest(guest);

				expectedList.add(reservation);
			}
		}

		return expectedList;
	}
}
