package com.opentable.sampleapplication.model;

import java.math.BigDecimal;

import lombok.Data;

/**
 * Model class for a reservation.
 * 
 * @author patrick
 *
 */
@Data
public class Reservation {

	private String reservation_id;

	private int party_size;

	private String scheduled_date;

	private BigDecimal total_spend;

	private Guest guest;
}
