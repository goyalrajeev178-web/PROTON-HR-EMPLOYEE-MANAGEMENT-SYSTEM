package hrms.util;

import hrms.model.Employee;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Utility for exporting data to industry-standard CSV format.
 */
public class CsvExportUtil {

    private static final String[] EMPLOYEE_HEADERS = {
        "Employee ID", "Full Name", "Job Title", "Department", "Location",
        "Email", "Phone", "Join Date", "Status", "Salary", "Emergency Contact"
    };

    /**
     * Exports a list of employees to a target CSV file.
     * @param employees List of Employee records
     * @param targetFile Target file to write to
     * @return count of rows exported
     * @throws IOException if writing fails
     */
    public static int exportEmployeesToCsv(List<Employee> employees, File targetFile) throws IOException {
        if (targetFile == null) {
            throw new IllegalArgumentException("Target file cannot be null.");
        }

        File parent = targetFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        int count = 0;
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(targetFile), StandardCharsets.UTF_8))) {

            // UTF-8 BOM for Microsoft Excel compatibility
            writer.write('\ufeff');

            // Header row
            writeRow(writer, EMPLOYEE_HEADERS);

            // Data rows
            if (employees != null) {
                for (Employee emp : employees) {
                    String[] row = new String[]{
                        emp.getId(),
                        emp.getName(),
                        emp.getRole(),
                        emp.getDepartment(),
                        emp.getLocation(),
                        emp.getEmail(),
                        emp.getPhone(),
                        emp.getJoinDate(),
                        emp.getStatus(),
                        emp.getSalary(),
                        emp.getEmergency()
                    };
                    writeRow(writer, row);
                    count++;
                }
            }
            writer.flush();
        }
        return count;
    }

    /**
     * Generates a default timestamped file name for export.
     */
    public static String getDefaultExportFileName() {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return "ProtonHR_Employees_" + timestamp + ".csv";
    }

    private static void writeRow(BufferedWriter writer, String[] values) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(escapeCsvField(values[i]));
        }
        sb.append("\r\n");
        writer.write(sb.toString());
    }

    private static String escapeCsvField(String field) {
        if (field == null) return "\"\"";
        String s = field.trim();
        boolean needsQuotes = s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
        if (s.contains("\"")) {
            s = s.replace("\"", "\"\"");
        }
        return needsQuotes ? "\"" + s + "\"" : "\"" + s + "\"";
    }
}
