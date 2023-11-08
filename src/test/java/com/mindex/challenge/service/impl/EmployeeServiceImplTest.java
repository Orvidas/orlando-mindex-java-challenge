package com.mindex.challenge.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeServiceImplTest {

    private String employeeUrl;
    private String employeeIdUrl;
    private String reportingStructureIdUrl;
    private String compensationUrl;
    private String compensationReadUrl;

    @Autowired
    private EmployeeService employeeService;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        employeeUrl = "http://localhost:" + port + "/employee";
        employeeIdUrl = "http://localhost:" + port + "/employee/{id}";
        reportingStructureIdUrl = "http://localhost:" + port + "/employee/reporting-structure/{id}";
        compensationUrl = "http://localhost:" + port + "/employee/compensation";
        compensationReadUrl = "http://localhost:" + port + "/employee/compensation/{id}";
    }

    @Test
    public void testCreateReadUpdate() {
        Employee testEmployee = new Employee();
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setDepartment("Engineering");
        testEmployee.setPosition("Developer");

        // Create checks
        Employee createdEmployee = restTemplate.postForEntity(employeeUrl, testEmployee, Employee.class).getBody();

        assertNotNull(createdEmployee.getEmployeeId());
        assertEmployeeEquivalence(testEmployee, createdEmployee);


        // Read checks
        Employee readEmployee = restTemplate.getForEntity(employeeIdUrl, Employee.class, createdEmployee.getEmployeeId()).getBody();
        assertEquals(createdEmployee.getEmployeeId(), readEmployee.getEmployeeId());
        assertEmployeeEquivalence(createdEmployee, readEmployee);


        // Update checks
        readEmployee.setPosition("Development Manager");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Employee updatedEmployee =
                restTemplate.exchange(employeeIdUrl,
                        HttpMethod.PUT,
                        new HttpEntity<Employee>(readEmployee, headers),
                        Employee.class,
                        readEmployee.getEmployeeId()).getBody();

        assertEmployeeEquivalence(readEmployee, updatedEmployee);
    }

    private static void assertEmployeeEquivalence(Employee expected, Employee actual) {
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getDepartment(), actual.getDepartment());
        assertEquals(expected.getPosition(), actual.getPosition());
    }

    @Test
    public void testGenerateReportingStructureWith4Reports() throws IOException {
        insertTestEmployeeData();
        String testEmployee = "16a596ae-edd3-4847-99fe-c4518e82c86f";

        ReportingStructure actualReport = restTemplate.getForEntity(reportingStructureIdUrl, ReportingStructure.class, testEmployee).getBody();

        assertNotNull(actualReport);
        assertEquals(testEmployee, actualReport.getEmployee());
        assertEquals(4, actualReport.getNumberOfReports());
    }

    @Test
    public void testGenerateReportingStructureWith0Reports() throws IOException {
        insertTestEmployeeData();
        String testEmployee = "c0c2293d-16bd-4603-8e08-638a9d18b22c";

        ReportingStructure actualReport = restTemplate.getForEntity(reportingStructureIdUrl, ReportingStructure.class, testEmployee).getBody();

        assertNotNull(actualReport);
        assertEquals(testEmployee, actualReport.getEmployee());
        assertEquals(0, actualReport.getNumberOfReports());
    }

    @Test
    public void testGenerateReportingStructureWithInvalidEmployeeId() throws IOException {
        insertTestEmployeeData();
        String invalidEmployee = "NotReal";

        ResponseEntity<ReportingStructure> response = restTemplate.getForEntity(reportingStructureIdUrl, ReportingStructure.class, invalidEmployee);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    private void insertTestEmployeeData() throws IOException {
        InputStream inputStream = this.getClass().getResourceAsStream("/static/employee_database.json");
        Employee[] employees = objectMapper.readValue(inputStream, Employee[].class);
        Arrays.stream(employees).forEach(employee -> restTemplate.postForEntity(employeeUrl, employee, Employee.class));
    }

    @Test
    public void testCreateReadCompensation() throws IOException {
        insertTestEmployeeData();
        String employeeId = "16a596ae-edd3-4847-99fe-c4518e82c86f";
        Compensation expectedCompensation = new Compensation();
        expectedCompensation.setEmployee(employeeId);
        expectedCompensation.setSalary(100000);
        expectedCompensation.setEffectiveDate(new Date());

        // Write Compensation test
        Compensation writeCompensation = restTemplate.postForEntity(compensationUrl, expectedCompensation, Compensation.class).getBody();

        assertNotNull(writeCompensation);
        assertCompensationEquivalence(expectedCompensation, writeCompensation);

        // Read Compensation Test
        Compensation readCompensation = restTemplate.getForEntity(compensationReadUrl, Compensation.class, employeeId).getBody();
        assertNotNull(readCompensation);
        assertCompensationEquivalence(expectedCompensation, readCompensation);
    }

    private static void assertCompensationEquivalence(Compensation expected, Compensation actual) {
        assertEquals(expected.getEmployee(), actual.getEmployee());
        assertEquals(expected.getSalary(), actual.getSalary(), 0.01);
        assertEquals(expected.getEffectiveDate(), actual.getEffectiveDate());
    }

    @Test
    public void testCreateCompensationInvalidEmployee() throws IOException {
        insertTestEmployeeData();
        Compensation expectedCompensation = new Compensation();
        expectedCompensation.setEmployee("No Employee");
        expectedCompensation.setSalary(100000);
        expectedCompensation.setEffectiveDate(new Date());

        ResponseEntity<Compensation> response = restTemplate.postForEntity(compensationUrl, expectedCompensation, Compensation.class);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
