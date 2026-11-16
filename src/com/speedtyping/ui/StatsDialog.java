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

public class StatsDialog extends JDialog {

    private final ResultDAO resultDAO;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public StatsDialog(JFrame parent) {
        super(parent, "Typing Statistics & Leaderboard", true);
        this.resultDAO = new ResultDAO();

        setupUI();
    }

    private void setupUI() {
        setSize(780, 560);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIUtils.COLOR_BG);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIUtils.COLOR_PRIMARY);
        headerPanel.setBorder(new EmptyBorder(16, 24, 16, 24));

        JLabel titleLbl = new JLabel("Performance & Global Leaderboard");
        titleLbl.setFont(UIUtils.FONT_TITLE);
        titleLbl.setForeground(Color.WHITE);

        headerPanel.add(titleLbl, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Tabbed Pane for My Stats vs Leaderboard
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIUtils.FONT_BODY_BOLD);
        tabbedPane.setBackground(UIUtils.COLOR_SURFACE);
        tabbedPane.setForeground(UIUtils.COLOR_TEXT_MAIN);

        tabbedPane.addTab("My Typing History", createMyStatsPanel());
        tabbedPane.addTab("Global Leaderboard", createLeaderboardPanel());

        add(tabbedPane, BorderLayout.CENTER);

        // Bottom Close Button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 12));
        bottomPanel.setBackground(UIUtils.COLOR_SURFACE);
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIUtils.COLOR_BORDER));

        JButton closeBtn = UIUtils.createSecondaryButton("Close");
        closeBtn.addActionListener(e -> dispose());
        bottomPanel.add(closeBtn);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createMyStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        if (!AuthService.isLoggedIn()) {
            JPanel guestPanel = new JPanel(new GridBagLayout());
            guestPanel.setOpaque(false);
            JLabel msg = new JLabel("Please log in to view your personalized typing history and stats.");
            msg.setFont(UIUtils.FONT_HEADER);
            msg.setForeground(UIUtils.COLOR_TEXT_MUTED);
            guestPanel.add(msg);
            panel.add(guestPanel, BorderLayout.CENTER);
            return panel;
        }

        User user = AuthService.getCurrentUser();
        Map<String, Object> summary = resultDAO.getUserSummaryStats(user.getId());

        // Top Summary Cards
        JPanel summaryGrid = new JPanel(new GridLayout(1, 4, 12, 0));
        summaryGrid.setOpaque(false);

        JLabel totalLbl = new JLabel(String.valueOf(summary.get("totalTests")));
        JLabel bestWpmLbl = new JLabel(summary.get("bestWpm") + " WPM");
        JLabel avgWpmLbl = new JLabel(summary.get("avgWpm") + " WPM");
        JLabel avgAccLbl = new JLabel(summary.get("avgAccuracy") + "%");

        summaryGrid.add(UIUtils.createStatBox("TOTAL TESTS", totalLbl, "Tests recorded", UIUtils.COLOR_CYAN));
        summaryGrid.add(UIUtils.createStatBox("BEST SPEED", bestWpmLbl, "Highest score", UIUtils.COLOR_PRIMARY));
        summaryGrid.add(UIUtils.createStatBox("AVG SPEED", avgWpmLbl, "Overall average", UIUtils.COLOR_SUCCESS));
        summaryGrid.add(UIUtils.createStatBox("AVG ACCURACY", avgAccLbl, "Overall precision", UIUtils.COLOR_ACCENT));

        panel.add(summaryGrid, BorderLayout.NORTH);

        // History Table Container with Toolbar
        JPanel historyContainer = new JPanel(new BorderLayout(0, 8));
        historyContainer.setOpaque(false);

        JPanel tableToolbar = new JPanel(new BorderLayout());
        tableToolbar.setOpaque(false);

        JLabel historyTitle = new JLabel("Your Recorded Typing Sessions");
        historyTitle.setFont(UIUtils.FONT_BODY_BOLD);
        historyTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);

        JButton clearBtn = UIUtils.createSecondaryButton("Clear History");
        clearBtn.setToolTipText("Remove all your past test results");
        clearBtn.addActionListener(e -> {
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
                    JOptionPane.showMessageDialog(this, "Your test history has been cleared.", "History Cleared", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Unable to clear test history.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        tableToolbar.add(historyTitle, BorderLayout.WEST);
        tableToolbar.add(clearBtn, BorderLayout.EAST);
        historyContainer.add(tableToolbar, BorderLayout.NORTH);

        String[] columns = {"Date & Time", "Text Prompt", "Duration", "Net WPM", "Raw WPM", "Accuracy", "Errors"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        List<TypingResult> history = resultDAO.getUserResults(user.getId(), 50);
        for (TypingResult r : history) {
            model.addRow(new Object[]{
                    r.getTestTimestamp() != null ? dateFormat.format(r.getTestTimestamp()) : "Just now",
                    r.getTextTitle(),
                    r.getDurationSeconds() + "s",
                    r.getWpm() + " WPM",
                    r.getRawWpm() + " WPM",
                    String.format("%.1f%%", r.getAccuracy()),
                    r.getIncorrectChars()
            });
        }

        JTable table = new JTable(model);
        UIUtils.styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBackground(Color.WHITE);
        historyContainer.add(scrollPane, BorderLayout.CENTER);

        panel.add(historyContainer, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createLeaderboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(UIUtils.COLOR_BG);
        panel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JPanel tableToolbar = new JPanel(new BorderLayout());
        tableToolbar.setOpaque(false);

        JLabel leaderboardTitle = new JLabel("Global Leaderboard (Best Result Per Typist)");
        leaderboardTitle.setFont(UIUtils.FONT_BODY_BOLD);
        leaderboardTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);
        tableToolbar.add(leaderboardTitle, BorderLayout.WEST);

        panel.add(tableToolbar, BorderLayout.NORTH);

        String[] columns = {"Rank", "User", "Net Speed", "Raw Speed", "Accuracy", "Duration", "Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        List<TypingResult> topResults = resultDAO.getLeaderboard(25);
        int rank = 1;
        for (TypingResult r : topResults) {
            String rankStr;
            if (rank == 1) rankStr = "#1 (Gold)";
            else if (rank == 2) rankStr = "#2 (Silver)";
            else if (rank == 3) rankStr = "#3 (Bronze)";
            else rankStr = "#" + rank;

            model.addRow(new Object[]{
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

        JTable table = new JTable(model);
        UIUtils.styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBackground(Color.WHITE);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void styleTable(JTable table) {
        UIUtils.styleTable(table);
    }
}
