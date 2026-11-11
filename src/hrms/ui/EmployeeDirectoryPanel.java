package hrms.ui;

import hrms.dao.EmployeeDAO;
import hrms.model.Employee;
import hrms.util.CsvExportUtil;
import hrms.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsive, fully functional Employee Directory.
 * Supports MySQL CRUD, local memory fallback, CSV export, multi-criteria filtering, and photo management.
 */
public class EmployeeDirectoryPanel extends JPanel implements Scrollable {

    private JPanel cardsGridContainer;
    private JTable table;
    private DefaultTableModel tableModel;
    private CardLayout viewModeLayout;
    private JPanel viewModeContainer;

    private JComboBox<String> deptFilter;
    private JComboBox<String> statusFilter;
    private JComboBox<String> locationFilter;
    private JTextField searchInput;
    private JLabel matchCountLabel;
    private JButton btnGridView;
    private JButton btnTableView;
    private boolean isGridView = true;

    public static final Color[][] AVATAR_GRADIENTS = EmployeeDAO.AVATAR_GRADIENTS;

    /**
     * Backward-compatible alias extending domain Employee model.
     */
    public static class EmployeeRecord extends Employee {
        public EmployeeRecord(String id, String name, String role, String department, String location,
                              String email, String phone, String joinDate, String status, String salary,
                              String emergency, Color[] avatarColors) {
            super(id, name, role, department, location, email, phone, joinDate, status, salary, emergency, avatarColors);
        }

        public EmployeeRecord(Employee e) {
            super(e.getId(), e.getName(), e.getRole(), e.getDepartment(), e.getLocation(),
                  e.getEmail(), e.getPhone(), e.getJoinDate(), e.getStatus(), e.getSalary(),
                  e.getEmergency(), e.getAvatarColors());
            this.setCustomPhoto(e.getCustomPhoto());
        }
    }

    // Maintained for backward compatibility; kept synchronized with EmployeeDAO
    public static List<EmployeeRecord> employeeList = new ArrayList<>();

    public EmployeeDirectoryPanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(Theme.BG_APP);
        setBorder(new EmptyBorder(16, 20, 20, 20));

        // 1. Top Section (Header + Actions)
        JPanel topSection = createHeaderSection();
        add(topSection, BorderLayout.NORTH);

        // 2. Center Section (Filter Bar + Content)
        JPanel centerPanel = new JPanel(new BorderLayout(14, 14));
        centerPanel.setOpaque(false);

        JPanel filterBar = createFilterBar();
        centerPanel.add(filterBar, BorderLayout.NORTH);

        // View Mode container (CardLayout between Square Cards Grid & Table)
        viewModeLayout = new CardLayout();
        viewModeContainer = new JPanel(viewModeLayout);
        viewModeContainer.setOpaque(false);

        // Grid View
        JScrollPane gridScrollPane = createGridViewSection();
        viewModeContainer.add(gridScrollPane, "GRID");

        // Table View
        JScrollPane tableScrollPane = createTableViewSection();
        viewModeContainer.add(tableScrollPane, "TABLE");

        centerPanel.add(viewModeContainer, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Initial Filter and Table Load
        applyFilters();
    }

    private JPanel createHeaderSection() {
        JPanel header = new JPanel(new BorderLayout(14, 14));
        header.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("<html><body><b>Employee Directory</b></body></html>");
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("<html><body>Manage, filter, search, view, edit, and export all personal employee records.</body></html>");
        subtitleLabel.setFont(Theme.FONT_BODY);
        subtitleLabel.setForeground(Theme.TEXT_SECONDARY);

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 4)));
        titlePanel.add(subtitleLabel);

        header.add(titlePanel, BorderLayout.WEST);

        // Actions & View Switcher
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        btnGridView = new JButton(IconFactory.createIcon(IconFactory.IconType.GRID, 18, Theme.PRIMARY));
        btnGridView.setToolTipText("Square Grid View");
        btnGridView.setPreferredSize(new Dimension(36, 36));
        btnGridView.setBackground(Theme.PRIMARY_LIGHT);
        btnGridView.setBorder(BorderFactory.createLineBorder(Theme.PRIMARY, 1, true));
        btnGridView.setFocusPainted(false);
        btnGridView.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnTableView = new JButton(IconFactory.createIcon(IconFactory.IconType.LIST, 18, Theme.TEXT_MUTED));
        btnTableView.setToolTipText("Table List View");
        btnTableView.setPreferredSize(new Dimension(36, 36));
        btnTableView.setBackground(Theme.SURFACE_CARD);
        btnTableView.setBorder(BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1, true));
        btnTableView.setFocusPainted(false);
        btnTableView.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnGridView.addActionListener(e -> {
            isGridView = true;
            btnGridView.setBackground(Theme.PRIMARY_LIGHT);
            btnGridView.setBorder(BorderFactory.createLineBorder(Theme.PRIMARY, 1, true));
            btnGridView.setIcon(IconFactory.createIcon(IconFactory.IconType.GRID, 18, Theme.PRIMARY));

            btnTableView.setBackground(Theme.SURFACE_CARD);
            btnTableView.setBorder(BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1, true));
            btnTableView.setIcon(IconFactory.createIcon(IconFactory.IconType.LIST, 18, Theme.TEXT_MUTED));

            viewModeLayout.show(viewModeContainer, "GRID");
        });

        btnTableView.addActionListener(e -> {
            isGridView = false;
            btnTableView.setBackground(Theme.PRIMARY_LIGHT);
            btnTableView.setBorder(BorderFactory.createLineBorder(Theme.PRIMARY, 1, true));
            btnTableView.setIcon(IconFactory.createIcon(IconFactory.IconType.LIST, 18, Theme.PRIMARY));

            btnGridView.setBackground(Theme.SURFACE_CARD);
            btnGridView.setBorder(BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1, true));
            btnGridView.setIcon(IconFactory.createIcon(IconFactory.IconType.GRID, 18, Theme.TEXT_MUTED));

            viewModeLayout.show(viewModeContainer, "TABLE");
        });

        // Export CSV Button
        JButton exportBtn = Theme.createSecondaryButton("📥 Export CSV");
        exportBtn.setFont(Theme.FONT_BODY_BOLD);
        exportBtn.setBorder(BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1, true));
        exportBtn.setPreferredSize(new Dimension(130, 36));
        exportBtn.addActionListener(e -> handleExportCsv());

        // Add Employee Button
        JButton addBtn = new JButton("  Add Employee");
        addBtn.setIcon(IconFactory.createIcon(IconFactory.IconType.PLUS, 16, Theme.TEXT_ON_PRIMARY));
        addBtn.setFont(Theme.FONT_BODY_BOLD);
        addBtn.setBackground(Theme.PRIMARY);
        addBtn.setForeground(Theme.TEXT_ON_PRIMARY);
        addBtn.setFocusPainted(false);
        addBtn.setPreferredSize(new Dimension(150, 36));
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBtn.addActionListener(e -> showAddEmployeeDialog(this, this::applyFilters));

        actions.add(btnGridView);
        actions.add(btnTableView);
        actions.add(Box.createRigidArea(new Dimension(4, 0)));
        actions.add(exportBtn);
        actions.add(addBtn);

        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private void handleExportCsv() {
        String selectedDept = (String) deptFilter.getSelectedItem();
        String selectedStatus = (String) statusFilter.getSelectedItem();
        String selectedLocation = (String) locationFilter.getSelectedItem();
        String keyword = searchInput.getText().trim();

        List<Employee> toExport = EmployeeDAO.getInstance().search(selectedDept, selectedStatus, selectedLocation, keyword);
        if (toExport.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No matching employee records to export.", "Export CSV", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Employee Records to CSV");
        fileChooser.setSelectedFile(new File(CsvExportUtil.getDefaultExportFileName()));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".csv");
            }

            try {
                int count = CsvExportUtil.exportEmployeesToCsv(toExport, fileToSave);
                JOptionPane.showMessageDialog(this,
                    "Successfully exported " + count + " employee records to:\n" + fileToSave.getAbsolutePath(),
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Failed to export CSV file:\n" + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createFilterBar() {
        JPanel card = new JPanel(new BorderLayout(12, 8));
        card.setBackground(Theme.SURFACE_CARD);
        card.setMinimumSize(new Dimension(0, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER_LIGHT, 1, true),
            new EmptyBorder(10, 14, 10, 14)
        ));

        JPanel filtersLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filtersLeft.setOpaque(false);

        JLabel filterIcon = new JLabel("Filters:", IconFactory.createIcon(IconFactory.IconType.FILTER, 18, Theme.PRIMARY), SwingConstants.LEFT);
        filterIcon.setFont(Theme.FONT_BODY_BOLD);
        filterIcon.setForeground(Theme.TEXT_PRIMARY);
        filtersLeft.add(filterIcon);

        deptFilter = new JComboBox<>(new String[]{
            "All Departments", "Engineering", "Human Resources", "Design", "Marketing", "Sales", "Finance", "Operations"
        });
        styleComboBox(deptFilter);
        filtersLeft.add(createFilterField("Dept:", deptFilter));

        statusFilter = new JComboBox<>(new String[]{
            "All Statuses", "Active", "On Leave", "Remote", "Terminated"
        });
        styleComboBox(statusFilter);
        filtersLeft.add(createFilterField("Status:", statusFilter));

        locationFilter = new JComboBox<>(new String[]{
            "All Locations", "Bengaluru, KA", "Mumbai, MH", "Delhi NCR", "Hyderabad, TS", "Pune, MH", "Remote"
        });
        styleComboBox(locationFilter);
        filtersLeft.add(createFilterField("Location:", locationFilter));

        JButton resetBtn = new JButton("Reset");
        resetBtn.setFont(Theme.FONT_SMALL_BOLD);
        resetBtn.setForeground(Theme.PRIMARY);
        resetBtn.setBackground(Theme.SURFACE_CARD);
        resetBtn.setFocusPainted(false);
        resetBtn.setBorder(BorderFactory.createLineBorder(Theme.PRIMARY_LIGHT, 1, true));
        resetBtn.setPreferredSize(new Dimension(68, 32));
        resetBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetBtn.addActionListener(e -> {
            deptFilter.setSelectedIndex(0);
            statusFilter.setSelectedIndex(0);
            locationFilter.setSelectedIndex(0);
            searchInput.setText("");
            applyFilters();
        });
        filtersLeft.add(resetBtn);

        card.add(filtersLeft, BorderLayout.WEST);

        JPanel filtersRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filtersRight.setOpaque(false);

        matchCountLabel = new JLabel("Showing employees");
        matchCountLabel.setFont(Theme.FONT_SMALL_BOLD);
        matchCountLabel.setForeground(Theme.TEXT_MUTED);

        searchInput = new JTextField();
        searchInput.putClientProperty("JTextField.placeholderText", "Search employee...");
        searchInput.setFont(Theme.FONT_BODY);
        searchInput.setPreferredSize(new Dimension(180, 32));
        searchInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1, true),
            new EmptyBorder(4, 8, 4, 8)
        ));
        searchInput.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                applyFilters();
            }
        });

        filtersRight.add(matchCountLabel);
        filtersRight.add(searchInput);

        card.add(filtersRight, BorderLayout.EAST);

        ActionListener filterListener = (ActionEvent e) -> applyFilters();
        deptFilter.addActionListener(filterListener);
        statusFilter.addActionListener(filterListener);
        locationFilter.addActionListener(filterListener);

        return card;
    }

    public void setSearchText(String text) {
        if (searchInput != null) {
            searchInput.setText(text != null ? text : "");
            applyFilters();
        }
    }

    private JPanel createFilterField(String label, JComponent comp) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_SMALL_BOLD);
        lbl.setForeground(Theme.TEXT_SECONDARY);
        p.add(lbl);
        p.add(comp);
        return p;
    }

    private void styleComboBox(JComboBox<String> box) {
        box.setFont(Theme.FONT_BODY);
        box.setPreferredSize(new Dimension(140, 32));
        box.setBackground(Theme.SURFACE_CARD);
    }

    private JScrollPane createGridViewSection() {
        cardsGridContainer = new JPanel();
        cardsGridContainer.setLayout(new GridBagLayout());
        cardsGridContainer.setBackground(Theme.BG_APP);

        JPanel outerWrapper = new JPanel(new BorderLayout());
        outerWrapper.setBackground(Theme.BG_APP);
        outerWrapper.add(cardsGridContainer, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(outerWrapper);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Theme.BG_APP);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        return scrollPane;
    }

    private JPanel createEmployeeSquareCard(Employee emp) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Theme.SURFACE_CARD);
        card.setPreferredSize(new Dimension(260, 290));
        card.setMinimumSize(new Dimension(220, 270));
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER_LIGHT, 1, true),
            new EmptyBorder(14, 14, 14, 14)
        ));

        JPanel topRow = new JPanel(new BorderLayout(8, 0));
        topRow.setOpaque(false);

        // Display photo or generated avatar
        JLabel avatarLabel;
        if (emp.getCustomPhoto() != null) {
            avatarLabel = new JLabel(emp.getCustomPhoto());
        } else {
            Color[] grad = emp.getAvatarColors() != null ? emp.getAvatarColors() : AVATAR_GRADIENTS[0];
            ImageIcon avatarIcon = IconFactory.createAvatarIcon(emp.getName(), 56, 10, grad[0], grad[1]);
            avatarLabel = new JLabel(avatarIcon);
        }
        avatarLabel.setPreferredSize(new Dimension(56, 56));
        avatarLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        avatarLabel.setToolTipText("Click to view full record");
        avatarLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showEmployeeDetailsModal(emp);
            }
        });

        JLabel statusBadge = createStatusBadgeLabel(emp.getStatus());
        JPanel badgeWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        badgeWrapper.setOpaque(false);
        badgeWrapper.add(statusBadge);

        topRow.add(avatarLabel, BorderLayout.WEST);
        topRow.add(badgeWrapper, BorderLayout.NORTH);
        card.add(topRow);
        card.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel nameLabel = new JLabel("<html><body style='width: 100%;'><b>" + emp.getName() + "</b></body></html>");
        nameLabel.setFont(Theme.FONT_SUBTITLE);
        nameLabel.setForeground(Theme.TEXT_PRIMARY);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel roleLabel = new JLabel("<html><body style='width: 100%;'>" + emp.getRole() + "</body></html>");
        roleLabel.setFont(Theme.FONT_SMALL_BOLD);
        roleLabel.setForeground(Theme.PRIMARY);
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel deptLabel = new JLabel(emp.getDepartment() + " • " + emp.getId());
        deptLabel.setFont(Theme.FONT_SMALL);
        deptLabel.setForeground(Theme.TEXT_MUTED);
        deptLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(nameLabel);
        card.add(Box.createRigidArea(new Dimension(0, 2)));
        card.add(roleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 2)));
        card.add(deptLabel);
        card.add(Box.createRigidArea(new Dimension(0, 8)));

        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER_LIGHT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(sep);
        card.add(Box.createRigidArea(new Dimension(0, 8)));

        card.add(createCardInfoRow(IconFactory.IconType.LOCATION, emp.getLocation()));
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(createCardInfoRow(IconFactory.IconType.MAIL, emp.getEmail()));
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(createCardInfoRow(IconFactory.IconType.PHONE, emp.getPhone()));

        card.add(Box.createVerticalGlue());
        card.add(Box.createRigidArea(new Dimension(0, 8)));

        JButton viewBtn = new JButton("View & Manage");
        viewBtn.setFont(Theme.FONT_SMALL_BOLD);
        viewBtn.setForeground(Theme.PRIMARY);
        viewBtn.setBackground(Theme.PRIMARY_LIGHT);
        viewBtn.setFocusPainted(false);
        viewBtn.setBorder(BorderFactory.createLineBorder(Theme.PRIMARY, 1, true));
        viewBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        viewBtn.setPreferredSize(new Dimension(220, 30));
        viewBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        viewBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewBtn.addActionListener(e -> showEmployeeDetailsModal(emp));

        card.add(viewBtn);
        return card;
    }

    private JPanel createCardInfoRow(IconFactory.IconType iconType, String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel iconLbl = new JLabel(IconFactory.createIcon(iconType, 13, Theme.TEXT_MUTED));
        JLabel textLbl = new JLabel("<html><body style='width: 100%;'>" + text + "</body></html>");
        textLbl.setFont(Theme.FONT_SMALL);
        textLbl.setForeground(Theme.TEXT_SECONDARY);

        p.add(iconLbl);
        p.add(textLbl);
        return p;
    }

    private JLabel createStatusBadgeLabel(String status) {
        JLabel badge = new JLabel("  " + status + "  ", SwingConstants.CENTER);
        badge.setFont(Theme.FONT_SMALL_BOLD);
        badge.setOpaque(true);

        if ("Active".equalsIgnoreCase(status)) {
            badge.setBackground(Theme.STATUS_APPROVED_BG);
            badge.setForeground(Theme.STATUS_APPROVED_FG);
        } else if ("On Leave".equalsIgnoreCase(status)) {
            badge.setBackground(Theme.STATUS_PENDING_BG);
            badge.setForeground(Theme.STATUS_PENDING_FG);
        } else if ("Remote".equalsIgnoreCase(status)) {
            badge.setBackground(new Color(0xDB, 0xEA, 0xFE));
            badge.setForeground(new Color(0x1D, 0x4E, 0xD8));
        } else {
            badge.setBackground(Theme.STATUS_REJECTED_BG);
            badge.setForeground(Theme.STATUS_REJECTED_FG);
        }
        badge.setBorder(new LineBorder(badge.getBackground(), 1, true));
        return badge;
    }

    private JScrollPane createTableViewSection() {
        String[] columns = {"ID", "Employee Profile", "Department", "Location", "Contact Email", "Join Date", "Status", "Actions"};

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFont(Theme.FONT_BODY);
        table.setForeground(Theme.TEXT_PRIMARY);
        table.setRowHeight(54);
        table.setShowVerticalLines(false);
        table.setGridColor(Theme.BORDER_LIGHT);
        table.setSelectionBackground(Theme.PRIMARY_LIGHT);
        table.setSelectionForeground(Theme.TEXT_PRIMARY);

        table.getTableHeader().setFont(Theme.FONT_SMALL_BOLD);
        table.getTableHeader().setForeground(Theme.TEXT_SECONDARY);
        table.getTableHeader().setBackground(new Color(0xF1, 0xF5, 0xF9));
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));

        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setPreferredWidth(180);
        table.getColumnModel().getColumn(5).setPreferredWidth(95);
        table.getColumnModel().getColumn(6).setPreferredWidth(95);
        table.getColumnModel().getColumn(7).setPreferredWidth(115);

        table.getColumnModel().getColumn(1).setCellRenderer(new TableAvatarNameRenderer());
        table.getColumnModel().getColumn(6).setCellRenderer(new LeaveRequestsPanel.StatusBadgeRenderer());
        table.getColumnModel().getColumn(7).setCellRenderer(new ActionButtonsCellRenderer());

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    String empId = (String) table.getValueAt(row, 0);
                    Employee emp = EmployeeDAO.getInstance().getById(empId);
                    if (emp != null) {
                        showEmployeeDetailsModal(emp);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1, true));
        scrollPane.getViewport().setBackground(Theme.SURFACE_CARD);

        return scrollPane;
    }

    public void applyFilters() {
        String selectedDept = (String) deptFilter.getSelectedItem();
        String selectedStatus = (String) statusFilter.getSelectedItem();
        String selectedLocation = (String) locationFilter.getSelectedItem();
        String keyword = searchInput.getText().trim();

        List<Employee> filtered = EmployeeDAO.getInstance().search(selectedDept, selectedStatus, selectedLocation, keyword);

        // Sync legacy static list
        employeeList.clear();
        for (Employee e : EmployeeDAO.getInstance().getAll()) {
            employeeList.add(new EmployeeRecord(e));
        }

        tableModel.setRowCount(0);
        cardsGridContainer.removeAll();

        int matched = 0;
        int gridColumns = 4;

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;

        int row = 0;
        int col = 0;

        for (Employee emp : filtered) {
            matched++;

            tableModel.addRow(new Object[]{
                emp.getId(),
                emp,
                emp.getDepartment(),
                emp.getLocation(),
                emp.getEmail(),
                emp.getJoinDate(),
                emp.getStatus(),
                "View Record"
            });

            gbc.gridx = col;
            gbc.gridy = row;
            cardsGridContainer.add(createEmployeeSquareCard(emp), gbc);

            col++;
            if (col >= gridColumns) {
                col = 0;
                row++;
            }
        }

        matchCountLabel.setText("Showing " + matched + " of " + EmployeeDAO.getInstance().getAll().size() + " employees");
        cardsGridContainer.revalidate();
        cardsGridContainer.repaint();
    }

    public void search(String keyword) {
        if (searchInput != null) {
            searchInput.setText(keyword != null ? keyword : "");
            applyFilters();
        }
    }

    public static EmployeeRecord findEmployeeById(String id) {
        Employee emp = EmployeeDAO.getInstance().getById(id);
        return emp != null ? new EmployeeRecord(emp) : null;
    }

    private void showEmployeeDetailsModal(Employee emp) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Employee Record - " + emp.getName(), true);
        dialog.setSize(680, 620);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBackground(Theme.BG_APP);
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Header with Avatar & Actions
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        leftHeader.setOpaque(false);

        JLabel avatarLbl;
        if (emp.getCustomPhoto() != null) {
            avatarLbl = new JLabel(emp.getCustomPhoto());
        } else {
            Color[] grad = emp.getAvatarColors() != null ? emp.getAvatarColors() : AVATAR_GRADIENTS[0];
            avatarLbl = new JLabel(IconFactory.createAvatarIcon(emp.getName(), 64, 12, grad[0], grad[1]));
        }

        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.setOpaque(false);

        JLabel nameLbl = new JLabel(emp.getName() + " (" + emp.getId() + ")");
        nameLbl.setFont(Theme.FONT_TITLE);
        nameLbl.setForeground(Theme.TEXT_PRIMARY);

        JLabel roleLbl = new JLabel(emp.getRole() + " • " + emp.getDepartment());
        roleLbl.setFont(Theme.FONT_BODY_BOLD);
        roleLbl.setForeground(Theme.PRIMARY);

        namePanel.add(nameLbl);
        namePanel.add(Box.createRigidArea(new Dimension(0, 4)));
        namePanel.add(roleLbl);

        leftHeader.add(avatarLbl);
        leftHeader.add(namePanel);
        header.add(leftHeader, BorderLayout.WEST);

        // Header Right Buttons (Photo, Edit, Delete)
        JPanel headActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 10));
        headActions.setOpaque(false);

        JButton changePhotoBtn = Theme.createSecondaryButton("📷 Photo");
        changePhotoBtn.setBorder(BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1, true));
        changePhotoBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int res = chooser.showOpenDialog(dialog);
            if (res == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                ImageIcon img = new ImageIcon(file.getAbsolutePath());
                Image scaled = img.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                emp.setCustomPhoto(new ImageIcon(scaled));
                dialog.dispose();
                applyFilters();
                showEmployeeDetailsModal(emp);
            }
        });

        JButton editEmpBtn = Theme.createPrimaryButton("✏️ Edit");
        editEmpBtn.addActionListener(e -> {
            dialog.dispose();
            showEditEmployeeDialog(emp);
        });

        // Destructive Delete Button
        JButton deleteBtn = new JButton("🗑️ Delete");
        deleteBtn.setFont(Theme.FONT_BODY_BOLD);
        deleteBtn.setForeground(Theme.STATUS_REJECTED_FG);
        deleteBtn.setBackground(Theme.STATUS_REJECTED_BG);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.STATUS_REJECTED_FG, 1, true),
            new EmptyBorder(6, 12, 6, 12)
        ));
        deleteBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(dialog,
                "Are you sure you want to delete employee:\n" + emp.getName() + " (" + emp.getId() + ")?\n\nThis operation cannot be undone.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    boolean success = EmployeeDAO.getInstance().delete(emp.getId());
                    if (success) {
                        dialog.dispose();
                        applyFilters();
                        JOptionPane.showMessageDialog(this,
                            "Employee " + emp.getName() + " was deleted successfully.",
                            "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Could not delete employee record.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Error deleting record:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        headActions.add(changePhotoBtn);
        headActions.add(editEmpBtn);
        headActions.add(deleteBtn);
        header.add(headActions, BorderLayout.EAST);

        root.add(header, BorderLayout.NORTH);

        // 4 Details Sections Grid
        JPanel grid = new JPanel(new GridLayout(2, 2, 14, 14));
        grid.setOpaque(false);

        grid.add(createCardSection("Employment Info", new String[][]{
            {"Department:", emp.getDepartment()},
            {"Location:", emp.getLocation()},
            {"Joined Date:", emp.getJoinDate()},
            {"Status:", emp.getStatus()}
        }));

        grid.add(createCardSection("Contact Information", new String[][]{
            {"Work Email:", emp.getEmail()},
            {"Phone:", emp.getPhone()},
            {"Emergency Contact:", emp.getEmergency()}
        }));

        grid.add(createCardSection("Compensation & Payroll", new String[][]{
            {"Salary / Rate:", emp.getSalary()},
            {"Payment Cycle:", "Bi-Weekly"},
            {"Tax Exempt:", "Standard"}
        }));

        grid.add(createCardSection("System & Access", new String[][]{
            {"Badge ID:", emp.getId()},
            {"Access Tier:", "Level 2 (Standard)"},
            {"Directory Sync:", "Active"}
        }));

        root.add(grid, BorderLayout.CENTER);

        // Close Button
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottom.setOpaque(false);
        JButton closeBtn = Theme.createPrimaryButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        bottom.add(closeBtn);
        root.add(bottom, BorderLayout.SOUTH);

        dialog.add(root);
        dialog.setVisible(true);
    }

    private JPanel createCardSection(String title, String[][] data) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Theme.SURFACE_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER_LIGHT, 1, true),
            new EmptyBorder(12, 14, 12, 14)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 4, 3, 10);

        JLabel t = new JLabel(title);
        t.setFont(Theme.FONT_SUBTITLE);
        t.setForeground(Theme.TEXT_PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        card.add(t, gbc);

        gbc.gridy++;
        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER_LIGHT);
        card.add(sep, gbc);

        gbc.gridwidth = 1;
        for (String[] row : data) {
            gbc.gridy++;
            gbc.gridx = 0;
            gbc.weightx = 0.4;
            JLabel k = new JLabel(row[0]);
            k.setFont(Theme.FONT_SMALL);
            k.setForeground(Theme.TEXT_SECONDARY);
            card.add(k, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.6;
            JLabel v = new JLabel(row[1]);
            v.setFont(Theme.FONT_SMALL_BOLD);
            v.setForeground(Theme.TEXT_PRIMARY);
            card.add(v, gbc);
        }

        return card;
    }

    private void showEditEmployeeDialog(Employee emp) {
        JTextField nameField = new JTextField(emp.getName());
        JTextField roleField = new JTextField(emp.getRole());
        JComboBox<String> deptBox = new JComboBox<>(new String[]{"Engineering", "Human Resources", "Design", "Marketing", "Sales", "Finance", "Operations"});
        deptBox.setSelectedItem(emp.getDepartment());
        JComboBox<String> locBox = new JComboBox<>(new String[]{"Bengaluru, KA", "Mumbai, MH", "Delhi NCR", "Hyderabad, TS", "Pune, MH", "Remote"});
        locBox.setSelectedItem(emp.getLocation());
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"Active", "On Leave", "Remote", "Terminated"});
        statusBox.setSelectedItem(emp.getStatus());
        JTextField emailField = new JTextField(emp.getEmail());
        JTextField phoneField = new JTextField(emp.getPhone());
        JTextField salaryField = new JTextField(emp.getSalary());
        JTextField emergencyField = new JTextField(emp.getEmergency());

        JPanel panel = new JPanel(new GridLayout(9, 2, 8, 8));
        panel.add(new JLabel("Full Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Job Title:"));
        panel.add(roleField);
        panel.add(new JLabel("Department:"));
        panel.add(deptBox);
        panel.add(new JLabel("Location:"));
        panel.add(locBox);
        panel.add(new JLabel("Status:"));
        panel.add(statusBox);
        panel.add(new JLabel("Email (@gmail.com required):"));
        panel.add(emailField);
        panel.add(new JLabel("Phone:"));
        panel.add(phoneField);
        panel.add(new JLabel("Salary:"));
        panel.add(salaryField);
        panel.add(new JLabel("Emergency Contact:"));
        panel.add(emergencyField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Employee - " + emp.getName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String role = roleField.getText().trim();
            String dept = (String) deptBox.getSelectedItem();
            String loc = (String) locBox.getSelectedItem();
            String status = (String) statusBox.getSelectedItem();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String salary = salaryField.getText().trim();
            String emergency = emergencyField.getText().trim();

            // Validate
            ValidationUtil.Result vName = ValidationUtil.validateName(name);
            if (!vName.isValid()) {
                JOptionPane.showMessageDialog(this, vName.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            ValidationUtil.Result vEmail = ValidationUtil.validateEmail(email);
            if (!vEmail.isValid()) {
                JOptionPane.showMessageDialog(this, vEmail.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            ValidationUtil.Result vPhone = ValidationUtil.validatePhone(phone);
            if (!vPhone.isValid()) {
                JOptionPane.showMessageDialog(this, vPhone.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            ValidationUtil.Result vSalary = ValidationUtil.validateSalary(salary);
            if (!vSalary.isValid()) {
                JOptionPane.showMessageDialog(this, vSalary.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            emp.setName(name);
            emp.setRole(role.isEmpty() ? "Specialist" : role);
            emp.setDepartment(dept);
            emp.setLocation(loc);
            emp.setStatus(status);
            emp.setEmail(email);
            emp.setPhone(phone);
            emp.setSalary(salary);
            emp.setEmergency(emergency.isEmpty() ? "N/A" : emergency);

            try {
                EmployeeDAO.getInstance().update(emp);
                applyFilters();
                JOptionPane.showMessageDialog(this, "Employee details updated successfully!", "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to update employee:\n" + ex.getMessage(), "Update Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Reusable Add New Employee dialog with full validation, ID generation, and photo chooser.
     */
    public static void showAddEmployeeDialog(Component parent, Runnable onAdded) {
        String generatedId = EmployeeDAO.getInstance().generateNextId();

        JTextField idField = new JTextField(generatedId);
        idField.setEditable(false);
        JTextField nameField = new JTextField();
        JTextField roleField = new JTextField();
        JComboBox<String> deptBox = new JComboBox<>(new String[]{"Engineering", "Human Resources", "Design", "Marketing", "Sales", "Finance", "Operations"});
        JComboBox<String> locBox = new JComboBox<>(new String[]{"Bengaluru, KA", "Mumbai, MH", "Delhi NCR", "Hyderabad, TS", "Pune, MH", "Remote"});
        JTextField emailField = new JTextField("@gmail.com");
        JTextField phoneField = new JTextField("+91 98765 ");
        JTextField salaryField = new JTextField("₹12,00,000");
        JTextField emergencyField = new JTextField();

        final ImageIcon[] chosenPhoto = {null};
        JButton choosePhotoBtn = new JButton("Browse Photo File...");
        choosePhotoBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int res = chooser.showOpenDialog(parent);
            if (res == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                ImageIcon img = new ImageIcon(file.getAbsolutePath());
                Image scaled = img.getImage().getScaledInstance(56, 56, Image.SCALE_SMOOTH);
                chosenPhoto[0] = new ImageIcon(scaled);
                choosePhotoBtn.setText("Photo: " + file.getName());
            }
        });

        JPanel panel = new JPanel(new GridLayout(10, 2, 8, 8));
        panel.add(new JLabel("Assigned Employee ID:"));
        panel.add(idField);
        panel.add(new JLabel("Full Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Job Title:"));
        panel.add(roleField);
        panel.add(new JLabel("Department:"));
        panel.add(deptBox);
        panel.add(new JLabel("Location:"));
        panel.add(locBox);
        panel.add(new JLabel("Email (@gmail.com required):"));
        panel.add(emailField);
        panel.add(new JLabel("Phone:"));
        panel.add(phoneField);
        panel.add(new JLabel("Base Salary:"));
        panel.add(salaryField);
        panel.add(new JLabel("Emergency Contact:"));
        panel.add(emergencyField);
        panel.add(new JLabel("Profile Photo:"));
        panel.add(choosePhotoBtn);

        int result = JOptionPane.showConfirmDialog(parent, panel, "Register New Employee", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String role = roleField.getText().trim();
            String dept = (String) deptBox.getSelectedItem();
            String loc = (String) locBox.getSelectedItem();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String salary = salaryField.getText().trim();
            String emergency = emergencyField.getText().trim();

            // Validation
            ValidationUtil.Result vName = ValidationUtil.validateName(name);
            if (!vName.isValid()) {
                JOptionPane.showMessageDialog(parent, vName.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            ValidationUtil.Result vEmail = ValidationUtil.validateEmail(email);
            if (!vEmail.isValid()) {
                JOptionPane.showMessageDialog(parent, vEmail.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            ValidationUtil.Result vPhone = ValidationUtil.validatePhone(phone);
            if (!vPhone.isValid()) {
                JOptionPane.showMessageDialog(parent, vPhone.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            ValidationUtil.Result vSalary = ValidationUtil.validateSalary(salary);
            if (!vSalary.isValid()) {
                JOptionPane.showMessageDialog(parent, vSalary.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Color[] grad = AVATAR_GRADIENTS[EmployeeDAO.getInstance().getAll().size() % AVATAR_GRADIENTS.length];
            Employee newEmp = new Employee(generatedId, name, role.isEmpty() ? "Specialist" : role, dept, loc,
                    email, phone, "Today", "Active", salary, emergency.isEmpty() ? "N/A" : emergency, grad);
            if (chosenPhoto[0] != null) {
                newEmp.setCustomPhoto(chosenPhoto[0]);
            }

            try {
                EmployeeDAO.getInstance().add(newEmp);
                if (onAdded != null) {
                    onAdded.run();
                }
                JOptionPane.showMessageDialog(parent,
                    "Employee " + name + " (" + generatedId + ") successfully registered!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parent,
                    "Failed to register employee:\n" + ex.getMessage(),
                    "Registration Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    static class TableAvatarNameRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
            p.setOpaque(true);
            p.setBackground(isSelected ? table.getSelectionBackground() : Theme.SURFACE_CARD);

            if (value instanceof Employee) {
                Employee emp = (Employee) value;

                JLabel avatarLbl;
                if (emp.getCustomPhoto() != null) {
                    avatarLbl = new JLabel(emp.getCustomPhoto());
                } else {
                    Color[] grad = emp.getAvatarColors() != null ? emp.getAvatarColors() : AVATAR_GRADIENTS[0];
                    ImageIcon avatar = IconFactory.createAvatarIcon(emp.getName(), 38, 8, grad[0], grad[1]);
                    avatarLbl = new JLabel(avatar);
                }

                JPanel textP = new JPanel();
                textP.setLayout(new BoxLayout(textP, BoxLayout.Y_AXIS));
                textP.setOpaque(false);

                JLabel nameLabel = new JLabel(emp.getName());
                nameLabel.setFont(Theme.FONT_BODY_BOLD);
                nameLabel.setForeground(Theme.TEXT_PRIMARY);

                JLabel roleLabel = new JLabel(emp.getRole());
                roleLabel.setFont(Theme.FONT_SMALL);
                roleLabel.setForeground(Theme.TEXT_SECONDARY);

                textP.add(nameLabel);
                textP.add(Box.createRigidArea(new Dimension(0, 2)));
                textP.add(roleLabel);

                p.add(avatarLbl);
                p.add(textP);
            }
            return p;
        }
    }

    static class ActionButtonsCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            JLabel lbl = new JLabel("View / Manage ↗", SwingConstants.CENTER);
            lbl.setFont(Theme.FONT_SMALL_BOLD);
            lbl.setForeground(Theme.PRIMARY);
            lbl.setOpaque(true);
            lbl.setBackground(isSelected ? table.getSelectionBackground() : Theme.SURFACE_CARD);
            return lbl;
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
