package com.speedtyping.service;

import com.speedtyping.model.TypingResult;

public class TypingEngine {

    public static final int DURATION_UNTIL_FINISHED = 0;

    public interface TypingEventListener {
        void onTick(int secondsRemainingOrElapsed, int currentWpm, int rawWpm, double accuracy, int errors);
        void onFinished(TypingResult result);
    }

    private String targetText = "";
    private int totalDurationSeconds = 60;
    private int secondsRemaining = 60;
    private int elapsedSeconds = 0;
    private boolean isRunning = false;
    private boolean isFinished = false;
    private long startTimeMillis = 0;

    // Keystroke metrics across the entire test session (supports virtualized segments)
    private int accumulatedCorrect = 0;
    private int accumulatedIncorrect = 0;
    private int accumulatedTotalTyped = 0;

    private int correctChars = 0;
    private int incorrectChars = 0;
    private int totalTyped = 0;

    private TypingEventListener listener;
    private javax.swing.Timer swingTimer;

    public TypingEngine() {}

    public void setListener(TypingEventListener listener) {
        this.listener = listener;
    }

    public boolean isUntilFinishedMode() {
        return totalDurationSeconds <= 0;
    }

    public void init(String text, int durationSeconds) {
        stopTimer();
        this.targetText = (text == null) ? "" : text;
        this.totalDurationSeconds = durationSeconds;
        this.secondsRemaining = durationSeconds;
        this.elapsedSeconds = 0;
        this.isRunning = false;
        this.isFinished = false;
        this.startTimeMillis = 0;

        this.accumulatedCorrect = 0;
        this.accumulatedIncorrect = 0;
        this.accumulatedTotalTyped = 0;

        this.correctChars = 0;
        this.incorrectChars = 0;
        this.totalTyped = 0;
    }

    public void start() {
        if (isRunning || isFinished) return;
        isRunning = true;
        startTimeMillis = System.currentTimeMillis();

        if (swingTimer != null) {
            swingTimer.stop();
        }

        swingTimer = new javax.swing.Timer(1000, e -> {
            elapsedSeconds++;
            if (isUntilFinishedMode()) {
                triggerTick();
            } else {
                secondsRemaining--;
                triggerTick();
                if (secondsRemaining <= 0) {
                    finish();
                }
            }
        });
        swingTimer.start();
    }

    public void triggerTick() {
        if (listener != null) {
            int displayTime = isUntilFinishedMode() ? elapsedSeconds : secondsRemaining;
            listener.onTick(
                    displayTime,
                    calculateNetWpm(),
                    calculateGrossWpm(),
                    calculateAccuracy(),
                    incorrectChars
            );
        }
    }

    /**
     * Commits the completed keystrokes of a segment into session totals
     * when seamlessly transitioning between streaming text segments.
     */
    public void commitSegment(int segmentCorrect, int segmentIncorrect, int segmentTyped) {
        this.accumulatedCorrect += segmentCorrect;
        this.accumulatedIncorrect += segmentIncorrect;
        this.accumulatedTotalTyped += segmentTyped;

        this.correctChars = this.accumulatedCorrect;
        this.incorrectChars = this.accumulatedIncorrect;
        this.totalTyped = this.accumulatedTotalTyped;

        triggerTick();
    }

    /**
     * Updates active keystrokes combining committed segments + current segment live input.
     */
    public void updateKeystrokes(int currentSegmentCorrect, int currentSegmentIncorrect, int currentSegmentTyped) {
        this.correctChars = this.accumulatedCorrect + currentSegmentCorrect;
        this.incorrectChars = this.accumulatedIncorrect + currentSegmentIncorrect;
        this.totalTyped = this.accumulatedTotalTyped + currentSegmentTyped;

        if (!isRunning && !isFinished && totalTyped > 0) {
            start();
        }

        triggerTick();
    }

    public int getEffectiveElapsedSeconds() {
        if (isUntilFinishedMode()) {
            if (elapsedSeconds > 0) return elapsedSeconds;
            if (startTimeMillis > 0) {
                return (int) Math.max(1, (System.currentTimeMillis() - startTimeMillis) / 1000);
            }
            return 1;
        } else {
            int elapsed = totalDurationSeconds - Math.max(0, secondsRemaining);
            return Math.max(1, elapsed);
        }
    }

    public void finish() {
        if (isFinished) return;
        isFinished = true;
        isRunning = false;
        stopTimer();

        int elapsed = getEffectiveElapsedSeconds();

        int finalNetWpm = calculateNetWpm(elapsed);
        int finalRawWpm = calculateGrossWpm(elapsed);
        double finalAccuracy = calculateAccuracy();

        TypingResult result = new TypingResult();
        result.setWpm(finalNetWpm);
        result.setRawWpm(finalRawWpm);
        result.setAccuracy(finalAccuracy);
        result.setCorrectChars(correctChars);
        result.setIncorrectChars(incorrectChars);
        result.setTotalChars(totalTyped);
        result.setDurationSeconds(elapsed);

        if (listener != null) {
            listener.onFinished(result);
        }
    }

    public void reset() {
        stopTimer();
        init(this.targetText, this.totalDurationSeconds);
        triggerTick();
    }

    private void stopTimer() {
        if (swingTimer != null) {
            swingTimer.stop();
            swingTimer = null;
        }
    }

    public int calculateGrossWpm() {
        return calculateGrossWpm(getEffectiveElapsedSeconds());
    }

    public int calculateGrossWpm(int elapsedSeconds) {
        if (elapsedSeconds <= 0) return 0;
        double minutes = elapsedSeconds / 60.0;
        return (int) Math.round((totalTyped / 5.0) / minutes);
    }

    public int calculateNetWpm() {
        return calculateNetWpm(getEffectiveElapsedSeconds());
    }

    public int calculateNetWpm(int elapsedSeconds) {
        if (elapsedSeconds <= 0) return 0;
        double minutes = elapsedSeconds / 60.0;
        int net = (int) Math.round((correctChars / 5.0) / minutes);
        return Math.max(0, net);
    }

    public double calculateAccuracy() {
        if (totalTyped <= 0) return 100.0;
        double acc = (correctChars * 100.0) / totalTyped;
        return Math.min(100.0, Math.max(0.0, Math.round(acc * 10.0) / 10.0));
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public int getSecondsRemaining() {
        return secondsRemaining;
    }

    public int getElapsedSeconds() {
        return elapsedSeconds;
    }

    public String getTargetText() {
        return targetText;
    }

    public int getTotalDurationSeconds() {
        return totalDurationSeconds;
    }
}
