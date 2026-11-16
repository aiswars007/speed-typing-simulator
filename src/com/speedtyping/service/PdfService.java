package com.speedtyping.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;

/**
 * High-performance service for loading, extracting, and normalizing text from PDF documents.
 * Optimized specifically for speed typing exercises and capable of processing
 * large documents with up to millions of words in milliseconds.
 */
public class PdfService {

    /**
     * Retrieves the total page count of a PDF file.
     *
     * @param file the PDF file to inspect
     * @return total number of pages
     * @throws IOException if the file cannot be read or is invalid
     */
    public static int getPageCount(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IOException("File does not exist: " + (file != null ? file.getPath() : "null"));
        }
        try (PDDocument document = Loader.loadPDF(file)) {
            return document.getNumberOfPages();
        }
    }

    /**
     * Extracts text from all pages in the PDF file and cleans it for typing.
     *
     * @param file the PDF file
     * @return cleaned, typable text
     * @throws IOException if reading fails
     */
    public static String extractText(File file) throws IOException {
        return extractText(file, 1, Integer.MAX_VALUE);
    }

    /**
     * Extracts text from a specified page range (1-indexed inclusive) in the PDF file.
     *
     * @param file the PDF file
     * @param startPage starting page (1-based)
     * @param endPage ending page (1-based)
     * @return cleaned, typable text
     * @throws IOException if reading fails
     */
    public static String extractText(File file, int startPage, int endPage) throws IOException {
        if (file == null || !file.exists()) {
            throw new IOException("PDF file does not exist.");
        }

        try (PDDocument document = Loader.loadPDF(file)) {
            int totalPages = document.getNumberOfPages();
            if (totalPages == 0) {
                return "";
            }

            int validStart = Math.max(1, startPage);
            int validEnd = Math.min(totalPages, Math.max(validStart, endPage));

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(validStart);
            stripper.setEndPage(validEnd);
            stripper.setSortByPosition(true);

            String rawText = stripper.getText(document);
            return cleanAndNormalizeForTyping(rawText);
        }
    }

    /**
     * Single-pass ultra-fast text normalizer that processes millions of characters in milliseconds:
     * - Merges words hyphenated at line wraps (e.g. "com-\nputing" -> "computing")
     * - Normalizes smart quotes, apostrophes, dashes, and ligatures to standard keyboard characters
     * - Flattens line breaks and multi-space whitespace sequences into single spaces
     * - Strips unprintable control characters
     */
    public static String cleanAndNormalizeForTyping(String rawText) {
        if (rawText == null || rawText.isEmpty()) {
            return "";
        }

        int len = rawText.length();
        StringBuilder sb = new StringBuilder(len);
        boolean prevWasSpace = true; // Automatically trims leading spaces

        for (int i = 0; i < len; i++) {
            char c = rawText.charAt(i);

            // Handle word hyphenated at line wrap (e.g. "inter-\n" or "inter-\r\n")
            if (c == '-' && i + 1 < len) {
                int next = i + 1;
                if (rawText.charAt(next) == '\r') next++;
                if (next < len && rawText.charAt(next) == '\n') {
                    // Check if hyphen is between letters
                    if (i > 0 && Character.isLetter(rawText.charAt(i - 1))) {
                        int afterNl = next + 1;
                        while (afterNl < len && (rawText.charAt(afterNl) == ' ' || rawText.charAt(afterNl) == '\t')) {
                            afterNl++;
                        }
                        if (afterNl < len && Character.isLetter(rawText.charAt(afterNl))) {
                            // Skip hyphen, newline and indentation spaces
                            i = afterNl - 1;
                            continue;
                        }
                    }
                }
            }

            // Ligature handling
            if (c == '\uFB01') { sb.append("fi"); prevWasSpace = false; continue; }
            if (c == '\uFB02') { sb.append("fl"); prevWasSpace = false; continue; }
            if (c == '\uFB00') { sb.append("ff"); prevWasSpace = false; continue; }
            if (c == '\uFB03') { sb.append("ffi"); prevWasSpace = false; continue; }
            if (c == '\uFB04') { sb.append("ffl"); prevWasSpace = false; continue; }
            if (c == '\u0153') { sb.append("oe"); prevWasSpace = false; continue; }
            if (c == '\u00E6') { sb.append("ae"); prevWasSpace = false; continue; }
            if (c == '\u2026') { sb.append("..."); prevWasSpace = false; continue; }

            // Normalize quotes, dashes, spaces
            char norm = normalizeChar(c);
            if (norm == '\0') {
                continue; // Strip unprintable character
            }

            if (norm == ' ') {
                if (!prevWasSpace) {
                    sb.append(' ');
                    prevWasSpace = true;
                }
            } else {
                sb.append(norm);
                prevWasSpace = false;
            }
        }

        // Trim trailing space if any
        int outLen = sb.length();
        if (outLen > 0 && sb.charAt(outLen - 1) == ' ') {
            sb.setLength(outLen - 1);
        }

        return sb.toString();
    }

    private static char normalizeChar(char c) {
        // Smart double quotes
        if (c == '\u201C' || c == '\u201D' || c == '\u201E' || c == '\u201F' || c == '\u00AB' || c == '\u00BB') {
            return '"';
        }
        // Smart single quotes / apostrophes
        if (c == '\u2018' || c == '\u2019' || c == '\u201A' || c == '\u201B' || c == '\u0060' || c == '\u00B4') {
            return '\'';
        }
        // Dashes / hyphens
        if (c == '\u2014' || c == '\u2013' || c == '\u2015' || c == '\u2212' || c == '\u2012') {
            return '-';
        }
        // Bullets
        if (c == '\u2022' || c == '\u00B7' || c == '\u25E6' || c == '\u25AA' || c == '\u25AB') {
            return '-';
        }
        // Whitespace, tabs, newlines, and non-breaking spaces
        if (c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '\f' ||
            c == '\u00A0' || c == '\u2007' || c == '\u202F' || c == '\u200B' || c == '\u3000' || c == '\uFEFF') {
            return ' ';
        }
        // Strip unprintable control characters
        if ((c < 32 && c != ' ') || c == 127) {
            return '\0';
        }
        return c;
    }

    /**
     * Fast O(N) word counter that allocates zero temporary objects.
     */
    public static int countWords(CharSequence text) {
        if (text == null) {
            return 0;
        }
        int len = text.length();
        int count = 0;
        boolean inWord = false;
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c) || c == ' ') {
                inWord = false;
            } else if (!inWord) {
                inWord = true;
                count++;
            }
        }
        return count;
    }

    /**
     * Fast word truncation that scans to the specified word count boundary
     * without creating string arrays or allocating excess memory.
     */
    public static String truncateToWordCount(String text, int maxWords) {
        if (text == null || maxWords <= 0) {
            return "";
        }
        int len = text.length();
        int count = 0;
        boolean inWord = false;
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c) || c == ' ') {
                if (inWord) {
                    count++;
                    if (count >= maxWords) {
                        return text.substring(0, i).trim();
                    }
                    inWord = false;
                }
            } else {
                inWord = true;
            }
        }
        return text.trim();
    }
}
