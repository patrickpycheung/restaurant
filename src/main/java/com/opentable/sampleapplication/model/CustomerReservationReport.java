package com.opentable.sampleapplication.model;

import java.math.BigDecimal;

import lombok.Data;

/**
 * Model class for a customer reservation report.
 * 
 * @author patrick
 *
 */
@Data
public class CustomerReservationReport implements Comparable<CustomerReservationReport> {

	private String name;
	private int num_of_visit;
	private BigDecimal total_spend;

	@Override
	public int compareTo(CustomerReservationReport customerReservationReport) {
		return this.getName().compareTo(customerReservationReport.getName());
	}
}
