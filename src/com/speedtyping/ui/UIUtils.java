package com.speedtyping.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UIUtils {

    public static final String APP_NAME = "Intelligent Speed Typing Analytics Platform";

    // Warm Earthy Color Palette:
    // Background: #FDFBF7 is the main window.
    // Surfaces: #F3EFE7 is the sidebar, cards, headers and the progress bar track.
    // Primary text: #3A3532 is all task names, headings and body copy.
    // Secondary text: #7E7671 is the hints, placeholder text, completed tasks, unchecked circles and input borders.
    // Accent: #6B7F72 is the active list, the Add task button, the checked circles, the progress bar and the confirmation toast.

    // Core Canvas & Surfaces
    public static final Color COLOR_BG = new Color(0xFD, 0xFB, 0xF7);          // Main Window #FDFBF7
    public static final Color COLOR_SURFACE = new Color(0xF3, 0xEF, 0xE7);     // Surfaces #F3EFE7
    public static final Color COLOR_CARD = new Color(0xF3, 0xEF, 0xE7);        // Card / Container #F3EFE7
    public static final Color COLOR_CARD_WHITE = Color.WHITE;                  // Card White Surface #FFFFFF
    public static final Color COLOR_CARD_MUTED = new Color(0xEB, 0xE6, 0xDC);  // Recessed Surface #EBE6DC
    public static final Color COLOR_HEADER = new Color(0xF3, 0xEF, 0xE7);      // Header Bar #F3EFE7

    // Core Typography
    public static final Color COLOR_TEXT_MAIN = new Color(0x3A, 0x35, 0x32);   // Primary Text #3A3532
    public static final Color COLOR_TEXT_MUTED = new Color(0x7E, 0x76, 0x71);  // Secondary Text #7E7671

    // Core Accent & Primary Actions
    public static final Color COLOR_PRIMARY = new Color(0x6B, 0x7F, 0x72);       // Accent #6B7F72
    public static final Color COLOR_PRIMARY_HOVER = new Color(0x59, 0x6C, 0x60); // Deep Sage Hover #596C60
    public static final Color COLOR_PRIMARY_DARK = new Color(0x4A, 0x5A, 0x4F);  // Dark Sage #4A5A4F
    public static final Color COLOR_PRIMARY_TINT = new Color(0xE7, 0xEE, 0xE8);  // Soft Sage Tint #E7EEE8
    public static final Color COLOR_PRIMARY_BADGE_BG = new Color(0xE3, 0xEB, 0xE5);// Sage Badge Surface #E3EBE5

    // Borders & Dividers
    public static final Color COLOR_BORDER = new Color(0xD8, 0xD2, 0xC7);       // Structural Border #D8D2C7
    public static final Color COLOR_BORDER_INPUT = new Color(0x7E, 0x76, 0x71); // Input Border #7E7671

    // Secondary & Elevated Elements
    public static final Color COLOR_SECONDARY = new Color(0x7E, 0x76, 0x71);    // Slate Brown #7E7671
    public static final Color COLOR_SECONDARY_BG = new Color(0xEA, 0xE4, 0xDA); // Warm Surface Button #EAE4DA
    public static final Color COLOR_SECONDARY_HOVER = new Color(0xDC, 0xD5, 0xC9); // Button Hover #DCD5C9

    // Harmonious Semantic Accents
    public static final Color COLOR_SUCCESS = new Color(0x4F, 0x7A, 0x5D);      // Forest Green #4F7A5D
    public static final Color COLOR_SUCCESS_HOVER = new Color(0x3F, 0x65, 0x4C);
    public static final Color COLOR_ACCENT = new Color(0xC2, 0x82, 0x3A);       // Warm Ochre Amber #C2823A
    public static final Color COLOR_ACCENT_HOVER = new Color(0xA8, 0x6E, 0x2E);
    public static final Color COLOR_CYAN = new Color(0x56, 0x7B, 0x85);         // Slate Teal #567B85
    public static final Color COLOR_CYAN_HOVER = new Color(0x44, 0x65, 0x6E);
    public static final Color COLOR_PURPLE = new Color(0x7B, 0x68, 0x82);       // Muted Mauve #7B6882
    public static final Color COLOR_ERROR = new Color(0xB8, 0x4A, 0x39);        // Terracotta Red #B84A39
    public static final Color COLOR_ERROR_BG = new Color(0xFA, 0xE7, 0xE4);     // Soft Terracotta BG #FAE7E4

    // Typing Arena Feedback Colors
    public static final Color COLOR_TYPING_CORRECT_FG = new Color(0x2E, 0x5E, 0x3F);  // Moss Forest #2E5E3F
    public static final Color COLOR_TYPING_CORRECT_BG = new Color(0xE3, 0xEE, 0xE6);  // Sage Tint #E3EEE6
    public static final Color COLOR_TYPING_ERROR_FG = new Color(0xB8, 0x4A, 0x39);    // Terracotta Red #B84A39
    public static final Color COLOR_TYPING_ERROR_BG = new Color(0xFA, 0xE7, 0xE4);    // Terracotta Tint #FAE7E4
    public static final Color COLOR_TYPING_CURRENT_FG = Color.WHITE;                  // White
    public static final Color COLOR_TYPING_CURRENT_BG = new Color(0x6B, 0x7F, 0x72);  // Accent Sage #6B7F72

    // Fonts
    public static final Font FONT_APP_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_STAT_NUMBER = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_TYPING = new Font("Consolas", Font.PLAIN, 20);

    public static void setupModernLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        UIManager.put("Button.font", FONT_BODY_BOLD);
        UIManager.put("Label.font", FONT_BODY);
        UIManager.put("TextField.font", FONT_BODY);
        UIManager.put("PasswordField.font", FONT_BODY);
        UIManager.put("ComboBox.font", FONT_BODY);
        UIManager.put("Table.font", FONT_BODY);
        UIManager.put("TableHeader.font", FONT_BODY_BOLD);

        // Warm earthy theme UI defaults
        UIManager.put("Panel.background", COLOR_BG);
        UIManager.put("Label.foreground", COLOR_TEXT_MAIN);
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("TextField.foreground", COLOR_TEXT_MAIN);
        UIManager.put("TextField.caretForeground", COLOR_PRIMARY);
        UIManager.put("TextField.selectionBackground", COLOR_PRIMARY);
        UIManager.put("TextField.selectionForeground", Color.WHITE);
        UIManager.put("PasswordField.background", Color.WHITE);
        UIManager.put("PasswordField.foreground", COLOR_TEXT_MAIN);
        UIManager.put("PasswordField.caretForeground", COLOR_PRIMARY);
        UIManager.put("PasswordField.selectionBackground", COLOR_PRIMARY);
        UIManager.put("PasswordField.selectionForeground", Color.WHITE);
        UIManager.put("ComboBox.background", Color.WHITE);
        UIManager.put("ComboBox.foreground", COLOR_TEXT_MAIN);
        UIManager.put("ComboBox.selectionBackground", COLOR_PRIMARY);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        UIManager.put("Table.background", Color.WHITE);
        UIManager.put("Table.foreground", COLOR_TEXT_MAIN);
        UIManager.put("Table.gridColor", COLOR_BORDER);
        UIManager.put("TableHeader.background", COLOR_SURFACE);
        UIManager.put("TableHeader.foreground", COLOR_TEXT_MAIN);
        UIManager.put("TabbedPane.background", COLOR_SURFACE);
        UIManager.put("TabbedPane.foreground", COLOR_TEXT_MAIN);
        UIManager.put("TabbedPane.selected", Color.WHITE);
        UIManager.put("TabbedPane.focus", COLOR_PRIMARY);
        UIManager.put("ScrollPane.background", COLOR_BG);
        UIManager.put("Viewport.background", Color.WHITE);
        UIManager.put("ProgressBar.foreground", COLOR_PRIMARY);       // Accent #6B7F72
        UIManager.put("ProgressBar.background", COLOR_SURFACE);       // Surfaces #F3EFE7 track
        UIManager.put("ProgressBar.selectionForeground", Color.WHITE);
        UIManager.put("ProgressBar.selectionBackground", COLOR_TEXT_MAIN);
    }

    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, COLOR_PRIMARY, COLOR_PRIMARY_HOVER, Color.WHITE);
    }

    public static JButton createLargeCtaButton(String text) {
        JButton btn = createStyledButton(text, COLOR_PRIMARY, COLOR_PRIMARY_HOVER, Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_PRIMARY_DARK, 1, true),
                new EmptyBorder(12, 24, 12, 24)
        ));
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = createStyledButton(text, COLOR_SECONDARY_BG, COLOR_SECONDARY_HOVER, COLOR_TEXT_MAIN);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER_INPUT, 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        return btn;
    }

    public static JButton createSuccessButton(String text) {
        return createStyledButton(text, COLOR_SUCCESS, COLOR_SUCCESS_HOVER, Color.WHITE);
    }

    public static JButton createAccentButton(String text) {
        return createStyledButton(text, COLOR_ACCENT, COLOR_ACCENT_HOVER, Color.WHITE);
    }

    public static JButton createCyanButton(String text) {
        return createStyledButton(text, COLOR_CYAN, COLOR_CYAN_HOVER, Color.WHITE);
    }

    public static JButton createStyledButton(String text, Color bg, Color hoverBg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY_BOLD);
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.darker(), 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverBg);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    public static JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(COLOR_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));
        return card;
    }

    public static JPanel createStatBox(String title, JLabel valueLabel, String subtitle, Color accentColor) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(COLOR_CARD);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(COLOR_BORDER, 1),
                        new EmptyBorder(10, 14, 10, 14)
                )
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(COLOR_TEXT_MUTED);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLabel.setFont(FONT_STAT_NUMBER);
        valueLabel.setForeground(COLOR_TEXT_MAIN);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(FONT_SMALL);
        subLbl.setForeground(COLOR_TEXT_MUTED);
        subLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        box.add(titleLbl);
        box.add(Box.createVerticalStrut(4));
        box.add(valueLabel);
        box.add(Box.createVerticalStrut(2));
        box.add(subLbl);

        box.setPreferredSize(new Dimension(145, 95));
        return box;
    }

    public static JLabel createLogoBadge(String text) {
        JLabel badge = new JLabel(text);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 13));
        badge.setForeground(COLOR_PRIMARY);           // Accent #6B7F72
        badge.setBackground(COLOR_PRIMARY_BADGE_BG);    // Soft Sage Tint
        badge.setOpaque(true);
        badge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_PRIMARY, 1, true),
                new EmptyBorder(4, 10, 4, 10)
        ));
        return badge;
    }

    public static void styleTextField(JTextField field) {
        field.setBackground(Color.WHITE);
        field.setForeground(COLOR_TEXT_MAIN);
        field.setCaretColor(COLOR_PRIMARY);
        field.setSelectedTextColor(Color.WHITE);
        field.setSelectionColor(COLOR_PRIMARY);
        field.setFont(FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER_INPUT, 1, true), // Secondary #7E7671 input border
                new EmptyBorder(8, 12, 8, 12)
        ));
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(COLOR_PRIMARY, 1, true), // Accent #6B7F72 focus ring
                        new EmptyBorder(8, 12, 8, 12)
                ));
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(COLOR_BORDER_INPUT, 1, true),
                        new EmptyBorder(8, 12, 8, 12)
                ));
            }
        });
    }

    public static void stylePasswordField(JPasswordField field) {
        styleTextField(field);
    }

    public static <T> void styleComboBox(JComboBox<T> combo) {
        combo.setBackground(Color.WHITE);
        combo.setForeground(COLOR_TEXT_MAIN);
        combo.setFont(FONT_BODY);
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER_INPUT, 1, true),
                new EmptyBorder(2, 6, 2, 6)
        ));
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(34);
        table.setFont(FONT_BODY);
        table.setBackground(Color.WHITE);
        table.setForeground(COLOR_TEXT_MAIN);
        table.setShowGrid(true);
        table.setGridColor(COLOR_BORDER);
        table.setSelectionBackground(COLOR_PRIMARY);
        table.setSelectionForeground(Color.WHITE);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BODY_BOLD);
        header.setBackground(COLOR_SURFACE);
        header.setForeground(COLOR_TEXT_MAIN);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_BORDER));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFoc, int row, int col) {
                Component comp = super.getTableCellRendererComponent(t, val, isSel, hasFoc, row, col);
                setHorizontalAlignment(JLabel.CENTER);
                if (isSel) {
                    comp.setBackground(COLOR_PRIMARY);
                    comp.setForeground(Color.WHITE);
                } else {
                    comp.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF9, 0xF6, 0xF1));
                    comp.setForeground(COLOR_TEXT_MAIN);
                }
                return comp;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    public static String getSpeedRank(int wpm) {
        if (wpm <= 0) return "Not Ranked";
        if (wpm < 30) return "Beginner";
        if (wpm < 50) return "Intermediate";
        if (wpm < 70) return "Proficient";
        if (wpm < 90) return "Advanced";
        return "Master Typist";
    }
}
