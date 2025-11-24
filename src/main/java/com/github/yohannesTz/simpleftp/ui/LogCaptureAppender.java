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
    private Style receivedStyle;
    private Style sentStyle;
    private Style connectionStyle;
    
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
        StyleConstants.setBold(warnStyle, true);
        
        // ERROR style - Red
        errorStyle = document.addStyle("ERROR", null);
        StyleConstants.setForeground(errorStyle, new Color(244, 67, 54)); // Material Red
        StyleConstants.setBold(errorStyle, true);
        
        // DEBUG style - Gray
        debugStyle = document.addStyle("DEBUG", null);
        StyleConstants.setForeground(debugStyle, new Color(117, 117, 117)); // Gray
        
        // RECEIVED style - Cyan (for FTP commands received from client)
        receivedStyle = document.addStyle("RECEIVED", null);
        StyleConstants.setForeground(receivedStyle, new Color(0, 188, 212)); // Material Cyan
        
        // SENT style - Purple (for FTP responses sent to client)
        sentStyle = document.addStyle("SENT", null);
        StyleConstants.setForeground(sentStyle, new Color(156, 39, 176)); // Material Purple
        
        // CONNECTION style - Green (for connection events: CREATED, OPENED, CLOSED)
        connectionStyle = document.addStyle("CONNECTION", null);
        StyleConstants.setForeground(connectionStyle, new Color(76, 175, 80)); // Material Green
        StyleConstants.setBold(connectionStyle, true);
        
        // Default style
        defaultStyle = document.addStyle("DEFAULT", null);
        Color foreground = UIManager.getColor("TextPane.foreground");
        if (foreground == null) {
            foreground = Color.BLACK;
        }
        StyleConstants.setForeground(defaultStyle, foreground);
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
        // Skip empty messages
        if (message == null || message.trim().isEmpty()) {
            return;
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                // Always determine style based on message content, not which stream it came from
                // (slf4j-simple logs INFO to stderr by default, but we want to color by log level)
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
        String upperMessage = message.toUpperCase();
        
        // Check for connection events (highest priority for visibility)
        if (upperMessage.contains(" CREATED") || upperMessage.contains(" OPENED") || 
            upperMessage.contains(" CLOSED") || upperMessage.contains("LOGIN SUCCESS") ||
            upperMessage.contains("USER LOGGED IN")) {
            return connectionStyle;
        }
        
        // Check for FTP commands (second priority)
        if (message.contains("RECEIVED:")) {
            return receivedStyle;
        } else if (message.contains("SENT:")) {
            return sentStyle;
        }
        
        // Check for log levels in various formats (third priority)
        if (upperMessage.contains(" WARN ") || upperMessage.contains("]WARN ") || 
            upperMessage.contains("] WARN ") || upperMessage.contains("WARNING")) {
            return warnStyle;
        } else if (upperMessage.contains(" ERROR ") || upperMessage.contains("]ERROR ") || 
                   upperMessage.contains("] ERROR ")) {
            return errorStyle;
        } else if (upperMessage.contains(" DEBUG ") || upperMessage.contains("]DEBUG ") || 
                   upperMessage.contains("] DEBUG ")) {
            return debugStyle;
        } else if (upperMessage.contains(" INFO ") || upperMessage.contains("]INFO ") || 
                   upperMessage.contains("] INFO ")) {
            return infoStyle;
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
            try {
                if (isError) {
                    originalErr.write(b);
                } else {
                    originalOut.write(b);
                }
            } catch (Exception e) {
                // Ignore errors writing to original streams
            }
            
            if (c == '\n') {
                String line = buffer.toString();
                if (!line.trim().isEmpty()) {
                    appendLog(line + "\n", isError);
                }
                buffer.setLength(0);
            } else if (c != '\r') { // Ignore carriage returns
                buffer.append(c);
            }
        }
        
        @Override
        public void flush() {
            try {
                if (isError) {
                    originalErr.flush();
                } else {
                    originalOut.flush();
                }
            } catch (Exception e) {
                // Ignore flush errors
            }
            
            if (buffer.length() > 0) {
                String line = buffer.toString();
                if (!line.trim().isEmpty()) {
                    appendLog(line + "\n", isError);
                }
                buffer.setLength(0);
            }
        }
    }
}

