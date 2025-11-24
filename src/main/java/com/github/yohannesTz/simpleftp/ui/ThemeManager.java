package com.github.yohannesTz.simpleftp.ui;

import com.formdev.flatlaf.*;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manages FlatLaf themes
 */
public class ThemeManager {
    
    private static final Map<String, String> THEMES = new LinkedHashMap<>();
    
    static {
        THEMES.put("Flat Light", FlatLightLaf.class.getName());
        THEMES.put("Flat Dark", FlatDarkLaf.class.getName());
        THEMES.put("Flat IntelliJ", FlatIntelliJLaf.class.getName());
        THEMES.put("Flat Darcula", FlatDarculaLaf.class.getName());
    }
    
    public static String[] getThemeNames() {
        return THEMES.keySet().toArray(new String[0]);
    }
    
    public static void applyTheme(String themeName) {
        String themeClassName = THEMES.get(themeName);
        if (themeClassName == null) {
            return;
        }
        
        try {
            UIManager.setLookAndFeel(themeClassName);
            // Update all existing windows
            for (Window window : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(window);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                "Failed to apply theme: " + e.getMessage(), 
                "Theme Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static String getCurrentTheme() {
        String currentLaf = UIManager.getLookAndFeel().getClass().getName();
        for (Map.Entry<String, String> entry : THEMES.entrySet()) {
            if (entry.getValue().equals(currentLaf)) {
                return entry.getKey();
            }
        }
        return "Flat Light";
    }
}

