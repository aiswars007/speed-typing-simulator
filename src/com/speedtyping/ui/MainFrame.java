package com.speedtyping.ui;

import com.speedtyping.config.DatabaseConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.sql.Connection;
import java.util.List;

public class MainFrame extends JFrame {

    public static final String CARD_LOGIN = "LOGIN";
    public static final String CARD_DASHBOARD = "DASHBOARD";
    public static final String CARD_TEST = "TEST";

    private CardLayout cardLayout;
    private JPanel cardsPanel;

    private LoginPanel loginPanel;
    private DashboardPanel dashboardPanel;
    private TypingPanel typingPanel;
    private JLabel dbStatusLabel;

    public MainFrame() {
        super(UIUtils.APP_NAME);
        setupUI();
        checkDatabaseConnection();
    }

    private void setupUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setMinimumSize(new Dimension(920, 640));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIUtils.COLOR_BG);

        // Card Layout for Login -> Dashboard -> Test Arena
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);

        loginPanel = new LoginPanel(this);
        dashboardPanel = new DashboardPanel(this);
        typingPanel = new TypingPanel(this);

        cardsPanel.add(loginPanel, CARD_LOGIN);
        cardsPanel.add(dashboardPanel, CARD_DASHBOARD);
        cardsPanel.add(typingPanel, CARD_TEST);

        add(cardsPanel, BorderLayout.CENTER);

        // Persistent Minimal Bottom Status Bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(UIUtils.COLOR_HEADER);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIUtils.COLOR_BORDER),
                new EmptyBorder(6, 24, 6, 24)
        ));

        dbStatusLabel = new JLabel("Connecting to MySQL...");
        dbStatusLabel.setFont(UIUtils.FONT_SMALL);
        dbStatusLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);

        JLabel appVersionLabel = new JLabel("ISTA Platform v2.0 | MySQL Integrated");
        appVersionLabel.setFont(UIUtils.FONT_SMALL);
        appVersionLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);

        statusBar.add(dbStatusLabel, BorderLayout.WEST);
        statusBar.add(appVersionLabel, BorderLayout.EAST);
        add(statusBar, BorderLayout.SOUTH);

        setupDragAndDrop();

        // Default screen is Login
        showLogin();
    }

    private void setupDragAndDrop() {
        new DropTarget(this, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    Object data = evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    if (data instanceof List<?>) {
                        List<?> files = (List<?>) data;
                        for (Object obj : files) {
                            if (obj instanceof File) {
                                File f = (File) obj;
                                if (f.getName().toLowerCase().endsWith(".pdf")) {
                                    openPdfFileDirectly(f);
                                    evt.dropComplete(true);
                                    return;
                                }
                            }
                        }
                    }
                    evt.dropComplete(false);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    evt.dropComplete(false);
                }
            }
        });
    }

    public void openPdfFileDirectly(File file) {
        PdfImportDialog dialog = new PdfImportDialog(
                this,
                new com.speedtyping.dao.TextDAO(),
                customText -> startTestWithText(customText, 60),
                () -> {
                    if (typingPanel != null) {
                        typingPanel.reloadTexts();
                    }
                }
        );
        dialog.loadPdfFile(file);
        dialog.setVisible(true);
    }

    private void checkDatabaseConnection() {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                try (Connection conn = DatabaseConfig.getConnection()) {
                    return conn != null && !conn.isClosed();
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    boolean ok = get();
                    if (ok) {
                        dbStatusLabel.setText("[Online] Connected to MySQL (speedtyping_db)");
                        dbStatusLabel.setForeground(UIUtils.COLOR_SUCCESS);
                    } else {
                        dbStatusLabel.setText("[Offline] Running in standalone local mode");
                        dbStatusLabel.setForeground(UIUtils.COLOR_ERROR);
                    }
                } catch (Exception e) {
                    dbStatusLabel.setText("[Offline] Database note: " + e.getMessage());
                    dbStatusLabel.setForeground(UIUtils.COLOR_ERROR);
                }
            }
        };
        worker.execute();
    }

    public void showLogin() {
        loginPanel.resetForm();
        cardLayout.show(cardsPanel, CARD_LOGIN);
    }

    public void showDashboard() {
        dashboardPanel.refreshDashboard();
        cardLayout.show(cardsPanel, CARD_DASHBOARD);
    }

    public void startTest(int durationSeconds) {
        typingPanel.setTestDuration(durationSeconds);
        cardLayout.show(cardsPanel, CARD_TEST);
        typingPanel.focusInput();
    }

    public void startTest() {
        startTest(60);
    }

    public void startTestWithText(com.speedtyping.model.TypingText text, int durationSeconds) {
        typingPanel.setTestDuration(durationSeconds);
        typingPanel.loadCustomText(text);
        cardLayout.show(cardsPanel, CARD_TEST);
        typingPanel.focusInput();
    }

    public TypingPanel getTypingPanel() {
        return typingPanel;
    }

    public DashboardPanel getDashboardPanel() {
        return dashboardPanel;
    }
}
