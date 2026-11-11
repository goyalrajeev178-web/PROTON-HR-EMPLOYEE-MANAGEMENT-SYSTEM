package hrms.test;

import hrms.dao.EmployeeDAO;
import hrms.dao.LeaveDAO;
import hrms.db.DatabaseConfig;
import hrms.model.Employee;
import hrms.model.LeaveRecord;
import hrms.util.CsvExportUtil;
import hrms.util.ValidationUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

/**
 * Comprehensive automated integration test suite for HRMS.
 * Tests validation rules, Employee CRUD, Leave management, CSV export, and DatabaseConfig.
 */
public class SystemIntegrationTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("Starting HRMS Automated System Integration Tests");
        System.out.println("==================================================");

        testValidationRules();
        testEmployeeCrudAndIdGenerator();
        testCsvExport();
        testLeaveWorkflow();
        testDatabaseConfigUrlParsing();

        System.out.println("==================================================");
        System.out.println("TEST RUN COMPLETED");
        System.out.println("Tests Passed: " + testsPassed);
        System.out.println("Tests Failed: " + testsFailed);
        System.out.println("==================================================");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition, String failureReason) {
        if (condition) {
            System.out.println("  [PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("  [FAIL] " + testName + " -> " + failureReason);
            testsFailed++;
        }
    }

    private static void testValidationRules() {
        System.out.println("\n--- Testing Validation Rules ---");

        // 1. Email validation (@gmail.com constraint)
        ValidationUtil.Result v1 = ValidationUtil.validateEmail("rajesh.sharma@gmail.com");
        assertTrue("Valid @gmail.com email accepted", v1.isValid(), v1.getMessage());

        ValidationUtil.Result v2 = ValidationUtil.validateEmail("rajesh.sharma@yahoo.com");
        assertTrue("Non-gmail email rejected (@yahoo.com)", !v2.isValid(), "Should reject non-gmail");

        ValidationUtil.Result v3 = ValidationUtil.validateEmail("invalid-email");
        assertTrue("Malformed email rejected", !v3.isValid(), "Should reject malformed email");

        ValidationUtil.Result v4 = ValidationUtil.validateEmail("");
        assertTrue("Empty email rejected", !v4.isValid(), "Should reject empty email");

        // 2. Name validation
        ValidationUtil.Result n1 = ValidationUtil.validateName("Aarav Patel");
        assertTrue("Valid full name accepted", n1.isValid(), n1.getMessage());

        ValidationUtil.Result n2 = ValidationUtil.validateName("A");
        assertTrue("Single character name rejected", !n2.isValid(), "Should reject single char");

        // 3. Phone validation
        ValidationUtil.Result p1 = ValidationUtil.validatePhone("+91 98765 43210");
        assertTrue("Standard phone number accepted", p1.isValid(), p1.getMessage());

        ValidationUtil.Result p2 = ValidationUtil.validatePhone("123");
        assertTrue("Short phone number rejected", !p2.isValid(), "Should reject short phone");

        // 4. Salary validation
        ValidationUtil.Result s1 = ValidationUtil.validateSalary("₹15,00,000");
        assertTrue("Formatted currency salary accepted", s1.isValid(), s1.getMessage());

        ValidationUtil.Result s2 = ValidationUtil.validateSalary("abc");
        assertTrue("Non-numeric salary rejected", !s2.isValid(), "Should reject non-numeric");
    }

    private static void testEmployeeCrudAndIdGenerator() {
        System.out.println("\n--- Testing Employee DAO & CRUD ---");

        EmployeeDAO dao = EmployeeDAO.getInstance();
        int initialCount = dao.getAll().size();
        assertTrue("Initial employees loaded into DAO", initialCount >= 10, "Expected at least 10 seeded employees, got: " + initialCount);

        // 1. Generate Next ID
        String nextId = dao.generateNextEmployeeId();
        assertTrue("Generated Next Employee ID format", nextId != null && nextId.startsWith("EMP-"), "Unexpected ID format: " + nextId);

        // 2. Add Employee
        Employee newEmp = new Employee(
            nextId,
            "Manish Kumar",
            "Full Stack Developer",
            "Engineering",
            "Bengaluru, KA",
            "manish.kumar@gmail.com",
            "+91 98765 11223",
            "Today",
            "Active",
            "₹14,00,000",
            "Sita Kumar - 98765-00112",
            null
        );

        try {
            boolean added = dao.add(newEmp);
            assertTrue("Add new employee", added, "Failed to add employee");
        } catch (Exception e) {
            assertTrue("Add new employee exception", false, e.getMessage());
        }

        // 3. Get By ID and Find By Email
        Employee fetched = dao.getById(nextId);
        assertTrue("Get added employee by ID", fetched != null && fetched.getName().equals("Manish Kumar"), "Could not find added employee by ID");

        Employee byEmail = dao.findByEmail("manish.kumar@gmail.com");
        assertTrue("Find added employee by email", byEmail != null && byEmail.getId().equals(nextId), "Could not find added employee by email");

        // 4. Duplicate ID prevention
        boolean dupIdBlocked = false;
        try {
            Employee duplicate = new Employee(nextId, "Another Person", "Analyst", "Finance", "Mumbai, MH", "other.person@gmail.com", "+91 98765 99887", "Today", "Active", "₹10,00,000", "None", null);
            dao.add(duplicate);
        } catch (IllegalArgumentException e) {
            dupIdBlocked = true;
        } catch (Exception e) {
            dupIdBlocked = false;
        }
        assertTrue("Duplicate Employee ID prevented", dupIdBlocked, "Should throw IllegalArgumentException on duplicate ID");

        // 5. Duplicate Email prevention
        boolean dupEmailBlocked = false;
        try {
            Employee duplicate = new Employee(dao.generateNextEmployeeId(), "Another Person", "Analyst", "Finance", "Mumbai, MH", "manish.kumar@gmail.com", "+91 98765 99887", "Today", "Active", "₹10,00,000", "None", null);
            dao.add(duplicate);
        } catch (IllegalArgumentException e) {
            dupEmailBlocked = true;
        } catch (Exception e) {
            dupEmailBlocked = false;
        }
        assertTrue("Duplicate Employee Email prevented", dupEmailBlocked, "Should throw IllegalArgumentException on duplicate Email");

        // 6. Search functionality
        List<Employee> searchDept = dao.search("Engineering", null, null, "");
        assertTrue("Search employees by Department", !searchDept.isEmpty(), "No employees found in Engineering");

        List<Employee> searchKeyword = dao.search(null, null, null, "Manish");
        assertTrue("Search employees by Name Keyword", searchKeyword.size() == 1 && searchKeyword.get(0).getName().equals("Manish Kumar"), "Search keyword failed");

        // 7. Delete Employee
        try {
            boolean deleted = dao.delete(nextId);
            assertTrue("Delete employee", deleted, "Failed to delete employee");
            Employee afterDelete = dao.getById(nextId);
            assertTrue("Verify employee deleted from DAO", afterDelete == null, "Employee still exists after deletion");
        } catch (Exception e) {
            assertTrue("Delete employee exception", false, e.getMessage());
        }
    }

    private static void testCsvExport() {
        System.out.println("\n--- Testing CSV Export Utility ---");

        List<Employee> employees = EmployeeDAO.getInstance().getAll();
        File tempFile = null;
        try {
            tempFile = File.createTempFile("hrms_test_export_", ".csv");
            int exported = CsvExportUtil.exportEmployeesToCsv(employees, tempFile);
            assertTrue("Export count matches employee count", exported == employees.size(), "Export count mismatch");
            assertTrue("Exported CSV file exists and is non-empty", tempFile.exists() && tempFile.length() > 0, "File is missing or empty");

            // Read first line (header) and verify columns
            try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
                String header = reader.readLine();
                assertTrue("CSV header contains Employee ID", header.contains("Employee ID"), "Header missing Employee ID");
                assertTrue("CSV header contains Full Name", header.contains("Full Name"), "Header missing Full Name");
                assertTrue("CSV header contains Email", header.contains("Email"), "Header missing Email");
            }
        } catch (Exception e) {
            assertTrue("CSV export execution", false, e.getMessage());
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    private static void testLeaveWorkflow() {
        System.out.println("\n--- Testing Leave Workflow & DAO ---");

        LeaveDAO dao = LeaveDAO.getInstance();
        int initialPending = dao.getPendingCount();

        // 1. Submit New Leave Record
        LeaveRecord lr = new LeaveRecord(0, "EMP-1001", "Rajesh Sharma", "Sick Leave", "Dec 01", "1 day", "Doctor checkup", "Pending");
        try {
            boolean added = dao.add(lr);
            assertTrue("Submit leave record", added, "Failed to add leave record");
            assertTrue("Pending count incremented", dao.getPendingCount() == initialPending + 1, "Pending count not updated");
        } catch (Exception e) {
            assertTrue("Submit leave record exception", false, e.getMessage());
        }

        // 2. Approve Leave
        int assignedId = lr.getId();
        boolean approved = dao.updateStatus(assignedId, "Approved", "EMP-1005");
        assertTrue("Approve leave status", approved, "Failed to update status to Approved");

        // Verify status
        List<LeaveRecord> all = dao.getAll();
        boolean verified = false;
        for (LeaveRecord r : all) {
            if (r.getId() == assignedId && "Approved".equalsIgnoreCase(r.getStatus())) {
                verified = true;
                break;
            }
        }
        assertTrue("Verified status is Approved in DAO", verified, "Leave record status not updated in cache");
        assertTrue("Pending count restored after approval", dao.getPendingCount() == initialPending, "Pending count did not decrement");
    }

    private static void testDatabaseConfigUrlParsing() {
        System.out.println("\n--- Testing DatabaseConfig URL Parsing ---");

        DatabaseConfig cfg = DatabaseConfig.getInstance();
        String host = cfg.getHost();
        String port = cfg.getPort();
        String db = cfg.getDatabase();

        assertTrue("Parsed host is non-empty", host != null && !host.isEmpty(), "Host empty");
        assertTrue("Parsed port is 3306 or valid integer", port != null && port.matches("\\d+"), "Port invalid: " + port);
        assertTrue("Parsed database name is non-empty", db != null && !db.isEmpty(), "DB empty");
    }
}
