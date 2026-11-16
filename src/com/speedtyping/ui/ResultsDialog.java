package com.speedtyping.ui;

import com.speedtyping.model.TypingResult;
import com.speedtyping.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ResultsDialog extends JDialog {

    private final Runnable onTryAgain;
    private final Runnable onNextText;
    private final Runnable onGoToDashboard;

    public ResultsDialog(MainFrame parent, TypingResult result, Runnable onTryAgain, Runnable onNextText, Runnable onGoToDashboard) {
        super(parent, "Test Results - Intelligent Speed Typing Analytics", true);
        this.onTryAgain = onTryAgain;
        this.onNextText = onNextText;
        this.onGoToDashboard = onGoToDashboard;

        setupUI(parent, result);
    }

    private void setupUI(MainFrame parent, TypingResult result) {
        setSize(520, 530);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIUtils.COLOR_BG);

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(UIUtils.COLOR_PRIMARY);
        headerPanel.setBorder(new EmptyBorder(18, 20, 18, 20));
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel("Test Session Complete!");
        titleLbl.setFont(UIUtils.FONT_TITLE);
        titleLbl.setForeground(Color.WHITE);
        titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel scoreLbl = new JLabel(result.getWpm() + " WPM");
        scoreLbl.setFont(new Font("Segoe UI", Font.BOLD, 46));
        scoreLbl.setForeground(Color.WHITE);
        scoreLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel("Net Typing Velocity | Rank: " + UIUtils.getSpeedRank(result.getWpm()));
        subLbl.setFont(UIUtils.FONT_BODY);
        subLbl.setForeground(UIUtils.COLOR_PRIMARY_TINT);
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(titleLbl);
        headerPanel.add(Box.createVerticalStrut(6));
        headerPanel.add(scoreLbl);
        headerPanel.add(subLbl);

        add(headerPanel, BorderLayout.NORTH);

        // Body / Breakdown Panel
        JPanel bodyPanel = new JPanel();
        bodyPanel.setBackground(UIUtils.COLOR_BG);
        bodyPanel.setBorder(new EmptyBorder(18, 24, 18, 24));
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));

        JPanel gridPanel = new JPanel(new GridLayout(3, 2, 14, 12));
        gridPanel.setOpaque(false);

        gridPanel.add(createMetricItem("Accuracy", String.format("%.1f%%", result.getAccuracy()), UIUtils.COLOR_ACCENT));
        gridPanel.add(createMetricItem("Raw Speed", result.getRawWpm() + " WPM", UIUtils.COLOR_CYAN));
        gridPanel.add(createMetricItem("Correct Chars", String.valueOf(result.getCorrectChars()), UIUtils.COLOR_SUCCESS));
        gridPanel.add(createMetricItem("Error Chars", String.valueOf(result.getIncorrectChars()), UIUtils.COLOR_ERROR));
        gridPanel.add(createMetricItem("Total Typed", String.valueOf(result.getTotalChars()), UIUtils.COLOR_TEXT_MAIN));
        int durSec = result.getDurationSeconds();
        String durDisplay = durSec >= 60
                ? String.format("%dm %02ds (%ds)", durSec / 60, durSec % 60, durSec)
                : durSec + "s";
        gridPanel.add(createMetricItem("Duration", durDisplay, UIUtils.COLOR_TEXT_MAIN));

        bodyPanel.add(gridPanel);
        bodyPanel.add(Box.createVerticalStrut(14));

        // Save status info banner
        JLabel statusBanner = new JLabel();
        statusBanner.setFont(UIUtils.FONT_SMALL);
        statusBanner.setAlignmentX(Component.CENTER_ALIGNMENT);

        if (AuthService.isLoggedIn()) {
            statusBanner.setText("[Saved] Stored in cloud for user: " + AuthService.getCurrentUser().getUsername());
            statusBanner.setForeground(UIUtils.COLOR_SUCCESS);
        } else {
            statusBanner.setText("[Guest Mode] Log in to permanently record your scores on the leaderboard.");
            statusBanner.setForeground(UIUtils.COLOR_TEXT_MUTED);
        }
        bodyPanel.add(statusBanner);

        add(bodyPanel, BorderLayout.CENTER);

        // Bottom Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 14));
        actionPanel.setBackground(UIUtils.COLOR_CARD);
        actionPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIUtils.COLOR_BORDER));

        JButton tryAgainBtn = UIUtils.createPrimaryButton("Try Again");
        tryAgainBtn.addActionListener(e -> {
            dispose();
            if (onTryAgain != null) onTryAgain.run();
        });

        JButton nextBtn = UIUtils.createSecondaryButton("Next Text");
        nextBtn.addActionListener(e -> {
            dispose();
            if (onNextText != null) onNextText.run();
        });

        JButton dashboardBtn = UIUtils.createAccentButton("Dashboard & Analytics");
        dashboardBtn.addActionListener(e -> {
            dispose();
            if (onGoToDashboard != null) onGoToDashboard.run();
        });

        actionPanel.add(tryAgainBtn);
        actionPanel.add(nextBtn);
        actionPanel.add(dashboardBtn);

        add(actionPanel, BorderLayout.SOUTH);
    }

    private JPanel createMetricItem(String label, String value, Color valueColor) {
        JPanel item = new JPanel(new BorderLayout(8, 2));
        item.setBackground(Color.WHITE);
        item.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.COLOR_BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)
        ));

        JLabel lbl = new JLabel(label);
        lbl.setFont(UIUtils.FONT_SMALL);
        lbl.setForeground(UIUtils.COLOR_TEXT_MUTED);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 16));
        val.setForeground(valueColor);

        item.add(lbl, BorderLayout.NORTH);
        item.add(val, BorderLayout.CENTER);
        return item;
    }
}
