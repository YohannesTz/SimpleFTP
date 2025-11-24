package com.github.yohannesTz.simpleftp.ui;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.Color;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Captures console output and redirects it to the GUI log panel with color coding
 */
public class LogCaptureAppender {
    private final JTextPane logPane;
    private final StyledDocument document;
    private final PrintStream originalOut;
    private final PrintStream originalErr;
    
    // Color styles for different log levels
    private Style infoStyle;
    private Style warnStyle;
    private Style errorStyle;
    private Style debugStyle;
    private Style defaultStyle;
    
    public LogCaptureAppender(JTextPane logPane) {
        this.logPane = logPane;
        this.document = logPane.getStyledDocument();
        this.originalOut = System.out;
        this.originalErr = System.err;
        
        initializeStyles();
    }
    
    private void initializeStyles() {
        // INFO style - Blue
        infoStyle = document.addStyle("INFO", null);
        StyleConstants.setForeground(infoStyle, new Color(33, 150, 243)); // Material Blue
        
        // WARN style - Orange
        warnStyle = document.addStyle("WARN", null);
        StyleConstants.setForeground(warnStyle, new Color(255, 152, 0)); // Material Orange
        
        // ERROR style - Red
        errorStyle = document.addStyle("ERROR", null);
        StyleConstants.setForeground(errorStyle, new Color(244, 67, 54)); // Material Red
        StyleConstants.setBold(errorStyle, true);
        
        // DEBUG style - Gray
        debugStyle = document.addStyle("DEBUG", null);
        StyleConstants.setForeground(debugStyle, new Color(117, 117, 117)); // Gray
        
        // Default style
        defaultStyle = document.addStyle("DEFAULT", null);
        StyleConstants.setForeground(defaultStyle, UIManager.getColor("TextPane.foreground"));
    }
    
    /**
     * Start capturing console output and redirecting to GUI
     */
    public void startCapture() {
        // Redirect System.out
        System.setOut(new PrintStream(new LogOutputStream(false), true));
        
        // Redirect System.err
        System.setErr(new PrintStream(new LogOutputStream(true), true));
    }
    
    /**
     * Stop capturing and restore original streams
     */
    public void stopCapture() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }
    
    /**
     * Append a log message with appropriate color coding
     */
    private void appendLog(String message, boolean isError) {
        SwingUtilities.invokeLater(() -> {
            try {
                Style style = getStyleForMessage(message);
                document.insertString(document.getLength(), message, style);
                
                // Auto-scroll to bottom
                logPane.setCaretPosition(document.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace(originalErr);
            }
        });
    }
    
    /**
     * Determine the appropriate style based on the log message content
     */
    private Style getStyleForMessage(String message) {
        if (message.contains(" INFO ")) {
            return infoStyle;
        } else if (message.contains(" WARN ")) {
            return warnStyle;
        } else if (message.contains(" ERROR ")) {
            return errorStyle;
        } else if (message.contains(" DEBUG ")) {
            return debugStyle;
        } else {
            return defaultStyle;
        }
    }
    
    /**
     * Custom OutputStream that redirects to the log panel
     */
    private class LogOutputStream extends OutputStream {
        private final StringBuilder buffer = new StringBuilder();
        private final boolean isError;
        
        public LogOutputStream(boolean isError) {
            this.isError = isError;
        }
        
        @Override
        public void write(int b) {
            char c = (char) b;
            
            // Also write to original stream for debugging
            if (isError) {
                originalErr.write(b);
            } else {
                originalOut.write(b);
            }
            
            if (c == '\n') {
                String line = buffer.toString();
                if (!line.trim().isEmpty()) {
                    appendLog(line + "\n", isError);
                }
                buffer.setLength(0);
            } else {
                buffer.append(c);
            }
        }
        
        @Override
        public void flush() {
            if (buffer.length() > 0) {
                String line = buffer.toString();
                appendLog(line, isError);
                buffer.setLength(0);
            }
        }
    }
}

