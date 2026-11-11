package hrms.ui;

import hrms.dao.LeaveDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Root Application Frame for the HR Management System.
 * Supports smooth cross-fade theme transitions, live profile header sync,
 * interactive search bar, dynamic notifications, and direct New Employee creation.
 */
public class MainFrame extends JFrame {

    private static final Object[][] NAV_ITEMS = {
        {"Dashboard", IconFactory.IconType.DASHBOARD},
        {"Employee Directory", IconFactory.IconType.EMPLOYEES},
        {"Leave Requests", IconFactory.IconType.LEAVES},
        {"Settings", IconFactory.IconType.SETTINGS}
    };
    
    private CardLayout rootCardLayout;
    private JPanel rootContainer;
    
    private CardLayout appCardLayout;
    private JPanel appContentPanel;
    private List<JButton> navButtons;
    private String currentCard = "Dashboard";
    
    private JButton btnProfile;
    private JTextField searchField;
    private DashboardPanel dashboardPanel;
    private EmployeeDirectoryPanel employeeDirectoryPanel;
    private LeaveRequestsPanel leaveRequestsPanel;
    private SettingsPanel settingsPanel;
    private UserProfilePanel userProfilePanel;
    
    private ThemeTransitionGlassPane transitionGlassPane;
    
    public MainFrame() {
        setTitle("HR Management System - Proton HR");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1260, 840);
        setMinimumSize(new Dimension(850, 550));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG_APP);

        // GlassPane for silky smooth transitions
        transitionGlassPane = new ThemeTransitionGlassPane();
        setGlassPane(transitionGlassPane);

        // Root CardLayout (LOGIN vs APP)
        rootCardLayout = new CardLayout();
        rootContainer = new JPanel(rootCardLayout);
        rootContainer.setBackground(Theme.BG_APP);

        // 1. Login Panel View
        LoginPanel loginPanel = new LoginPanel(() -> {
            showAppShell();
        });
        rootContainer.add(loginPanel, "LOGIN");

        // 2. Main App Shell View
        JPanel appShell = createAppShell();
        rootContainer.add(appShell, "APP");

        setContentPane(rootContainer);
        
        // Show Login initially
        rootCardLayout.show(rootContainer, "LOGIN");
    }

    private void showAppShell() {
        rootCardLayout.show(rootContainer, "APP");
        navigateToCard("Dashboard");
        updateHeaderProfileInfo();
    }

    private void showLoginScreen() {
        rootCardLayout.show(rootContainer, "LOGIN");
    }

    public void navigateToCard(String cardName) {
        currentCard = cardName;
        appCardLayout.show(appContentPanel, cardName);
        updateNavButtonStyles(cardName);

        // Refresh screen state dynamically
        if ("Dashboard".equals(cardName) && dashboardPanel != null) {
            dashboardPanel.refreshData();
        } else if ("Leave Requests".equals(cardName) && leaveRequestsPanel != null) {
            leaveRequestsPanel.refreshData();
        } else if ("Employee Directory".equals(cardName) && employeeDirectoryPanel != null) {
            employeeDirectoryPanel.applyFilters();
        } else if ("Settings".equals(cardName) && settingsPanel != null) {
            settingsPanel.buildUI();
        }
    }

    private JPanel createAppShell() {
        JPanel shell = new JPanel(new BorderLayout());
        shell.setBackground(Theme.BG_APP);

        // Sidebar (West)
        JPanel sidebar = createSidebar();
        shell.add(sidebar, BorderLayout.WEST);

        // Top Bar (North)
        JPanel topBar = createTopBar();
        shell.add(topBar, BorderLayout.NORTH);

        // Center Content Area with CardLayout
        appCardLayout = new CardLayout();
        appContentPanel = new JPanel(appCardLayout);
        appContentPanel.setBackground(Theme.BG_APP);

        // Initialize screens
        dashboardPanel = new DashboardPanel(() -> navigateToCard("Leave Requests"));
        employeeDirectoryPanel = new EmployeeDirectoryPanel();
        leaveRequestsPanel = new LeaveRequestsPanel();
        
        userProfilePanel = new UserProfilePanel(() -> {
            updateHeaderProfileInfo();
            if (settingsPanel != null) settingsPanel.buildUI();
        });

        settingsPanel = new SettingsPanel(
            () -> performSmoothThemeTransition(),
            () -> updateHeaderProfileInfo(),
            () -> navigateToCard("UserProfile")
        );

        appContentPanel.add(dashboardPanel, "Dashboard");
        appContentPanel.add(employeeDirectoryPanel, "Employee Directory");
        appContentPanel.add(leaveRequestsPanel, "Leave Requests");
        appContentPanel.add(settingsPanel, "Settings");
        appContentPanel.add(userProfilePanel, "UserProfile");

        JScrollPane scrollPane = new JScrollPane(appContentPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Theme.BG_APP);
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(18);

        shell.add(scrollPane, BorderLayout.CENTER);
        return shell;
    }

    /**
     * Executes a buttery smooth visual cross-fade animation when switching themes.
     */
    public void performSmoothThemeTransition() {
        // 1. Capture snapshot before change
        BufferedImage snapshotBefore = transitionGlassPane.capture(getContentPane());

        // 2. Apply theme and re-render components
        refreshAllThemeStyles();

        // 3. Capture snapshot of new theme state
        BufferedImage snapshotAfter = transitionGlassPane.capture(getContentPane());

        // 4. Start smooth cross-fade animation on glass pane
        transitionGlassPane.startTransition(snapshotBefore, snapshotAfter, null);
    }

    private void refreshAllThemeStyles() {
        getContentPane().setBackground(Theme.BG_APP);
        rootContainer.setBackground(Theme.BG_APP);
        appContentPanel.setBackground(Theme.BG_APP);
        
        // Rebuild screens with new theme palette
        if (dashboardPanel != null) {
            appContentPanel.remove(dashboardPanel);
            dashboardPanel = new DashboardPanel(() -> navigateToCard("Leave Requests"));
            appContentPanel.add(dashboardPanel, "Dashboard");
        }
        if (employeeDirectoryPanel != null) {
            appContentPanel.remove(employeeDirectoryPanel);
            employeeDirectoryPanel = new EmployeeDirectoryPanel();
            appContentPanel.add(employeeDirectoryPanel, "Employee Directory");
        }
        if (leaveRequestsPanel != null) {
            appContentPanel.remove(leaveRequestsPanel);
            leaveRequestsPanel = new LeaveRequestsPanel();
            appContentPanel.add(leaveRequestsPanel, "Leave Requests");
        }
        if (settingsPanel != null) {
            appContentPanel.remove(settingsPanel);
            settingsPanel = new SettingsPanel(
                () -> performSmoothThemeTransition(),
                () -> updateHeaderProfileInfo(),
                () -> navigateToCard("UserProfile")
            );
            appContentPanel.add(settingsPanel, "Settings");
        }
        if (userProfilePanel != null) {
            userProfilePanel.buildUI();
        }

        appCardLayout.show(appContentPanel, currentCard);
        SwingUtilities.updateComponentTreeUI(this);
        updateHeaderProfileInfo();
    }

    private void updateHeaderProfileInfo() {
        if (btnProfile != null) {
            btnProfile.setText(" " + UserProfilePanel.currentUserName);
            if (UserProfilePanel.customAvatarIcon != null) {
                Image scaled = UserProfilePanel.customAvatarIcon.getImage().getScaledInstance(22, 22, Image.SCALE_SMOOTH);
                btnProfile.setIcon(new ImageIcon(scaled));
            } else {
                btnProfile.setIcon(IconFactory.createAvatarIcon(UserProfilePanel.currentUserName, 22, 4, 
                    UserProfilePanel.avatarColors[0], UserProfilePanel.avatarColors[1]));
            }
        }
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(250, 0));
        sidebar.setMinimumSize(new Dimension(220, 0));
        sidebar.setBorder(new EmptyBorder(24, 16, 24, 16));
        
        // Logo
        JLabel logo = new JLabel("Proton HR");
        logo.setIcon(IconFactory.createIcon(IconFactory.IconType.DASHBOARD, 24, Theme.PRIMARY));
        logo.setIconTextGap(10);
        logo.setFont(new Font(Theme.FONT_FAMILY, Font.BOLD, 22));
        logo.setForeground(Theme.TEXT_ON_PRIMARY);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        sidebar.add(logo);
        sidebar.add(Box.createRigidArea(new Dimension(0, 26)));
        
        // Navigation Buttons
        navButtons = new ArrayList<>();

        for (Object[] itemData : NAV_ITEMS) {
            String item = (String) itemData[0];
            IconFactory.IconType iconType = (IconFactory.IconType) itemData[1];

            JButton btn = new JButton("  " + item);
            btn.setIcon(IconFactory.createIcon(iconType, 18, Theme.TEXT_SIDEBAR));
            btn.setFont(Theme.FONT_BODY_BOLD);
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setFocusPainted(false);
            btn.setBackground(Theme.SIDEBAR_BG);
            btn.setForeground(Theme.TEXT_SIDEBAR);
            btn.setBorder(new EmptyBorder(8, 14, 8, 14));
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            btn.addActionListener((ActionEvent e) -> {
                navigateToCard(item);
            });
            
            navButtons.add(btn);
            sidebar.add(btn);
            sidebar.add(Box.createRigidArea(new Dimension(0, 6)));
        }
        
        updateNavButtonStyles("Dashboard");

        sidebar.add(Box.createVerticalGlue());
        
        // New Employee Button (Opens Add Employee dialog with @gmail.com validation)
        JButton newEmpBtn = new JButton("  New Employee");
        newEmpBtn.setIcon(IconFactory.createIcon(IconFactory.IconType.PLUS, 16, Theme.TEXT_ON_PRIMARY));
        newEmpBtn.setFont(Theme.FONT_BODY_BOLD);
        newEmpBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        newEmpBtn.setPreferredSize(new Dimension(228, 42));
        newEmpBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        newEmpBtn.setBackground(Theme.PRIMARY);
        newEmpBtn.setForeground(Theme.TEXT_ON_PRIMARY);
        newEmpBtn.setFocusPainted(false);
        newEmpBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        newEmpBtn.setBorder(new EmptyBorder(8, 14, 8, 14));
        newEmpBtn.addActionListener(e -> {
            EmployeeDirectoryPanel.showAddEmployeeDialog(MainFrame.this, () -> {
                navigateToCard("Employee Directory");
            });
        });
        sidebar.add(newEmpBtn);
        
        return sidebar;
    }

    private void updateNavButtonStyles(String activeItem) {
        if (navButtons == null) return;
        for (int i = 0; i < navButtons.size(); i++) {
            JButton btn = navButtons.get(i);
            String item = (String) NAV_ITEMS[i][0];
            IconFactory.IconType iconType = (IconFactory.IconType) NAV_ITEMS[i][1];

            if (item.equals(activeItem)) {
                btn.setBackground(new Color(0x1E, 0x29, 0x3B));
                btn.setForeground(Theme.TEXT_SIDEBAR_ACTIVE);
                btn.setIcon(IconFactory.createIcon(iconType, 18, Theme.PRIMARY));
            } else {
                btn.setBackground(Theme.SIDEBAR_BG);
                btn.setForeground(Theme.TEXT_SIDEBAR);
                btn.setIcon(IconFactory.createIcon(iconType, 18, Theme.TEXT_SIDEBAR));
            }
        }
    }
    
    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout(12, 0));
        topBar.setBackground(Theme.TOPBAR_BG);
        topBar.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, Theme.BORDER_LIGHT),
            new EmptyBorder(10, 20, 10, 20)
        ));
        
        // Search Container
        JPanel searchContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchContainer.setOpaque(false);
        
        JLabel searchIcon = new JLabel(IconFactory.createIcon(IconFactory.IconType.SEARCH, 18, Theme.TEXT_MUTED));
        searchIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        String placeholderText = "Search employees, leaves, reports...";
        searchField = new JTextField(placeholderText);
        searchField.putClientProperty("JTextField.placeholderText", placeholderText);
        searchField.setFont(Theme.FONT_BODY);
        searchField.setForeground(Theme.TEXT_MUTED);
        searchField.setPreferredSize(new Dimension(280, 36));
        searchField.setMinimumSize(new Dimension(140, 36));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1, true),
            new EmptyBorder(6, 12, 6, 12)
        ));
        
        Runnable executeSearch = () -> {
            String text = searchField.getText().trim();
            if (!text.isEmpty() && !text.equals(placeholderText)) {
                navigateToCard("Employee Directory");
                if (employeeDirectoryPanel != null) {
                    employeeDirectoryPanel.setSearchText(text);
                }
            }
        };

        searchField.addActionListener(e -> executeSearch.run());

        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (searchField.getText().equals(placeholderText)) {
                    searchField.setText("");
                    searchField.setForeground(Theme.TEXT_PRIMARY);
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (searchField.getText().trim().isEmpty()) {
                    searchField.setText(placeholderText);
                    searchField.setForeground(Theme.TEXT_MUTED);
                }
            }
        });

        searchIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                executeSearch.run();
            }
        });
        
        searchContainer.add(searchIcon);
        searchContainer.add(searchField);
        topBar.add(searchContainer, BorderLayout.WEST);
        
        // Action Icons
        JPanel iconsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        iconsPanel.setOpaque(false);
        
        JButton btnBell = createIconButton(IconFactory.IconType.BELL, 20, "Notifications", e -> {
            int pendingCount = LeaveDAO.getInstance().getPendingCount();
            String msg = "Notifications Center:\n\n"
                       + "• " + pendingCount + " Leave Request(s) awaiting your review\n"
                       + "• Monthly payroll disbursement is ready for processing\n"
                       + "• System database synchronized";

            Object[] options = {"Review Leave Requests", "Dismiss"};
            int chosen = JOptionPane.showOptionDialog(
                MainFrame.this,
                msg,
                "Notifications (" + pendingCount + " Pending)",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
            );

            if (chosen == JOptionPane.YES_OPTION) {
                navigateToCard("Leave Requests");
            }
        });
        
        JButton btnHelp = createIconButton(IconFactory.IconType.HELP, 20, "Help & Support", e -> {
            JOptionPane.showMessageDialog(MainFrame.this, 
                "HRMS Support Desk\n\n"
                + "Email: support@protonhr.com\n"
                + "Toll-Free Phone: 1-800-555-HRMS\n"
                + "Documentation: https://docs.protonhr.internal\n\n"
                + "Support engineers are available 24 hours a day, 7 days a week.", 
                "Help & Support", JOptionPane.INFORMATION_MESSAGE);
        });
        
        // Dynamic Profile Button in Top Bar
        btnProfile = new JButton(" " + UserProfilePanel.currentUserName);
        btnProfile.setIcon(IconFactory.createAvatarIcon(UserProfilePanel.currentUserName, 22, 4, 
            UserProfilePanel.avatarColors[0], UserProfilePanel.avatarColors[1]));
        btnProfile.setFont(Theme.FONT_BODY_BOLD);
        btnProfile.setForeground(Theme.TEXT_PRIMARY);
        btnProfile.setBackground(Theme.SURFACE_CARD);
        btnProfile.setFocusPainted(false);
        btnProfile.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnProfile.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1, true),
            new EmptyBorder(6, 14, 6, 14)
        ));
        btnProfile.addActionListener(e -> {
            navigateToCard("UserProfile");
        });

        // Sign Out Button
        JButton btnSignOut = new JButton("Sign Out");
        btnSignOut.setFont(Theme.FONT_SMALL_BOLD);
        btnSignOut.setForeground(Theme.STATUS_REJECTED_FG);
        btnSignOut.setBackground(Theme.STATUS_REJECTED_BG);
        btnSignOut.setFocusPainted(false);
        btnSignOut.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSignOut.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.STATUS_REJECTED_FG, 1, true),
            new EmptyBorder(6, 12, 6, 12)
        ));
        btnSignOut.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(MainFrame.this, 
                "Are you sure you want to sign out?", "Sign Out", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                showLoginScreen();
            }
        });
        
        iconsPanel.add(btnBell);
        iconsPanel.add(btnHelp);
        iconsPanel.add(btnProfile);
        iconsPanel.add(btnSignOut);
        
        topBar.add(iconsPanel, BorderLayout.EAST);
        
        return topBar;
    }

    private JButton createIconButton(IconFactory.IconType type, int size, String tooltip, java.awt.event.ActionListener action) {
        JButton btn = new JButton(IconFactory.createIcon(type, size, Theme.TEXT_SECONDARY));
        btn.setToolTipText(tooltip);
        btn.setPreferredSize(new Dimension(36, 36));
        btn.setBackground(Theme.SURFACE_CARD);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1, true),
            new EmptyBorder(6, 6, 6, 6)
        ));
        btn.addActionListener(action);
        return btn;
    }
}
