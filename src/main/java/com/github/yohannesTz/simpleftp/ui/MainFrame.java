package com.github.yohannesTz.simpleftp.ui;

import com.formdev.flatlaf.FlatLightLaf;
import com.github.yohannesTz.simpleftp.config.ConfigManager;
import com.github.yohannesTz.simpleftp.model.ServerConfig;
import com.github.yohannesTz.simpleftp.model.UserAccount;
import com.github.yohannesTz.simpleftp.server.FTPServerManager;
import org.apache.ftpserver.ftplet.FtpException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Main application window for the FTP Server
 */
public class MainFrame extends JFrame {
    private ServerConfig config;
    private FTPServerManager serverManager;
    
    // UI Components
    private JTextField portField;
    private JTextField addressField;
    private JTextField maxLoginsField;
    private JTextField baseFolderField;
    private JCheckBox anonymousCheckBox;
    private JButton startButton;
    private JButton stopButton;
    private JButton configureUsersButton;
    private JTextPane logPane;
    private StyledDocument logDocument;
    private LogCaptureAppender logCaptureAppender;
    private JLabel statusLabel;
    private DefaultListModel<UserAccount> userListModel;
    private JList<UserAccount> userList;
    private JTextField connectionCommandField;
    private JComboBox<String> themeSelector;
    
    // Styles for log messages
    private Style infoStyle;
    private Style errorStyle;
    private Style successStyle;
    private Style defaultStyle;

    public MainFrame() {
        // Load saved configuration
        ConfigManager.ConfigData configData = ConfigManager.loadConfig();
        config = configData.serverConfig;
        serverManager = new FTPServerManager(config);
        
        initUI();
        
        // Apply saved theme
        if (configData.theme != null && !configData.theme.equals("Flat Light")) {
            ThemeManager.applyTheme(configData.theme);
            if (themeSelector != null) {
                themeSelector.setSelectedItem(configData.theme);
            }
        }
        
        setupListeners();
        updateUIState(false);
        
        // Initialize log styles
        initializeLogStyles();
        
        // Start capturing console output
        logCaptureAppender = new LogCaptureAppender(logPane);
        logCaptureAppender.startCapture();
        
        // Log configuration load
        if (ConfigManager.configExists()) {
            logMessage("Configuration loaded from " + System.getProperty("user.home") + "/.simpleftp", "info");
        } else {
            logMessage("Using default configuration", "info");
        }
    }

    private void initUI() {
        setTitle("Simple FTP Server Manager");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(900, 750);
        setLocationRelativeTo(null);
        
        // Set application icon
        try {
            java.net.URL iconURL = getClass().getResource("/ftp-server-48.png");
            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                setIconImage(icon.getImage());
            }
        } catch (Exception e) {
            // Icon loading failed, continue without icon
            System.err.println("Failed to load application icon: " + e.getMessage());
        }

        // Main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Top section with theme selector and config
        JPanel topSection = new JPanel(new BorderLayout(5, 5));
        topSection.add(createThemePanel(), BorderLayout.NORTH);
        topSection.add(createConfigPanel(), BorderLayout.CENTER);
        topSection.add(createConnectionCommandPanel(), BorderLayout.SOUTH);
        mainPanel.add(topSection, BorderLayout.NORTH);

        // Center panel with tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Users", createUsersPanel());
        tabbedPane.addTab("Server Log", createLogPanel());
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Bottom panel with status
        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
        
        // Add menu bar
        setJMenuBar(createMenuBar());
    }
    
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        
        JMenuItem saveConfigItem = new JMenuItem("Save Configuration");
        saveConfigItem.addActionListener(e -> {
            saveConfiguration();
            JOptionPane.showMessageDialog(this,
                "Configuration saved successfully!",
                "Saved",
                JOptionPane.INFORMATION_MESSAGE);
        });
        fileMenu.add(saveConfigItem);
        
        fileMenu.addSeparator();
        
        JMenuItem resetConfigItem = new JMenuItem("Reset to Defaults");
        resetConfigItem.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to reset all settings to defaults?\nThis will delete saved configuration.",
                "Confirm Reset",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                resetToDefaults();
            }
        });
        fileMenu.add(resetConfigItem);
        
        fileMenu.addSeparator();
        
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> {
            dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        });
        fileMenu.add(exitItem);
        
        menuBar.add(fileMenu);
        
        // Help menu
        JMenu helpMenu = new JMenu("Help");
        
        // GitHub menu item with icon
        JMenuItem githubItem = new JMenuItem("View on GitHub");
        try {
            java.net.URL iconURL = getClass().getResource("/github-48.png");
            if (iconURL != null) {
                ImageIcon originalIcon = new ImageIcon(iconURL);
                Image scaledImage = originalIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                githubItem.setIcon(new ImageIcon(scaledImage));
            }
        } catch (Exception e) {
            // Icon loading failed, continue without icon
        }
        githubItem.addActionListener(e -> openGitHub());
        helpMenu.add(githubItem);
        
        helpMenu.addSeparator();
        
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);
        
        JMenuItem configLocationItem = new JMenuItem("Configuration Location");
        configLocationItem.addActionListener(e -> {
            String configPath = System.getProperty("user.home") + "/.simpleftp";
            JOptionPane.showMessageDialog(this,
                "Configuration is stored at:\n" + configPath + "\n\n" +
                "Files:\n" +
                "- config.properties (server settings)\n" +
                "- users.dat (user accounts)",
                "Configuration Location",
                JOptionPane.INFORMATION_MESSAGE);
        });
        helpMenu.add(configLocationItem);
        
        menuBar.add(helpMenu);
        
        return menuBar;
    }
    
    private void resetToDefaults() {
        try {
            ConfigManager.deleteConfig();
            logMessage("Configuration reset to defaults", "success");
            JOptionPane.showMessageDialog(this,
                "Configuration has been reset to defaults.\nPlease restart the application.",
                "Reset Complete",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            logMessage("Failed to reset configuration: " + e.getMessage(), "error");
            JOptionPane.showMessageDialog(this,
                "Failed to reset configuration: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showAboutDialog() {
        // Create custom panel with GitHub link
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Add icon
        try {
            java.net.URL iconURL = getClass().getResource("/ftp-server-96.png");
            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                JLabel iconLabel = new JLabel(icon);
                iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
                panel.add(iconLabel, BorderLayout.NORTH);
            }
        } catch (Exception e) {
            // Icon loading failed, continue without icon
        }
        
        // Info text
        JTextArea textArea = new JTextArea();
        textArea.setText(
            "Simple FTP Server Manager\n" +
            "Version 1.0\n\n" +
            "A modern FTP server with user management,\n" +
            "configurable settings, and theme support.\n\n" +
            "Package: com.github.yohannesTz.simpleftp\n\n" +
            "GitHub: https://github.com/YohannesTz/SimpleFTP"
        );
        textArea.setEditable(false);
        textArea.setOpaque(false);
        textArea.setFont(UIManager.getFont("Label.font"));
        textArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(textArea, BorderLayout.CENTER);
        
        // GitHub button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton githubButton = new JButton("Open GitHub Repository");
        try {
            java.net.URL iconURL = getClass().getResource("/github-48.png");
            if (iconURL != null) {
                ImageIcon originalIcon = new ImageIcon(iconURL);
                Image scaledImage = originalIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                githubButton.setIcon(new ImageIcon(scaledImage));
            }
        } catch (Exception e) {
            // Icon loading failed, continue without icon
        }
        githubButton.addActionListener(e -> openGitHub());
        setButtonSize(githubButton);
        buttonPanel.add(githubButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        JOptionPane.showMessageDialog(this, panel, "About Simple FTP Server", 
            JOptionPane.PLAIN_MESSAGE);
    }
    
    /**
     * Opens the GitHub repository in the default browser
     */
    private void openGitHub() {
        try {
            String url = "https://github.com/YohannesTz/SimpleFTP";
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new java.net.URI(url));
                logMessage("Opened GitHub repository in browser", "success");
            } else {
                // Fallback: copy to clipboard
                StringSelection selection = new StringSelection(url);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
                JOptionPane.showMessageDialog(this,
                    "GitHub URL copied to clipboard:\n" + url,
                    "GitHub",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            logMessage("Failed to open GitHub: " + e.getMessage(), "error");
            JOptionPane.showMessageDialog(this,
                "Could not open browser. Visit:\nhttps://github.com/YohannesTz/SimpleFTP",
                "GitHub",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private JPanel createThemePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        panel.add(new JLabel("Theme:"));
        
        themeSelector = new JComboBox<>(ThemeManager.getThemeNames());
        themeSelector.setSelectedItem(ThemeManager.getCurrentTheme());
        themeSelector.addActionListener(e -> {
            String selectedTheme = (String) themeSelector.getSelectedItem();
            ThemeManager.applyTheme(selectedTheme);
            logMessage("Theme changed to: " + selectedTheme, "info");
            saveConfiguration();
        });
        setButtonSize(themeSelector);
        panel.add(themeSelector);
        
        return panel;
    }
    
    private JPanel createConnectionCommandPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBorder(new TitledBorder("FTP Connection Command"));
        
        connectionCommandField = new JTextField();
        connectionCommandField.setEditable(false);
        connectionCommandField.setFont(new Font("Monospaced", Font.PLAIN, 12));
        updateConnectionCommand();
        
        JButton copyButton = new JButton("Copy");
        copyButton.setToolTipText("Copy command to clipboard");
        copyButton.addActionListener(e -> copyConnectionCommand());
        setButtonSize(copyButton);
        
        panel.add(connectionCommandField, BorderLayout.CENTER);
        panel.add(copyButton, BorderLayout.EAST);
        
        return panel;
    }

    private JPanel createConfigPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new TitledBorder("Server Configuration"));

        // Settings panel
        JPanel settingsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Port
        gbc.gridx = 0;
        gbc.gridy = 0;
        settingsPanel.add(new JLabel("Port:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        portField = new JTextField(String.valueOf(config.getPort()), 10);
        settingsPanel.add(portField, gbc);

        // Server Address
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        settingsPanel.add(new JLabel("Address:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        addressField = new JTextField(config.getServerAddress(), 10);
        settingsPanel.add(addressField, gbc);

        // Max Logins
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        settingsPanel.add(new JLabel("Max Logins:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        maxLoginsField = new JTextField(String.valueOf(config.getMaxLogins()), 10);
        settingsPanel.add(maxLoginsField, gbc);

        // Base Folder
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        settingsPanel.add(new JLabel("Base Folder:"), gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        baseFolderField = new JTextField(config.getBaseFolder(), 10);
        settingsPanel.add(baseFolderField, gbc);
        
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JButton browseFolderButton = new JButton("Browse...");
        browseFolderButton.addActionListener(e -> browseBaseFolder());
        setButtonSize(browseFolderButton);
        settingsPanel.add(browseFolderButton, gbc);

        // Anonymous Login
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        anonymousCheckBox = new JCheckBox("Enable Anonymous Login");
        anonymousCheckBox.setSelected(config.isAnonymousEnabled());
        settingsPanel.add(anonymousCheckBox, gbc);

        panel.add(settingsPanel, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        // Create start button with icon
        startButton = new JButton("Start Server");
        try {
            java.net.URL iconURL = getClass().getResource("/ftp-server-48.png");
            if (iconURL != null) {
                ImageIcon originalIcon = new ImageIcon(iconURL);
                // Scale icon to 16x16 for button
                Image scaledImage = originalIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                startButton.setIcon(new ImageIcon(scaledImage));
            }
        } catch (Exception e) {
            // Fallback to default icon
            startButton.setIcon(UIManager.getIcon("FileView.hardDriveIcon"));
        }
        startButton.addActionListener(e -> startServer());
        setButtonSize(startButton);
        
        // Create stop button with scaled icon
        stopButton = new JButton("Stop Server");
        // Use a simple colored square for stop (16x16 to match start button icon)
        Icon stopIcon = new ImageIcon(createStopIcon());
        stopButton.setIcon(stopIcon);
        stopButton.addActionListener(e -> stopServer());
        stopButton.setEnabled(false);
        setButtonSize(stopButton);

        buttonsPanel.add(startButton);
        buttonsPanel.add(stopButton);
        
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        return panel;
    }
    
    /**
     * Creates a simple red square icon for the stop button (16x16)
     */
    private Image createStopIcon() {
        int size = 16;
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(
            size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw red square with rounded corners
        g2d.setColor(new Color(220, 53, 69));
        g2d.fillRoundRect(2, 2, 12, 12, 3, 3);
        
        g2d.dispose();
        return image;
    }

    private JPanel createUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // User list
        userListModel = new DefaultListModel<>();
        for (UserAccount user : config.getUsers()) {
            userListModel.addElement(user);
        }
        
        userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(userList);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton addButton = new JButton("Add User");
        addButton.addActionListener(e -> addUser());
        setButtonSize(addButton);
        
        JButton editButton = new JButton("Edit User");
        editButton.addActionListener(e -> editUser());
        setButtonSize(editButton);
        
        JButton deleteButton = new JButton("Delete User");
        deleteButton.addActionListener(e -> deleteUser());
        setButtonSize(deleteButton);

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        logPane = new JTextPane();
        logPane.setEditable(false);
        logPane.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logDocument = logPane.getStyledDocument();
        JScrollPane scrollPane = new JScrollPane(logPane);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton clearButton = new JButton("Clear Log");
        clearButton.addActionListener(e -> {
            try {
                logDocument.remove(0, logDocument.getLength());
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        });
        setButtonSize(clearButton);
        buttonPanel.add(clearButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        logMessage("FTP Server initialized", "info");

        return panel;
    }
    
    /**
     * Sets a consistent size for buttons to ensure equal height
     */
    private void setButtonSize(JButton button) {
        // Get the preferred size to ensure text fits
        Dimension prefSize = button.getPreferredSize();
        
        // Set a minimum width to accommodate the text with padding
        int minWidth = Math.max(prefSize.width + 20, 80); // Add padding and minimum width
        int height = 32; // Standard button height
        
        // Set both preferred and minimum size for consistent height
        button.setPreferredSize(new Dimension(minWidth, height));
        button.setMinimumSize(new Dimension(minWidth, height));
    }
    
    /**
     * Sets a consistent size for combo boxes to ensure equal height
     */
    private void setButtonSize(JComboBox<?> comboBox) {
        Dimension prefSize = comboBox.getPreferredSize();
        
        // Ensure combo box width fits the content
        int minWidth = Math.max(prefSize.width + 10, 100);
        int height = 32; // Standard height
        
        comboBox.setPreferredSize(new Dimension(minWidth, height));
        comboBox.setMinimumSize(new Dimension(minWidth, height));
    }
    
    private void browseBaseFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        String currentPath = baseFolderField.getText();
        if (!currentPath.isEmpty()) {
            chooser.setCurrentDirectory(new File(currentPath));
        } else {
            chooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        }
        
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDir = chooser.getSelectedFile();
            baseFolderField.setText(selectedDir.getAbsolutePath());
        }
    }
    
    private void updateConnectionCommand() {
        String address = addressField != null ? addressField.getText() : config.getServerAddress();
        if (address.equals("0.0.0.0") || address.equals("localhost")) {
            address = "localhost";
        }
        int port = portField != null ? Integer.parseInt(portField.getText()) : config.getPort();
        
        String command = String.format("ftp %s %d", address, port);
        if (connectionCommandField != null) {
            connectionCommandField.setText(command);
        }
    }
    
    private void copyConnectionCommand() {
        String command = connectionCommandField.getText();
        StringSelection selection = new StringSelection(command);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
        logMessage("Connection command copied to clipboard: " + command, "success");
        JOptionPane.showMessageDialog(this,
            "Command copied to clipboard:\n" + command,
            "Copied",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEtchedBorder());
        
        statusLabel = new JLabel("  Server Status: Stopped");
        statusLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        panel.add(statusLabel, BorderLayout.WEST);

        return panel;
    }

    private void setupListeners() {
        // Window closing listener
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (serverManager.isRunning()) {
                    int result = JOptionPane.showConfirmDialog(
                        MainFrame.this,
                        "Server is still running. Stop it and exit?",
                        "Confirm Exit",
                        JOptionPane.YES_NO_OPTION
                    );
                    if (result == JOptionPane.YES_OPTION) {
                        serverManager.stopServer();
                        saveConfiguration();
                        cleanup();
                        System.exit(0);
                    }
                } else {
                    saveConfiguration();
                    cleanup();
                    System.exit(0);
                }
            }
        });

        // Server status listener
        serverManager.addStatusListener((running, message) -> {
            SwingUtilities.invokeLater(() -> {
                updateUIState(running);
                logMessage(message);
            });
        });
    }

    private void startServer() {
        try {
            // Update config from UI
            config.setPort(Integer.parseInt(portField.getText()));
            config.setServerAddress(addressField.getText());
            config.setMaxLogins(Integer.parseInt(maxLoginsField.getText()));
            config.setAnonymousEnabled(anonymousCheckBox.isSelected());
            config.setBaseFolder(baseFolderField.getText());
            
            // Ensure base folder exists
            File baseDir = new File(config.getBaseFolder());
            if (!baseDir.exists()) {
                int result = JOptionPane.showConfirmDialog(this,
                    "Base folder does not exist. Create it?",
                    "Create Folder",
                    JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    if (!baseDir.mkdirs()) {
                        throw new Exception("Failed to create base folder");
                    }
                } else {
                    return;
                }
            }

            serverManager.updateConfig(config);
            serverManager.startServer();
            updateConnectionCommand();
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Invalid number format in configuration",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        } catch (FtpException ex) {
            JOptionPane.showMessageDialog(this,
                "Failed to start server: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            logMessage("ERROR: " + ex.getMessage(), "error");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            logMessage("ERROR: " + ex.getMessage(), "error");
        }
    }

    private void stopServer() {
        serverManager.stopServer();
    }

    private void updateUIState(boolean running) {
        startButton.setEnabled(!running);
        stopButton.setEnabled(running);
        portField.setEnabled(!running);
        addressField.setEnabled(!running);
        maxLoginsField.setEnabled(!running);
        baseFolderField.setEnabled(!running);
        anonymousCheckBox.setEnabled(!running);
        
        if (running) {
            statusLabel.setText("  Server Status: Running on port " + config.getPort());
            statusLabel.setForeground(new Color(0, 128, 0));
            updateConnectionCommand();
        } else {
            statusLabel.setText("  Server Status: Stopped");
            statusLabel.setForeground(Color.RED);
        }
    }

    /**
     * Initialize color styles for log messages
     */
    private void initializeLogStyles() {
        // INFO style - Blue
        infoStyle = logDocument.addStyle("INFO_STYLE", null);
        StyleConstants.setForeground(infoStyle, new Color(33, 150, 243)); // Material Blue
        
        // ERROR style - Red
        errorStyle = logDocument.addStyle("ERROR_STYLE", null);
        StyleConstants.setForeground(errorStyle, new Color(244, 67, 54)); // Material Red
        StyleConstants.setBold(errorStyle, true);
        
        // SUCCESS style - Green
        successStyle = logDocument.addStyle("SUCCESS_STYLE", null);
        StyleConstants.setForeground(successStyle, new Color(76, 175, 80)); // Material Green
        StyleConstants.setBold(successStyle, true);
        
        // Default style
        defaultStyle = logDocument.addStyle("DEFAULT_STYLE", null);
        StyleConstants.setForeground(defaultStyle, UIManager.getColor("TextPane.foreground"));
    }
    
    /**
     * Log a message with color coding based on type
     * @param message The message to log
     * @param type The message type: "info", "error", "success", or "default"
     */
    private void logMessage(String message, String type) {
        SwingUtilities.invokeLater(() -> {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timestamp = sdf.format(new Date());
                String fullMessage = "[" + timestamp + "] " + message + "\n";
                
                Style style;
                switch (type.toLowerCase()) {
                    case "info":
                        style = infoStyle;
                        break;
                    case "error":
                        style = errorStyle;
                        break;
                    case "success":
                        style = successStyle;
                        break;
                    default:
                        style = defaultStyle;
                        break;
                }
                
                logDocument.insertString(logDocument.getLength(), fullMessage, style);
                logPane.setCaretPosition(logDocument.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Legacy method for backward compatibility - defaults to info style
     */
    private void logMessage(String message) {
        logMessage(message, "info");
    }

    private void addUser() {
        UserManagementDialog dialog = new UserManagementDialog(this, null);
        dialog.setVisible(true);
        
        UserAccount newUser = dialog.getUserAccount();
        if (newUser != null) {
            config.addUser(newUser);
            userListModel.addElement(newUser);
            logMessage("User added: " + newUser.getUsername(), "success");
            saveConfiguration();
        }
    }

    private void editUser() {
        UserAccount selectedUser = userList.getSelectedValue();
        if (selectedUser == null) {
            JOptionPane.showMessageDialog(this,
                "Please select a user to edit",
                "No Selection",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        UserManagementDialog dialog = new UserManagementDialog(this, selectedUser);
        dialog.setVisible(true);
        
        UserAccount updatedUser = dialog.getUserAccount();
        if (updatedUser != null) {
            config.updateUser(selectedUser, updatedUser);
            int index = userListModel.indexOf(selectedUser);
            userListModel.set(index, updatedUser);
            logMessage("User updated: " + updatedUser.getUsername(), "success");
            saveConfiguration();
        }
    }

    private void deleteUser() {
        UserAccount selectedUser = userList.getSelectedValue();
        if (selectedUser == null) {
            JOptionPane.showMessageDialog(this,
                "Please select a user to delete",
                "No Selection",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete user '" + selectedUser.getUsername() + "'?",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION);
            
        if (result == JOptionPane.YES_OPTION) {
            config.removeUser(selectedUser);
            userListModel.removeElement(selectedUser);
            logMessage("User deleted: " + selectedUser.getUsername(), "success");
            saveConfiguration();
        }
    }
    
    /**
     * Saves current configuration to disk
     */
    private void saveConfiguration() {
        try {
            // Update config from UI fields
            if (portField != null) {
                config.setPort(Integer.parseInt(portField.getText()));
            }
            if (addressField != null) {
                config.setServerAddress(addressField.getText());
            }
            if (maxLoginsField != null) {
                config.setMaxLogins(Integer.parseInt(maxLoginsField.getText()));
            }
            if (baseFolderField != null) {
                config.setBaseFolder(baseFolderField.getText());
            }
            if (anonymousCheckBox != null) {
                config.setAnonymousEnabled(anonymousCheckBox.isSelected());
            }
            
            String currentTheme = themeSelector != null ? 
                (String) themeSelector.getSelectedItem() : "Flat Light";
            
            ConfigManager.saveConfig(config, currentTheme);
            logMessage("Configuration saved", "success");
        } catch (Exception e) {
            logMessage("Failed to save configuration: " + e.getMessage(), "error");
        }
    }
    
    /**
     * Cleanup resources before exiting
     */
    private void cleanup() {
        if (logCaptureAppender != null) {
            logCaptureAppender.stopCapture();
        }
    }

    public static void main(String[] args) {
        // Set FlatLaf look and feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}

