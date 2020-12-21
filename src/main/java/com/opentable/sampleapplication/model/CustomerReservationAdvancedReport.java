package com.opentable.sampleapplication.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerReservationAdvancedReport extends CustomerReservationReport {

	private int max_party_size;

	public CustomerReservationAdvancedReport() {
		super();
	}
}
