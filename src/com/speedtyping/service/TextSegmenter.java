package com.speedtyping.service;

import java.util.ArrayList;
import java.util.List;

/**
 * High-performance virtualized text segmenter designed to handle
 * up to millions of words with zero lag and minimal memory footprint.
 * Breaks text into comfortable typing segments (~50 words / ~300 chars)
 * at clean word boundaries.
 */
public class TextSegmenter {

    public static final int DEFAULT_TARGET_WORDS_PER_SEGMENT = 50;

    private final String fullText;
    private final int[] segmentOffsets; // Starting character index of each segment
    private final int totalWords;
    private final int segmentCount;

    public static String normalizeText(String raw) {
        if (raw == null) return "";
        return raw.replaceAll("[\\r\\n\\t\\f]+", " ").replaceAll(" +", " ").trim();
    }

    public TextSegmenter(String fullText) {
        this(fullText, DEFAULT_TARGET_WORDS_PER_SEGMENT);
    }

    public TextSegmenter(String fullText, int targetWordsPerSegment) {
        this.fullText = normalizeText(fullText);
        int textLen = this.fullText.length();

        if (textLen == 0) {
            this.segmentOffsets = new int[]{0, 0};
            this.segmentCount = 1;
            this.totalWords = 0;
            return;
        }

        // Single-pass scan to find word boundaries and calculate segment offsets
        List<Integer> offsetList = new ArrayList<>(Math.max(16, textLen / 250));
        offsetList.add(0);

        int wordsInCurrentSegment = 0;
        int totalWordCount = 0;
        boolean inWord = false;

        for (int i = 0; i < textLen; i++) {
            char c = this.fullText.charAt(i);
            boolean isSpace = Character.isWhitespace(c) || c == ' ';

            if (isSpace) {
                if (inWord) {
                    inWord = false;
                    wordsInCurrentSegment++;
                    totalWordCount++;

                    // If target words reached, create segment boundary after this space
                    if (wordsInCurrentSegment >= targetWordsPerSegment && i + 1 < textLen) {
                        // Skip any consecutive spaces to find next word start
                        int nextWordStart = i + 1;
                        while (nextWordStart < textLen && (Character.isWhitespace(this.fullText.charAt(nextWordStart)) || this.fullText.charAt(nextWordStart) == ' ')) {
                            nextWordStart++;
                        }
                        if (nextWordStart < textLen) {
                            offsetList.add(nextWordStart);
                            wordsInCurrentSegment = 0;
                            i = nextWordStart - 1; // loop will increment to nextWordStart
                        }
                    }
                }
            } else {
                inWord = true;
            }
        }

        if (inWord) {
            totalWordCount++;
        }

        // Add sentinel for end of text
        offsetList.add(textLen);

        this.totalWords = totalWordCount;
        this.segmentCount = offsetList.size() - 1;
        this.segmentOffsets = new int[offsetList.size()];
        for (int i = 0; i < offsetList.size(); i++) {
            this.segmentOffsets[i] = offsetList.get(i);
        }
    }

    public int getSegmentCount() {
        return Math.max(1, segmentCount);
    }

    public int getTotalWords() {
        return totalWords;
    }

    public int getTotalChars() {
        return fullText.length();
    }

    public String getSegmentText(int index) {
        if (segmentCount <= 0 || fullText.isEmpty()) {
            return "";
        }
        int clampedIdx = Math.max(0, Math.min(index, segmentCount - 1));
        int start = segmentOffsets[clampedIdx];
        int end = segmentOffsets[clampedIdx + 1];
        return fullText.substring(start, end).trim();
    }

    public int getSegmentStartCharOffset(int index) {
        int clampedIdx = Math.max(0, Math.min(index, segmentCount - 1));
        return segmentOffsets[clampedIdx];
    }

    public int getSegmentEndCharOffset(int index) {
        int clampedIdx = Math.max(0, Math.min(index, segmentCount - 1));
        return segmentOffsets[clampedIdx + 1];
    }

    public String getFullText() {
        return fullText;
    }
}
