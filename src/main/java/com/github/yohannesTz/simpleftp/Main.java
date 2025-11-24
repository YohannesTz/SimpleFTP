package com.github.yohannesTz.simpleftp;

import com.formdev.flatlaf.FlatLightLaf;
import com.github.yohannesTz.simpleftp.ui.MainFrame;

import javax.swing.*;

/**
 * Main entry point for the Simple FTP Server application
 */
public class Main {
    public static void main(String[] args) {
        // Set FlatLaf look and feel for a modern UI
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            System.err.println("Failed to set FlatLaf look and feel: " + e.getMessage());
            e.printStackTrace();
        }

        // Launch the application on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}

