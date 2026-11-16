package com.speedtyping.ui;

import com.speedtyping.dao.ResultDAO;
import com.speedtyping.dao.TextDAO;
import com.speedtyping.model.TypingResult;
import com.speedtyping.model.TypingText;
import com.speedtyping.service.AuthService;
import com.speedtyping.service.PdfService;
import com.speedtyping.service.TextSegmenter;
import com.speedtyping.service.TypingEngine;
import com.speedtyping.service.TypingEvaluator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Ultra-responsive SpeedTyping arena featuring virtualized segment streaming
 * capable of handling documents up to millions of words with 0ms keystroke lag.
 */
public class TypingPanel extends JPanel {

    private final MainFrame mainFrame;
    private final TextDAO textDAO;
    private final ResultDAO resultDAO;
    private final TypingEngine engine;

    // Stat Labels
    private JLabel timeValueLabel;
    private JLabel timeCardTitleLabel;
    private JLabel timeCardSubLabel;
    private JLabel wpmValueLabel;
    private JLabel accuracyValueLabel;
    private JLabel errorsValueLabel;

    // Controls
    private JButton backToDashboardBtn;
    private JComboBox<Integer> durationCombo;
    private JComboBox<TypingText> textCombo;
    private JButton restartBtn;
    private JButton randomTextBtn;

    // Virtual Segment Streaming UI
    private JPanel segmentBar;
    private JLabel segmentInfoLabel;
    private JButton prevSegmentBtn;
    private JButton nextSegmentBtn;

    // Prompt & Input
    private JTextPane promptTextPane;
    private JTextField inputField;
    private JLabel instructionLabel;
    private JProgressBar testProgressBar;

    // Styles for text highlight
    private SimpleAttributeSet styleNormal;
    private SimpleAttributeSet styleCorrect;
    private SimpleAttributeSet styleIncorrect;
    private SimpleAttributeSet styleCurrent;

    private List<TypingText> availableTexts;
    private TypingText currentText;
    private TextSegmenter segmenter;
    private int currentSegmentIndex = 0;
    private String currentSegmentTarget = "";

    private boolean isUpdatingStyle = false;

    public TypingPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.textDAO = new TextDAO();
        this.resultDAO = new ResultDAO();
        this.engine = new TypingEngine();

        initStyles();
        setupUI();
        initEngine();
        loadTexts();
    }

    private void initStyles() {
        styleNormal = new SimpleAttributeSet();
        StyleConstants.setFontFamily(styleNormal, "Consolas");
        StyleConstants.setFontSize(styleNormal, 20);
        StyleConstants.setForeground(styleNormal, UIUtils.COLOR_TEXT_MUTED);
        StyleConstants.setBackground(styleNormal, UIUtils.COLOR_CARD_MUTED);
        StyleConstants.setUnderline(styleNormal, false);

        styleCorrect = new SimpleAttributeSet();
        StyleConstants.setFontFamily(styleCorrect, "Consolas");
        StyleConstants.setFontSize(styleCorrect, 20);
        StyleConstants.setForeground(styleCorrect, UIUtils.COLOR_TYPING_CORRECT_FG);
        StyleConstants.setBackground(styleCorrect, UIUtils.COLOR_TYPING_CORRECT_BG);
        StyleConstants.setUnderline(styleCorrect, false);

        styleIncorrect = new SimpleAttributeSet();
        StyleConstants.setFontFamily(styleIncorrect, "Consolas");
        StyleConstants.setFontSize(styleIncorrect, 20);
        StyleConstants.setForeground(styleIncorrect, UIUtils.COLOR_TYPING_ERROR_FG);
        StyleConstants.setBackground(styleIncorrect, UIUtils.COLOR_TYPING_ERROR_BG);
        StyleConstants.setUnderline(styleIncorrect, true);

        styleCurrent = new SimpleAttributeSet();
        StyleConstants.setFontFamily(styleCurrent, "Consolas");
        StyleConstants.setFontSize(styleCurrent, 20);
        StyleConstants.setForeground(styleCurrent, UIUtils.COLOR_TYPING_CURRENT_FG);
        StyleConstants.setBackground(styleCurrent, UIUtils.COLOR_TYPING_CURRENT_BG);
        StyleConstants.setUnderline(styleCurrent, true);
        StyleConstants.setBold(styleCurrent, true);
    }

    private void setupUI() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIUtils.COLOR_BG);
        setBorder(new EmptyBorder(16, 28, 20, 28));

        // Top Section: Nav + Controls + Stats Cards
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        // Row 1: Nav Bar (Back to Dashboard on Left, Restart Button on Right)
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setOpaque(false);
        navBar.setBorder(new EmptyBorder(0, 0, 8, 0));

        backToDashboardBtn = UIUtils.createSecondaryButton("<- Back to Dashboard");
        backToDashboardBtn.addActionListener(e -> {
            engine.reset();
            mainFrame.showDashboard();
        });

        restartBtn = UIUtils.createPrimaryButton("Restart Test (Tab)");
        restartBtn.addActionListener(e -> resetTest());

        navBar.add(backToDashboardBtn, BorderLayout.WEST);
        navBar.add(restartBtn, BorderLayout.EAST);

        // Row 2: Test Options Bar (Duration, Prompt Text dropdown, Random Text, Import PDF)
        JPanel optionsBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        optionsBar.setOpaque(false);
        optionsBar.setBorder(new EmptyBorder(0, 0, 14, 0));

        JLabel durationLbl = new JLabel("Duration:");
        durationLbl.setFont(UIUtils.FONT_BODY_BOLD);
        durationLbl.setForeground(UIUtils.COLOR_TEXT_MAIN);
        durationCombo = new JComboBox<>(new Integer[]{30, 60, 120, 180, 300, 0});
        durationCombo.setSelectedItem(60);
        UIUtils.styleComboBox(durationCombo);
        durationCombo.setRenderer(new DefaultListCellRenderer() {
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
        durationCombo.addActionListener(e -> resetTest());

        JLabel textLbl = new JLabel("Prompt Text:");
        textLbl.setFont(UIUtils.FONT_BODY_BOLD);
        textLbl.setForeground(UIUtils.COLOR_TEXT_MAIN);
        textCombo = new JComboBox<>();
        textCombo.setPreferredSize(new Dimension(280, 32));
        UIUtils.styleComboBox(textCombo);
        textCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TypingText) {
                    setText(((TypingText) value).getTitle());
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
        textCombo.addActionListener(e -> {
            TypingText selected = (TypingText) textCombo.getSelectedItem();
            if (selected != null && selected != currentText) {
                currentText = selected;
                resetTest();
            }
        });

        randomTextBtn = UIUtils.createSecondaryButton("Random Text");
        randomTextBtn.addActionListener(e -> pickRandomText());

        JButton importPdfBtn = UIUtils.createPrimaryButton("📄 Import PDF");
        importPdfBtn.setToolTipText("Load text from any PDF document to practice typing");
        importPdfBtn.addActionListener(e -> openPdfImportDialog());

        optionsBar.add(durationLbl);
        optionsBar.add(durationCombo);
        optionsBar.add(Box.createHorizontalStrut(10));
        optionsBar.add(textLbl);
        optionsBar.add(textCombo);
        optionsBar.add(randomTextBtn);
        optionsBar.add(importPdfBtn);

        // Stat Boxes Panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 16, 0));
        statsPanel.setOpaque(false);

        timeValueLabel = new JLabel("01:00");
        timeCardTitleLabel = new JLabel("TIME LEFT");
        timeCardSubLabel = new JLabel("Countdown timer");
        JPanel timeCard = createCustomStatBox(timeCardTitleLabel, timeValueLabel, timeCardSubLabel, UIUtils.COLOR_PRIMARY);

        wpmValueLabel = new JLabel("0");
        JPanel wpmCard = UIUtils.createStatBox("NET SPEED", wpmValueLabel, "Words Per Minute", UIUtils.COLOR_SUCCESS);

        accuracyValueLabel = new JLabel("100%");
        JPanel accCard = UIUtils.createStatBox("ACCURACY", accuracyValueLabel, "Typing precision", UIUtils.COLOR_ACCENT);

        errorsValueLabel = new JLabel("0");
        JPanel errCard = UIUtils.createStatBox("ERRORS", errorsValueLabel, "Incorrect keystrokes", UIUtils.COLOR_ERROR);

        statsPanel.add(timeCard);
        statsPanel.add(wpmCard);
        statsPanel.add(accCard);
        statsPanel.add(errCard);

        topContainer.add(navBar);
        topContainer.add(optionsBar);
        topContainer.add(statsPanel);
        add(topContainer, BorderLayout.NORTH);

        // Center Section: Prompt Text Box & Input Area
        JPanel centerCard = UIUtils.createCardPanel();
        centerCard.setLayout(new BorderLayout(0, 10));

        // Segment Navigation Toolbar (for large documents)
        segmentBar = new JPanel(new BorderLayout());
        segmentBar.setOpaque(false);
        segmentBar.setBorder(new EmptyBorder(0, 2, 4, 2));

        segmentInfoLabel = new JLabel("Standard Prompt Mode");
        segmentInfoLabel.setFont(UIUtils.FONT_BODY_BOLD);
        segmentInfoLabel.setForeground(UIUtils.COLOR_PRIMARY);

        JPanel segNavBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        segNavBtns.setOpaque(false);

        prevSegmentBtn = UIUtils.createSecondaryButton("◀ Prev Segment");
        prevSegmentBtn.setFont(UIUtils.FONT_SMALL);
        prevSegmentBtn.addActionListener(e -> {
            if (currentSegmentIndex > 0) {
                goToSegment(currentSegmentIndex - 1);
            }
        });

        nextSegmentBtn = UIUtils.createSecondaryButton("Next Segment ▶");
        nextSegmentBtn.setFont(UIUtils.FONT_SMALL);
        nextSegmentBtn.addActionListener(e -> {
            if (segmenter != null && currentSegmentIndex + 1 < segmenter.getSegmentCount()) {
                goToSegment(currentSegmentIndex + 1);
            }
        });

        segNavBtns.add(prevSegmentBtn);
        segNavBtns.add(nextSegmentBtn);

        segmentBar.add(segmentInfoLabel, BorderLayout.WEST);
        segmentBar.add(segNavBtns, BorderLayout.EAST);
        centerCard.add(segmentBar, BorderLayout.NORTH);

        // Prompt Display
        promptTextPane = new JTextPane();
        promptTextPane.setEditable(false);
        promptTextPane.setFont(UIUtils.FONT_TYPING);
        promptTextPane.setBackground(Color.WHITE);
        promptTextPane.setForeground(UIUtils.COLOR_TEXT_MAIN);
        promptTextPane.setCaretColor(UIUtils.COLOR_PRIMARY);
        promptTextPane.setMargin(new Insets(16, 18, 16, 18));
        promptTextPane.setFocusable(false);

        JScrollPane scrollPane = new JScrollPane(promptTextPane);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER_INPUT, 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBackground(Color.WHITE);
        scrollPane.setPreferredSize(new Dimension(800, 190));

        // Bottom Input Container
        JPanel inputContainer = new JPanel(new BorderLayout(0, 8));
        inputContainer.setOpaque(false);

        instructionLabel = new JLabel("Click below and begin typing to start the countdown... (Press Tab or Esc to restart)");
        instructionLabel.setFont(UIUtils.FONT_BODY);
        instructionLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);

        inputField = new JTextField();
        inputField.setFont(UIUtils.FONT_TYPING);
        inputField.setBackground(Color.WHITE);
        inputField.setForeground(UIUtils.COLOR_TEXT_MAIN);
        inputField.setCaretColor(UIUtils.COLOR_PRIMARY);
        inputField.setSelectedTextColor(Color.WHITE);
        inputField.setSelectionColor(UIUtils.COLOR_PRIMARY);
        inputField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIUtils.COLOR_BORDER_INPUT, 2, true),
                new EmptyBorder(10, 14, 10, 14)
        ));

        // Highlight input border on focus
        inputField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                inputField.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(UIUtils.COLOR_PRIMARY, 2, true),
                        new EmptyBorder(10, 14, 10, 14)
                ));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                inputField.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(UIUtils.COLOR_BORDER_INPUT, 2, true),
                        new EmptyBorder(10, 14, 10, 14)
                ));
            }
        });

        // Progress bar showing completion of the current text
        testProgressBar = new JProgressBar(0, 100);
        testProgressBar.setValue(0);
        testProgressBar.setStringPainted(true);
        testProgressBar.setForeground(UIUtils.COLOR_PRIMARY);
        testProgressBar.setBackground(UIUtils.COLOR_SURFACE);
        testProgressBar.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER, 1));
        testProgressBar.setPreferredSize(new Dimension(800, 14));

        inputContainer.add(instructionLabel, BorderLayout.NORTH);
        inputContainer.add(inputField, BorderLayout.CENTER);
        inputContainer.add(testProgressBar, BorderLayout.SOUTH);

        centerCard.add(scrollPane, BorderLayout.CENTER);
        centerCard.add(inputContainer, BorderLayout.SOUTH);

        add(centerCard, BorderLayout.CENTER);

        // Keyboard & Document listeners
        inputField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                onInputChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onInputChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onInputChanged();
            }
        });

        // Tab or Escape to quick restart
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_TAB || e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    e.consume();
                    resetTest();
                }
            }
        });
    }

    private void initEngine() {
        engine.setListener(new TypingEngine.TypingEventListener() {
            @Override
            public void onTick(int secondsRemainingOrElapsed, int currentWpm, int rawWpm, double accuracy, int errors) {
                SwingUtilities.invokeLater(() -> {
                    int minutes = secondsRemainingOrElapsed / 60;
                    int secs = secondsRemainingOrElapsed % 60;
                    timeValueLabel.setText(String.format("%02d:%02d", minutes, secs));
                    wpmValueLabel.setText(String.valueOf(currentWpm));
                    accuracyValueLabel.setText(String.format("%.1f%%", accuracy));
                    errorsValueLabel.setText(String.valueOf(errors));

                    if (engine.isRunning()) {
                        if (engine.isUntilFinishedMode()) {
                            instructionLabel.setText("Typing in progress... Maintain steady rhythm until the test is finished!");
                        } else {
                            instructionLabel.setText("Typing in progress... Maintain steady rhythm and accuracy!");
                        }
                        instructionLabel.setForeground(UIUtils.COLOR_SUCCESS);
                    }
                });
            }

            @Override
            public void onFinished(TypingResult result) {
                SwingUtilities.invokeLater(() -> handleTestFinished(result));
            }
        });
    }

    public void openPdfImportDialog() {
        PdfImportDialog dialog = new PdfImportDialog(
                SwingUtilities.getWindowAncestor(this),
                textDAO,
                customText -> loadCustomText(customText),
                () -> reloadTexts()
        );
        dialog.setVisible(true);
    }

    public void loadCustomText(TypingText text) {
        if (text == null) return;
        reloadTexts();
        boolean matched = false;
        for (int i = 0; i < textCombo.getItemCount(); i++) {
            TypingText item = textCombo.getItemAt(i);
            if (item.getId() == text.getId() || item.getTitle().equalsIgnoreCase(text.getTitle())) {
                textCombo.setSelectedIndex(i);
                currentText = item;
                matched = true;
                break;
            }
        }
        if (!matched) {
            textCombo.addItem(text);
            textCombo.setSelectedItem(text);
            currentText = text;
        }
        resetTest();
        focusInput();
    }

    public void reloadTexts() {
        TypingText selectedBefore = (TypingText) textCombo.getSelectedItem();
        availableTexts = textDAO.getAllTexts();
        textCombo.removeAllItems();
        TypingText toSelect = null;
        for (TypingText t : availableTexts) {
            textCombo.addItem(t);
            if (selectedBefore != null && (t.getId() == selectedBefore.getId() || t.getTitle().equals(selectedBefore.getTitle()))) {
                toSelect = t;
            }
        }
        if (toSelect != null) {
            textCombo.setSelectedItem(toSelect);
            currentText = toSelect;
        } else if (!availableTexts.isEmpty()) {
            currentText = availableTexts.get(0);
            textCombo.setSelectedItem(currentText);
        }
    }

    private void loadTexts() {
        reloadTexts();
        resetTest();
    }

    private void pickRandomText() {
        if (availableTexts != null && !availableTexts.isEmpty()) {
            int currentIdx = textCombo.getSelectedIndex();
            int nextIdx = (currentIdx + 1 + (int)(Math.random() * (availableTexts.size() - 1))) % availableTexts.size();
            textCombo.setSelectedIndex(nextIdx);
        }
    }

    public void setTestDuration(int seconds) {
        durationCombo.setSelectedItem(seconds);
        resetTest();
    }

    public void resetTest() {
        if (currentText == null) return;
        Integer duration = (Integer) durationCombo.getSelectedItem();
        if (duration == null) duration = 60;

        // Initialize virtual segment streaming engine
        segmenter = new TextSegmenter(currentText.getContent(), TextSegmenter.DEFAULT_TARGET_WORDS_PER_SEGMENT);
        currentSegmentIndex = 0;
        currentSegmentTarget = segmenter.getSegmentText(0);

        engine.init(currentText.getContent(), duration);

        inputField.setText("");
        inputField.setEnabled(true);
        inputField.requestFocusInWindow();

        if (engine.isUntilFinishedMode()) {
            timeCardTitleLabel.setText("TIME ELAPSED");
            timeCardSubLabel.setText("Until the test is finished");
            timeValueLabel.setText("00:00");
            instructionLabel.setText("Click below and begin typing... (No time limit - practice until the test is finished)");
        } else {
            timeCardTitleLabel.setText("TIME LEFT");
            timeCardSubLabel.setText("Countdown timer");
            int min = duration / 60;
            int sec = duration % 60;
            timeValueLabel.setText(String.format("%02d:%02d", min, sec));
            instructionLabel.setText("Click below and begin typing to start the countdown... (Press Tab or Esc to restart)");
        }
        instructionLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);

        wpmValueLabel.setText("0");
        accuracyValueLabel.setText("100%");
        errorsValueLabel.setText("0");

        testProgressBar.setValue(0);
        testProgressBar.setString("0% Completed");

        updateSegmentUI();
        renderPromptCurrentSegment();
    }

    private JPanel createCustomStatBox(JLabel titleLbl, JLabel valueLabel, JLabel subLbl, Color accentColor) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(UIUtils.COLOR_CARD);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UIUtils.COLOR_BORDER, 1),
                        new EmptyBorder(10, 14, 10, 14)
                )
        ));

        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(UIUtils.COLOR_TEXT_MUTED);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLabel.setFont(UIUtils.FONT_STAT_NUMBER);
        valueLabel.setForeground(UIUtils.COLOR_TEXT_MAIN);
        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        subLbl.setFont(UIUtils.FONT_SMALL);
        subLbl.setForeground(UIUtils.COLOR_TEXT_MUTED);
        subLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        box.add(titleLbl);
        box.add(Box.createVerticalStrut(4));
        box.add(valueLabel);
        box.add(Box.createVerticalStrut(2));
        box.add(subLbl);

        box.setPreferredSize(new Dimension(145, 95));
        return box;
    }

    private void updateSegmentUI() {
        if (segmenter == null) return;
        int totalSegments = segmenter.getSegmentCount();
        int totalWords = segmenter.getTotalWords();

        if (totalSegments > 1) {
            segmentBar.setVisible(true);
            segmentInfoLabel.setText(String.format("Segment %d of %,d  |  Document: %,d Words  (Continuous Virtual Flow)",
                    currentSegmentIndex + 1, totalSegments, totalWords));
            prevSegmentBtn.setEnabled(currentSegmentIndex > 0);
            nextSegmentBtn.setEnabled(currentSegmentIndex + 1 < totalSegments);
        } else {
            segmentBar.setVisible(false);
        }
    }

    private void goToSegment(int targetIndex) {
        if (segmenter == null) return;
        int clamped = Math.max(0, Math.min(targetIndex, segmenter.getSegmentCount() - 1));
        currentSegmentIndex = clamped;
        currentSegmentTarget = segmenter.getSegmentText(clamped);

        inputField.setText("");
        inputField.requestFocusInWindow();
        updateSegmentUI();
        renderPromptCurrentSegment();
    }

    private SimpleAttributeSet getStyleForStatus(int status) {
        switch (status) {
            case TypingEvaluator.STATUS_CORRECT:
                return styleCorrect;
            case TypingEvaluator.STATUS_INCORRECT:
                return styleIncorrect;
            case TypingEvaluator.STATUS_CURRENT:
                return styleCurrent;
            default:
                return styleNormal;
        }
    }

    private void advanceToNextSegment(int correct, int incorrect, int typedCount) {
        engine.commitSegment(correct, incorrect, typedCount);
        currentSegmentIndex++;
        currentSegmentTarget = segmenter.getSegmentText(currentSegmentIndex);

        // Defer UI update to prevent Swing "Attempt to mutate in notification" IllegalStateException
        SwingUtilities.invokeLater(() -> {
            isUpdatingStyle = true;
            try {
                inputField.setText("");
            } finally {
                isUpdatingStyle = false;
            }
            updateSegmentUI();
            renderPromptCurrentSegment();
        });
    }

    /**
     * Renders only the active ~50-word segment into the prompt text pane,
     * maintaining blazing fast performance regardless of total document size.
     */
    private void renderPromptCurrentSegment() {
        if (currentSegmentTarget == null) return;
        StyledDocument doc = promptTextPane.getStyledDocument();
        try {
            isUpdatingStyle = true;
            doc.remove(0, doc.getLength());
            doc.insertString(0, currentSegmentTarget, styleNormal);

            // Highlight the initial character
            if (!currentSegmentTarget.isEmpty()) {
                doc.setCharacterAttributes(0, 1, styleCurrent, true);
            }
            promptTextPane.setCaretPosition(0);
        } catch (BadLocationException e) {
            e.printStackTrace();
        } finally {
            isUpdatingStyle = false;
        }
    }

    /**
     * Ultra-fast word-synchronized keystroke handler.
     * Prevents cascading off-by-one errors by synchronizing input at the word level,
     * isolating mistyped characters to their respective words and ensuring seamless segment transitions.
     */
    private void onInputChanged() {
        if (isUpdatingStyle || currentSegmentTarget == null || engine.isFinished()) return;

        String target = currentSegmentTarget;
        String typed = inputField.getText();

        boolean isLastSeg = (segmenter == null || currentSegmentIndex >= segmenter.getSegmentCount() - 1);
        TypingEvaluator.MatchResult res = TypingEvaluator.evaluate(target, typed, isLastSeg);

        int targetLen = target.length();
        StyledDocument doc = promptTextPane.getStyledDocument();

        try {
            isUpdatingStyle = true;

            int[] status = res.targetCharStatus;
            int runStart = 0;
            int runStatus = (status.length > 0) ? status[0] : TypingEvaluator.STATUS_NORMAL;

            for (int i = 1; i < status.length; i++) {
                if (status[i] != runStatus) {
                    doc.setCharacterAttributes(runStart, i - runStart, getStyleForStatus(runStatus), true);
                    runStart = i;
                    runStatus = status[i];
                }
            }
            if (status.length > runStart) {
                doc.setCharacterAttributes(runStart, status.length - runStart, getStyleForStatus(runStatus), true);
            }

            int caretPos = Math.min(targetLen, Math.max(0, res.currentTargetCharIndex));
            promptTextPane.setCaretPosition(caretPos);

            // Calculate overall document progress
            int totalWords = Math.max(1, segmenter != null ? segmenter.getTotalWords() : 1);
            int wordsBefore = currentSegmentIndex * TextSegmenter.DEFAULT_TARGET_WORDS_PER_SEGMENT;
            int totalWordsDone = Math.min(totalWords, wordsBefore + res.completedWords);
            int percent = (int) Math.min(100, Math.round((totalWordsDone * 100.0) / totalWords));
            testProgressBar.setValue(percent);
            testProgressBar.setString(String.format("Completed: %,d / %,d Words (%d%%)", totalWordsDone, totalWords, percent));

            int currentSegTyped = res.correctChars + res.incorrectChars;
            engine.updateKeystrokes(res.correctChars, res.incorrectChars, currentSegTyped);

            // Check for segment completion
            if (res.isSegmentFinished) {
                if (segmenter != null && currentSegmentIndex + 1 < segmenter.getSegmentCount()) {
                    advanceToNextSegment(res.correctChars, res.incorrectChars, currentSegTyped);
                } else {
                    engine.finish();
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            isUpdatingStyle = false;
        }
    }

    private void handleTestFinished(TypingResult result) {
        inputField.setEnabled(false);
        instructionLabel.setText("Test session completed!");
        instructionLabel.setForeground(UIUtils.COLOR_SUCCESS);

        result.setTextTitle(currentText != null ? currentText.getTitle() : "Standard Test");

        // Save to database if user is logged in
        if (AuthService.isLoggedIn()) {
            result.setUserId(AuthService.getCurrentUser().getId());
            boolean saved = resultDAO.saveResult(result);
            if (!saved) {
                System.err.println("Notice: Could not save test result to database.");
            }
        }

        // Show Results Dialog
        ResultsDialog dialog = new ResultsDialog(mainFrame, result, () -> {
            resetTest();
        }, () -> {
            pickRandomText();
            resetTest();
        }, () -> {
            mainFrame.showDashboard();
        });
        dialog.setVisible(true);
    }

    public void focusInput() {
        SwingUtilities.invokeLater(() -> inputField.requestFocusInWindow());
    }
}
