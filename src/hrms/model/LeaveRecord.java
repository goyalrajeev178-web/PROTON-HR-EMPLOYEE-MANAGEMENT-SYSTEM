package hrms.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Domain model representing a Leave Application in the HRMS.
 */
public class LeaveRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String employeeId;
    private String employeeName;
    private String leaveType;
    private String dates;
    private String duration;
    private String reason;
    private String status;
    private String reviewerId;
    private String reviewedAt;

    public LeaveRecord() {
        this.status = "Pending";
        this.leaveType = "Annual Leave";
    }

    public LeaveRecord(int id, String employeeId, String employeeName, String leaveType,
                       String dates, String duration, String reason, String status) {
        this.id = id;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.leaveType = leaveType;
        this.dates = dates;
        this.duration = duration;
        this.reason = reason;
        this.status = status != null ? status : "Pending";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getLeaveType() { return leaveType; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }

    public String getDates() { return dates; }
    public void setDates(String dates) { this.dates = dates; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReviewerId() { return reviewerId; }
    public void setReviewerId(String reviewerId) { this.reviewerId = reviewerId; }

    public String getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(String reviewedAt) { this.reviewedAt = reviewedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LeaveRecord that = (LeaveRecord) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
