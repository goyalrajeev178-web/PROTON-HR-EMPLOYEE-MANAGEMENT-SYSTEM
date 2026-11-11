package hrms.ui;

import hrms.dao.EmployeeDAO;
import hrms.dao.LeaveDAO;
import hrms.model.Employee;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Fully responsive Dashboard Overview panel.
 * Displays live KPI metrics from EmployeeDAO and LeaveDAO, interactive Run Payroll dialog,
 * and quick navigation actions.
 */
public class DashboardPanel extends JPanel implements Scrollable {

    private final Runnable onNavigateToLeaves;

    // Stat card value labels for live refresh
    private JLabel lblTotalEmployees;
    private JLabel lblActiveDepartments;
    private JLabel lblPendingRequests;
    private JLabel lblTotalPayroll;

    public DashboardPanel(Runnable onNavigateToLeaves) {
        this.onNavigateToLeaves = onNavigateToLeaves;
        buildUI();
    }

    public DashboardPanel() {
        this(null);
    }

    private void buildUI() {
        setLayout(new GridBagLayout());
        setBackground(Theme.BG_APP);
        setBorder(new EmptyBorder(16, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(8, 8, 8, 8);

        // ==========================================
        // 1. Header (Title + Subtitle)
        // ==========================================
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("<html><body style='width: 100%;'>Dashboard Overview</body></html>");
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("<html><body style='width: 100%;'>Welcome back! Here's what's happening today across the organization.</body></html>");
        subtitleLabel.setFont(Theme.FONT_BODY);
        subtitleLabel.setForeground(Theme.TEXT_SECONDARY);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        headerPanel.add(subtitleLabel);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        add(headerPanel, gbc);

        // ==========================================
        // 2. Top KPI / Metric Cards (Proportional Grid)
        // ==========================================
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 14, 14));
        statsPanel.setOpaque(false);
        statsPanel.setMinimumSize(new Dimension(0, 0));

        JPanel cardEmployees = createStatCard("Total Employees", "...", "Active in Directory");
        JPanel cardDepts = createStatCard("Departments", "...", "Operational teams");
        JPanel cardRequests = createStatCard("Pending Leaves", "...", "Requires HR action");
        JPanel cardPayroll = createStatCard("Monthly Payroll", "...", "Estimated disbursal");

        statsPanel.add(cardEmployees);
        statsPanel.add(cardDepts);
        statsPanel.add(cardRequests);
        statsPanel.add(cardPayroll);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        add(statsPanel, gbc);

        // ==========================================
        // 3. Main Body: Announcements (70% weightx)
        // ==========================================
        JPanel announcementsPanel = new JPanel();
        announcementsPanel.setLayout(new BoxLayout(announcementsPanel, BoxLayout.Y_AXIS));
        announcementsPanel.setBackground(Theme.SURFACE_CARD);
        announcementsPanel.setMinimumSize(new Dimension(0, 0));
        announcementsPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER_LIGHT, 1, true),
            new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel annTitle = new JLabel("Announcements & Notices");
        annTitle.setFont(Theme.FONT_SECTION);
        annTitle.setForeground(Theme.TEXT_PRIMARY);
        announcementsPanel.add(annTitle);
        announcementsPanel.add(Box.createRigidArea(new Dimension(0, 14)));

        announcementsPanel.add(createAnnouncement("Townhall Meeting", "Today, 10:00 AM", 
            "Join us for the monthly company townhall with the leadership team to review Q4 performance and product roadmap."));
        announcementsPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        announcementsPanel.add(createAnnouncement("New Health & Wellness Benefits", "Yesterday", 
            "Check out the updated comprehensive health and insurance benefit options for current enrollment."));
        announcementsPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        announcementsPanel.add(createAnnouncement("Security & Compliance Refresher", "3 days ago", 
            "Annual cybersecurity refresher module is now available in your learning portal. Please complete before Friday."));

        announcementsPanel.add(Box.createVerticalGlue());

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0.70;
        gbc.weighty = 1.0;
        add(announcementsPanel, gbc);

        // ==========================================
        // 4. Main Body: Quick Actions & Birthdays (30% weightx)
        // ==========================================
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setOpaque(false);
        sidePanel.setMinimumSize(new Dimension(0, 0));

        // Quick Actions Card
        JPanel actionsPanel = new JPanel();
        actionsPanel.setLayout(new BoxLayout(actionsPanel, BoxLayout.Y_AXIS));
        actionsPanel.setBackground(Theme.SURFACE_CARD);
        actionsPanel.setMinimumSize(new Dimension(0, 0));
        actionsPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER_LIGHT, 1, true),
            new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel actionsTitle = new JLabel("Quick Actions");
        actionsTitle.setFont(Theme.FONT_SUBTITLE);
        actionsTitle.setForeground(Theme.TEXT_PRIMARY);
        actionsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionsPanel.add(actionsTitle);
        actionsPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        JButton btnPayroll = Theme.createPrimaryButton("💰 Run Payroll");
        btnPayroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnPayroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnPayroll.addActionListener(e -> openRunPayrollDialog());

        JButton btnTime = Theme.createSecondaryButton("📋 Review Leaves");
        btnTime.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnTime.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnTime.setBorder(BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1, true));
        btnTime.addActionListener(e -> {
            if (onNavigateToLeaves != null) {
                onNavigateToLeaves.run();
            } else {
                int pending = LeaveDAO.getInstance().getPendingCount();
                JOptionPane.showMessageDialog(this, 
                    "There are currently " + pending + " pending leave requests awaiting approval.\nNavigate to 'Leave Requests' in the sidebar to review.",
                    "Pending Leaves", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        actionsPanel.add(btnPayroll);
        actionsPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        actionsPanel.add(btnTime);

        sidePanel.add(actionsPanel);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 14)));

        // Upcoming Birthdays Card
        JPanel birthdaysPanel = new JPanel();
        birthdaysPanel.setLayout(new BoxLayout(birthdaysPanel, BoxLayout.Y_AXIS));
        birthdaysPanel.setBackground(Theme.SURFACE_CARD);
        birthdaysPanel.setMinimumSize(new Dimension(0, 0));
        birthdaysPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER_LIGHT, 1, true),
            new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel bdayTitle = new JLabel("Upcoming Celebrations");
        bdayTitle.setFont(Theme.FONT_SUBTITLE);
        bdayTitle.setForeground(Theme.TEXT_PRIMARY);
        bdayTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        birthdaysPanel.add(bdayTitle);
        birthdaysPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        JLabel bday1 = new JLabel("<html><body>🎂  <b>Aaradhya Nair</b> (Tomorrow)<br><span style='color:#64748b; font-size:11px;'>Engineering</span></body></html>");
        bday1.setFont(Theme.FONT_BODY);
        bday1.setForeground(Theme.TEXT_SECONDARY);
        bday1.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel bday2 = new JLabel("<html><body>🎉  <b>Rohan Deshmukh</b> (Next Week)<br><span style='color:#64748b; font-size:11px;'>Human Resources</span></body></html>");
        bday2.setFont(Theme.FONT_BODY);
        bday2.setForeground(Theme.TEXT_SECONDARY);
        bday2.setAlignmentX(Component.LEFT_ALIGNMENT);

        birthdaysPanel.add(bday1);
        birthdaysPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        birthdaysPanel.add(bday2);

        sidePanel.add(birthdaysPanel);
        sidePanel.add(Box.createVerticalGlue());

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0.30;
        gbc.weighty = 1.0;
        add(sidePanel, gbc);

        refreshData();
    }

    public void refreshData() {
        List<Employee> employees = EmployeeDAO.getInstance().getAll();
        int empCount = employees.size();

        Set<String> departments = new HashSet<>();
        double totalSalary = 0;
        for (Employee emp : employees) {
            if (emp.getDepartment() != null && !emp.getDepartment().trim().isEmpty()) {
                departments.add(emp.getDepartment().trim());
            }
            totalSalary += emp.getSalaryNumeric();
        }

        int pendingLeaves = LeaveDAO.getInstance().getPendingCount();

        if (lblTotalEmployees != null) {
            lblTotalEmployees.setText(String.format("%,d", empCount));
        }
        if (lblActiveDepartments != null) {
            lblActiveDepartments.setText(String.valueOf(departments.size()));
        }
        if (lblPendingRequests != null) {
            lblPendingRequests.setText(String.valueOf(pendingLeaves));
        }
        if (lblTotalPayroll != null) {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"));
            lblTotalPayroll.setText(currencyFormat.format(totalSalary));
        }
    }

    private void openRunPayrollDialog() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, "Execute Monthly Payroll", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(500, 460);
        dialog.setLocationRelativeTo(parentWindow);
        dialog.setLayout(new BorderLayout());

        List<Employee> employees = EmployeeDAO.getInstance().getAll();
        double grossPayroll = 0;
        for (Employee emp : employees) {
            grossPayroll += emp.getSalaryNumeric();
        }

        double deductions = grossPayroll * 0.10; // 10% statutory tax/PF
        double netPayout = grossPayroll - deductions;

        NumberFormat currFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"));
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"));

        JPanel card = new JPanel(new GridLayout(6, 2, 10, 12));
        card.setBackground(Theme.SURFACE_CARD);
        card.setBorder(new EmptyBorder(24, 28, 24, 28));

        card.add(createLabel("Pay Period:", true));
        card.add(createLabel(currentMonth, false));

        card.add(createLabel("Eligible Employees:", true));
        card.add(createLabel(employees.size() + " active employees", false));

        card.add(createLabel("Gross Payroll:", true));
        card.add(createLabel(currFormat.format(grossPayroll), false));

        card.add(createLabel("Est. Tax & Deductions (10%):", true));
        card.add(createLabel("- " + currFormat.format(deductions), false));

        card.add(createLabel("Net Disbursal:", true));
        JLabel netLbl = createLabel(currFormat.format(netPayout), true);
        netLbl.setForeground(Theme.PRIMARY);
        card.add(netLbl);

        card.add(createLabel("Disbursal Mode:", true));
        card.add(createLabel("Direct Bank Deposit (NEFT/RTGS)", false));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnPanel.setBackground(Theme.BG_APP);

        JButton btnCancel = Theme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dialog.dispose());

        JButton btnConfirm = Theme.createPrimaryButton("Disburse Payout");
        btnConfirm.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(dialog, 
                "Are you sure you want to approve and disburse " + currFormat.format(netPayout) + " to " + employees.size() + " employees?",
                "Confirm Payroll Disbursal", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dialog.dispose();
                JOptionPane.showMessageDialog(this, 
                    "✅ Payroll Run Successful!\n\n"
                    + "Period: " + currentMonth + "\n"
                    + "Employees Processed: " + employees.size() + "\n"
                    + "Total Amount Disbursed: " + currFormat.format(netPayout) + "\n"
                    + "Transaction Reference: PR-" + System.currentTimeMillis() % 1000000 + "\n\n"
                    + "Disbursal advice has been queued for bank transfer.",
                    "Payroll Disbursed", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnConfirm);

        dialog.add(card, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JLabel createLabel(String text, boolean bold) {
        JLabel l = new JLabel(text);
        l.setFont(bold ? Theme.FONT_BODY_BOLD : Theme.FONT_BODY);
        l.setForeground(bold ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY);
        return l;
    }

    private JPanel createStatCard(String title, String value, String change) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.SURFACE_CARD);
        card.setMinimumSize(new Dimension(0, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER_LIGHT, 1, true),
            new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel titleLabel = new JLabel("<html><body style='width: 100%;'>" + title + "</body></html>");
        titleLabel.setFont(Theme.FONT_SMALL);
        titleLabel.setForeground(Theme.TEXT_SECONDARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font(Theme.FONT_FAMILY, Font.BOLD, 22));
        valueLabel.setForeground(Theme.TEXT_PRIMARY);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (title.contains("Employees")) {
            lblTotalEmployees = valueLabel;
        } else if (title.contains("Departments")) {
            lblActiveDepartments = valueLabel;
        } else if (title.contains("Leaves")) {
            lblPendingRequests = valueLabel;
        } else if (title.contains("Payroll")) {
            lblTotalPayroll = valueLabel;
        }

        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(valueLabel);

        if (!change.isEmpty()) {
            card.add(Box.createRigidArea(new Dimension(0, 4)));
            JLabel changeLabel = new JLabel("<html><body style='width: 100%; color:#16a34a; font-size:11px;'>" + change + "</body></html>");
            changeLabel.setFont(Theme.FONT_SMALL_BOLD);
            changeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(changeLabel);
        }

        return card;
    }

    private JPanel createAnnouncement(String title, String time, String desc) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLbl = new JLabel("<html><body><b>" + title + "</b> <span style='color:#94a3b8; font-size:11px;'>• " + time + "</span></body></html>");
        titleLbl.setFont(Theme.FONT_BODY_BOLD);
        titleLbl.setForeground(Theme.TEXT_PRIMARY);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descLbl = new JLabel("<html><body style='width: 100%; color:#475569;'>" + desc + "</body></html>");
        descLbl.setFont(Theme.FONT_BODY);
        descLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        p.add(titleLbl);
        p.add(Box.createRigidArea(new Dimension(0, 4)));
        p.add(descLbl);

        return p;
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 16;
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
