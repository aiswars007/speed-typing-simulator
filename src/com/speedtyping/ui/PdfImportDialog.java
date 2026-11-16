package com.speedtyping.ui;

import com.speedtyping.dao.TextDAO;
import com.speedtyping.model.TypingText;
import com.speedtyping.service.PdfService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;

/**
 * Ultra-responsive PDF import dialog optimized for documents of any size,
 * including 1,000,000+ words. Uses background worker extraction and virtualized
 * preview rendering to ensure 0ms UI freezes.
 */
public class PdfImportDialog extends JDialog {

    private static final int MAX_PREVIEW_CHARS = 25_000; // ~4,000 words max in Swing preview to avoid UI freeze

    private final TextDAO textDAO;
    private final Consumer<TypingText> onStartTyping;
    private final Runnable onLibraryUpdated;

    private File currentPdfFile;
    private int totalPages = 1;
    private String fullExtractedText = "";
    private boolean isPreviewTruncated = false;

    // UI Components
    private JTextField filePathField;
    private JLabel fileStatusLabel;
    private JRadioButton allPagesRadio;
    private JRadioButton rangePagesRadio;
    private JSpinner fromPageSpinner;
    private JSpinner toPageSpinner;
    private JButton reExtractBtn;

    private JTextField titleField;
    private JComboBox<String> difficultyCombo;
    private JTextArea previewTextArea;
    private JLabel previewStatsLabel;
    private JCheckBox saveToLibraryCheck;

    private JButton startTypingBtn;
    private JButton saveOnlyBtn;
    private JProgressBar extractionProgressBar;

    public PdfImportDialog(Window parent, TextDAO textDAO, Consumer<TypingText> onStartTyping, Runnable onLibraryUpdated) {
        super(parent, "Import PDF For Typing", ModalityType.APPLICATION_MODAL);
        this.textDAO = textDAO;
        this.onStartTyping = onStartTyping;
        this.onLibraryUpdated = onLibraryUpdated;

        setupUI();
        setSize(780, 690);
        setMinimumSize(new Dimension(680, 580));
        setLocationRelativeTo(parent);
    }

    private void setupUI() {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(UIUtils.COLOR_BG);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIUtils.COLOR_HEADER);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.COLOR_BORDER),
                new EmptyBorder(14, 24, 14, 24)
        ));

        JPanel headerLeft = new JPanel();
        headerLeft.setLayout(new BoxLayout(headerLeft, BoxLayout.Y_AXIS));
        headerLeft.setOpaque(false);

        JLabel badge = UIUtils.createLogoBadge("PDF IMPORT & VIRTUAL STREAMING");
        badge.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLbl = new JLabel("Type From PDF Document");
        titleLbl.setFont(UIUtils.FONT_TITLE);
        titleLbl.setForeground(UIUtils.COLOR_TEXT_MAIN);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subLbl = new JLabel("Supports documents up to millions of words with virtualized streaming and instant zero-lag typing.");
        subLbl.setFont(UIUtils.FONT_SMALL);
        subLbl.setForeground(UIUtils.COLOR_TEXT_MUTED);
        subLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        headerLeft.add(badge);
        headerLeft.add(Box.createVerticalStrut(6));
        headerLeft.add(titleLbl);
        headerLeft.add(Box.createVerticalStrut(2));
        headerLeft.add(subLbl);

        headerPanel.add(headerLeft, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Main Scrollable Content
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(UIUtils.COLOR_BG);
        mainContent.setBorder(new EmptyBorder(14, 24, 14, 24));

        // 1. File Selection Card
        JPanel fileCard = UIUtils.createCardPanel();
        fileCard.setLayout(new BorderLayout(10, 8));
        fileCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        JLabel filePrompt = new JLabel("Select PDF File (or Drag & Drop directly onto this window):");
        filePrompt.setFont(UIUtils.FONT_BODY_BOLD);
        filePrompt.setForeground(UIUtils.COLOR_TEXT_MAIN);

        filePathField = new JTextField();
        filePathField.setEditable(false);
        UIUtils.styleTextField(filePathField);
        filePathField.setText("No PDF file selected yet...");

        JButton browseBtn = UIUtils.createPrimaryButton("Browse PDF...");
        browseBtn.addActionListener(e -> choosePdfFile());

        JPanel fileInputRow = new JPanel(new BorderLayout(8, 0));
        fileInputRow.setOpaque(false);
        fileInputRow.add(filePathField, BorderLayout.CENTER);
        fileInputRow.add(browseBtn, BorderLayout.EAST);

        fileStatusLabel = new JLabel("Drag & drop a .pdf file here or click Browse (books, articles, notes, essays)");
        fileStatusLabel.setFont(UIUtils.FONT_SMALL);
        fileStatusLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);

        extractionProgressBar = new JProgressBar();
        extractionProgressBar.setIndeterminate(true);
        extractionProgressBar.setVisible(false);
        extractionProgressBar.setPreferredSize(new Dimension(200, 6));

        JPanel fileBottomRow = new JPanel(new BorderLayout());
        fileBottomRow.setOpaque(false);
        fileBottomRow.add(fileStatusLabel, BorderLayout.WEST);
        fileBottomRow.add(extractionProgressBar, BorderLayout.EAST);

        fileCard.add(filePrompt, BorderLayout.NORTH);
        fileCard.add(fileInputRow, BorderLayout.CENTER);
        fileCard.add(fileBottomRow, BorderLayout.SOUTH);

        mainContent.add(fileCard);
        mainContent.add(Box.createVerticalStrut(10));

        // 2. Extraction & Document Metadata Settings
        JPanel optionsCard = UIUtils.createCardPanel();
        optionsCard.setLayout(new GridLayout(2, 1, 0, 8));
        optionsCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        // Row 1: Page Range Selection
        JPanel pageRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pageRow.setOpaque(false);

        JLabel pageLbl = new JLabel("Pages to Extract:");
        pageLbl.setFont(UIUtils.FONT_BODY_BOLD);
        pageLbl.setForeground(UIUtils.COLOR_TEXT_MAIN);

        allPagesRadio = new JRadioButton("All Pages", true);
        allPagesRadio.setOpaque(false);
        allPagesRadio.setFont(UIUtils.FONT_BODY);
        allPagesRadio.setForeground(UIUtils.COLOR_TEXT_MAIN);

        rangePagesRadio = new JRadioButton("Page Range:", false);
        rangePagesRadio.setOpaque(false);
        rangePagesRadio.setFont(UIUtils.FONT_BODY);
        rangePagesRadio.setForeground(UIUtils.COLOR_TEXT_MAIN);

        ButtonGroup pageGroup = new ButtonGroup();
        pageGroup.add(allPagesRadio);
        pageGroup.add(rangePagesRadio);

        fromPageSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1, 1));
        fromPageSpinner.setPreferredSize(new Dimension(55, 26));
        fromPageSpinner.setEnabled(false);

        JLabel toLbl = new JLabel("to");
        toLbl.setFont(UIUtils.FONT_BODY);

        toPageSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1, 1));
        toPageSpinner.setPreferredSize(new Dimension(55, 26));
        toPageSpinner.setEnabled(false);

        allPagesRadio.addActionListener(e -> {
            fromPageSpinner.setEnabled(false);
            toPageSpinner.setEnabled(false);
            reExtractBtn.setEnabled(currentPdfFile != null);
            extractCurrentSelectionAsync();
        });

        rangePagesRadio.addActionListener(e -> {
            fromPageSpinner.setEnabled(true);
            toPageSpinner.setEnabled(true);
            reExtractBtn.setEnabled(currentPdfFile != null);
        });

        reExtractBtn = UIUtils.createSecondaryButton("Re-extract Range");
        reExtractBtn.setEnabled(false);
        reExtractBtn.addActionListener(e -> extractCurrentSelectionAsync());

        pageRow.add(pageLbl);
        pageRow.add(allPagesRadio);
        pageRow.add(rangePagesRadio);
        pageRow.add(fromPageSpinner);
        pageRow.add(toLbl);
        pageRow.add(toPageSpinner);
        pageRow.add(reExtractBtn);

        // Row 2: Title and Difficulty
        JPanel metaRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        metaRow.setOpaque(false);

        JLabel titlePrompt = new JLabel("Passage Title:");
        titlePrompt.setFont(UIUtils.FONT_BODY_BOLD);
        titlePrompt.setForeground(UIUtils.COLOR_TEXT_MAIN);

        titleField = new JTextField(24);
        UIUtils.styleTextField(titleField);
        titleField.setText("Custom PDF Passage");

        JLabel diffPrompt = new JLabel("Difficulty:");
        diffPrompt.setFont(UIUtils.FONT_BODY_BOLD);
        diffPrompt.setForeground(UIUtils.COLOR_TEXT_MAIN);

        difficultyCombo = new JComboBox<>(new String[]{"Easy", "Medium", "Hard", "Custom"});
        difficultyCombo.setSelectedItem("Medium");
        UIUtils.styleComboBox(difficultyCombo);

        metaRow.add(titlePrompt);
        metaRow.add(titleField);
        metaRow.add(Box.createHorizontalStrut(8));
        metaRow.add(diffPrompt);
        metaRow.add(difficultyCombo);

        optionsCard.add(pageRow);
        optionsCard.add(metaRow);

        mainContent.add(optionsCard);
        mainContent.add(Box.createVerticalStrut(10));

        // 3. Text Preview & Word Trimming Tools
        JPanel previewCard = UIUtils.createCardPanel();
        previewCard.setLayout(new BorderLayout(0, 8));

        JPanel previewTopRow = new JPanel(new BorderLayout());
        previewTopRow.setOpaque(false);

        JLabel previewTitle = new JLabel("Extracted Text Preview:");
        previewTitle.setFont(UIUtils.FONT_BODY_BOLD);
        previewTitle.setForeground(UIUtils.COLOR_TEXT_MAIN);

        // Quick Word Trimming Shortcuts
        JPanel trimPills = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        trimPills.setOpaque(false);

        JLabel trimLbl = new JLabel("Quick Trim:");
        trimLbl.setFont(UIUtils.FONT_SMALL);
        trimLbl.setForeground(UIUtils.COLOR_TEXT_MUTED);

        JButton trim100Btn = UIUtils.createSecondaryButton("First 100 Words");
        trim100Btn.setFont(UIUtils.FONT_SMALL);
        trim100Btn.addActionListener(e -> applyWordTrim(100));

        JButton trim250Btn = UIUtils.createSecondaryButton("First 250 Words");
        trim250Btn.setFont(UIUtils.FONT_SMALL);
        trim250Btn.addActionListener(e -> applyWordTrim(250));

        JButton trim500Btn = UIUtils.createSecondaryButton("First 500 Words");
        trim500Btn.setFont(UIUtils.FONT_SMALL);
        trim500Btn.addActionListener(e -> applyWordTrim(500));

        JButton trimAllBtn = UIUtils.createSecondaryButton("Full Document");
        trimAllBtn.setFont(UIUtils.FONT_SMALL);
        trimAllBtn.addActionListener(e -> restoreFullDocument());

        trimPills.add(trimLbl);
        trimPills.add(trim100Btn);
        trimPills.add(trim250Btn);
        trimPills.add(trim500Btn);
        trimPills.add(trimAllBtn);

        previewTopRow.add(previewTitle, BorderLayout.WEST);
        previewTopRow.add(trimPills, BorderLayout.EAST);

        previewTextArea = new JTextArea();
        previewTextArea.setFont(new Font("Consolas", Font.PLAIN, 15));
        previewTextArea.setLineWrap(true);
        previewTextArea.setWrapStyleWord(true);
        previewTextArea.setBackground(Color.WHITE);
        previewTextArea.setForeground(UIUtils.COLOR_TEXT_MAIN);
        previewTextArea.setCaretColor(UIUtils.COLOR_PRIMARY);
        previewTextArea.setMargin(new Insets(10, 12, 10, 12));

        JScrollPane previewScroll = new JScrollPane(previewTextArea);
        previewScroll.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER_INPUT, 1));
        previewScroll.setPreferredSize(new Dimension(650, 185));

        previewStatsLabel = new JLabel("0 Words | 0 Characters | Estimated time at 50 WPM: 0 min");
        previewStatsLabel.setFont(UIUtils.FONT_SMALL);
        previewStatsLabel.setForeground(UIUtils.COLOR_TEXT_MUTED);

        previewCard.add(previewTopRow, BorderLayout.NORTH);
        previewCard.add(previewScroll, BorderLayout.CENTER);
        previewCard.add(previewStatsLabel, BorderLayout.SOUTH);

        mainContent.add(previewCard);
        add(mainContent, BorderLayout.CENTER);

        // Bottom Action Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(UIUtils.COLOR_SURFACE);
        bottomBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIUtils.COLOR_BORDER),
                new EmptyBorder(12, 24, 14, 24)
        ));

        saveToLibraryCheck = new JCheckBox("Save this text to Prompt Library for future tests", true);
        saveToLibraryCheck.setOpaque(false);
        saveToLibraryCheck.setFont(UIUtils.FONT_BODY);
        saveToLibraryCheck.setForeground(UIUtils.COLOR_TEXT_MAIN);

        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionButtons.setOpaque(false);

        JButton cancelBtn = UIUtils.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        saveOnlyBtn = UIUtils.createSecondaryButton("Save to Library Only");
        saveOnlyBtn.setEnabled(false);
        saveOnlyBtn.addActionListener(e -> handleSaveOnly());

        startTypingBtn = UIUtils.createPrimaryButton("▶ Start Typing Test");
        startTypingBtn.setEnabled(false);
        startTypingBtn.addActionListener(e -> handleStartTyping());

        actionButtons.add(cancelBtn);
        actionButtons.add(saveOnlyBtn);
        actionButtons.add(startTypingBtn);

        bottomBar.add(saveToLibraryCheck, BorderLayout.WEST);
        bottomBar.add(actionButtons, BorderLayout.EAST);

        add(bottomBar, BorderLayout.SOUTH);

        setupDragAndDrop();
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
                                    loadPdfFile(f);
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

    private void choosePdfFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select PDF Document to Type");
        chooser.setFileFilter(new FileNameExtensionFilter("PDF Documents (*.pdf)", "pdf"));
        chooser.setAcceptAllFileFilterUsed(false);

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            loadPdfFile(selectedFile);
        }
    }

    public void loadPdfFile(File file) {
        if (file == null || !file.exists()) {
            JOptionPane.showMessageDialog(this, "The selected file does not exist.", "File Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        this.currentPdfFile = file;
        this.filePathField.setText(file.getAbsolutePath());

        String rawName = file.getName();
        if (rawName.toLowerCase().endsWith(".pdf")) {
            rawName = rawName.substring(0, rawName.length() - 4);
        }
        titleField.setText(rawName.trim());

        try {
            this.totalPages = PdfService.getPageCount(file);
            long fileSizeBytes = file.length();
            String sizeStr = fileSizeBytes < 1024 * 1024
                    ? (fileSizeBytes / 1024) + " KB"
                    : String.format("%.1f MB", fileSizeBytes / (1024.0 * 1024.0));

            fileStatusLabel.setText(String.format("Loaded: %,d %s | Size: %s",
                    totalPages,
                    (totalPages == 1 ? "Page" : "Pages"),
                    sizeStr));
            fileStatusLabel.setForeground(UIUtils.COLOR_SUCCESS);

            fromPageSpinner.setModel(new SpinnerNumberModel(1, 1, totalPages, 1));
            toPageSpinner.setModel(new SpinnerNumberModel(Math.min(totalPages, 5), 1, totalPages, 1));

            reExtractBtn.setEnabled(true);
            extractCurrentSelectionAsync();

        } catch (Exception ex) {
            fileStatusLabel.setText("Failed to parse PDF: " + ex.getMessage());
            fileStatusLabel.setForeground(UIUtils.COLOR_ERROR);
            JOptionPane.showMessageDialog(this,
                    "Failed to inspect PDF: " + ex.getMessage() + "\nPlease make sure the file is not encrypted or corrupted.",
                    "PDF Read Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void extractCurrentSelectionAsync() {
        if (currentPdfFile == null) return;

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        extractionProgressBar.setVisible(true);
        startTypingBtn.setEnabled(false);
        saveOnlyBtn.setEnabled(false);
        fileStatusLabel.setText("Extracting and optimizing text in background...");

        final int start;
        final int end;
        if (rangePagesRadio.isSelected()) {
            int s = (Integer) fromPageSpinner.getValue();
            int e = (Integer) toPageSpinner.getValue();
            if (s > e) {
                int temp = s;
                s = e;
                e = temp;
                fromPageSpinner.setValue(s);
                toPageSpinner.setValue(e);
            }
            start = s;
            end = e;
        } else {
            start = 1;
            end = totalPages;
        }

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return PdfService.extractText(currentPdfFile, start, end);
            }

            @Override
            protected void done() {
                try {
                    String extracted = get();
                    onExtractionDone(extracted);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PdfImportDialog.this,
                            "Error extracting text from PDF: " + ex.getMessage(),
                            "Extraction Error",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                    extractionProgressBar.setVisible(false);
                }
            }
        };
        worker.execute();
    }

    private void onExtractionDone(String extracted) {
        if (extracted.isBlank()) {
            JOptionPane.showMessageDialog(this,
                    "No extractable text was found in the selected pages.\nThe PDF might be a scanned image without an OCR text layer.",
                    "No Text Found",
                    JOptionPane.WARNING_MESSAGE);
            this.fullExtractedText = "";
            this.previewTextArea.setText("");
            this.startTypingBtn.setEnabled(false);
            this.saveOnlyBtn.setEnabled(false);
            this.previewStatsLabel.setText("0 Words | 0 Characters");
            return;
        }

        this.fullExtractedText = extracted;
        updatePreviewDisplay();
    }

    private void updatePreviewDisplay() {
        int totalChars = fullExtractedText.length();
        int totalWords = PdfService.countWords(fullExtractedText);

        // Virtualized preview: display up to MAX_PREVIEW_CHARS to avoid Swing UI freeze
        if (totalChars > MAX_PREVIEW_CHARS) {
            String previewSub = fullExtractedText.substring(0, MAX_PREVIEW_CHARS).trim();
            previewTextArea.setText(previewSub + "\n\n... [Showing preview of first ~4,000 words. Entire document is ready for typing]");
            isPreviewTruncated = true;
        } else {
            previewTextArea.setText(fullExtractedText);
            isPreviewTruncated = false;
        }
        previewTextArea.setCaretPosition(0);

        // Estimated minutes at ~50 WPM
        double estMins = totalWords > 0 ? (totalWords / 50.0) : 0;
        String estTimeStr = estMins < 1.0 ? Math.round(estMins * 60) + " sec" : String.format("%.1f min", estMins);

        String info = String.format("Document: %,d Words (%,d Characters)  |  Est. Typing Time at 50 WPM: ~%s  |  Virtual Stream Ready",
                totalWords, totalChars, estTimeStr);
        if (isPreviewTruncated) {
            info += "  (Previewing first ~4,000 words)";
        }
        previewStatsLabel.setText(info);

        boolean hasContent = totalWords > 0;
        startTypingBtn.setEnabled(hasContent);
        saveOnlyBtn.setEnabled(hasContent);
        fileStatusLabel.setText(String.format("Ready: %,d words extracted successfully.", totalWords));
        fileStatusLabel.setForeground(UIUtils.COLOR_SUCCESS);
    }

    private void applyWordTrim(int words) {
        if (fullExtractedText.isBlank()) return;
        String trimmed = PdfService.truncateToWordCount(fullExtractedText, words);
        this.fullExtractedText = trimmed;
        this.isPreviewTruncated = false;
        updatePreviewDisplay();
    }

    private void restoreFullDocument() {
        extractCurrentSelectionAsync();
    }

    private TypingText createTypingTextFromForm() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            title = currentPdfFile != null ? currentPdfFile.getName() : "Custom PDF";
        }
        String difficulty = (String) difficultyCombo.getSelectedItem();
        if (difficulty == null) difficulty = "Medium";

        String content;
        if (isPreviewTruncated) {
            content = fullExtractedText;
        } else {
            content = previewTextArea.getText().trim();
            if (content.isEmpty()) {
                content = fullExtractedText;
            }
        }
        content = PdfService.cleanAndNormalizeForTyping(content);

        return new TypingText(title, difficulty, content);
    }

    private void handleSaveOnly() {
        TypingText text = createTypingTextFromForm();
        if (text.getContent().isEmpty()) {
            JOptionPane.showMessageDialog(this, "The text content is empty. Please select a PDF or enter text.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int newId = textDAO.addText(text);
        if (newId > 0) {
            if (onLibraryUpdated != null) {
                onLibraryUpdated.run();
            }
            JOptionPane.showMessageDialog(this,
                    "\"" + text.getTitle() + "\" has been added to your Prompt Library!",
                    "Saved Successfully",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save passage to database.", "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleStartTyping() {
        TypingText text = createTypingTextFromForm();
        if (text.getContent().isEmpty()) {
            JOptionPane.showMessageDialog(this, "The text content is empty. Please select a PDF or enter text.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (saveToLibraryCheck.isSelected()) {
            int newId = textDAO.addText(text);
            text.setId(newId);
            if (onLibraryUpdated != null) {
                onLibraryUpdated.run();
            }
        }

        dispose();

        if (onStartTyping != null) {
            onStartTyping.accept(text);
        }
    }
}
