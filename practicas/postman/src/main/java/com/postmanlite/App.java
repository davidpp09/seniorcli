package com.postmanlite;

import com.postmanlite.ui.MainFrame;

import javax.swing.SwingUtilities;

public final class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
