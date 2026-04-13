package com.srijeet.employee_server.config;

import com.srijeet.employee_server.entity.Employee;
import com.srijeet.employee_server.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;

    @Override
    public void run(String... args) {
        if (employeeRepository.count() == 0) {
            Employee emp1 = new Employee();
            emp1.setName("张三");
            emp1.setEmail("zhangsan@example.com");
            emp1.setPhone("13800138001");
            emp1.setDepartment("技术部");
            employeeRepository.save(emp1);

            Employee emp2 = new Employee();
            emp2.setName("李四");
            emp2.setEmail("lisi@example.com");
            emp2.setPhone("13800138002");
            emp2.setDepartment("市场部");
            employeeRepository.save(emp2);

            Employee emp3 = new Employee();
            emp3.setName("王五");
            emp3.setEmail("wangwu@example.com");
            emp3.setPhone("13800138003");
            emp3.setDepartment("人事部");
            employeeRepository.save(emp3);

            System.out.println("Test employees initialized successfully");
        }
    }
}
