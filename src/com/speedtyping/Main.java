package com.speedtyping;

import com.speedtyping.ui.MainFrame;
import com.speedtyping.ui.UIUtils;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UIUtils.setupModernLookAndFeel();
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
