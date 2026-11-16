package com.speedtyping.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Intelligent word-synchronized typing evaluation engine.
 * Prevents cascading off-by-one errors by synchronizing input at the word level,
 * isolating mistyped characters to their respective words and handling segment
 * transitions with zero keystroke drift.
 */
public class TypingEvaluator {

    public static final int STATUS_NORMAL = 0;
    public static final int STATUS_CORRECT = 1;
    public static final int STATUS_INCORRECT = 2;
    public static final int STATUS_CURRENT = 3;

    public static class WordSpan {
        public final String text;
        public final int start;
        public final int end;

        public WordSpan(String text, int start, int end) {
            this.text = text;
            this.start = start;
            this.end = end;
        }
    }

    public static class MatchResult {
        public int correctChars;
        public int incorrectChars;
        public int currentTargetCharIndex;
        public int[] targetCharStatus;
        public int completedWords;
        public boolean isSegmentFinished;
    }

    public static List<WordSpan> extractWords(String text) {
        List<WordSpan> list = new ArrayList<>();
        if (text == null) return list;
        int len = text.length();
        int wordStart = -1;
        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c)) {
                if (wordStart != -1) {
                    list.add(new WordSpan(text.substring(wordStart, i), wordStart, i));
                    wordStart = -1;
                }
            } else {
                if (wordStart == -1) {
                    wordStart = i;
                }
            }
        }
        if (wordStart != -1) {
            list.add(new WordSpan(text.substring(wordStart, len), wordStart, len));
        }
        return list;
    }

    public static MatchResult evaluate(String target, String typed, boolean isLastSegment) {
        MatchResult res = new MatchResult();
        if (target == null || target.isEmpty()) {
            res.targetCharStatus = new int[0];
            res.isSegmentFinished = true;
            return res;
        }

        List<WordSpan> targetWords = extractWords(target);
        int targetLen = target.length();
        res.targetCharStatus = new int[targetLen];

        if (targetWords.isEmpty()) {
            res.isSegmentFinished = true;
            return res;
        }

        if (typed == null) {
            typed = "";
        }

        int typedLen = typed.length();
        int tIdx = 0;
        int wordIdx = 0;

        int totalCorrect = 0;
        int totalIncorrect = 0;
        int nextTargetCursor = 0;

        while (tIdx < typedLen && wordIdx < targetWords.size()) {
            // Skip leading spaces before a typed word (absorbs transition spacebar hits)
            while (tIdx < typedLen && typed.charAt(tIdx) == ' ') {
                tIdx++;
            }
            if (tIdx >= typedLen) {
                break;
            }

            int typedWordStart = tIdx;
            while (tIdx < typedLen && typed.charAt(tIdx) != ' ') {
                tIdx++;
            }
            String typedWord = typed.substring(typedWordStart, tIdx);
            boolean hasTrailingSpace = (tIdx < typedLen && typed.charAt(tIdx) == ' ');
            if (hasTrailingSpace) {
                tIdx++; // consume the delimiter space
            }

            WordSpan targetWord = targetWords.get(wordIdx);
            int twLen = targetWord.text.length();
            int curTwLen = typedWord.length();

            boolean isLastWordInSegment = (wordIdx == targetWords.size() - 1);
            boolean wordCompleted = hasTrailingSpace
                    || (isLastSegment && isLastWordInSegment && curTwLen >= twLen)
                    || (isLastWordInSegment && curTwLen > twLen);

            if (wordCompleted) {
                // Word is completed
                int minL = Math.min(twLen, curTwLen);
                for (int c = 0; c < minL; c++) {
                    if (typedWord.charAt(c) == targetWord.text.charAt(c)) {
                        res.targetCharStatus[targetWord.start + c] = STATUS_CORRECT;
                        totalCorrect++;
                    } else {
                        res.targetCharStatus[targetWord.start + c] = STATUS_INCORRECT;
                        totalIncorrect++;
                    }
                }
                // Extra chars typed beyond target word
                if (curTwLen > twLen) {
                    totalIncorrect += (curTwLen - twLen);
                } else if (curTwLen < twLen) {
                    // Untyped chars in target word marked as incorrect
                    for (int c = curTwLen; c < twLen; c++) {
                        res.targetCharStatus[targetWord.start + c] = STATUS_INCORRECT;
                        totalIncorrect++;
                    }
                }

                // Delimiter after word in target
                int spacePos = targetWord.end;
                if (spacePos < targetLen && Character.isWhitespace(target.charAt(spacePos))) {
                    if (hasTrailingSpace) {
                        res.targetCharStatus[spacePos] = STATUS_CORRECT;
                        totalCorrect++;
                    } else {
                        res.targetCharStatus[spacePos] = STATUS_NORMAL;
                    }
                }

                wordIdx++;
                res.completedWords = wordIdx;
                if (wordIdx < targetWords.size()) {
                    nextTargetCursor = targetWords.get(wordIdx).start;
                } else {
                    nextTargetCursor = targetLen;
                }
            } else {
                // Currently typing this word
                int minL = Math.min(twLen, curTwLen);
                for (int c = 0; c < minL; c++) {
                    if (typedWord.charAt(c) == targetWord.text.charAt(c)) {
                        res.targetCharStatus[targetWord.start + c] = STATUS_CORRECT;
                        totalCorrect++;
                    } else {
                        res.targetCharStatus[targetWord.start + c] = STATUS_INCORRECT;
                        totalIncorrect++;
                    }
                }
                if (curTwLen > twLen) {
                    totalIncorrect += (curTwLen - twLen);
                    nextTargetCursor = targetWord.end;
                } else {
                    nextTargetCursor = targetWord.start + curTwLen;
                }
                break;
            }
        }

        // Set cursor position
        res.currentTargetCharIndex = Math.min(targetLen, Math.max(0, nextTargetCursor));

        // Highlight cursor position if within bounds and not yet typed
        if (res.currentTargetCharIndex < targetLen && res.targetCharStatus[res.currentTargetCharIndex] == STATUS_NORMAL) {
            res.targetCharStatus[res.currentTargetCharIndex] = STATUS_CURRENT;
        }

        // Check if segment is finished
        if (wordIdx >= targetWords.size()) {
            res.isSegmentFinished = true;
        }

        res.correctChars = totalCorrect;
        res.incorrectChars = totalIncorrect;
        return res;
    }
}
