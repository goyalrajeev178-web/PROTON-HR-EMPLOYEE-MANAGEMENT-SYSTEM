package hrms.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages database lifecycle, JDBC connection pooling/creation, schema migrations, and diagnostics.
 */
public class DatabaseManager {

    private static final DatabaseManager INSTANCE = new DatabaseManager();

    private boolean connected = false;
    private String statusMessage = "Not connected";
    private boolean initialized = false;

    private DatabaseManager() {
        tryConnectAndInitialize();
    }

    public static DatabaseManager getInstance() {
        return INSTANCE;
    }

    public synchronized void tryConnectAndInitialize() {
        DatabaseConfig config = DatabaseConfig.getInstance();
        config.load();

        try {
            Class.forName(config.getDriver());
        } catch (ClassNotFoundException e) {
            connected = false;
            statusMessage = "MySQL JDBC Driver not found on classpath.";
            System.err.println(statusMessage);
            return;
        }

        try (Connection conn = DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword())) {
            connected = true;
            statusMessage = "Connected to MySQL (" + extractHostAndDb(config.getUrl()) + ")";
            System.out.println("DatabaseManager: Successfully connected to MySQL database.");

            // Initialize schema if needed
            initializeSchema(conn);
            seedInitialDataIfEmpty(conn);
            initialized = true;
        } catch (SQLException e) {
            connected = false;
            statusMessage = "MySQL offline or credentials rejected: " + e.getMessage();
            System.err.println("DatabaseManager: Notice - " + statusMessage);
            System.err.println("DatabaseManager: Running in resilient in-memory mode with full functionality.");
        }
    }

    public synchronized void initialize() {
        tryConnectAndInitialize();
    }

    public Connection getConnection() throws SQLException {
        DatabaseConfig config = DatabaseConfig.getInstance();
        return DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
    }

    public boolean testConnection(String url, String user, String pass) {
        try {
            Class.forName(DatabaseConfig.getInstance().getDriver());
            try (Connection conn = DriverManager.getConnection(url, user, pass)) {
                return conn.isValid(3);
            }
        } catch (Exception e) {
            System.err.println("DatabaseManager: Test connection failed: " + e.getMessage());
            return false;
        }
    }

    private void initializeSchema(Connection conn) {
        String[] ddlStatements = {
            "CREATE TABLE IF NOT EXISTS departments (" +
            "  department_id INT AUTO_INCREMENT PRIMARY KEY," +
            "  department_name VARCHAR(100) NOT NULL UNIQUE," +
            "  location VARCHAR(100) NOT NULL," +
            "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")",

            "CREATE TABLE IF NOT EXISTS roles (" +
            "  role_id INT AUTO_INCREMENT PRIMARY KEY," +
            "  title VARCHAR(100) NOT NULL," +
            "  department_id INT NOT NULL," +
            "  salary_min DECIMAL(12, 2)," +
            "  salary_max DECIMAL(12, 2)" +
            ")",

            "CREATE TABLE IF NOT EXISTS employees (" +
            "  employee_id VARCHAR(20) PRIMARY KEY," +
            "  full_name VARCHAR(150) NOT NULL," +
            "  email VARCHAR(150) NOT NULL UNIQUE," +
            "  password_hash VARCHAR(255) NOT NULL DEFAULT 'password123'," +
            "  phone VARCHAR(30) NOT NULL," +
            "  dob DATE," +
            "  age INT," +
            "  gender VARCHAR(20)," +
            "  address TEXT," +
            "  department_name VARCHAR(100)," +
            "  role_title VARCHAR(100)," +
            "  manager_id VARCHAR(20)," +
            "  location VARCHAR(100) NOT NULL DEFAULT 'Bengaluru, KA'," +
            "  status VARCHAR(30) DEFAULT 'Active'," +
            "  hire_date VARCHAR(50) NOT NULL," +
            "  salary VARCHAR(50) NOT NULL," +
            "  bank_account VARCHAR(50)," +
            "  emergency_contact VARCHAR(255)," +
            "  avatar_color_start INT DEFAULT 0," +
            "  avatar_color_end INT DEFAULT 0," +
            "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
            ")",

            "CREATE TABLE IF NOT EXISTS leave_requests (" +
            "  request_id INT AUTO_INCREMENT PRIMARY KEY," +
            "  employee_id VARCHAR(20) NOT NULL," +
            "  employee_name VARCHAR(150) NOT NULL," +
            "  leave_type VARCHAR(50) NOT NULL," +
            "  dates VARCHAR(100) NOT NULL," +
            "  duration VARCHAR(50) NOT NULL," +
            "  reason TEXT," +
            "  status VARCHAR(30) DEFAULT 'Pending'," +
            "  reviewer_id VARCHAR(20)," +
            "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")",

            "CREATE TABLE IF NOT EXISTS system_settings (" +
            "  setting_key VARCHAR(100) PRIMARY KEY," +
            "  setting_value VARCHAR(255) NOT NULL," +
            "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
            ")"
        };

        try (Statement stmt = conn.createStatement()) {
            for (String sql : ddlStatements) {
                stmt.execute(sql);
            }
        } catch (SQLException e) {
            System.err.println("DatabaseManager: Schema initialization warning: " + e.getMessage());
        }
    }

    private void seedInitialDataIfEmpty(Connection conn) {
        String checkSql = "SELECT COUNT(*) FROM employees";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("DatabaseManager: Seeding initial employee records into MySQL...");
                String insertSql = "INSERT INTO employees (employee_id, full_name, email, phone, location, department_name, role_title, status, hire_date, salary, emergency_contact) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    Object[][] initialEmps = {
                        {"EMP-1001", "Rajesh Sharma", "rajesh.sharma@gmail.com", "+91 98765 43210", "Bengaluru, KA", "Engineering", "Senior Software Engineer", "Active", "Mar 15, 2021", "₹18,00,000", "Sunita Sharma (Spouse) - 98765-43210"},
                        {"EMP-1002", "Priya Patel", "priya.patel@gmail.com", "+91 98765 12345", "Mumbai, MH", "Human Resources", "HR Business Partner", "Active", "Jan 10, 2020", "₹14,50,000", "Amit Patel - 98765-54321"},
                        {"EMP-1003", "Ananya Iyer", "ananya.iyer@gmail.com", "+91 98765 23456", "Bengaluru, KA", "Design", "Product Designer", "Active", "Jun 01, 2022", "₹16,00,000", "Suresh Iyer - 98765-67890"},
                        {"EMP-1004", "Vikram Verma", "v.verma@gmail.com", "+91 98765 34567", "Delhi NCR", "Sales", "Regional Sales Director", "Active", "Nov 12, 2018", "₹22,00,000", "Pooja Verma - 98765-99999"},
                        {"EMP-1005", "Kavita Rao", "kavita.rao@gmail.com", "+91 98765 45678", "Bengaluru, KA", "Engineering", "VP of Engineering", "Active", "Feb 20, 2019", "₹35,00,000", "Rohan Rao - 98765-00000"},
                        {"EMP-1006", "Arjun Kapoor", "a.kapoor@gmail.com", "+91 98765 56789", "Mumbai, MH", "Finance", "Financial Analyst", "Remote", "Sep 15, 2022", "₹13,50,000", "Neha Kapoor - 98765-11223"},
                        {"EMP-1007", "Deepika Singh", "d.singh@gmail.com", "+91 98765 67890", "Hyderabad, TS", "Marketing", "Marketing Lead", "Active", "Apr 05, 2023", "₹15,00,000", "Ranveer Singh - 98765-33445"},
                        {"EMP-1008", "Aarav Mehta", "aarav.m@gmail.com", "+91 98765 78901", "Pune, MH", "Engineering", "DevOps Engineer", "On Leave", "Aug 18, 2021", "₹17,50,000", "Sneha Mehta - 98765-55667"},
                        {"EMP-1009", "Neha Gupta", "neha.g@gmail.com", "+91 98765 89012", "Delhi NCR", "Human Resources", "Talent Acquisition", "Remote", "May 22, 2022", "₹11,00,000", "Rohan Gupta - 98765-77889"},
                        {"EMP-1010", "Siddharth Joshi", "s.joshi@gmail.com", "+91 98765 90123", "Pune, MH", "Marketing", "Content Strategist", "Active", "Oct 01, 2023", "₹12,50,000", "Ritu Joshi - 98765-99001"},
                        {"EMP-1011", "Pooja Reddy", "p.reddy@gmail.com", "+91 98765 01234", "Hyderabad, TS", "Operations", "Operations Specialist", "Active", "Jan 15, 2024", "₹11,50,000", "Karthik Reddy - 98765-22334"},
                        {"EMP-1012", "Rohan Malhotra", "r.malhotra@gmail.com", "+91 98765 12340", "Remote", "Engineering", "Security Consultant", "Remote", "Dec 01, 2021", "₹19,00,000", "Simran Malhotra - 98765-44556"}
                    };
                    for (Object[] row : initialEmps) {
                        for (int i = 0; i < row.length; i++) {
                            ps.setObject(i + 1, row[i]);
                        }
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                // Seed Leave Requests
                String leaveSql = "INSERT INTO leave_requests (employee_id, employee_name, leave_type, dates, duration, reason, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement psLeave = conn.prepareStatement(leaveSql)) {
                    Object[][] initialLeaves = {
                        {"EMP-1001", "Rajesh Sharma", "Annual Leave", "Oct 10 - Oct 12", "3 days", "Family vacation", "Approved"},
                        {"EMP-1002", "Priya Patel", "Sick Leave", "Oct 15", "1 day", "Medical appointment", "Pending"},
                        {"EMP-1003", "Ananya Iyer", "Annual Leave", "Nov 01 - Nov 05", "5 days", "Personal travel", "Rejected"},
                        {"EMP-1004", "Vikram Verma", "Casual Leave", "Nov 12", "1 day", "Personal errand", "Approved"}
                    };
                    for (Object[] row : initialLeaves) {
                        for (int i = 0; i < row.length; i++) {
                            psLeave.setObject(i + 1, row[i]);
                        }
                        psLeave.addBatch();
                    }
                    psLeave.executeBatch();
                }
            }
        } catch (SQLException e) {
            System.err.println("DatabaseManager: Seed data check failed: " + e.getMessage());
        }
    }

    private String extractHostAndDb(String jdbcUrl) {
        if (jdbcUrl == null) return "unknown";
        try {
            int doubleSlash = jdbcUrl.indexOf("//");
            if (doubleSlash != -1) {
                int questionMark = jdbcUrl.indexOf('?', doubleSlash);
                if (questionMark != -1) {
                    return jdbcUrl.substring(doubleSlash + 2, questionMark);
                }
                return jdbcUrl.substring(doubleSlash + 2);
            }
        } catch (Exception ignored) {}
        return "MySQL";
    }

    public boolean isConnected() {
        return connected;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public static void closeQuietly(AutoCloseable... closeables) {
        if (closeables == null) return;
        for (AutoCloseable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ignored) {}
            }
        }
    }
}
