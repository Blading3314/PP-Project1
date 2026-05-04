package Employee;

import java.util.List;
import java.util.Optional;

public interface EmployeeDAO {
    Optional<Employee> getEmployeeById(int employeeID);
    List<Employee> getAllEmployees();
    List<Employee> getEmployeesByFirstName(String firstName);
    List<Employee> getEmployeesByLastName(String lastName);
    void saveEmployee(Employee employee);
    void updateEmployee(Employee employee);
    void deleteEmployeeByID(int employeeID);
}
