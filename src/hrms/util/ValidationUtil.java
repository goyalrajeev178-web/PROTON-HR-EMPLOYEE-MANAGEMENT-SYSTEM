package hrms.util;

import java.util.regex.Pattern;

/**
 * Enterprise validation utility for HRMS forms and operations.
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    private static final String REQUIRED_EMAIL_DOMAIN = "@gmail.com";

    public static class Result {
        private final boolean valid;
        private final String message;

        public Result(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }

        public static Result ok() {
            return new Result(true, null);
        }

        public static Result error(String message) {
            return new Result(false, message);
        }
    }

    /**
     * Validates employee email according to system business rules.
     * Enforces valid email syntax AND @gmail.com domain constraint.
     */
    public static Result validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Result.error("Email address cannot be empty.");
        }
        String trimmed = email.trim();
        if (!trimmed.toLowerCase().endsWith(REQUIRED_EMAIL_DOMAIN)) {
            return Result.error("Email must end with " + REQUIRED_EMAIL_DOMAIN + " (e.g. employee" + REQUIRED_EMAIL_DOMAIN + ")");
        }
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            return Result.error("Invalid email address syntax.");
        }
        return Result.ok();
    }

    public static boolean isValidGmail(String email) {
        return validateEmail(email).isValid();
    }

    /**
     * Validates employee full name.
     */
    public static Result validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Result.error("Employee full name is required.");
        }
        String trimmed = name.trim();
        if (trimmed.length() < 2) {
            return Result.error("Name must contain at least 2 characters.");
        }
        if (trimmed.length() > 100) {
            return Result.error("Name cannot exceed 100 characters.");
        }
        if (!trimmed.matches("^[\\p{L} .'-]+$")) {
            return Result.error("Name contains invalid characters.");
        }
        return Result.ok();
    }

    /**
     * Validates phone number.
     */
    public static Result validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return Result.error("Contact phone number is required.");
        }
        String trimmed = phone.trim();
        String digitsOnly = trimmed.replaceAll("[^0-9]", "");
        if (digitsOnly.length() < 7 || digitsOnly.length() > 15) {
            return Result.error("Phone number must contain between 7 and 15 digits.");
        }
        return Result.ok();
    }

    /**
     * Validates compensation / salary input.
     */
    public static Result validateSalary(String salary) {
        if (salary == null || salary.trim().isEmpty()) {
            return Result.error("Base salary is required.");
        }
        String cleaned = salary.replaceAll("[^0-9.]", "");
        if (cleaned.isEmpty()) {
            return Result.error("Salary must contain a valid numeric amount.");
        }
        try {
            double amount = Double.parseDouble(cleaned);
            if (amount <= 0) {
                return Result.error("Salary must be a positive number.");
            }
        } catch (NumberFormatException e) {
            return Result.error("Invalid numeric salary format.");
        }
        return Result.ok();
    }

    /**
     * Validates employee ID.
     */
    public static Result validateEmployeeId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Result.error("Employee ID is required.");
        }
        String trimmed = id.trim();
        if (trimmed.length() < 3 || trimmed.length() > 20) {
            return Result.error("Employee ID length must be between 3 and 20 characters.");
        }
        return Result.ok();
    }
}
