package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.CompensationRepository;
import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CompensationRepository compensationRepository;

    @Override
    public Employee create(Employee employee) {
        LOG.debug("Creating employee [{}]", employee);

        employee.setEmployeeId(UUID.randomUUID().toString());
        employeeRepository.insert(employee);

        return employee;
    }

    @Override
    public Employee read(String id) {
        LOG.debug("Creating employee with id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        return employee;
    }

    @Override
    public Employee update(Employee employee) {
        LOG.debug("Updating employee [{}]", employee);

        return employeeRepository.save(employee);
    }

    @Override
    public ReportingStructure generateReportingStructure(String id) {
        LOG.debug("Reading employee with id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        ReportingStructure reportingStructure = new ReportingStructure();
        reportingStructure.setEmployee(employee.getEmployeeId());
        reportingStructure.setNumberOfReports(findNumberOfDirectReports(employee));
        return reportingStructure;
    }

    private int findNumberOfDirectReports(Employee employee) {
        // The termination condition for the recursion.
        if(employee.getDirectReports() == null || employee.getDirectReports().isEmpty()) {
            return 0;
        }

        int numberOfDirectReports = employee.getDirectReports().size();

        for(Employee directReport : employee.getDirectReports()) {
            Employee directEmployee = employeeRepository.findByEmployeeId(directReport.getEmployeeId());

            if (directEmployee == null) {
                throw new RuntimeException("Invalid employeeId: " + directReport.getEmployeeId());
            }

            // Using recursion to get the nested number of direct reports
            numberOfDirectReports += findNumberOfDirectReports(directEmployee);
        }

        return numberOfDirectReports;
    }

    @Override
    public Compensation createCompensation(Compensation compensation) {
        LOG.debug("Creating compensation [{}]", compensation);
        String employeeId = compensation.getEmployee();

        Employee directEmployee = employeeRepository.findByEmployeeId(employeeId);

        if (directEmployee == null) {
            throw new RuntimeException("Invalid employeeId: " + employeeId);
        }

        // Using insert to prevent updating existing compensation entries
        return compensationRepository.insert(compensation);
    }

    @Override
    public Compensation getCompensation(String id) {
        LOG.debug("Reading compensation with id [{}]", id);
        Optional<Compensation> optionalCompensation = compensationRepository.findById(id);

        if(!optionalCompensation.isPresent()) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        return optionalCompensation.get();
    }
}
