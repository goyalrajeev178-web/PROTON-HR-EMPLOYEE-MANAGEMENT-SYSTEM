package hrms.dao;

import hrms.db.DatabaseManager;
import hrms.model.Employee;
import hrms.util.ValidationUtil;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Data Access Object for Employee records.
 * Integrates directly with MySQL when available, and maintains high-performance local memory store.
 */
public class EmployeeDAO {

    // Default color palette for Indian profile avatars
    public static final Color[][] AVATAR_GRADIENTS = {
        {new Color(0x3B, 0x82, 0xF6), new Color(0x1D, 0x4E, 0xD8)}, // Blue
        {new Color(0x10, 0xB9, 0x81), new Color(0x04, 0x78, 0x57)}, // Green
        {new Color(0x8B, 0x5C, 0xF6), new Color(0x6D, 0x28, 0xD9)}, // Purple
        {new Color(0xF5, 0x9E, 0xB4), new Color(0xB4, 0x53, 0x09)}, // Amber
        {new Color(0xEC, 0x48, 0x99), new Color(0xBE, 0x18, 0x5D)}, // Pink
        {new Color(0x06, 0xB6, 0xD4), new Color(0x0E, 0x74, 0x90)}  // Cyan
    };

    private static final EmployeeDAO INSTANCE = new EmployeeDAO();

    private final List<Employee> memoryCache = new CopyOnWriteArrayList<>();

    private EmployeeDAO() {
        initDefaultCache();
        loadFromDatabaseIfConnected();
    }

    public static EmployeeDAO getInstance() {
        return INSTANCE;
    }

    private void initDefaultCache() {
        memoryCache.add(new Employee("EMP-1001", "Rajesh Sharma", "Senior Software Engineer", "Engineering", "Bengaluru, KA", "rajesh.sharma@gmail.com", "+91 98765 43210", "Mar 15, 2021", "Active", "₹18,00,000", "Sunita Sharma (Spouse) - 98765-43210", AVATAR_GRADIENTS[0]));
        memoryCache.add(new Employee("EMP-1002", "Priya Patel", "HR Business Partner", "Human Resources", "Mumbai, MH", "priya.patel@gmail.com", "+91 98765 12345", "Jan 10, 2020", "Active", "₹14,50,000", "Amit Patel - 98765-54321", AVATAR_GRADIENTS[1]));
        memoryCache.add(new Employee("EMP-1003", "Ananya Iyer", "Product Designer", "Design", "Bengaluru, KA", "ananya.iyer@gmail.com", "+91 98765 23456", "Jun 01, 2022", "Active", "₹16,00,000", "Suresh Iyer - 98765-67890", AVATAR_GRADIENTS[2]));
        memoryCache.add(new Employee("EMP-1004", "Vikram Verma", "Regional Sales Director", "Sales", "Delhi NCR", "v.verma@gmail.com", "+91 98765 34567", "Nov 12, 2018", "Active", "₹22,00,000", "Pooja Verma - 98765-99999", AVATAR_GRADIENTS[3]));
        memoryCache.add(new Employee("EMP-1005", "Kavita Rao", "VP of Engineering", "Engineering", "Bengaluru, KA", "kavita.rao@gmail.com", "+91 98765 45678", "Feb 20, 2019", "Active", "₹35,00,000", "Rohan Rao - 98765-00000", AVATAR_GRADIENTS[4]));
        memoryCache.add(new Employee("EMP-1006", "Arjun Kapoor", "Financial Analyst", "Finance", "Mumbai, MH", "a.kapoor@gmail.com", "+91 98765 56789", "Sep 15, 2022", "Remote", "₹13,50,000", "Neha Kapoor - 98765-11223", AVATAR_GRADIENTS[5]));
        memoryCache.add(new Employee("EMP-1007", "Deepika Singh", "Marketing Lead", "Marketing", "Hyderabad, TS", "d.singh@gmail.com", "+91 98765 67890", "Apr 05, 2023", "Active", "₹15,00,000", "Ranveer Singh - 98765-33445", AVATAR_GRADIENTS[0]));
        memoryCache.add(new Employee("EMP-1008", "Aarav Mehta", "DevOps Engineer", "Engineering", "Pune, MH", "aarav.m@gmail.com", "+91 98765 78901", "Aug 18, 2021", "On Leave", "₹17,50,000", "Sneha Mehta - 98765-55667", AVATAR_GRADIENTS[1]));
        memoryCache.add(new Employee("EMP-1009", "Neha Gupta", "Talent Acquisition", "Human Resources", "Delhi NCR", "neha.g@gmail.com", "+91 98765 89012", "May 22, 2022", "Remote", "₹11,00,000", "Rohan Gupta - 98765-77889", AVATAR_GRADIENTS[2]));
        memoryCache.add(new Employee("EMP-1010", "Siddharth Joshi", "Content Strategist", "Marketing", "Pune, MH", "s.joshi@gmail.com", "+91 98765 90123", "Oct 01, 2023", "Active", "₹12,50,000", "Ritu Joshi - 98765-99001", AVATAR_GRADIENTS[3]));
        memoryCache.add(new Employee("EMP-1011", "Pooja Reddy", "Operations Specialist", "Operations", "Hyderabad, TS", "p.reddy@gmail.com", "+91 98765 01234", "Jan 15, 2024", "Active", "₹11,50,000", "Karthik Reddy - 98765-22334", AVATAR_GRADIENTS[4]));
        memoryCache.add(new Employee("EMP-1012", "Rohan Malhotra", "Security Consultant", "Engineering", "Remote", "r.malhotra@gmail.com", "+91 98765 12340", "Dec 01, 2021", "Remote", "₹19,00,000", "Simran Malhotra - 98765-44556", AVATAR_GRADIENTS[5]));
    }

    public synchronized void loadFromDatabaseIfConnected() {
        if (!DatabaseManager.getInstance().isConnected()) return;

        String sql = "SELECT employee_id, full_name, role_title, department_name, location, email, phone, hire_date, status, salary, emergency_contact, avatar_color_start, avatar_color_end FROM employees ORDER BY employee_id ASC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            List<Employee> loaded = new ArrayList<>();
            int idx = 0;
            while (rs.next()) {
                String id = rs.getString("employee_id");
                String name = rs.getString("full_name");
                String role = rs.getString("role_title");
                String dept = rs.getString("department_name");
                String loc = rs.getString("location");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                String hireDate = rs.getString("hire_date");
                String status = rs.getString("status");
                String salary = rs.getString("salary");
                String emergency = rs.getString("emergency_contact");

                Color[] grad = AVATAR_GRADIENTS[idx % AVATAR_GRADIENTS.length];
                idx++;

                loaded.add(new Employee(id, name, role, dept, loc, email, phone, hireDate, status, salary, emergency, grad));
            }

            if (!loaded.isEmpty()) {
                memoryCache.clear();
                memoryCache.addAll(loaded);
                System.out.println("EmployeeDAO: Synced " + loaded.size() + " records from MySQL.");
            }
        } catch (SQLException e) {
            System.err.println("EmployeeDAO: Failed reading from MySQL: " + e.getMessage());
        }
    }

    public List<Employee> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(memoryCache));
    }

    public Employee getById(String id) {
        if (id == null) return null;
        for (Employee emp : memoryCache) {
            if (emp.getId().equalsIgnoreCase(id.trim())) {
                return emp;
            }
        }
        return null;
    }

    public Employee findByEmail(String email) {
        if (email == null) return null;
        for (Employee emp : memoryCache) {
            if (emp.getEmail().equalsIgnoreCase(email.trim())) {
                return emp;
            }
        }
        return null;
    }

    public boolean add(Employee emp) throws Exception {
        if (emp == null) throw new IllegalArgumentException("Employee record cannot be null.");

        // Validation
        ValidationUtil.Result vName = ValidationUtil.validateName(emp.getName());
        if (!vName.isValid()) throw new IllegalArgumentException(vName.getMessage());

        ValidationUtil.Result vEmail = ValidationUtil.validateEmail(emp.getEmail());
        if (!vEmail.isValid()) throw new IllegalArgumentException(vEmail.getMessage());

        ValidationUtil.Result vPhone = ValidationUtil.validatePhone(emp.getPhone());
        if (!vPhone.isValid()) throw new IllegalArgumentException(vPhone.getMessage());

        ValidationUtil.Result vSalary = ValidationUtil.validateSalary(emp.getSalary());
        if (!vSalary.isValid()) throw new IllegalArgumentException(vSalary.getMessage());

        // Check duplicate email
        for (Employee existing : memoryCache) {
            if (existing.getEmail().equalsIgnoreCase(emp.getEmail().trim())) {
                throw new IllegalArgumentException("An employee with email '" + emp.getEmail() + "' already exists.");
            }
            if (existing.getId().equalsIgnoreCase(emp.getId().trim())) {
                throw new IllegalArgumentException("Employee ID '" + emp.getId() + "' already exists.");
            }
        }

        // MySQL persistence
        if (DatabaseManager.getInstance().isConnected()) {
            String sql = "INSERT INTO employees (employee_id, full_name, role_title, department_name, location, email, phone, hire_date, status, salary, emergency_contact) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseManager.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, emp.getId());
                ps.setString(2, emp.getName());
                ps.setString(3, emp.getRole());
                ps.setString(4, emp.getDepartment());
                ps.setString(5, emp.getLocation());
                ps.setString(6, emp.getEmail());
                ps.setString(7, emp.getPhone());
                ps.setString(8, emp.getJoinDate() != null ? emp.getJoinDate() : "Today");
                ps.setString(9, emp.getStatus());
                ps.setString(10, emp.getSalary());
                ps.setString(11, emp.getEmergency() != null ? emp.getEmergency() : "N/A");
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("EmployeeDAO: MySQL Insert error: " + e.getMessage());
                throw new SQLException("Database insertion error: " + e.getMessage(), e);
            }
        }

        // Memory cache update
        if (emp.getAvatarColors() == null) {
            emp.setAvatarColors(AVATAR_GRADIENTS[memoryCache.size() % AVATAR_GRADIENTS.length]);
        }
        memoryCache.add(emp);
        return true;
    }

    public boolean update(Employee emp) throws Exception {
        if (emp == null || emp.getId() == null) throw new IllegalArgumentException("Invalid employee update target.");

        ValidationUtil.Result vName = ValidationUtil.validateName(emp.getName());
        if (!vName.isValid()) throw new IllegalArgumentException(vName.getMessage());

        ValidationUtil.Result vEmail = ValidationUtil.validateEmail(emp.getEmail());
        if (!vEmail.isValid()) throw new IllegalArgumentException(vEmail.getMessage());

        ValidationUtil.Result vPhone = ValidationUtil.validatePhone(emp.getPhone());
        if (!vPhone.isValid()) throw new IllegalArgumentException(vPhone.getMessage());

        ValidationUtil.Result vSalary = ValidationUtil.validateSalary(emp.getSalary());
        if (!vSalary.isValid()) throw new IllegalArgumentException(vSalary.getMessage());

        // Check duplicate email with OTHER employees
        for (Employee existing : memoryCache) {
            if (!existing.getId().equalsIgnoreCase(emp.getId()) &&
                 existing.getEmail().equalsIgnoreCase(emp.getEmail().trim())) {
                throw new IllegalArgumentException("Email '" + emp.getEmail() + "' is already assigned to another employee (" + existing.getName() + ").");
            }
        }

        // MySQL persistence
        if (DatabaseManager.getInstance().isConnected()) {
            String sql = "UPDATE employees SET full_name = ?, role_title = ?, department_name = ?, location = ?, email = ?, phone = ?, status = ?, salary = ?, emergency_contact = ? WHERE employee_id = ?";
            try (Connection conn = DatabaseManager.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, emp.getName());
                ps.setString(2, emp.getRole());
                ps.setString(3, emp.getDepartment());
                ps.setString(4, emp.getLocation());
                ps.setString(5, emp.getEmail());
                ps.setString(6, emp.getPhone());
                ps.setString(7, emp.getStatus());
                ps.setString(8, emp.getSalary());
                ps.setString(9, emp.getEmergency());
                ps.setString(10, emp.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("EmployeeDAO: MySQL Update error: " + e.getMessage());
                throw new SQLException("Database update error: " + e.getMessage(), e);
            }
        }

        // Memory cache update
        for (int i = 0; i < memoryCache.size(); i++) {
            if (memoryCache.get(i).getId().equalsIgnoreCase(emp.getId())) {
                Employee old = memoryCache.get(i);
                if (emp.getAvatarColors() == null) emp.setAvatarColors(old.getAvatarColors());
                if (emp.getCustomPhoto() == null) emp.setCustomPhoto(old.getCustomPhoto());
                memoryCache.set(i, emp);
                return true;
            }
        }
        return false;
    }

    public boolean delete(String id) throws Exception {
        if (id == null || id.trim().isEmpty()) return false;

        // MySQL persistence
        if (DatabaseManager.getInstance().isConnected()) {
            String sql = "DELETE FROM employees WHERE employee_id = ?";
            try (Connection conn = DatabaseManager.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, id.trim());
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("EmployeeDAO: MySQL Delete error: " + e.getMessage());
                throw new SQLException("Database deletion error: " + e.getMessage(), e);
            }
        }

        // Memory cache update
        return memoryCache.removeIf(emp -> emp.getId().equalsIgnoreCase(id.trim()));
    }

    public List<Employee> search(String dept, String status, String location, String keyword) {
        List<Employee> results = new ArrayList<>();
        String kw = (keyword != null) ? keyword.trim().toLowerCase() : "";

        for (Employee emp : memoryCache) {
            if (dept != null && !"All Departments".equalsIgnoreCase(dept) && !emp.getDepartment().equalsIgnoreCase(dept)) {
                continue;
            }
            if (status != null && !"All Statuses".equalsIgnoreCase(status) && !emp.getStatus().equalsIgnoreCase(status)) {
                continue;
            }
            if (location != null && !"All Locations".equalsIgnoreCase(location) && !emp.getLocation().equalsIgnoreCase(location)) {
                continue;
            }
            if (!kw.isEmpty()) {
                boolean matches = emp.getName().toLowerCase().contains(kw)
                        || emp.getRole().toLowerCase().contains(kw)
                        || emp.getId().toLowerCase().contains(kw)
                        || emp.getDepartment().toLowerCase().contains(kw)
                        || emp.getEmail().toLowerCase().contains(kw)
                        || emp.getLocation().toLowerCase().contains(kw);
                if (!matches) continue;
            }
            results.add(emp);
        }
        return results;
    }

    public String generateNextId() {
        int max = 1000;
        for (Employee emp : memoryCache) {
            try {
                String id = emp.getId();
                if (id.startsWith("EMP-")) {
                    int num = Integer.parseInt(id.substring(4));
                    if (num > max) max = num;
                }
            } catch (Exception ignored) {}
        }
        return "EMP-" + (max + 1);
    }

    public String generateNextEmployeeId() {
        return generateNextId();
    }
}
