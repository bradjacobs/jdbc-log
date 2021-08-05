package com.github.bradjacobs.logging.jdbc.demo;

import com.github.bradjacobs.logging.jdbc.demo.dao.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DemoComponent implements ApplicationListener<ApplicationReadyEvent>
{

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event)
    {
        // start by deleting any preexisting records.
        employeeRepository.deleteAll();

        Employee employee1 = new Employee();
        employee1.setId(1L);
        employee1.setFirstName("George");
        employee1.setLastName("Washington");

        employeeRepository.save(employee1);


        List<Employee> queriedEmpolyees = employeeRepository.findAll();

        Employee queriedEmpolyee = queriedEmpolyees.get(0);

        queriedEmpolyee.setNotes("making an update!!");

        employeeRepository.save(queriedEmpolyee);


        System.out.println("dome complete.");



    }
}
