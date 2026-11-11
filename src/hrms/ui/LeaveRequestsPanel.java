package hrms.ui;

import hrms.dao.EmployeeDAO;
import hrms.dao.LeaveDAO;
import hrms.model.Employee;
import hrms.model.LeaveRecord;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsive, interactive Leave Requests screen with real LeaveDAO integration.
 * Supports submitting new requests, filtering by status, and approving/rejecting applications.
 */
public class LeaveRequestsPanel extends JPanel implements Scrollable {

    private DefaultTableModel tableModel;
    private JTable table;
    private List<LeaveRecord> displayedRecords = new ArrayList<>();
    private String currentFilter = "All";

    private JPanel annualBalanceCard;
    private JPanel sickBalanceCard;
    private JLabel annualBalanceLabel;
    private JProgressBar annualProgressBar;
    private JLabel sickBalanceLabel;
    private JProgressBar sickProgressBar;

    public LeaveRequestsPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(Theme.BG_APP);
        setBorder(new EmptyBorder(16, 20, 20, 20));

        // Top Section: Balances & Header Actions
        JPanel topSection = new JPanel(new BorderLayout(12, 12));
        topSection.setOpaque(false);

        // Header title & Actions
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("<html><body><b>Leave Requests</b></body></html>");
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsPanel.setOpaque(false);

        // Filter Dropdown
        String[] filterOptions = {"All Requests", "Pending Only", "Approved Only", "Rejected Only"};
        JComboBox<String> filterCombo = new JComboBox<>(filterOptions);
        filterCombo.setFont(Theme.FONT_BODY);
        filterCombo.setPreferredSize(new Dimension(140, 36));
        filterCombo.setBackground(Theme.SURFACE_CARD);
        filterCombo.setForeground(Theme.TEXT_PRIMARY);
        filterCombo.addActionListener(e -> {
            String selected = (String) filterCombo.getSelectedItem();
            if ("Pending Only".equals(selected)) {
                currentFilter = "Pending";
            } else if ("Approved Only".equals(selected)) {
                currentFilter = "Approved";
            } else if ("Rejected Only".equals(selected)) {
                currentFilter = "Rejected";
            } else {
                currentFilter = "All";
            }
            refreshData();
        });

        JButton btnReview = Theme.createSecondaryButton("Review Selected");
        btnReview.setBorder(BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1, true));
        btnReview.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0 && row < displayedRecords.size()) {
                openReviewDialog(displayedRecords.get(row));
            } else {
                JOptionPane.showMessageDialog(this, "Please select a leave request row from the table first.", "Select Request", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton btnNewRequest = Theme.createPrimaryButton("+ New Request");
        btnNewRequest.addActionListener(e -> openNewRequestDialog());

        actionsPanel.add(new JLabel("Filter: "));
        actionsPanel.add(filterCombo);
        actionsPanel.add(btnReview);
        actionsPanel.add(btnNewRequest);
        headerPanel.add(actionsPanel, BorderLayout.EAST);

        topSection.add(headerPanel, BorderLayout.NORTH);

        // Balance Cards
        JPanel balancesPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        balancesPanel.setOpaque(false);
        balancesPanel.setMinimumSize(new Dimension(0, 0));

        annualBalanceCard = createBalanceCardComponent("Annual Leave (Org Total)", 25.0);
        sickBalanceCard = createBalanceCardComponent("Sick Leave (Org Total)", 10.0);

        balancesPanel.add(annualBalanceCard);
        balancesPanel.add(sickBalanceCard);

        topSection.add(balancesPanel, BorderLayout.CENTER);
        add(topSection, BorderLayout.NORTH);

        // Data Table
        String[] columns = {"ID", "Employee", "Type", "Dates", "Duration", "Status", "Action"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFont(Theme.FONT_BODY);
        table.setForeground(Theme.TEXT_PRIMARY);
        table.setRowHeight(42);
        table.setShowVerticalLines(false);
        table.setGridColor(Theme.BORDER_LIGHT);
        table.setSelectionBackground(Theme.PRIMARY_LIGHT);
        table.setSelectionForeground(Theme.TEXT_PRIMARY);

        table.getTableHeader().setFont(Theme.FONT_SMALL_BOLD);
        table.getTableHeader().setForeground(Theme.TEXT_SECONDARY);
        table.getTableHeader().setOpaque(false);
        table.getTableHeader().setBackground(new Color(0xF1, 0xF5, 0xF9));
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(0).setMaxWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(150);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);
        table.getColumnModel().getColumn(6).setPreferredWidth(90);

        // Center alignment
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        // Status badge renderer
        table.getColumnModel().getColumn(5).setCellRenderer(new StatusBadgeRenderer());

        // Double-click row listener
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0 && row < displayedRecords.size()) {
                    if (e.getClickCount() == 2 || table.getSelectedColumn() == 6) {
                        openReviewDialog(displayedRecords.get(row));
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1, true));
        scrollPane.getViewport().setBackground(Theme.SURFACE_CARD);

        add(scrollPane, BorderLayout.CENTER);

        refreshData();
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        displayedRecords.clear();

        List<LeaveRecord> allRecords = LeaveDAO.getInstance().getAll();
        int annualTaken = 0;
        int sickTaken = 0;

        for (LeaveRecord lr : allRecords) {
            if ("Approved".equalsIgnoreCase(lr.getStatus())) {
                int days = parseDurationDays(lr.getDuration());
                if ("Annual Leave".equalsIgnoreCase(lr.getLeaveType())) {
                    annualTaken += days;
                } else if ("Sick Leave".equalsIgnoreCase(lr.getLeaveType())) {
                    sickTaken += days;
                }
            }

            boolean matches = false;
            if ("All".equalsIgnoreCase(currentFilter)) {
                matches = true;
            } else if (currentFilter.equalsIgnoreCase(lr.getStatus())) {
                matches = true;
            }

            if (matches) {
                displayedRecords.add(lr);
                String actionText = "Pending".equalsIgnoreCase(lr.getStatus()) ? "🔍 Review" : "👁 View";
                tableModel.addRow(new Object[]{
                    "#" + lr.getId(),
                    lr.getEmployeeName(),
                    lr.getLeaveType(),
                    lr.getDates(),
                    lr.getDuration(),
                    lr.getStatus(),
                    actionText
                });
            }
        }

        // Update balance cards
        double annualTotal = 25.0;
        double annualAvailable = Math.max(0, annualTotal - annualTaken);
        if (annualBalanceLabel != null) {
            annualBalanceLabel.setText(annualAvailable + " days available of " + annualTotal + " days");
        }
        if (annualProgressBar != null) {
            annualProgressBar.setValue((int) ((annualTotal - annualAvailable) * 10));
        }

        double sickTotal = 10.0;
        double sickAvailable = Math.max(0, sickTotal - sickTaken);
        if (sickBalanceLabel != null) {
            sickBalanceLabel.setText(sickAvailable + " days available of " + sickTotal + " days");
        }
        if (sickProgressBar != null) {
            sickProgressBar.setValue((int) ((sickTotal - sickAvailable) * 10));
        }
    }

    private int parseDurationDays(String duration) {
        if (duration == null) return 1;
        try {
            String digits = duration.replaceAll("[^0-9]", "");
            return digits.isEmpty() ? 1 : Integer.parseInt(digits);
        } catch (Exception e) {
            return 1;
        }
    }

    private JPanel createBalanceCardComponent(String title, double total) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.SURFACE_CARD);
        card.setMinimumSize(new Dimension(0, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER_LIGHT, 1, true),
            new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.FONT_SUBTITLE);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);

        JLabel balanceLabel = new JLabel("0 days available of " + total + " days");
        balanceLabel.setFont(Theme.FONT_BODY);
        balanceLabel.setForeground(Theme.TEXT_SECONDARY);

        JProgressBar progressBar = new JProgressBar(0, (int) (total * 10));
        progressBar.setValue(0);
        progressBar.setForeground(Theme.PRIMARY);
        progressBar.setBackground(new Color(0xEE, 0xF2, 0xF6));
        progressBar.setStringPainted(false);
        progressBar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 10));
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));

        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(balanceLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(progressBar);

        if (title.contains("Annual")) {
            annualBalanceLabel = balanceLabel;
            annualProgressBar = progressBar;
        } else {
            sickBalanceLabel = balanceLabel;
            sickProgressBar = progressBar;
        }

        return card;
    }

    private void openNewRequestDialog() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, "Submit New Leave Request", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(480, 520);
        dialog.setLocationRelativeTo(parentWindow);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.SURFACE_CARD);
        form.setBorder(new EmptyBorder(24, 28, 24, 28));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;

        // Employee Selection
        form.add(new JLabel("Employee *"), gbc);
        gbc.gridy++;

        List<Employee> employees = EmployeeDAO.getInstance().getAll();
        JComboBox<Employee> empCombo = new JComboBox<>(employees.toArray(new Employee[0]));
        empCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Employee) {
                    Employee e = (Employee) value;
                    setText(e.getName() + " (" + e.getId() + " - " + e.getDepartment() + ")");
                }
                return this;
            }
        });
        empCombo.setPreferredSize(new Dimension(0, 36));
        form.add(empCombo, gbc);

        // Leave Type
        gbc.gridy++;
        form.add(new JLabel("Leave Type *"), gbc);
        gbc.gridy++;
        String[] leaveTypes = {"Annual Leave", "Sick Leave", "Casual Leave", "Maternity / Paternity", "Unpaid Leave"};
        JComboBox<String> typeCombo = new JComboBox<>(leaveTypes);
        typeCombo.setPreferredSize(new Dimension(0, 36));
        form.add(typeCombo, gbc);

        // Dates
        gbc.gridy++;
        form.add(new JLabel("Dates (e.g. Nov 15 - Nov 18) *"), gbc);
        gbc.gridy++;
        JTextField datesField = new JTextField();
        datesField.setPreferredSize(new Dimension(0, 36));
        form.add(datesField, gbc);

        // Duration
        gbc.gridy++;
        form.add(new JLabel("Duration (e.g. 3 days) *"), gbc);
        gbc.gridy++;
        JTextField durationField = new JTextField("1 day");
        durationField.setPreferredSize(new Dimension(0, 36));
        form.add(durationField, gbc);

        // Reason
        gbc.gridy++;
        form.add(new JLabel("Reason / Notes"), gbc);
        gbc.gridy++;
        JTextArea reasonArea = new JTextArea(3, 20);
        reasonArea.setFont(Theme.FONT_BODY);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        JScrollPane reasonScroll = new JScrollPane(reasonArea);
        form.add(reasonScroll, gbc);

        // Actions
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnPanel.setBackground(Theme.BG_APP);
        JButton btnCancel = Theme.createSecondaryButton("Cancel");
        JButton btnSubmit = Theme.createPrimaryButton("Submit Application");

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSubmit.addActionListener(e -> {
            Employee selectedEmp = (Employee) empCombo.getSelectedItem();
            String dates = datesField.getText().trim();
            String duration = durationField.getText().trim();
            String reason = reasonArea.getText().trim();
            String leaveType = (String) typeCombo.getSelectedItem();

            if (selectedEmp == null) {
                JOptionPane.showMessageDialog(dialog, "Please select an employee.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (dates.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter the leave date range.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (duration.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter the duration.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            LeaveRecord newRecord = new LeaveRecord(
                0,
                selectedEmp.getId(),
                selectedEmp.getName(),
                leaveType,
                dates,
                duration,
                reason,
                "Pending"
            );

            try {
                LeaveDAO.getInstance().add(newRecord);
                refreshData();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Leave application for " + selectedEmp.getName() + " submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error submitting application: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSubmit);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void openReviewDialog(LeaveRecord record) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentWindow, "Leave Request Details - #" + record.getId(), Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(440, 420);
        dialog.setLocationRelativeTo(parentWindow);
        dialog.setLayout(new BorderLayout());

        JPanel details = new JPanel(new GridLayout(6, 2, 10, 12));
        details.setBackground(Theme.SURFACE_CARD);
        details.setBorder(new EmptyBorder(24, 28, 24, 28));

        details.add(createDetailLabel("Employee:", true));
        details.add(createDetailLabel(record.getEmployeeName() + " (" + record.getEmployeeId() + ")", false));

        details.add(createDetailLabel("Leave Type:", true));
        details.add(createDetailLabel(record.getLeaveType(), false));

        details.add(createDetailLabel("Dates:", true));
        details.add(createDetailLabel(record.getDates(), false));

        details.add(createDetailLabel("Duration:", true));
        details.add(createDetailLabel(record.getDuration(), false));

        details.add(createDetailLabel("Reason:", true));
        details.add(createDetailLabel(record.getReason() != null && !record.getReason().isEmpty() ? record.getReason() : "None provided", false));

        details.add(createDetailLabel("Current Status:", true));
        JLabel statusLbl = createDetailLabel(record.getStatus(), false);
        if ("Approved".equalsIgnoreCase(record.getStatus())) {
            statusLbl.setForeground(Theme.STATUS_APPROVED_FG);
        } else if ("Pending".equalsIgnoreCase(record.getStatus())) {
            statusLbl.setForeground(Theme.STATUS_PENDING_FG);
        } else {
            statusLbl.setForeground(Theme.STATUS_REJECTED_FG);
        }
        details.add(statusLbl);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnPanel.setBackground(Theme.BG_APP);

        JButton btnClose = Theme.createSecondaryButton("Close");
        btnClose.addActionListener(e -> dialog.dispose());
        btnPanel.add(btnClose);

        if ("Pending".equalsIgnoreCase(record.getStatus())) {
            JButton btnReject = new JButton("❌ Reject");
            btnReject.setFont(Theme.FONT_BODY_BOLD);
            btnReject.setBackground(Theme.STATUS_REJECTED_BG);
            btnReject.setForeground(Theme.STATUS_REJECTED_FG);
            btnReject.setFocusPainted(false);
            btnReject.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.STATUS_REJECTED_FG, 1, true),
                new EmptyBorder(8, 14, 8, 14)
            ));
            btnReject.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnReject.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(dialog, "Are you sure you want to REJECT this leave application?", "Confirm Rejection", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    LeaveDAO.getInstance().updateStatus(record.getId(), "Rejected", "EMP-1005");
                    refreshData();
                    dialog.dispose();
                    JOptionPane.showMessageDialog(this, "Leave application marked as Rejected.", "Rejected", JOptionPane.INFORMATION_MESSAGE);
                }
            });

            JButton btnApprove = new JButton("✅ Approve");
            btnApprove.setFont(Theme.FONT_BODY_BOLD);
            btnApprove.setBackground(Theme.STATUS_APPROVED_BG);
            btnApprove.setForeground(Theme.STATUS_APPROVED_FG);
            btnApprove.setFocusPainted(false);
            btnApprove.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.STATUS_APPROVED_FG, 1, true),
                new EmptyBorder(8, 14, 8, 14)
            ));
            btnApprove.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnApprove.addActionListener(e -> {
                LeaveDAO.getInstance().updateStatus(record.getId(), "Approved", "EMP-1005");
                refreshData();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Leave application has been APPROVED!", "Approved", JOptionPane.INFORMATION_MESSAGE);
            });

            btnPanel.add(btnReject);
            btnPanel.add(btnApprove);
        }

        dialog.add(details, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JLabel createDetailLabel(String text, boolean bold) {
        JLabel l = new JLabel(text);
        l.setFont(bold ? Theme.FONT_BODY_BOLD : Theme.FONT_BODY);
        l.setForeground(bold ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY);
        return l;
    }

    static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setOpaque(true);
            label.setFont(Theme.FONT_SMALL_BOLD);

            String status = (value != null) ? value.toString() : "";

            if ("Approved".equalsIgnoreCase(status)) {
                label.setBackground(Theme.STATUS_APPROVED_BG);
                label.setForeground(Theme.STATUS_APPROVED_FG);
            } else if ("Pending".equalsIgnoreCase(status)) {
                label.setBackground(Theme.STATUS_PENDING_BG);
                label.setForeground(Theme.STATUS_PENDING_FG);
            } else if ("Rejected".equalsIgnoreCase(status)) {
                label.setBackground(Theme.STATUS_REJECTED_BG);
                label.setForeground(Theme.STATUS_REJECTED_FG);
            } else {
                label.setBackground(Theme.SURFACE_CARD);
                label.setForeground(Theme.TEXT_PRIMARY);
            }

            label.setBorder(new EmptyBorder(4, 12, 4, 12));
            return label;
        }
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
