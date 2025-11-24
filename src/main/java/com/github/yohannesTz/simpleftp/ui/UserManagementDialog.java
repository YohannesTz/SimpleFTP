package com.github.yohannesTz.simpleftp.ui;

import com.github.yohannesTz.simpleftp.model.UserAccount;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

/**
 * Dialog for adding or editing FTP user accounts
 */
public class UserManagementDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField homeDirectoryField;
    private JButton browseButton;
    private JCheckBox writePermissionCheckBox;
    private JSpinner idleTimeSpinner;
    private JButton okButton;
    private JButton cancelButton;
    
    private UserAccount userAccount;
    private boolean isEditMode;

    public UserManagementDialog(Frame parent, UserAccount user) {
        super(parent, user == null ? "Add User" : "Edit User", true);
        this.userAccount = null;
        this.isEditMode = (user != null);
        
        // Set dialog icon to match main window
        try {
            java.net.URL iconURL = getClass().getResource("/ftp-server-48.png");
            if (iconURL != null) {
                ImageIcon icon = new ImageIcon(iconURL);
                setIconImage(icon.getImage());
            }
        } catch (Exception e) {
            // Icon loading failed, continue without icon
            System.err.println("Failed to load dialog icon: " + e.getMessage());
        }
        
        initUI();
        
        if (user != null) {
            populateFields(user);
        }
        
        pack();
        setLocationRelativeTo(parent);
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        usernameField = new JTextField(20);
        formPanel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Password:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        passwordField = new JPasswordField(20);
        formPanel.add(passwordField, gbc);

        // Confirm Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Confirm Password:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        confirmPasswordField = new JPasswordField(20);
        formPanel.add(confirmPasswordField, gbc);

        // Home Directory
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Home Directory:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        homeDirectoryField = new JTextField(20);
        formPanel.add(homeDirectoryField, gbc);
        
        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        browseButton = new JButton("Browse...");
        browseButton.addActionListener(e -> browseDirectory());
        setButtonSize(browseButton);
        formPanel.add(browseButton, gbc);

        // Write Permission
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        writePermissionCheckBox = new JCheckBox("Allow Write Permission");
        writePermissionCheckBox.setSelected(true);
        formPanel.add(writePermissionCheckBox, gbc);

        // Max Idle Time
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 1;
        formPanel.add(new JLabel("Max Idle Time (sec):"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(300, 0, 3600, 10);
        idleTimeSpinner = new JSpinner(spinnerModel);
        formPanel.add(idleTimeSpinner, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        okButton = new JButton("OK");
        okButton.addActionListener(e -> onOK());
        setButtonSize(okButton);
        
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> onCancel());
        setButtonSize(cancelButton);

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        
        // Set default button
        getRootPane().setDefaultButton(okButton);
    }
    
    /**
     * Sets a consistent size for buttons to ensure equal height
     */
    private void setButtonSize(JButton button) {
        Dimension size = button.getPreferredSize();
        size.height = 32; // Standard button height
        button.setPreferredSize(size);
    }

    private void populateFields(UserAccount user) {
        usernameField.setText(user.getUsername());
        usernameField.setEnabled(false); // Don't allow username changes in edit mode
        passwordField.setText(user.getPassword());
        confirmPasswordField.setText(user.getPassword());
        homeDirectoryField.setText(user.getHomeDirectory());
        writePermissionCheckBox.setSelected(user.isWritePermission());
        idleTimeSpinner.setValue(user.getMaxIdleTime());
    }

    private void browseDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        String currentPath = homeDirectoryField.getText();
        if (!currentPath.isEmpty()) {
            chooser.setCurrentDirectory(new File(currentPath));
        } else {
            chooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        }
        
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedDir = chooser.getSelectedFile();
            homeDirectoryField.setText(selectedDir.getAbsolutePath());
        }
    }

    private void onOK() {
        // Validate input
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Username cannot be empty",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Password cannot be empty",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                "Passwords do not match",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        String homeDirectory = homeDirectoryField.getText().trim();
        if (homeDirectory.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Home directory cannot be empty",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create directory if it doesn't exist
        File homeDir = new File(homeDirectory);
        if (!homeDir.exists()) {
            int result = JOptionPane.showConfirmDialog(this,
                "Directory does not exist. Create it?",
                "Create Directory",
                JOptionPane.YES_NO_OPTION);
            
            if (result == JOptionPane.YES_OPTION) {
                if (!homeDir.mkdirs()) {
                    JOptionPane.showMessageDialog(this,
                        "Failed to create directory",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                return;
            }
        }

        boolean writePermission = writePermissionCheckBox.isSelected();
        int maxIdleTime = (Integer) idleTimeSpinner.getValue();

        userAccount = new UserAccount(username, password, homeDirectory, 
                                     writePermission, maxIdleTime);
        dispose();
    }

    private void onCancel() {
        userAccount = null;
        dispose();
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }
}

