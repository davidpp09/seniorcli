package com.postmanlite;

import com.postmanlite.ui.MainFrame;

import javax.swing.SwingUtilities;

public final class App {
    public static void main(String[] args) {
        // Deben fijarse antes de cualquier uso de red.
        System.setProperty("java.net.useSystemProxies", "true");
        System.setProperty("java.net.preferIPv4Stack", "true");
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
