package com.speedtyping.ui;

import com.speedtyping.dao.ResultDAO;
import com.speedtyping.model.TypingResult;
import com.speedtyping.model.User;
import com.speedtyping.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class DashboardPanel extends JPanel {

    private final MainFrame mainFrame;
    private final ResultDAO resultDAO;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    // Dynamic Labels
    private JLabel welcomeLabel;
    private JLabel userStatusLabel;
    private JButton authActionBtn;

    // Stat metric value labels
    private JLabel bestWpmValue;
    private JLabel avgWpmValue;
    private JLabel avgAccValue;
    private JLabel totalTestsValue;
    private JLabel rankValue;

    // Tables
    private DefaultTableModel historyTableModel;
    private DefaultTableModel leaderboardTableModel;
    private JComboBox<Integer> quickDurationCombo;

    public DashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.resultDAO = new ResultDAO();

        setupUI();
    }

    private void setupUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UIUtils.COLOR_BG);

        // Header Bar
        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setBackground(UIUtils.COLOR_HEADER);
        headerBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.COLOR_BORDER),
                new EmptyBorder(12, 28, 12, 28)
        ));

        // Brand
        JPanel brand = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brand.setOpaque(false);

        JLabel brandBadge = UIUtils.createLogoBadge("ISTA");

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        JLabel brandTitle = new JLabel("Intelligent Speed Typing");
        brandTitle.setFont(UIUtils.FONT_TITLE);
        brandTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);

        JLabel brandSub = new JLabel("Analytics Platform | Performance Dashboard");
        brandSub.setFont(UIUtils.FONT_SMALL);
        brandSub.setForeground(UIUtils.COLOR_PRIMARY);

        titleBox.add(brandTitle);
        titleBox.add(brandSub);
        brand.add(brandBadge);
        brand.add(titleBox);

        headerBar.add(brand, BorderLayout.WEST);

        // Right side user bar
        JPanel rightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightBar.setOpaque(false);

        userStatusLabel = new JLabel("Account: Guest User");
        userStatusLabel.setFont(UIUtils.FONT_BODY_BOLD);
        userStatusLabel.setForeground(UIUtils.COLOR_TEXT_MAIN);

        JButton refreshBtn = UIUtils.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshDashboard());

        authActionBtn = UIUtils.createSecondaryButton("Sign In / Switch Account");
        authActionBtn.addActionListener(e -> handleAuthAction());

        rightBar.add(userStatusLabel);
        rightBar.add(refreshBtn);
        rightBar.add(authActionBtn);

        headerBar.add(rightBar, BorderLayout.EAST);
        add(headerBar, BorderLayout.NORTH);

        // Main Content Area (Scrollable)
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(UIUtils.COLOR_BG);
        contentPanel.setBorder(new EmptyBorder(20, 28, 24, 28));

        // 1. Hero Card with CTA "Start Typing Test"
        JPanel heroCard = UIUtils.createCardPanel();
        heroCard.setLayout(new BorderLayout(16, 12));
        heroCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 125));

        JPanel heroLeft = new JPanel();
        heroLeft.setLayout(new BoxLayout(heroLeft, BoxLayout.Y_AXIS));
        heroLeft.setOpaque(false);

        welcomeLabel = new JLabel("Welcome back!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        welcomeLabel.setForeground(UIUtils.COLOR_TEXT_MAIN);

        JLabel heroDesc = new JLabel("Measure your typing velocity, accuracy, and keystroke metrics with real-time feedback.");
        heroDesc.setFont(UIUtils.FONT_BODY);
        heroDesc.setForeground(UIUtils.COLOR_TEXT_MUTED);

        heroLeft.add(welcomeLabel);
        heroLeft.add(Box.createVerticalStrut(4));
        heroLeft.add(heroDesc);

        // Hero Right: Duration selector & Large Start Button
        JPanel heroRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        heroRight.setOpaque(false);

        JLabel durLbl = new JLabel("Test Duration:");
        durLbl.setFont(UIUtils.FONT_BODY_BOLD);
        durLbl.setForeground(UIUtils.COLOR_TEXT_MAIN);

        quickDurationCombo = new JComboBox<>(new Integer[]{30, 60, 120, 180, 300, 0});
        quickDurationCombo.setSelectedItem(60);
        UIUtils.styleComboBox(quickDurationCombo);
        quickDurationCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Integer) {
                    int sec = (Integer) value;
                    if (sec == 0) {
                        setText("Until the test is finished");
                    } else if (sec < 60) {
                        setText(sec + " Seconds");
                    } else {
                        setText((sec / 60) + " Minute" + (sec > 60 ? "s" : ""));
                    }
                }
                if (isSelected) {
                    setBackground(UIUtils.COLOR_PRIMARY);
                    setForeground(Color.WHITE);
                } else {
                    setBackground(Color.WHITE);
                    setForeground(UIUtils.COLOR_TEXT_MAIN);
                }
                setBorder(new EmptyBorder(4, 8, 4, 8));
                return this;
            }
        });

        JButton importPdfBtn = UIUtils.createAccentButton("📄 Type from PDF");
        importPdfBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        importPdfBtn.setToolTipText("Select and practice typing any PDF document");
        importPdfBtn.addActionListener(e -> {
            Integer dur = (Integer) quickDurationCombo.getSelectedItem();
            int duration = dur != null ? dur : 60;
            PdfImportDialog dialog = new PdfImportDialog(
                    SwingUtilities.getWindowAncestor(this),
                    new com.speedtyping.dao.TextDAO(),
                    customText -> mainFrame.startTestWithText(customText, duration),
                    () -> {
                        if (mainFrame.getTypingPanel() != null) {
                            mainFrame.getTypingPanel().reloadTexts();
                        }
                    }
            );
            dialog.setVisible(true);
        });

        JButton startTestBtn = UIUtils.createLargeCtaButton("Start Typing Test >");
        startTestBtn.addActionListener(e -> {
            Integer dur = (Integer) quickDurationCombo.getSelectedItem();
            mainFrame.startTest(dur != null ? dur : 60);
        });

        heroRight.add(durLbl);
        heroRight.add(quickDurationCombo);
        heroRight.add(importPdfBtn);
        heroRight.add(startTestBtn);

        heroCard.add(heroLeft, BorderLayout.WEST);
        heroCard.add(heroRight, BorderLayout.EAST);
        contentPanel.add(heroCard);

        contentPanel.add(Box.createVerticalStrut(18));

        // 2. Metrics Analytics Grid
        JPanel metricsGrid = new JPanel(new GridLayout(1, 5, 14, 0));
        metricsGrid.setOpaque(false);
        metricsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        bestWpmValue = new JLabel("0 WPM");
        avgWpmValue = new JLabel("0 WPM");
        avgAccValue = new JLabel("100%");
        totalTestsValue = new JLabel("0");
        rankValue = new JLabel("Novice");

        metricsGrid.add(UIUtils.createStatBox("PERSONAL BEST", bestWpmValue, "Highest recorded net speed", UIUtils.COLOR_PRIMARY));
        metricsGrid.add(UIUtils.createStatBox("AVERAGE SPEED", avgWpmValue, "Average across all tests", UIUtils.COLOR_SUCCESS));
        metricsGrid.add(UIUtils.createStatBox("ACCURACY", avgAccValue, "Lifetime typing precision", UIUtils.COLOR_ACCENT));
        metricsGrid.add(UIUtils.createStatBox("TESTS TAKEN", totalTestsValue, "Completed test sessions", UIUtils.COLOR_CYAN));
        metricsGrid.add(UIUtils.createStatBox("SKILL RANK", rankValue, "Dynamic ability tier", UIUtils.COLOR_PURPLE));

        contentPanel.add(metricsGrid);
        contentPanel.add(Box.createVerticalStrut(20));

        // 3. Tabbed Analytics Tables (History & Leaderboard)
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIUtils.FONT_BODY_BOLD);
        tabbedPane.setBackground(UIUtils.COLOR_SURFACE);
        tabbedPane.setForeground(UIUtils.COLOR_TEXT_MAIN);

        tabbedPane.addTab("Recent Test History", createHistoryTablePanel());
        tabbedPane.addTab("Global Top Typists Leaderboard", createLeaderboardTablePanel());

        contentPanel.add(tabbedPane);

        JScrollPane mainScrollPane = new JScrollPane(contentPanel);
        mainScrollPane.setBorder(null);
        mainScrollPane.getViewport().setBackground(UIUtils.COLOR_BG);
        mainScrollPane.setBackground(UIUtils.COLOR_BG);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(mainScrollPane, BorderLayout.CENTER);
    }

    private JPanel createHistoryTablePanel() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(UIUtils.COLOR_SURFACE);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Header toolbar with section label and Clear History button
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        JLabel sectionTitle = new JLabel("Your Past Typing Sessions");
        sectionTitle.setFont(UIUtils.FONT_BODY_BOLD);
        sectionTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);

        JButton clearBtn = UIUtils.createSecondaryButton("Clear History");
        clearBtn.setToolTipText("Remove all your past test results");
        clearBtn.addActionListener(e -> handleClearHistory());

        toolbar.add(sectionTitle, BorderLayout.WEST);
        toolbar.add(clearBtn, BorderLayout.EAST);
        p.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Date & Time", "Prompt Title", "Duration", "Net Speed (WPM)", "Raw Speed", "Accuracy", "Errors"};
        historyTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(historyTableModel);
        UIUtils.styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER, 1));
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBackground(Color.WHITE);
        scroll.setPreferredSize(new Dimension(800, 240));
        p.add(scroll, BorderLayout.CENTER);

        return p;
    }

    private JPanel createLeaderboardTablePanel() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setBackground(UIUtils.COLOR_SURFACE);
        p.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Header toolbar indicating best result per typist
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        JLabel sectionTitle = new JLabel("Global Leaderboard (Best Result Per Typist)");
        sectionTitle.setFont(UIUtils.FONT_BODY_BOLD);
        sectionTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);

        toolbar.add(sectionTitle, BorderLayout.WEST);
        p.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Rank", "Typist", "Net Speed (WPM)", "Raw Speed", "Accuracy", "Duration", "Date"};
        leaderboardTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(leaderboardTableModel);
        UIUtils.styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER, 1));
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBackground(Color.WHITE);
        scroll.setPreferredSize(new Dimension(800, 240));
        p.add(scroll, BorderLayout.CENTER);

        return p;
    }

    private void styleTable(JTable table) {
        UIUtils.styleTable(table);
    }

    public void refreshDashboard() {
        if (AuthService.isLoggedIn()) {
            User user = AuthService.getCurrentUser();
            String displayName = (user.getFullName() != null && !user.getFullName().isEmpty())
                    ? user.getFullName()
                    : user.getUsername();

            welcomeLabel.setText("Welcome back, " + displayName + "!");
            userStatusLabel.setText("Account: " + displayName);
            authActionBtn.setText("Sign Out");

            // Load metrics from MySQL
            Map<String, Object> stats = resultDAO.getUserSummaryStats(user.getId());
            int bestWpm = (int) stats.get("bestWpm");
            double avgWpm = (double) stats.get("avgWpm");
            double avgAcc = (double) stats.get("avgAccuracy");
            int total = (int) stats.get("totalTests");

            bestWpmValue.setText(bestWpm + " WPM");
            avgWpmValue.setText(avgWpm + " WPM");
            avgAccValue.setText(avgAcc + "%");
            totalTestsValue.setText(String.valueOf(total));
            rankValue.setText(UIUtils.getSpeedRank(bestWpm));

            // Load user test history
            historyTableModel.setRowCount(0);
            List<TypingResult> history = resultDAO.getUserResults(user.getId(), 50);
            for (TypingResult r : history) {
                historyTableModel.addRow(new Object[]{
                        r.getTestTimestamp() != null ? dateFormat.format(r.getTestTimestamp()) : "Recent",
                        r.getTextTitle(),
                        r.getDurationSeconds() + "s",
                        r.getWpm() + " WPM",
                        r.getRawWpm() + " WPM",
                        String.format("%.1f%%", r.getAccuracy()),
                        r.getIncorrectChars()
                });
            }
        } else {
            welcomeLabel.setText("Welcome to Intelligent Speed Typing Analytics!");
            userStatusLabel.setText("Account: Guest Mode (Offline)");
            authActionBtn.setText("Sign In / Register");

            bestWpmValue.setText("--");
            avgWpmValue.setText("--");
            avgAccValue.setText("--");
            totalTestsValue.setText("0");
            rankValue.setText("Guest");

            historyTableModel.setRowCount(0);
            historyTableModel.addRow(new Object[]{"-", "Sign in to save and view your test history", "-", "-", "-", "-", "-"});
        }

        // Always load global leaderboard
        leaderboardTableModel.setRowCount(0);
        List<TypingResult> topResults = resultDAO.getLeaderboard(25);
        int rank = 1;
        for (TypingResult r : topResults) {
            String rankStr;
            if (rank == 1) rankStr = "#1 (Gold)";
            else if (rank == 2) rankStr = "#2 (Silver)";
            else if (rank == 3) rankStr = "#3 (Bronze)";
            else rankStr = "#" + rank;

            leaderboardTableModel.addRow(new Object[]{
                    rankStr,
                    r.getUsername() != null ? r.getUsername() : "Anonymous",
                    r.getWpm() + " WPM",
                    r.getRawWpm() + " WPM",
                    String.format("%.1f%%", r.getAccuracy()),
                    r.getDurationSeconds() + "s",
                    r.getTestTimestamp() != null ? dateFormat.format(r.getTestTimestamp()) : "-"
            });
            rank++;
        }
    }

    private void handleAuthAction() {
        if (AuthService.isLoggedIn()) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to sign out?",
                    "Sign Out",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                AuthService.logout();
                mainFrame.showLogin();
            }
        } else {
            mainFrame.showLogin();
        }
    }

    private void handleClearHistory() {
        if (!AuthService.isLoggedIn()) {
            JOptionPane.showMessageDialog(
                    this,
                    "You are currently in Guest Mode. Please sign in to manage your recorded test history.",
                    "Sign In Required",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        User user = AuthService.getCurrentUser();
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to clear your entire typing history?\nThis will permanently remove all your past test scores.",
                "Confirm Clear History",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = resultDAO.clearUserHistory(user.getId());
            if (success) {
                refreshDashboard();
                JOptionPane.showMessageDialog(
                        this,
                        "Your test history has been cleared successfully.",
                        "History Cleared",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Unable to clear test history. Please check database connectivity.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }
}
