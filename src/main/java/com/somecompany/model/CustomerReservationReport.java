package com.somecompany.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Model class for a customer reservation report.
 *
 * @author patrick
 */
@Data
public abstract class CustomerReservationReport implements Comparable<CustomerReservationReport> {

    private String name;
    private int num_of_visit;
    private BigDecimal total_spend;

    @Override
    public int compareTo(CustomerReservationReport customerReservationReport) {
        return this.getName().compareTo(customerReservationReport.getName());
    }
}
