# The Restaturant Application

  Table of contents

  * [**About the application**](#about-the-application)
  * [**Version history**](#version-history)
  * [**Assumptions**](#assumptions)
  * [**Instructions**](#instructions)
    + [**Pre-requsites**](#pre-requsites)
    + [**Running the binaries**](#running-the-binaries)
    + [**RESTful API endpoints**](#restful-api-endpoints)
        * [**Get a report showing each customer's total restaurant visits and the respective total spending within a time period**](#get-a-report-showing-each-customer's-total-restaurant-visits-and-the-respective-total-spending-within-a-time-period)
  * [**Architecture**](#architecture)
    + [**Model layer**](#model-layer)
        * [**Database**](#database)
    + [**Controller layer**](#controller-layer)
    + [**Logging**](#logging)
  * [**Technology stack**](#technology-stack)

## **About the application**

  The Restaurant application is designed to provide insights on restaurant reservation and spending by customers. It is written in Java and Spring Boot.

  It currently provides the following endpoint:
  * Get a report showing each customer's total restaurant visits and the respective total spending within a time period

## **Version history**

  | Version| Date | Description | 
  | --- | --- | --- | 
  | 1.0.1 | 6 Dec 2020 | Initial release |

## **Assumptions**

The following assumptions are being made for the application:
* The provision of startDate and endDate params for the query is optional. User can provide both, one of either or none of them when calling the endpoint.

## **Instructions**

  ### Pre-requsites

  * Maven
  * JDK11
  * An API testing tool (e.g. Postman, or just use the Swagger UI)

  ### **Running the binaries**

  The applcation can be started directly by running the following command at the project root folder

    mvn spring-boot:run

  If you would like to create a JAR file from the source, run the following command

    mvn clean package

  A JAR file will be created in the "target" folder and can be executed by the following command

    java -jar account-<artifact version>.jar

  ### **RESTful API endpoints**

  The following API endpoints are available:

  | Verb | Path | URL param  | Request param | Request body | Description 
  | --- | --- | --- | --- | --- |  --- |
  | GET | /api/reservation/report | N/A | startDate (optional), endDate (optional) | N/A | Get a report showing each customer's total restaurant visits and the respective total spending within a time period 

  The required parameters and returns are described in the Swagger UI page.

  Swagger UI path (assuming you are running the application in local environment):<br/>
  http://localhost:8080/swagger-ui.html#/account-controller

  Sample call:

  #### **Get a report showing each customer's total restaurant visits and the respective total spending within a time period**

    GET /api/reservation/report?startDate=2020-11-06&endDate=2020-11-26 HTTP/1.1
    User-Agent: PostmanRuntime/7.26.8
    Accept: */*
    Cache-Control: no-cache
    Postman-Token: 842f7eab-ca27-4424-bb25-05eea920e16f
    Host: localhost:8080
    Accept-Encoding: gzip, deflate, br
    Connection: keep-alive

    HTTP/1.1 200 OK
    Content-Type: application/json
    Transfer-Encoding: chunked
    Date: Sun, 06 Dec 2020 04:20:27 GMT
    Keep-Alive: timeout=60
    Connection: keep-alive

    [{"name":"Felix Fish","num_of_visit":2,"total_spend":152.50},{"name":"Harry Houdini","num_of_visit":6,"total_spend":437.00},{"name":"Jack Rabbit","num_of_visit":3,"total_spend":168.94},{"name":"Rock Lobster","num_of_visit":3,"total_spend":256.07}]


## **Architecture**

### Model layer

#### Database

The datasource is a JSON file containing all reservation records. Each provides information of the customer, reservation date, party size and the total spending. 

### Controller layer

The endpoints are hosted by the controller RestaurantController. It listens for API calls.
When one is received, it triggers the corresponding method to handle the request.

It will then perform field validation (for GET request on request params and URL params) and bean validation (for PUT/POST request on request body which will be translated a a java object).

A custom exception handler CustomGlobalExceptionHandler is created to handle the error response arised from problematic requests. If any validation on the request fails, it will return a response with a HTTP 400 BAD REQUEST staus, together with the error message. This provides a response with more meaningful details.

e.g.

    GET /api/reservation/report?startDate=20201106&endDate=20201126 HTTP/1.1
    User-Agent: PostmanRuntime/7.26.8
    Accept: */*
    Cache-Control: no-cache
    Postman-Token: aa68c5a0-f358-4efd-8252-a0c0db6c6ed6
    Host: localhost:8080
    Accept-Encoding: gzip, deflate, br
    Connection: keep-alive

    HTTP/1.1 400 Bad Request
    Content-Type: application/json
    Transfer-Encoding: chunked
    Date: Sun, 06 Dec 2020 04:29:18 GMT
    Connection: close

    {"status":"BAD_REQUEST","message":"getCustomerReservationReport.endDate: End date must have the pattern 'YYYY-MM-DD', getCustomerReservationReport.startDate: Start date must have the pattern 'YYYY-MM-DD'","errors":["com.opentable.sampleapplication.controller.RestaurantController getCustomerReservationReport.endDate: End date must have the pattern 'YYYY-MM-DD'","com.opentable.sampleapplication.controller.RestaurantController getCustomerReservationReport.startDate: Start date must have the pattern 'YYYY-MM-DD'"]}
    
When validation is passed, it passes the inputs to the service RestaurantService to perform the actual operation.

There are 3 stages of the service logic
* **Get all records from the datasource**

  This is done in getAllReservations().

  This will perform the actual JSON deserialization.

  An instance of ObjectMapper from "fasterxml" library is injected to deserialize the JSON datasource content to Java objects.

* **Filter to obtain only records within the date range specified by user**

  This is done in getReservationsByScheduledDate().

  This will check each Reservation record's scheduled_Date and compare with the startDate and/or endDate criteria specified by user. Out-of-range reservation records will be removed.

  If no startDate or endDate criteria is specified by the user, no filtering will be performed.

* **Aggregate the data to group by customer**

  This is done in getCustomerReservationReport().

  This will drill down into each of the Reservation, group the reservations by unique customer, and sum up all the reservation count and the total spending of all reservations.

The result from the service will be passed back the contoller and a ResponseEntity object will be returned. It will be translated to a JSON response to the API caller.

Note:
Although not yet implemented, API endpoints to "get all reservations" and to "get reservations by date" can be set up as well using the the existing service getAllReservations() and getReservationsByScheduledDate resepectively.

### Logging

Logging has been configured at INFO level which will publish informational logs, warnings as well as errors to both the console and the log file.

The log file is saved at the same location where the applciation is launched.
In addition, it is set to archive daily or whenever the file reaches the file size limit.

These can be configured in the logback-spring.xml.

To change the log level and log output target:

* Amend the "springProfile" property (for the default profile, i.e. !dev) in logback-spring.xml

## **Technology stack**

* Java 11
* Spring Boot v2.4.0
  * Dependencies:
    * spring-boot-starter-web (Proivdes a Tomcat web container)
    * lombok (Provides getters/setters to entity/model class properties, and provides slf4j support for logging)
    * spring-boot-starter-test (Provides JUnit support)
    * springfox-swagger2 (Provides Swagger support)
    * springfox-swagger-ui (Provides Swager UI)
    * spring-boot-maven-plugin (Provides Maven support)
    * spring-boot-starter-validation (Provides validation support)
