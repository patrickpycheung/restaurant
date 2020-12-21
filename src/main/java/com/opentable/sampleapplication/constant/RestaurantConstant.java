package com.opentable.sampleapplication.constant;

public class RestaurantConstant {

	public static final String CUSTOMER_RESERVATION_BASIC_REPORT = "CustomerReservationBasicReport";

	public static final String CUSTOMER_RESERVATION_ADVANCED_REPORT = "CustomerReservationAdvancedReport";

	public static final String CUSTOMER_RESERVATION_VIP_ADVANCED_REPORT = "CustomerReservationVipAdvancedReport";

	public enum CustomerReservationBasicReportField {
		NAME("name"), NUM_OF_VISIT("num_of_visit"), TOTAL_SPEND("total_spend");

		private String customerReservationBasicReportField;

		CustomerReservationBasicReportField(String customerReservationBasicReportField) {
			this.customerReservationBasicReportField = customerReservationBasicReportField;
		}

		public String getField() {
			return this.customerReservationBasicReportField;
		}
	}
}
