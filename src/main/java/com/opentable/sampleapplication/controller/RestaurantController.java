package com.opentable.sampleapplication.controller;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.opentable.sampleapplication.error.ApiError;
import com.opentable.sampleapplication.model.CustomerReservationReport;
import com.opentable.sampleapplication.service.RestaurantService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;

/**
 * Endpoints for API calls.
 * 
 * @author patrick
 *
 */
@RestController
@RequestMapping("/api/reservation")
@Validated
@Slf4j
public class RestaurantController {

	@Autowired
	private RestaurantService restaurantService;

	@GetMapping(path = "/report", produces = "application/json")
	@ApiOperation(value = "Get a report showing each customer's total restaurant visits and the respective total spending within a time period.")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Successfully retrieved customer report.", responseContainer = "List", response = CustomerReservationReport.class) })
	/**
	 * Endpoint for getting a report showing each customer's total restaurant visits and the respective total spending
	 * within a time period.
	 * 
	 * @param startDate
	 * @param endDate
	 * @return A ResponseEntity where the content is a list of report of each customer's total restaurant visits and the
	 *         respective total spending within a time period
	 */
	public ResponseEntity<Object> getCustomerReservationReport(
			@RequestParam(required = true) @NotEmpty(message = "Report name is required") String reportName,
			@RequestParam(required = false) @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Start date must have the pattern 'YYYY-MM-DD'") String startDate,
			@RequestParam(required = false) @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "End date must have the pattern 'YYYY-MM-DD'") String endDate) {
		try {
			return ResponseEntity.ok(restaurantService.getCustomerReservationReport(reportName, startDate, endDate));
		} catch (Exception e) {
			return getErrorResponse(e);
		}
	}

	/**
	 * Create error response from exception messages.
	 * 
	 * @param exception
	 * @return A list of all error responses
	 */
	private ResponseEntity<Object> getErrorResponse(Exception exception) {
		List<String> errors = new ArrayList<String>();
		String error = "Invalid request! Please check the request and request params.";
		errors.add(error);

		ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, exception.getLocalizedMessage(), errors);
		log.error("\n##################################################\n" + "Exception:\n"
				+ exception.getLocalizedMessage() + "\n##################################################");
		return new ResponseEntity<Object>(apiError, new HttpHeaders(), apiError.getStatus());
	}
}
