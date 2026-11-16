package com.speedtyping.ui;

import com.speedtyping.model.User;
import com.speedtyping.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginPanel extends JPanel {

    private final MainFrame mainFrame;
    private final AuthService authService;

    private CardLayout authCardLayout;
    private JPanel authCardsContainer;

    // Login Fields
    private JTextField loginUserField;
    private JPasswordField loginPassField;
    private JLabel loginErrorLabel;

    // Register Fields
    private JTextField regNameField;
    private JTextField regUserField;
    private JPasswordField regPassField;
    private JPasswordField regConfirmPassField;
    private JLabel regErrorLabel;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.authService = new AuthService();
        setupUI();
    }

    private void setupUI() {
        setLayout(new GridBagLayout());
        setBackground(UIUtils.COLOR_BG);

        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(480, 620));

        // Brand Banner
        JPanel brandBanner = new JPanel();
        brandBanner.setLayout(new BoxLayout(brandBanner, BoxLayout.Y_AXIS));
        brandBanner.setOpaque(false);

        JLabel logoBadge = UIUtils.createLogoBadge("ISTA PLATFORM");
        logoBadge.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel brandTitle = new JLabel("Intelligent Speed Typing");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        brandTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);
        brandTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel brandSub = new JLabel("Analytics Platform");
        brandSub.setFont(new Font("Segoe UI", Font.BOLD, 16));
        brandSub.setForeground(UIUtils.COLOR_PRIMARY);
        brandSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desc = new JLabel("Sign in to analyze your typing metrics and track speed progression");
        desc.setFont(UIUtils.FONT_SMALL);
        desc.setForeground(UIUtils.COLOR_TEXT_MUTED);
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        brandBanner.add(logoBadge);
        brandBanner.add(Box.createVerticalStrut(10));
        brandBanner.add(brandTitle);
        brandBanner.add(brandSub);
        brandBanner.add(Box.createVerticalStrut(6));
        brandBanner.add(desc);
        brandBanner.add(Box.createVerticalStrut(20));

        wrapper.add(brandBanner);

        // Auth Form Card
        JPanel card = UIUtils.createCardPanel();
        card.setLayout(new BorderLayout());

        authCardLayout = new CardLayout();
        authCardsContainer = new JPanel(authCardLayout);
        authCardsContainer.setBackground(UIUtils.COLOR_CARD);

        authCardsContainer.add(createLoginCard(), "LOGIN");
        authCardsContainer.add(createRegisterCard(), "REGISTER");

        card.add(authCardsContainer, BorderLayout.CENTER);
        wrapper.add(card);

        add(wrapper);
    }

    private JPanel createLoginCard() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel head = new JLabel("Sign In to Your Account");
        head.setFont(UIUtils.FONT_HEADER);
        head.setForeground(UIUtils.COLOR_TEXT_MAIN);
        head.setAlignmentX(Component.LEFT_ALIGNMENT);

        loginErrorLabel = new JLabel(" ");
        loginErrorLabel.setFont(UIUtils.FONT_SMALL);
        loginErrorLabel.setForeground(UIUtils.COLOR_ERROR);
        loginErrorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userLbl = new JLabel("Username");
        userLbl.setFont(UIUtils.FONT_BODY_BOLD);
        userLbl.setForeground(UIUtils.COLOR_TEXT_MAIN);
        userLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        loginUserField = new JTextField();
        loginUserField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        loginUserField.setAlignmentX(Component.LEFT_ALIGNMENT);
        UIUtils.styleTextField(loginUserField);

        JLabel passLbl = new JLabel("Password");
        passLbl.setFont(UIUtils.FONT_BODY_BOLD);
        passLbl.setForeground(UIUtils.COLOR_TEXT_MAIN);
        passLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        loginPassField = new JPasswordField();
        loginPassField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        loginPassField.setAlignmentX(Component.LEFT_ALIGNMENT);
        UIUtils.stylePasswordField(loginPassField);

        loginUserField.addActionListener(e -> performLogin());
        loginPassField.addActionListener(e -> performLogin());

        JButton loginBtn = UIUtils.createPrimaryButton("Log In & Enter Dashboard");
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> performLogin());

        JButton switchRegBtn = UIUtils.createSecondaryButton("New User? Create an Account");
        switchRegBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        switchRegBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        switchRegBtn.addActionListener(e -> {
            loginErrorLabel.setText(" ");
            authCardLayout.show(authCardsContainer, "REGISTER");
        });

        JButton guestBtn = new JButton("Skip & Continue as Guest ->");
        guestBtn.setFont(UIUtils.FONT_SMALL);
        guestBtn.setForeground(UIUtils.COLOR_PRIMARY);
        guestBtn.setBorderPainted(false);
        guestBtn.setContentAreaFilled(false);
        guestBtn.setFocusPainted(false);
        guestBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        guestBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        guestBtn.addActionListener(e -> {
            AuthService.logout();
            mainFrame.showDashboard();
        });

        p.add(head);
        p.add(Box.createVerticalStrut(4));
        p.add(loginErrorLabel);
        p.add(Box.createVerticalStrut(6));
        p.add(userLbl);
        p.add(Box.createVerticalStrut(4));
        p.add(loginUserField);
        p.add(Box.createVerticalStrut(12));
        p.add(passLbl);
        p.add(Box.createVerticalStrut(4));
        p.add(loginPassField);
        p.add(Box.createVerticalStrut(20));
        p.add(loginBtn);
        p.add(Box.createVerticalStrut(10));
        p.add(switchRegBtn);
        p.add(Box.createVerticalStrut(10));
        p.add(guestBtn);

        return p;
    }

    private JPanel createRegisterCard() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel head = new JLabel("Create Free Account");
        head.setFont(UIUtils.FONT_HEADER);
        head.setForeground(UIUtils.COLOR_TEXT_MAIN);
        head.setAlignmentX(Component.LEFT_ALIGNMENT);

        regErrorLabel = new JLabel(" ");
        regErrorLabel.setFont(UIUtils.FONT_SMALL);
        regErrorLabel.setForeground(UIUtils.COLOR_ERROR);
        regErrorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLbl = new JLabel("Full Name (Optional)");
        nameLbl.setFont(UIUtils.FONT_BODY_BOLD);
        nameLbl.setForeground(UIUtils.COLOR_TEXT_MAIN);
        regNameField = new JTextField();
        regNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        regNameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        UIUtils.styleTextField(regNameField);

        JLabel userLbl = new JLabel("Username (Required)");
        userLbl.setFont(UIUtils.FONT_BODY_BOLD);
        userLbl.setForeground(UIUtils.COLOR_TEXT_MAIN);
        regUserField = new JTextField();
        regUserField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        regUserField.setAlignmentX(Component.LEFT_ALIGNMENT);
        UIUtils.styleTextField(regUserField);

        JLabel passLbl = new JLabel("Password");
        passLbl.setFont(UIUtils.FONT_BODY_BOLD);
        passLbl.setForeground(UIUtils.COLOR_TEXT_MAIN);
        regPassField = new JPasswordField();
        regPassField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        regPassField.setAlignmentX(Component.LEFT_ALIGNMENT);
        UIUtils.stylePasswordField(regPassField);

        JLabel confirmLbl = new JLabel("Confirm Password");
        confirmLbl.setFont(UIUtils.FONT_BODY_BOLD);
        confirmLbl.setForeground(UIUtils.COLOR_TEXT_MAIN);
        regConfirmPassField = new JPasswordField();
        regConfirmPassField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        regConfirmPassField.setAlignmentX(Component.LEFT_ALIGNMENT);
        UIUtils.stylePasswordField(regConfirmPassField);

        regConfirmPassField.addActionListener(e -> performRegister());

        JButton regBtn = UIUtils.createSuccessButton("Create Account & Enter Dashboard");
        regBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        regBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        regBtn.addActionListener(e -> performRegister());

        JButton backBtn = UIUtils.createSecondaryButton("Already have an account? Sign In");
        backBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        backBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        backBtn.addActionListener(e -> {
            regErrorLabel.setText(" ");
            authCardLayout.show(authCardsContainer, "LOGIN");
        });

        p.add(head);
        p.add(regErrorLabel);
        p.add(Box.createVerticalStrut(4));
        p.add(nameLbl);
        p.add(Box.createVerticalStrut(2));
        p.add(regNameField);
        p.add(Box.createVerticalStrut(6));
        p.add(userLbl);
        p.add(Box.createVerticalStrut(2));
        p.add(regUserField);
        p.add(Box.createVerticalStrut(6));
        p.add(passLbl);
        p.add(Box.createVerticalStrut(2));
        p.add(regPassField);
        p.add(Box.createVerticalStrut(6));
        p.add(confirmLbl);
        p.add(Box.createVerticalStrut(2));
        p.add(regConfirmPassField);
        p.add(Box.createVerticalStrut(14));
        p.add(regBtn);
        p.add(Box.createVerticalStrut(8));
        p.add(backBtn);

        return p;
    }

    private void performLogin() {
        String username = loginUserField.getText().trim();
        String password = new String(loginPassField.getPassword());

        try {
            User user = authService.login(username, password);
            loginPassField.setText("");
            loginErrorLabel.setText(" ");
            mainFrame.showDashboard();
        } catch (IllegalArgumentException ex) {
            loginErrorLabel.setText(ex.getMessage());
        }
    }

    private void performRegister() {
        String name = regNameField.getText().trim();
        String username = regUserField.getText().trim();
        String password = new String(regPassField.getPassword());
        String confirm = new String(regConfirmPassField.getPassword());

        try {
            User user = authService.register(username, password, confirm, name);
            regPassField.setText("");
            regConfirmPassField.setText("");
            regErrorLabel.setText(" ");
            mainFrame.showDashboard();
        } catch (IllegalArgumentException ex) {
            regErrorLabel.setText(ex.getMessage());
        }
    }

    public void resetForm() {
        loginUserField.setText("");
        loginPassField.setText("");
        loginErrorLabel.setText(" ");
        regNameField.setText("");
        regUserField.setText("");
        regPassField.setText("");
        regConfirmPassField.setText("");
        regErrorLabel.setText(" ");
        authCardLayout.show(authCardsContainer, "LOGIN");
        SwingUtilities.invokeLater(() -> loginUserField.requestFocusInWindow());
    }
}
