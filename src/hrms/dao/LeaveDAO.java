package hrms.dao;

import hrms.db.DatabaseManager;
import hrms.model.LeaveRecord;

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
 * Data Access Object for Leave Requests.
 * Supports MySQL queries and local memory fallback.
 */
public class LeaveDAO {

    private static final LeaveDAO INSTANCE = new LeaveDAO();

    private final List<LeaveRecord> leaveCache = new CopyOnWriteArrayList<>();
    private int nextId = 5;

    private LeaveDAO() {
        initDefaultLeaves();
        loadFromDatabaseIfConnected();
    }

    public static LeaveDAO getInstance() {
        return INSTANCE;
    }

    private void initDefaultLeaves() {
        leaveCache.add(new LeaveRecord(1, "EMP-1001", "Rajesh Sharma", "Annual Leave", "Oct 10 - Oct 12", "3 days", "Family vacation", "Approved"));
        leaveCache.add(new LeaveRecord(2, "EMP-1002", "Priya Patel", "Sick Leave", "Oct 15", "1 day", "Medical appointment", "Pending"));
        leaveCache.add(new LeaveRecord(3, "EMP-1003", "Ananya Iyer", "Annual Leave", "Nov 01 - Nov 05", "5 days", "Personal travel", "Rejected"));
        leaveCache.add(new LeaveRecord(4, "EMP-1004", "Vikram Verma", "Casual Leave", "Nov 12", "1 day", "Personal errand", "Approved"));
    }

    public synchronized void loadFromDatabaseIfConnected() {
        if (!DatabaseManager.getInstance().isConnected()) return;

        String sql = "SELECT request_id, employee_id, employee_name, leave_type, dates, duration, reason, status FROM leave_requests ORDER BY request_id ASC";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            List<LeaveRecord> loaded = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("request_id");
                String empId = rs.getString("employee_id");
                String empName = rs.getString("employee_name");
                String type = rs.getString("leave_type");
                String dates = rs.getString("dates");
                String duration = rs.getString("duration");
                String reason = rs.getString("reason");
                String status = rs.getString("status");

                loaded.add(new LeaveRecord(id, empId, empName, type, dates, duration, reason, status));
                if (id >= nextId) nextId = id + 1;
            }

            if (!loaded.isEmpty()) {
                leaveCache.clear();
                leaveCache.addAll(loaded);
                System.out.println("LeaveDAO: Synced " + loaded.size() + " leave records from MySQL.");
            }
        } catch (SQLException e) {
            System.err.println("LeaveDAO: Failed reading from MySQL: " + e.getMessage());
        }
    }

    public List<LeaveRecord> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(leaveCache));
    }

    public int getPendingCount() {
        int count = 0;
        for (LeaveRecord lr : leaveCache) {
            if ("Pending".equalsIgnoreCase(lr.getStatus())) {
                count++;
            }
        }
        return count;
    }

    public boolean add(LeaveRecord record) throws Exception {
        if (record == null) throw new IllegalArgumentException("Leave record cannot be null.");

        if (record.getEmployeeName() == null || record.getEmployeeName().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee name is required.");
        }
        if (record.getDates() == null || record.getDates().trim().isEmpty()) {
            throw new IllegalArgumentException("Leave dates are required.");
        }

        int assignedId = nextId++;
        record.setId(assignedId);

        // MySQL persistence
        if (DatabaseManager.getInstance().isConnected()) {
            String sql = "INSERT INTO leave_requests (request_id, employee_id, employee_name, leave_type, dates, duration, reason, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseManager.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, record.getId());
                ps.setString(2, record.getEmployeeId());
                ps.setString(3, record.getEmployeeName());
                ps.setString(4, record.getLeaveType());
                ps.setString(5, record.getDates());
                ps.setString(6, record.getDuration());
                ps.setString(7, record.getReason() != null ? record.getReason() : "");
                ps.setString(8, record.getStatus() != null ? record.getStatus() : "Pending");
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("LeaveDAO: MySQL Insert error: " + e.getMessage());
            }
        }

        leaveCache.add(record);
        return true;
    }

    public boolean updateStatus(int requestId, String newStatus, String reviewer) {
        if (newStatus == null || newStatus.trim().isEmpty()) return false;

        // MySQL persistence
        if (DatabaseManager.getInstance().isConnected()) {
            String sql = "UPDATE leave_requests SET status = ?, reviewer_id = ? WHERE request_id = ?";
            try (Connection conn = DatabaseManager.getInstance().getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, newStatus.trim());
                ps.setString(2, reviewer != null ? reviewer : "EMP-1005");
                ps.setInt(3, requestId);
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("LeaveDAO: MySQL Update error: " + e.getMessage());
            }
        }

        // Memory cache update
        for (LeaveRecord lr : leaveCache) {
            if (lr.getId() == requestId) {
                lr.setStatus(newStatus.trim());
                lr.setReviewerId(reviewer != null ? reviewer : "EMP-1005");
                return true;
            }
        }
        return false;
    }
}
