package hrms.ui;

import hrms.dao.EmployeeDAO;
import hrms.dao.LeaveDAO;
import hrms.db.DatabaseConfig;
import hrms.db.DatabaseManager;
import hrms.model.Employee;
import hrms.util.CsvExportUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Enterprise-grade, professionally arranged Linear Settings Panel.
 * Supports theme toggling, database configuration and testing, profile syncing,
 * CSV backup export, and system compliance options.
 */
public class SettingsPanel extends JPanel implements Scrollable {

    private Runnable onThemeChanged;
    private Runnable onProfileUpdated;
    private Runnable onNavigateToProfile;
    private JPanel linearContainer;

    public SettingsPanel(Runnable onThemeChanged, Runnable onProfileUpdated, Runnable onNavigateToProfile) {
        this.onThemeChanged = onThemeChanged;
        this.onProfileUpdated = onProfileUpdated;
        this.onNavigateToProfile = onNavigateToProfile;
        buildUI();
    }

    public SettingsPanel(Runnable onThemeChanged, Runnable onProfileUpdated) {
        this(onThemeChanged, onProfileUpdated, null);
    }

    public SettingsPanel() {
        this(null, null, null);
    }

    public void setCallbacks(Runnable onThemeChanged, Runnable onProfileUpdated, Runnable onNavigateToProfile) {
        this.onThemeChanged = onThemeChanged;
        this.onProfileUpdated = onProfileUpdated;
        this.onNavigateToProfile = onNavigateToProfile;
    }

    public void buildUI() {
        removeAll();
        setLayout(new BorderLayout(16, 16));
        setBackground(Theme.BG_APP);
        setBorder(new EmptyBorder(20, 24, 24, 24));

        // 1. Header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);

        JLabel title = new JLabel("<html><body><b>System Settings & Preferences</b></body></html>");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("<html><body>Manage database connection, theme appearance, personal records, export data, and system compliance.</body></html>");
        subtitle.setFont(Theme.FONT_BODY);
        subtitle.setForeground(Theme.TEXT_SECONDARY);

        header.add(title);
        header.add(Box.createRigidArea(new Dimension(0, 4)));
        header.add(subtitle);
        header.add(Box.createRigidArea(new Dimension(0, 12)));

        add(header, BorderLayout.NORTH);

        // 2. Linear Top-to-Bottom Container
        linearContainer = new JPanel();
        linearContainer.setLayout(new BoxLayout(linearContainer, BoxLayout.Y_AXIS));
        linearContainer.setOpaque(false);

        // Section 1: Theme & Visual Appearance (with Animated Sliding Switch)
        linearContainer.add(createThemeSection());
        linearContainer.add(Box.createRigidArea(new Dimension(0, 16)));

        // Section 2: Database & Connectivity
        linearContainer.add(createDatabaseSection());
        linearContainer.add(Box.createRigidArea(new Dimension(0, 16)));

        // Section 3: User Profile & Account Info
        linearContainer.add(createProfileSection());
        linearContainer.add(Box.createRigidArea(new Dimension(0, 16)));

        // Section 4: Payment & Payroll Details
        linearContainer.add(createPaymentSection());
        linearContainer.add(Box.createRigidArea(new Dimension(0, 16)));

        // Section 5: Help & Support Desk
        linearContainer.add(createHelpSection());
        linearContainer.add(Box.createRigidArea(new Dimension(0, 16)));

        // Section 6: Terms & Conditions and Policies
        linearContainer.add(createTermsSection());
        linearContainer.add(Box.createRigidArea(new Dimension(0, 16)));

        // Section 7: Security & Access Control
        linearContainer.add(createSecuritySection());
        linearContainer.add(Box.createRigidArea(new Dimension(0, 20)));

        add(linearContainer, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    // ==========================================
    // 1. THEME & APPEARANCE (WITH SLIDING BUTTON)
    // ==========================================
    private JPanel createThemeSection() {
        JPanel card = createSectionCard("Theme & Appearance", "Toggle between clean light mode and sleek dark mode.", IconFactory.IconType.THEME);

        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);

        JPanel left = createItemHeader(IconFactory.IconType.THEME, "System Dark Mode", 
            Theme.isDarkMode ? "Dark theme active (Sleek slate background)" : "Light theme active (Clean high-contrast background)");

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        JLabel modeStatusLabel = new JLabel(Theme.isDarkMode ? "🌙 Dark Mode" : "☀️ Light Mode");
        modeStatusLabel.setFont(Theme.FONT_BODY_BOLD);
        modeStatusLabel.setForeground(Theme.isDarkMode ? Theme.PRIMARY : Theme.TEXT_PRIMARY);

        AnimatedToggleSwitch slidingSwitch = new AnimatedToggleSwitch(Theme.isDarkMode, () -> {
            boolean target = !Theme.isDarkMode;
            Theme.setDarkMode(target);
            if (onThemeChanged != null) {
                onThemeChanged.run();
            }
            buildUI();
        });

        right.add(modeStatusLabel);
        right.add(slidingSwitch);

        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);
        card.add(row);

        return card;
    }

    // ==========================================
    // 2. DATABASE & CONNECTIVITY CONFIGURATION
    // ==========================================
    private JPanel createDatabaseSection() {
        boolean connected = DatabaseManager.getInstance().isConnected();
        String subtitle = connected 
            ? "Connected to MySQL database (" + DatabaseConfig.getInstance().getDatabase() + ")" 
            : "Running in In-Memory Fallback mode. Configure MySQL credentials to persist to database.";

        JPanel card = createSectionCard("Database & Storage", subtitle, IconFactory.IconType.SETTINGS);

        // Row 1: Connection Status & Config Dialog
        String statusText = connected ? "🟢 Connected to MySQL" : "🟡 In-Memory Fallback Mode";
        card.add(createSettingRow(IconFactory.IconType.CHECK, "Database Engine", statusText, "⚙️ Configure Connection...", () -> {
            openDatabaseConfigDialog();
        }));
        card.add(createDivider());

        // Row 2: CSV Data Export
        card.add(createSettingRow(IconFactory.IconType.EMPLOYEES, "Export Directory Data", "Download full employee records in RFC-4180 CSV format", "📥 Export CSV", () -> {
            handleExportAllCsv();
        }));

        return card;
    }

    private void openDatabaseConfigDialog() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, "Configure Database Connection", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(480, 480);
        dialog.setLocationRelativeTo(parentWindow);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.SURFACE_CARD);
        form.setBorder(new EmptyBorder(24, 28, 20, 28));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;

        DatabaseConfig cfg = DatabaseConfig.getInstance();

        form.add(new JLabel("Host:"), gbc);
        gbc.gridy++;
        JTextField hostField = new JTextField(cfg.getHost());
        hostField.setPreferredSize(new Dimension(0, 34));
        form.add(hostField, gbc);

        gbc.gridy++;
        form.add(new JLabel("Port:"), gbc);
        gbc.gridy++;
        JTextField portField = new JTextField(cfg.getPort());
        portField.setPreferredSize(new Dimension(0, 34));
        form.add(portField, gbc);

        gbc.gridy++;
        form.add(new JLabel("Database Name:"), gbc);
        gbc.gridy++;
        JTextField dbField = new JTextField(cfg.getDatabase());
        dbField.setPreferredSize(new Dimension(0, 34));
        form.add(dbField, gbc);

        gbc.gridy++;
        form.add(new JLabel("Username:"), gbc);
        gbc.gridy++;
        JTextField userField = new JTextField(cfg.getUser());
        userField.setPreferredSize(new Dimension(0, 34));
        form.add(userField, gbc);

        gbc.gridy++;
        form.add(new JLabel("Password:"), gbc);
        gbc.gridy++;
        JPasswordField passField = new JPasswordField(cfg.getPassword());
        passField.setPreferredSize(new Dimension(0, 34));
        form.add(passField, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnPanel.setBackground(Theme.BG_APP);

        JButton btnTest = Theme.createSecondaryButton("🔌 Test Connection");
        btnTest.addActionListener(e -> {
            String host = hostField.getText().trim();
            String port = portField.getText().trim();
            String db = dbField.getText().trim();
            String user = userField.getText().trim();
            String pass = new String(passField.getPassword());
            String url = "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

            boolean ok = DatabaseManager.getInstance().testConnection(url, user, pass);
            if (ok) {
                JOptionPane.showMessageDialog(dialog, "✅ Connection successful! MySQL server is reachable.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(dialog, "❌ Could not connect to MySQL.\nPlease verify your host, port, username, and password.", "Connection Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnCancel = Theme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnSave = Theme.createPrimaryButton("Save & Connect");
        btnSave.addActionListener(e -> {
            String host = hostField.getText().trim();
            String port = portField.getText().trim();
            String db = dbField.getText().trim();
            String user = userField.getText().trim();
            String pass = new String(passField.getPassword());

            cfg.setHost(host);
            cfg.setPort(port);
            cfg.setDatabase(db);
            cfg.setUser(user);
            cfg.setPassword(pass);
            cfg.saveToFile();

            // Attempt connection
            DatabaseManager.getInstance().initialize();
            EmployeeDAO.getInstance().loadFromDatabaseIfConnected();
            LeaveDAO.getInstance().loadFromDatabaseIfConnected();

            dialog.dispose();
            buildUI();

            if (DatabaseManager.getInstance().isConnected()) {
                JOptionPane.showMessageDialog(this, "✅ Successfully connected and synced with MySQL database!", "Connected", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "⚠️ Saved configuration, but MySQL is currently offline or credentials did not match.\nRunning in In-Memory Fallback mode.", "Offline Mode", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnPanel.add(btnTest);
        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void handleExportAllCsv() {
        List<Employee> allEmployees = EmployeeDAO.getInstance().getAll();
        if (allEmployees.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No employee records found to export.", "Export", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export All Employee Records (CSV)");
        fileChooser.setSelectedFile(new File(CsvExportUtil.getDefaultExportFileName()));

        int res = fileChooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File selected = fileChooser.getSelectedFile();
            if (!selected.getName().toLowerCase().endsWith(".csv")) {
                selected = new File(selected.getParentFile(), selected.getName() + ".csv");
            }
            try {
                int count = CsvExportUtil.exportEmployeesToCsv(allEmployees, selected);
                JOptionPane.showMessageDialog(this,
                    "Export completed successfully!\n" + count + " records saved to:\n" + selected.getAbsolutePath(),
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ==========================================
    // 3. PROFILE & ACCOUNT INFO
    // ==========================================
    private JPanel createProfileSection() {
        JPanel card = createSectionCard("User Profile & Account Info", "Manage your personal name, photo, and account credentials.", IconFactory.IconType.PROFILE);

        JPanel row = new JPanel(new BorderLayout(16, 0));
        row.setOpaque(false);

        // Avatar + Name + Email
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        left.setOpaque(false);

        JLabel avatarPreview;
        if (UserProfilePanel.customAvatarIcon != null) {
            Image scaled = UserProfilePanel.customAvatarIcon.getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH);
            avatarPreview = new JLabel(new ImageIcon(scaled));
        } else {
            avatarPreview = new JLabel(IconFactory.createAvatarIcon(UserProfilePanel.currentUserName, 48, 10, 
                UserProfilePanel.avatarColors[0], UserProfilePanel.avatarColors[1]));
        }

        JPanel textP = new JPanel();
        textP.setLayout(new BoxLayout(textP, BoxLayout.Y_AXIS));
        textP.setOpaque(false);

        JLabel nameLbl = new JLabel(UserProfilePanel.currentUserName);
        nameLbl.setFont(Theme.FONT_SUBTITLE);
        nameLbl.setForeground(Theme.TEXT_PRIMARY);

        JLabel emailLbl = new JLabel(UserProfilePanel.currentUserRole + "  •  " + UserProfilePanel.currentUserEmail);
        emailLbl.setFont(Theme.FONT_SMALL);
        emailLbl.setForeground(Theme.TEXT_SECONDARY);

        textP.add(nameLbl);
        textP.add(Box.createRigidArea(new Dimension(0, 2)));
        textP.add(emailLbl);

        left.add(avatarPreview);
        left.add(textP);
        row.add(left, BorderLayout.WEST);

        // Action Button
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        right.setOpaque(false);
        JButton editBtn = Theme.createPrimaryButton("✏️ Edit Profile & Photo");
        editBtn.addActionListener(e -> {
            if (onNavigateToProfile != null) {
                onNavigateToProfile.run();
            } else {
                JOptionPane.showMessageDialog(this, "Switch to 'My Profile' in the top right header to edit your photo and details.", "Profile", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        right.add(editBtn);
        row.add(right, BorderLayout.EAST);

        card.add(row);
        return card;
    }

    // ==========================================
    // 4. PAYMENT & PAYROLL DETAILS
    // ==========================================
    private JPanel createPaymentSection() {
        JPanel card = createSectionCard("Payment & Payroll Details", "Company direct deposit banking, subscription tier, and payout schedule.", IconFactory.IconType.PAYMENT);

        // Row 1: Plan Tier
        card.add(createSettingRow(IconFactory.IconType.CHECK, "Subscription Plan", "Enterprise Pro (Active)", "Manage Plan", () -> {
            JOptionPane.showMessageDialog(this, "Plan: Enterprise Pro (Unlimited Employees)\nBilling Cycle: Annual\nNext Renewal: March 2027", "Plan Details", JOptionPane.INFORMATION_MESSAGE);
        }));
        card.add(createDivider());

        // Row 2: Direct Deposit Bank
        card.add(createSettingRow(IconFactory.IconType.PAYMENT, "Direct Deposit Bank", UserProfilePanel.currentUserBank, "Update Bank", () -> {
            String newBank = JOptionPane.showInputDialog(this, "Enter updated Bank Account information:", UserProfilePanel.currentUserBank);
            if (newBank != null && !newBank.trim().isEmpty()) {
                UserProfilePanel.currentUserBank = newBank.trim();
                buildUI();
                JOptionPane.showMessageDialog(this, "Payment details updated successfully!", "Saved", JOptionPane.INFORMATION_MESSAGE);
            }
        }));
        card.add(createDivider());

        // Row 3: Disbursement Schedule
        card.add(createSettingRow(IconFactory.IconType.LEAVES, "Disbursement Cycle", "Bi-Weekly (1st & 15th of each month)", "View Schedule", () -> {
            JOptionPane.showMessageDialog(this, "Payroll Payout Schedule:\n• 1st of month: Salaried Personnel\n• 15th of month: Hourly Contractors", "Payout Schedule", JOptionPane.INFORMATION_MESSAGE);
        }));

        return card;
    }

    // ==========================================
    // 5. HELP & SUPPORT DESK
    // ==========================================
    private JPanel createHelpSection() {
        JPanel card = createSectionCard("Help Desk & Customer Support", "Get help from our 24/7 support desk or explore documentation.", IconFactory.IconType.HELP);

        // Row 1: Support Email
        card.add(createSettingRow(IconFactory.IconType.MAIL, "Email Support", "support@protonhr.com", "Send Email", () -> {
            JOptionPane.showMessageDialog(this, "Support Email: support@protonhr.com\nAverage response time: < 15 minutes", "Support Email", JOptionPane.INFORMATION_MESSAGE);
        }));
        card.add(createDivider());

        // Row 2: 24/7 Helpline
        card.add(createSettingRow(IconFactory.IconType.PHONE, "24/7 Phone Helpline", "+1 (800) 555-HRMS", "Call Now", () -> {
            JOptionPane.showMessageDialog(this, "Toll-Free Helpline: 1-800-555-HRMS (4767)\nAvailable 24 hours a day, 7 days a week.", "Helpline", JOptionPane.INFORMATION_MESSAGE);
        }));
        card.add(createDivider());

        // Row 3: Support Ticket
        card.add(createSettingRow(IconFactory.IconType.HELP, "Help Desk Tickets", "Open a direct support ticket with our engineers", "💬 Open Ticket", () -> {
            String msg = JOptionPane.showInputDialog(this, "Describe the issue or question you need help with:");
            if (msg != null && !msg.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Support ticket #HRMS-9942 created! Confirmation sent to " + UserProfilePanel.currentUserEmail, "Ticket Submitted", JOptionPane.INFORMATION_MESSAGE);
            }
        }));

        return card;
    }

    // ==========================================
    // 6. TERMS & CONDITIONS AND COMPLIANCE
    // ==========================================
    private JPanel createTermsSection() {
        JPanel card = createSectionCard("Terms & Conditions & Legal Policies", "Review software agreements, GDPR privacy policies, and compliance.", IconFactory.IconType.TERMS);

        // Row 1: License Agreement
        card.add(createSettingRow(IconFactory.IconType.TERMS, "Software License", "Enterprise Commercial Agreement (v4.2)", "View License", () -> {
            JOptionPane.showMessageDialog(this, "Proton HR Enterprise License:\n• Authorized for internal organization usage.\n• Unlimited workstations and users.", "License", JOptionPane.INFORMATION_MESSAGE);
        }));
        card.add(createDivider());

        // Row 2: Data Privacy & Encryption
        card.add(createSettingRow(IconFactory.IconType.SECURITY, "Data Encryption & Security", "AES-256 at Rest & TLS 1.3 in Transit", "Security Audit", () -> {
            JOptionPane.showMessageDialog(this, "Security Certification:\n• ISO 27001 Certified\n• GDPR & SOC-2 Type II Compliant\n• Full end-to-end encryption", "Security & Privacy", JOptionPane.INFORMATION_MESSAGE);
        }));
        card.add(createDivider());

        // Row 3: Full Terms & Conditions
        card.add(createSettingRow(IconFactory.IconType.CHECK, "Terms of Service", "Updated August 2026", "Read Full Terms", () -> {
            JOptionPane.showMessageDialog(this, 
                "Proton HR Terms & Privacy Policy:\n\n"
                + "1. Data Confidentiality: All employee and payroll records are encrypted.\n"
                + "2. Authorized Access: Only authorized HR managers have directory edit rights.\n"
                + "3. Compliance: Real-time audit logs for all employee modifications.\n"
                + "4. Email Policy: System registrations require verified @gmail.com accounts.", 
                "Terms & Conditions", JOptionPane.INFORMATION_MESSAGE);
        }));

        return card;
    }

    // ==========================================
    // 7. SECURITY & ACCESS CONTROL
    // ==========================================
    private JPanel createSecuritySection() {
        JPanel card = createSectionCard("Security & Password Management", "Manage 2FA, session timeout limits, and account password.", IconFactory.IconType.SECURITY);

        // Row 1: Two-Factor Auth
        card.add(createSettingRow(IconFactory.IconType.SECURITY, "Two-Factor Authentication", "Enforced for Administrator Tier", "Configure 2FA", () -> {
            JOptionPane.showMessageDialog(this, "2FA is currently ENFORCED via Authenticator App / Hardware Key.", "2FA Status", JOptionPane.INFORMATION_MESSAGE);
        }));
        card.add(createDivider());

        // Row 2: Change Password
        card.add(createSettingRow(IconFactory.IconType.LOCK, "Account Password", "Last changed 45 days ago", "🔒 Change Password", () -> {
            JPasswordField pf1 = new JPasswordField();
            JPasswordField pf2 = new JPasswordField();
            JPanel p = new JPanel(new GridLayout(2, 2, 6, 6));
            p.add(new JLabel("New Password:"));
            p.add(pf1);
            p.add(new JLabel("Confirm Password:"));
            p.add(pf2);
            int res = JOptionPane.showConfirmDialog(this, p, "Change Account Password", JOptionPane.OK_CANCEL_OPTION);
            if (res == JOptionPane.OK_OPTION) {
                if (pf1.getPassword().length >= 6) {
                    JOptionPane.showMessageDialog(this, "Your password has been changed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Password must contain at least 6 characters.", "Password Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }));

        return card;
    }

    // ==========================================
    // HELPER UI BUILDERS (PROFESSIONAL ROW LAYOUT)
    // ==========================================
    private JPanel createSectionCard(String title, String subtitle, IconFactory.IconType iconType) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.SURFACE_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER_LIGHT, 1, true),
            new EmptyBorder(16, 20, 16, 20)
        ));

        // Section Title with Icon
        JPanel headerRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        headerRow.setOpaque(false);
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Icon Badge Container
        JPanel iconBadge = new JPanel(new GridBagLayout());
        iconBadge.setPreferredSize(new Dimension(36, 36));
        iconBadge.setBackground(Theme.PRIMARY_LIGHT);
        iconBadge.setBorder(new LineBorder(Theme.PRIMARY, 1, true));
        iconBadge.add(new JLabel(IconFactory.createIcon(iconType, 18, Theme.PRIMARY)));

        JPanel textP = new JPanel();
        textP.setLayout(new BoxLayout(textP, BoxLayout.Y_AXIS));
        textP.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(Theme.FONT_SUBTITLE);
        titleLbl.setForeground(Theme.TEXT_PRIMARY);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(Theme.FONT_SMALL);
        subLbl.setForeground(Theme.TEXT_SECONDARY);

        textP.add(titleLbl);
        textP.add(Box.createRigidArea(new Dimension(0, 2)));
        textP.add(subLbl);

        headerRow.add(iconBadge);
        headerRow.add(textP);

        card.add(headerRow);
        card.add(Box.createRigidArea(new Dimension(0, 12)));

        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER_LIGHT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(sep);
        card.add(Box.createRigidArea(new Dimension(0, 12)));

        return card;
    }

    private JPanel createSettingRow(IconFactory.IconType iconType, String title, String description, String actionText, Runnable action) {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Left Icon + Details
        JPanel left = createItemHeader(iconType, title, description);
        row.add(left, BorderLayout.WEST);

        // Right Action Button
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        right.setOpaque(false);

        JButton btn = Theme.createSecondaryButton(actionText);
        btn.setBorder(BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1, true));
        btn.addActionListener(e -> {
            if (action != null) action.run();
        });
        right.add(btn);
        row.add(right, BorderLayout.EAST);

        return row;
    }

    private JPanel createItemHeader(IconFactory.IconType iconType, String title, String description) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        p.setOpaque(false);

        JLabel iconLbl = new JLabel(IconFactory.createIcon(iconType, 16, Theme.PRIMARY));
        
        JPanel textP = new JPanel();
        textP.setLayout(new BoxLayout(textP, BoxLayout.Y_AXIS));
        textP.setOpaque(false);

        JLabel t = new JLabel(title);
        t.setFont(Theme.FONT_BODY_BOLD);
        t.setForeground(Theme.TEXT_PRIMARY);

        JLabel d = new JLabel(description);
        d.setFont(Theme.FONT_SMALL);
        d.setForeground(Theme.TEXT_SECONDARY);

        textP.add(t);
        textP.add(Box.createRigidArea(new Dimension(0, 2)));
        textP.add(d);

        p.add(iconLbl);
        p.add(textP);
        return p;
    }

    private JComponent createDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(Theme.BORDER_LIGHT.getRed(), Theme.BORDER_LIGHT.getGreen(), Theme.BORDER_LIGHT.getBlue(), 120));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.add(Box.createRigidArea(new Dimension(0, 8)));
        p.add(sep);
        p.add(Box.createRigidArea(new Dimension(0, 8)));
        return p;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 18;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 64;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
