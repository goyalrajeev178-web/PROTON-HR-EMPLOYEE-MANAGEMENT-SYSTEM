package hrms.model;

import javax.swing.ImageIcon;
import java.awt.Color;
import java.io.Serializable;
import java.util.Objects;

/**
 * Domain model representing an Employee record in the HRMS.
 * Fields are directly accessible for backward compatibility with UI components.
 */
public class Employee implements Serializable {
    private static final long serialVersionUID = 1L;

    public String id;
    public String name;
    public String role;
    public String department;
    public String location;
    public String email;
    public String phone;
    public String joinDate;
    public String status;
    public String salary;
    public String emergency;
    public Color[] avatarColors;
    public transient ImageIcon customPhoto;

    public Employee() {
        this.status = "Active";
        this.location = "Bengaluru, KA";
        this.department = "Engineering";
    }

    public Employee(String id, String name, String role, String department, String location,
                    String email, String phone, String joinDate, String status, String salary,
                    String emergency, Color[] avatarColors) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.department = department;
        this.location = location;
        this.email = email;
        this.phone = phone;
        this.joinDate = joinDate;
        this.status = status != null ? status : "Active";
        this.salary = salary;
        this.emergency = emergency;
        this.avatarColors = avatarColors;
        this.customPhoto = null;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getJoinDate() { return joinDate; }
    public void setJoinDate(String joinDate) { this.joinDate = joinDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSalary() { return salary; }
    public void setSalary(String salary) { this.salary = salary; }

    public String getEmergency() { return emergency; }
    public void setEmergency(String emergency) { this.emergency = emergency; }

    public Color[] getAvatarColors() { return avatarColors; }
    public void setAvatarColors(Color[] avatarColors) { this.avatarColors = avatarColors; }

    public ImageIcon getCustomPhoto() { return customPhoto; }
    public void setCustomPhoto(ImageIcon customPhoto) { this.customPhoto = customPhoto; }

    public double getNumericSalary() {
        if (salary == null || salary.trim().isEmpty()) return 0.0;
        try {
            String cleaned = salary.replaceAll("[^0-9.]", "");
            return cleaned.isEmpty() ? 0.0 : Double.parseDouble(cleaned);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public double getSalaryNumeric() {
        return getNumericSalary();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return Objects.equals(id, employee.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}
