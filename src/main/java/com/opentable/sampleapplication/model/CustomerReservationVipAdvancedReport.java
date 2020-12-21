package com.opentable.sampleapplication.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerReservationVipAdvancedReport extends CustomerReservationAdvancedReport {

	private int vip_credit;

	public CustomerReservationVipAdvancedReport() {
		super();
	}
}
