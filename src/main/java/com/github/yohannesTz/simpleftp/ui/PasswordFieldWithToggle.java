package com.github.yohannesTz.simpleftp.ui;

import com.formdev.flatlaf.extras.FlatSVGIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;

/**
 * A password field with a visibility toggle button
 */
public class PasswordFieldWithToggle extends JPanel {
    private final JPasswordField passwordField;
    private final JButton toggleButton;
    private boolean passwordVisible = false;
    
    public PasswordFieldWithToggle(int columns) {
        setLayout(new BorderLayout(2, 0));
        setOpaque(false);
        
        // Create password field
        passwordField = new JPasswordField(columns);
        add(passwordField, BorderLayout.CENTER);
        
        // Create toggle button with SVG icon
        toggleButton = new JButton();
        toggleButton.setFocusable(false);
        toggleButton.setBorderPainted(false);
        toggleButton.setContentAreaFilled(false);
        toggleButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Set preferred size for the button
        Dimension buttonSize = new Dimension(32, 28);
        toggleButton.setPreferredSize(buttonSize);
        toggleButton.setMinimumSize(buttonSize);
        toggleButton.setMaximumSize(buttonSize);
        
        // Load SVG icons
        updateIcon();
        
        // Add action listener
        toggleButton.addActionListener(e -> togglePasswordVisibility());
        
        add(toggleButton, BorderLayout.EAST);
    }
    
    private void updateIcon() {
        // Try to load SVG icon first
        Icon icon = loadSVGIcon();
        
        if (icon != null) {
            toggleButton.setIcon(icon);
            toggleButton.setText("");
        } else {
            // Fallback to programmatically drawn icon if SVG fails
            icon = createFallbackIcon();
            toggleButton.setIcon(icon);
            toggleButton.setText("");
        }
        
        toggleButton.setToolTipText(passwordVisible ? "Hide password" : "Show password");
    }
    
    private Icon createFallbackIcon() {
        int size = 18;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // Enable antialiasing for smooth drawing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Get foreground color
        Color color = UIManager.getColor("Button.foreground");
        if (color == null) color = Color.DARK_GRAY;
        g2d.setColor(color);
        
        if (passwordVisible) {
            // Draw an eye (visible)
            g2d.setStroke(new BasicStroke(1.5f));
            // Eye outline (ellipse)
            g2d.drawArc(2, 5, 14, 8, 0, 180);  // Upper arc
            g2d.drawArc(2, 5, 14, 8, 180, 180); // Lower arc
            // Iris (circle)
            g2d.fillOval(7, 7, 4, 4);
        } else {
            // Draw an eye with a slash through it (hidden)
            g2d.setStroke(new BasicStroke(1.5f));
            // Eye outline
            g2d.drawArc(2, 6, 14, 6, 0, 180);
            g2d.drawArc(2, 6, 14, 6, 180, 180);
            // Iris
            g2d.fillOval(7, 8, 4, 2);
            // Slash through it
            g2d.setStroke(new BasicStroke(2.0f));
            g2d.drawLine(2, 16, 16, 2);
        }
        
        g2d.dispose();
        return new ImageIcon(image);
    }
    
    private Icon loadSVGIcon() {
        try {
            String iconPath = passwordVisible ? "/visiblity-48.svg" : "/visiblity-off-48.svg";
            java.net.URL iconURL = getClass().getResource(iconPath);
            
            if (iconURL == null) {
                System.err.println("Icon resource not found: " + iconPath);
                return null;
            }
            
            FlatSVGIcon icon = new FlatSVGIcon(iconURL);
            
            // Get the current foreground color for the button
            Color foregroundColor = UIManager.getColor("Button.foreground");
            if (foregroundColor == null) {
                foregroundColor = UIManager.getColor("Label.foreground");
            }
            if (foregroundColor == null) {
                foregroundColor = Color.DARK_GRAY;
            }
            
            final Color finalColor = foregroundColor;
            icon.setColorFilter(new FlatSVGIcon.ColorFilter(color -> finalColor));
            
            // Scale the icon to 18x18 pixels
            icon = icon.derive(18, 18);
            
            return icon;
        } catch (Exception e) {
            System.err.println("Failed to load SVG icon: " + e.getMessage());
            return null;
        }
    }
    
    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            passwordField.setEchoChar((char) 0); // Show password
        } else {
            passwordField.setEchoChar('•'); // Hide password
        }
        updateIcon();
    }
    
    // Delegate methods to password field
    public char[] getPassword() {
        return passwordField.getPassword();
    }
    
    public void setText(String text) {
        passwordField.setText(text);
    }
    
    public JPasswordField getPasswordField() {
        return passwordField;
    }
    
    public void setEditable(boolean editable) {
        passwordField.setEditable(editable);
    }
    
    public void setEnabled(boolean enabled) {
        passwordField.setEnabled(enabled);
        toggleButton.setEnabled(enabled);
    }
}

