package hrms.ui;

import hrms.dao.EmployeeDAO;
import hrms.model.Employee;
import hrms.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Modern, beautifully arranged Login Screen for Proton HR.
 * Uses GridBagLayout for pixel-perfect vertical and horizontal alignment.
 */
public class LoginPanel extends JPanel {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JCheckBox rememberMeCheck;
    private JLabel errorLabel;
    private Runnable onLoginSuccess;

    public LoginPanel(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        setLayout(new GridBagLayout());
        setBackground(Theme.BG_APP);

        // Center card with responsive margins
        JPanel loginCard = createLoginCard();
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(24, 24, 24, 24);
        add(loginCard, gbc);
    }

    private JPanel createLoginCard() {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Theme.SURFACE_CARD);
        card.setPreferredSize(new Dimension(420, 540));
        card.setMinimumSize(new Dimension(360, 500));
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER_LIGHT, 1, true),
            new EmptyBorder(32, 36, 32, 36)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;

        // 1. App Logo & Brand Header
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        brandPanel.setOpaque(false);
        JLabel logoIcon = new JLabel(IconFactory.createIcon(IconFactory.IconType.DASHBOARD, 30, Theme.PRIMARY));
        JLabel brandName = new JLabel("Proton HR");
        brandName.setFont(new Font(Theme.FONT_FAMILY, Font.BOLD, 26));
        brandName.setForeground(Theme.TEXT_PRIMARY);
        brandPanel.add(logoIcon);
        brandPanel.add(brandName);

        gbc.insets = new Insets(0, 0, 6, 0);
        card.add(brandPanel, gbc);

        // 2. Subtitle
        gbc.gridy++;
        JLabel subtitle = new JLabel("Sign in to your organization portal", SwingConstants.CENTER);
        subtitle.setFont(Theme.FONT_BODY);
        subtitle.setForeground(Theme.TEXT_SECONDARY);
        gbc.insets = new Insets(0, 0, 16, 0);
        card.add(subtitle, gbc);

        // 3. Error Banner (Hidden by default)
        gbc.gridy++;
        errorLabel = new JLabel("Invalid credentials", SwingConstants.CENTER);
        errorLabel.setFont(Theme.FONT_SMALL_BOLD);
        errorLabel.setForeground(Theme.STATUS_REJECTED_FG);
        errorLabel.setBackground(Theme.STATUS_REJECTED_BG);
        errorLabel.setOpaque(true);
        errorLabel.setBorder(new EmptyBorder(6, 12, 6, 12));
        errorLabel.setVisible(false);
        gbc.insets = new Insets(0, 0, 12, 0);
        card.add(errorLabel, gbc);

        // 4. Email Label
        gbc.gridy++;
        JLabel emailLbl = new JLabel("Work Email");
        emailLbl.setFont(Theme.FONT_BODY_BOLD);
        emailLbl.setForeground(Theme.TEXT_PRIMARY);
        gbc.insets = new Insets(0, 0, 6, 0);
        card.add(emailLbl, gbc);

        // 5. Email Input Field with Email Icon
        gbc.gridy++;
        JPanel emailFieldWrapper = new JPanel(new BorderLayout(8, 0));
        emailFieldWrapper.setBackground(Theme.SURFACE_CARD);
        emailFieldWrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
        emailFieldWrapper.setPreferredSize(new Dimension(0, 42));

        JLabel emailIcon = new JLabel(IconFactory.createIcon(IconFactory.IconType.MAIL, 16, Theme.TEXT_MUTED));
        emailField = new JTextField("rajesh.sharma@gmail.com");
        emailField.setFont(Theme.FONT_BODY);
        emailField.setForeground(Theme.TEXT_PRIMARY);
        emailField.setBorder(null);

        emailFieldWrapper.add(emailIcon, BorderLayout.WEST);
        emailFieldWrapper.add(emailField, BorderLayout.CENTER);
        gbc.insets = new Insets(0, 0, 16, 0);
        card.add(emailFieldWrapper, gbc);

        // 6. Password Label
        gbc.gridy++;
        JLabel passLbl = new JLabel("Password");
        passLbl.setFont(Theme.FONT_BODY_BOLD);
        passLbl.setForeground(Theme.TEXT_PRIMARY);
        gbc.insets = new Insets(0, 0, 6, 0);
        card.add(passLbl, gbc);

        // 7. Password Input Field with Key Icon
        gbc.gridy++;
        JPanel passFieldWrapper = new JPanel(new BorderLayout(8, 0));
        passFieldWrapper.setBackground(Theme.SURFACE_CARD);
        passFieldWrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_LIGHT, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        ));
        passFieldWrapper.setPreferredSize(new Dimension(0, 42));

        JLabel passIcon = new JLabel(IconFactory.createIcon(IconFactory.IconType.SETTINGS, 16, Theme.TEXT_MUTED));
        passwordField = new JPasswordField("password123");
        passwordField.setFont(Theme.FONT_BODY);
        passwordField.setForeground(Theme.TEXT_PRIMARY);
        passwordField.setBorder(null);

        passFieldWrapper.add(passIcon, BorderLayout.WEST);
        passFieldWrapper.add(passwordField, BorderLayout.CENTER);
        gbc.insets = new Insets(0, 0, 14, 0);
        card.add(passFieldWrapper, gbc);

        // 8. Remember Me & Forgot Password Row
        gbc.gridy++;
        JPanel optionsPanel = new JPanel(new BorderLayout());
        optionsPanel.setOpaque(false);

        rememberMeCheck = new JCheckBox("Remember me");
        rememberMeCheck.setFont(Theme.FONT_SMALL);
        rememberMeCheck.setForeground(Theme.TEXT_SECONDARY);
        rememberMeCheck.setOpaque(false);
        rememberMeCheck.setSelected(true);

        JLabel forgotPass = new JLabel("Forgot password?");
        forgotPass.setFont(Theme.FONT_SMALL_BOLD);
        forgotPass.setForeground(Theme.PRIMARY);
        forgotPass.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotPass.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                JOptionPane.showMessageDialog(LoginPanel.this, 
                    "Password reset instructions have been sent to your work email.", 
                    "Reset Password", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        optionsPanel.add(rememberMeCheck, BorderLayout.WEST);
        optionsPanel.add(forgotPass, BorderLayout.EAST);
        gbc.insets = new Insets(0, 0, 20, 0);
        card.add(optionsPanel, gbc);

        // 9. Sign In Button
        gbc.gridy++;
        JButton signInBtn = new JButton("Sign In to Dashboard");
        signInBtn.setFont(Theme.FONT_BODY_BOLD);
        signInBtn.setBackground(Theme.PRIMARY);
        signInBtn.setForeground(Theme.TEXT_ON_PRIMARY);
        signInBtn.setFocusPainted(false);
        signInBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        signInBtn.setPreferredSize(new Dimension(0, 42));
        signInBtn.setBorder(new EmptyBorder(8, 14, 8, 14));
        signInBtn.addActionListener(e -> attemptLogin());
        gbc.insets = new Insets(0, 0, 16, 0);
        card.add(signInBtn, gbc);

        // 10. Demo Credentials Footer Box
        gbc.gridy++;
        JPanel demoBox = new JPanel(new BorderLayout());
        demoBox.setBackground(new Color(0xF1, 0xF5, 0xF9));
        demoBox.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Theme.BORDER_LIGHT, 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));

        JLabel demoText = new JLabel("<html><center style='color:#475569; font-size:11px;'>"
            + "<b>Demo Login:</b> rajesh.sharma@gmail.com &nbsp;|&nbsp; <b>Password:</b> password123"
            + "</center></html>", SwingConstants.CENTER);
        demoBox.add(demoText, BorderLayout.CENTER);
        gbc.insets = new Insets(0, 0, 0, 0);
        card.add(demoBox, gbc);

        // Enter key listener
        KeyAdapter enterKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    attemptLogin();
                }
            }
        };
        emailField.addKeyListener(enterKeyListener);
        passwordField.addKeyListener(enterKeyListener);

        return card;
    }

    private void attemptLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both email and password");
            errorLabel.setVisible(true);
            return;
        }

        if (!ValidationUtil.isValidGmail(email) && !email.equalsIgnoreCase("admin@protonhr.com")) {
            errorLabel.setText("Work email must end with @gmail.com");
            errorLabel.setVisible(true);
            return;
        }

        if (password.length() < 4) {
            errorLabel.setText("Password must contain at least 4 characters");
            errorLabel.setVisible(true);
            return;
        }

        Employee found = EmployeeDAO.getInstance().findByEmail(email);
        if (found != null) {
            UserProfilePanel.currentUserName = found.getName();
            UserProfilePanel.currentUserEmail = found.getEmail();
            UserProfilePanel.currentUserRole = found.getRole();
        } else {
            UserProfilePanel.currentUserEmail = email;
        }

        errorLabel.setVisible(false);
        if (onLoginSuccess != null) {
            onLoginSuccess.run();
        }
    }
}
