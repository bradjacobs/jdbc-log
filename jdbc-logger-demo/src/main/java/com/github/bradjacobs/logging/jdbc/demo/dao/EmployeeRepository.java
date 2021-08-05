package com.github.bradjacobs.logging.jdbc.demo.dao;

import com.github.bradjacobs.logging.jdbc.demo.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface EmployeeRepository extends JpaRepository<Employee, Long>
{


}