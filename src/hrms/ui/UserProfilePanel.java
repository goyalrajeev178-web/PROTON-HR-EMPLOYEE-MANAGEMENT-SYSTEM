package hrms.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;

/**
 * Interactive User Profile screen with photo upload and editable profile fields.
 */
public class UserProfilePanel extends JPanel implements Scrollable {

    public static String currentUserName = "Rajesh Sharma";
    public static String currentUserRole = "Senior Software Engineer";
    public static String currentUserAge = "32";
    public static String currentUserEmail = "rajesh.sharma@gmail.com";
    public static String currentUserPhone = "+91 98765 43210";
    public static String currentUserAddress = "123 Tech Park, Whitefield, Bengaluru, KA";
    public static String currentUserDept = "Engineering";
    public static String currentUserDOB = "1994-08-22";
    public static String currentUserGender = "Male";
    public static String currentUserSalary = "₹18,00,000 / year";
    public static String currentUserBank = "HDFC Bank ****1234";

    public static ImageIcon customAvatarIcon = null;
    public static Color[] avatarColors = {new Color(0x3B, 0x82, 0xF6), new Color(0x1D, 0x4E, 0xD8)};

    private JLabel photoLabel;
    private JLabel nameLabel;
    private JLabel roleLabel;
    private JLabel ageLabel;
    private JPanel detailsGrid;
    private Runnable onProfileUpdated;

    public UserProfilePanel(Runnable onProfileUpdated) {
        this.onProfileUpdated = onProfileUpdated;
        buildUI();
    }

    public UserProfilePanel() {
        this(null);
    }

    public void setOnProfileUpdated(Runnable onProfileUpdated) {
        this.onProfileUpdated = onProfileUpdated;
    }

    public void buildUI() {
        removeAll();
        setLayout(new BorderLayout(16, 16));
        setBackground(Theme.BG_APP);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        // 1. Header Section: Photo, Name, Role & Edit Actions
        JPanel headerPanel = new JPanel(new BorderLayout(16, 0));
        headerPanel.setOpaque(false);

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0));
        leftHeader.setOpaque(false);

        // Photo / Avatar with Change Photo Overlay
        if (customAvatarIcon != null) {
            photoLabel = new JLabel(customAvatarIcon);
        } else {
            photoLabel = new JLabel(IconFactory.createAvatarIcon(currentUserName, 76, 14, avatarColors[0], avatarColors[1]));
        }
        photoLabel.setPreferredSize(new Dimension(76, 76));
        photoLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        photoLabel.setToolTipText("Click to change profile picture");
        photoLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                changePhotoDialog();
            }
        });

        JPanel namePanel = new JPanel();
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));
        namePanel.setOpaque(false);

        nameLabel = new JLabel(currentUserName);
        nameLabel.setFont(Theme.FONT_TITLE);
        nameLabel.setForeground(Theme.TEXT_PRIMARY);

        roleLabel = new JLabel(currentUserRole + " • " + currentUserDept);
        roleLabel.setFont(Theme.FONT_BODY_BOLD);
        roleLabel.setForeground(Theme.PRIMARY);

        ageLabel = new JLabel("Age: " + currentUserAge + "  •  " + currentUserEmail);
        ageLabel.setFont(Theme.FONT_BODY);
        ageLabel.setForeground(Theme.TEXT_SECONDARY);

        namePanel.add(nameLabel);
        namePanel.add(Box.createRigidArea(new Dimension(0, 4)));
        namePanel.add(roleLabel);
        namePanel.add(Box.createRigidArea(new Dimension(0, 4)));
        namePanel.add(ageLabel);

        leftHeader.add(photoLabel);
        leftHeader.add(namePanel);
        headerPanel.add(leftHeader, BorderLayout.WEST);

        // Header Action Buttons
        JPanel actionHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionHeader.setOpaque(false);

        JButton changePhotoBtn = Theme.createSecondaryButton("📷 Change Photo");
        changePhotoBtn.setBorder(BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1, true));
        changePhotoBtn.addActionListener(e -> changePhotoDialog());

        JButton editBtn = Theme.createPrimaryButton("✏️ Edit Profile Details");
        editBtn.addActionListener(e -> showEditProfileDialog());

        actionHeader.add(changePhotoBtn);
        actionHeader.add(editBtn);
        headerPanel.add(actionHeader, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // 2. Details Grid (4 Sections)
        detailsGrid = new JPanel(new GridLayout(2, 2, 16, 16));
        detailsGrid.setOpaque(false);
        detailsGrid.setMinimumSize(new Dimension(0, 0));

        refreshDetailsGrid();
        add(detailsGrid, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private void refreshDetailsGrid() {
        detailsGrid.removeAll();

        // Contact Details
        detailsGrid.add(createSectionPanel("Contact Details", new String[][]{
            {"Work Email:", currentUserEmail},
            {"Phone:", currentUserPhone},
            {"Work Location / Address:", currentUserAddress}
        }));

        // Employment Details
        detailsGrid.add(createSectionPanel("Employment Details", new String[][]{
            {"Employee ID:", "EMP-1001"},
            {"Job Title:", currentUserRole},
            {"Department:", currentUserDept},
            {"Manager:", "Kavita Rao"}
        }));

        // Personal Details
        detailsGrid.add(createSectionPanel("Personal Details", new String[][]{
            {"Date of Birth:", currentUserDOB},
            {"Age:", currentUserAge},
            {"Gender:", currentUserGender},
            {"Emergency Contact:", "Sunita Sharma (Spouse) - 98765-43210"}
        }));

        // Payroll & Compensation
        detailsGrid.add(createSectionPanel("Payroll & Compensation", new String[][]{
            {"Base Salary:", currentUserSalary},
            {"Bank Account:", currentUserBank},
            {"Tax Status:", "Single, 0 Allowances"},
            {"Next Payday:", "Oct 31, 2026"}
        }));
    }

    private JPanel createSectionPanel(String title, String[][] data) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Theme.SURFACE_CARD);
        card.setMinimumSize(new Dimension(0, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER_LIGHT, 1, true),
            new EmptyBorder(16, 18, 16, 18)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 14);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.FONT_SUBTITLE);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        card.add(titleLabel, gbc);
        
        gbc.gridy++;
        JSeparator sep = new JSeparator();
        sep.setForeground(Theme.BORDER_LIGHT);
        card.add(sep, gbc);

        gbc.gridwidth = 1;
        for (String[] row : data) {
            gbc.gridy++;
            gbc.gridx = 0;
            gbc.weightx = 0.35;
            JLabel lblKey = new JLabel(row[0]);
            lblKey.setFont(Theme.FONT_BODY);
            lblKey.setForeground(Theme.TEXT_SECONDARY);
            card.add(lblKey, gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.65;
            JLabel lblValue = new JLabel(row[1]);
            lblValue.setFont(Theme.FONT_BODY_BOLD);
            lblValue.setForeground(Theme.TEXT_PRIMARY);
            card.add(lblValue, gbc);
        }
        
        gbc.gridy++;
        gbc.weighty = 1.0;
        card.add(Box.createVerticalGlue(), gbc);

        return card;
    }

    private void changePhotoDialog() {
        String[] options = {"Choose Local Image File", "Pick Avatar Color Theme", "Generate Initials Badge", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this, 
            "How would you like to update your profile photo?", 
            "Change Profile Photo", 
            JOptionPane.DEFAULT_OPTION, 
            JOptionPane.PLAIN_MESSAGE, 
            null, options, options[0]);

        if (choice == 0) {
            JFileChooser chooser = new JFileChooser();
            int res = chooser.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                ImageIcon img = new ImageIcon(file.getAbsolutePath());
                Image scaled = img.getImage().getScaledInstance(76, 76, Image.SCALE_SMOOTH);
                customAvatarIcon = new ImageIcon(scaled);
                buildUI();
                if (onProfileUpdated != null) onProfileUpdated.run();
                JOptionPane.showMessageDialog(this, "Profile photo updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } else if (choice == 1) {
            Color color1 = JColorChooser.showDialog(this, "Pick Primary Avatar Color", avatarColors[0]);
            if (color1 != null) {
                Color color2 = JColorChooser.showDialog(this, "Pick Secondary Gradient Color", avatarColors[1]);
                if (color2 != null) {
                    avatarColors = new Color[]{color1, color2};
                    customAvatarIcon = null;
                    buildUI();
                    if (onProfileUpdated != null) onProfileUpdated.run();
                }
            }
        } else if (choice == 2) {
            customAvatarIcon = null;
            buildUI();
            if (onProfileUpdated != null) onProfileUpdated.run();
        }
    }

    private void showEditProfileDialog() {
        JTextField nameField = new JTextField(currentUserName);
        JTextField roleField = new JTextField(currentUserRole);
        JTextField ageField = new JTextField(currentUserAge);
        JTextField emailField = new JTextField(currentUserEmail);
        JTextField phoneField = new JTextField(currentUserPhone);
        JTextField addressField = new JTextField(currentUserAddress);
        JComboBox<String> deptBox = new JComboBox<>(new String[]{"Engineering", "Human Resources", "Design", "Marketing", "Sales", "Finance", "Operations"});
        deptBox.setSelectedItem(currentUserDept);
        JTextField salaryField = new JTextField(currentUserSalary);
        JTextField bankField = new JTextField(currentUserBank);

        JPanel form = new JPanel(new GridLayout(9, 2, 8, 8));
        form.add(new JLabel("Full Name:"));
        form.add(nameField);
        form.add(new JLabel("Job Title:"));
        form.add(roleField);
        form.add(new JLabel("Age:"));
        form.add(ageField);
        form.add(new JLabel("Email (@gmail.com required):"));
        form.add(emailField);
        form.add(new JLabel("Phone:"));
        form.add(phoneField);
        form.add(new JLabel("Address:"));
        form.add(addressField);
        form.add(new JLabel("Department:"));
        form.add(deptBox);
        form.add(new JLabel("Base Salary:"));
        form.add(salaryField);
        form.add(new JLabel("Bank Account:"));
        form.add(bankField);

        int result = JOptionPane.showConfirmDialog(this, form, "Edit Profile Details", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String email = emailField.getText().trim();
            if (!email.toLowerCase().endsWith("@gmail.com")) {
                JOptionPane.showMessageDialog(this, "Validation Error: Email must end with @gmail.com", "Invalid Email", JOptionPane.ERROR_MESSAGE);
                return;
            }

            currentUserName = nameField.getText().trim();
            currentUserRole = roleField.getText().trim();
            currentUserAge = ageField.getText().trim();
            currentUserEmail = email;
            currentUserPhone = phoneField.getText().trim();
            currentUserAddress = addressField.getText().trim();
            currentUserDept = (String) deptBox.getSelectedItem();
            currentUserSalary = salaryField.getText().trim();
            currentUserBank = bankField.getText().trim();

            buildUI();
            if (onProfileUpdated != null) onProfileUpdated.run();
            JOptionPane.showMessageDialog(this, "Your profile details have been saved!", "Profile Updated", JOptionPane.INFORMATION_MESSAGE);
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
