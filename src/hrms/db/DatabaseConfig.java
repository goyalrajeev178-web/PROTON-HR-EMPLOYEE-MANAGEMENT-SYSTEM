package hrms.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles database configuration loading, persistence, and field parsing.
 * Supports db.properties and environment variables.
 */
public class DatabaseConfig {

    private static final String CONFIG_FILE = "db.properties";
    private static final DatabaseConfig INSTANCE = new DatabaseConfig();

    private String driver = "com.mysql.cj.jdbc.Driver";
    private String url = "jdbc:mysql://localhost:3306/proton_hr?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true";
    private String user = "root";
    private String password = "password";
    private boolean fallbackInMemory = true;

    private DatabaseConfig() {
        load();
    }

    public static DatabaseConfig getInstance() {
        return INSTANCE;
    }

    public synchronized void load() {
        Properties props = new Properties();
        File file = new File(CONFIG_FILE);

        if (file.exists()) {
            try (InputStream in = new FileInputStream(file)) {
                props.load(in);
            } catch (IOException e) {
                System.err.println("Could not read " + CONFIG_FILE + ": " + e.getMessage());
            }
        }

        if (props.containsKey("db.driver")) driver = props.getProperty("db.driver");
        if (props.containsKey("db.url")) url = props.getProperty("db.url");
        if (props.containsKey("db.user")) user = props.getProperty("db.user");
        if (props.containsKey("db.password")) password = props.getProperty("db.password");
        if (props.containsKey("db.fallback.in_memory")) {
            fallbackInMemory = Boolean.parseBoolean(props.getProperty("db.fallback.in_memory"));
        }

        // Environment variable overrides
        String envUrl = System.getenv("DB_URL");
        if (envUrl != null && !envUrl.trim().isEmpty()) url = envUrl.trim();

        String envUser = System.getenv("DB_USER");
        if (envUser != null && !envUser.trim().isEmpty()) user = envUser.trim();

        String envPass = System.getenv("DB_PASSWORD");
        if (envPass != null) password = envPass;
    }

    public synchronized void saveToFile() {
        Properties props = new Properties();
        props.setProperty("db.driver", driver != null ? driver : "com.mysql.cj.jdbc.Driver");
        props.setProperty("db.url", url != null ? url : "");
        props.setProperty("db.user", user != null ? user : "root");
        props.setProperty("db.password", password != null ? password : "");
        props.setProperty("db.fallback.in_memory", String.valueOf(fallbackInMemory));

        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "Proton HR Database Configuration - Updated via Application Settings");
            System.out.println("DatabaseConfig: Saved updated credentials to " + CONFIG_FILE);
        } catch (IOException e) {
            System.err.println("DatabaseConfig: Failed saving configuration: " + e.getMessage());
        }
    }

    public synchronized void updateCredentials(String newUrl, String newUser, String newPassword) throws IOException {
        if (newUrl != null && !newUrl.trim().isEmpty()) this.url = newUrl.trim();
        if (newUser != null && !newUser.trim().isEmpty()) this.user = newUser.trim();
        if (newPassword != null) this.password = newPassword;
        saveToFile();
    }

    public String getHost() {
        try {
            Matcher m = Pattern.compile("jdbc:mysql://([^:/]+)").matcher(url);
            if (m.find()) return m.group(1);
        } catch (Exception ignored) {}
        return "localhost";
    }

    public String getPort() {
        try {
            Matcher m = Pattern.compile("jdbc:mysql://[^:/]+:(\\d+)").matcher(url);
            if (m.find()) return m.group(1);
        } catch (Exception ignored) {}
        return "3306";
    }

    public String getDatabase() {
        try {
            Matcher m = Pattern.compile("jdbc:mysql://[^/]+/([^?]+)").matcher(url);
            if (m.find()) return m.group(1);
        } catch (Exception ignored) {}
        return "proton_hr";
    }

    public void setHost(String host) {
        rebuildUrl(host, getPort(), getDatabase());
    }

    public void setPort(String port) {
        rebuildUrl(getHost(), port, getDatabase());
    }

    public void setDatabase(String db) {
        rebuildUrl(getHost(), getPort(), db);
    }

    private void rebuildUrl(String host, String port, String db) {
        this.url = "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true";
    }

    public String getDriver() { return driver; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public boolean isFallbackInMemory() { return fallbackInMemory; }
    public void setFallbackInMemory(boolean fallback) { this.fallbackInMemory = fallback; }
}
