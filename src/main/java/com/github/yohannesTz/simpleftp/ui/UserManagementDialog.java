package com.github.yohannesTz.simpleftp.ui;

import com.github.yohannesTz.simpleftp.model.FTPPermissions;
import com.github.yohannesTz.simpleftp.model.UserAccount;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;

/**
 * Dialog for adding or editing FTP user accounts
 */
public class UserManagementDialog extends JDialog {
    private JTextField usernameField;
    private PasswordFieldWithToggle passwordField;
    private PasswordFieldWithToggle confirmPasswordField;
    private JTextField homeDirectoryField;
    private JButton browseButton;
    private JCheckBox writePermissionCheckBox; // Legacy - kept for compatibility
    private JSpinner idleTimeSpinner;
    private JButton okButton;
    private JButton cancelButton;
    
    // Permission checkboxes
    private JCheckBox readCheckBox;
    private JCheckBox writeCheckBox;
    private JCheckBox deleteCheckBox;
    private JCheckBox renameCheckBox;
    private JCheckBox createDirCheckBox;
    private JCheckBox removeDirCheckBox;
    private JCheckBox listCheckBox;
    
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
        
        // Initialize permission checkboxes BEFORE creating UI
        readCheckBox = new JCheckBox("Read Files");
        writeCheckBox = new JCheckBox("Write/Upload Files");
        deleteCheckBox = new JCheckBox("Delete Files");
        renameCheckBox = new JCheckBox("Rename Files");
        createDirCheckBox = new JCheckBox("Create Directories");
        removeDirCheckBox = new JCheckBox("Remove Directories");
        listCheckBox = new JCheckBox("List Directories");
        
        initUI();
        
        if (user != null) {
            populateFields(user);
        } else {
            // Default permissions for new users
            setReadOnly();
        }
        
        pack();
        setLocationRelativeTo(parent);
    }
    
    private JPanel createPermissionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10),
            BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Permissions",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("SansSerif", Font.BOLD, 12)
            )
        ));
        
        // Permission checkboxes in a grid
        JPanel checkboxPanel = new JPanel(new GridLayout(4, 2, 10, 5));
        checkboxPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        checkboxPanel.add(readCheckBox);
        checkboxPanel.add(writeCheckBox);
        checkboxPanel.add(deleteCheckBox);
        checkboxPanel.add(renameCheckBox);
        checkboxPanel.add(createDirCheckBox);
        checkboxPanel.add(removeDirCheckBox);
        checkboxPanel.add(listCheckBox);
        checkboxPanel.add(new JLabel("")); // Empty cell for layout
        
        panel.add(checkboxPanel, BorderLayout.CENTER);
        
        // Quick permission presets
        JPanel presetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton fullAccessBtn = new JButton("Full Access");
        JButton readOnlyBtn = new JButton("Read Only");
        JButton uploadBtn = new JButton("Upload Only");
        
        setButtonSize(fullAccessBtn);
        setButtonSize(readOnlyBtn);
        setButtonSize(uploadBtn);
        
        fullAccessBtn.addActionListener(e -> setFullAccess());
        readOnlyBtn.addActionListener(e -> setReadOnly());
        uploadBtn.addActionListener(e -> setUploadOnly());
        
        presetPanel.add(new JLabel("Quick Presets:"));
        presetPanel.add(fullAccessBtn);
        presetPanel.add(readOnlyBtn);
        presetPanel.add(uploadBtn);
        
        panel.add(presetPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void setFullAccess() {
        readCheckBox.setSelected(true);
        writeCheckBox.setSelected(true);
        deleteCheckBox.setSelected(true);
        renameCheckBox.setSelected(true);
        createDirCheckBox.setSelected(true);
        removeDirCheckBox.setSelected(true);
        listCheckBox.setSelected(true);
    }
    
    private void setReadOnly() {
        readCheckBox.setSelected(true);
        writeCheckBox.setSelected(false);
        deleteCheckBox.setSelected(false);
        renameCheckBox.setSelected(false);
        createDirCheckBox.setSelected(false);
        removeDirCheckBox.setSelected(false);
        listCheckBox.setSelected(true);
    }
    
    private void setUploadOnly() {
        readCheckBox.setSelected(true);
        writeCheckBox.setSelected(true);
        deleteCheckBox.setSelected(false);
        renameCheckBox.setSelected(false);
        createDirCheckBox.setSelected(true);
        removeDirCheckBox.setSelected(false);
        listCheckBox.setSelected(true);
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
        passwordField = new PasswordFieldWithToggle(20);
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
        confirmPasswordField = new PasswordFieldWithToggle(20);
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

        // Legacy Write Permission (hidden, kept for backward compatibility)
        writePermissionCheckBox = new JCheckBox("Allow Write Permission");
        writePermissionCheckBox.setSelected(true);
        writePermissionCheckBox.setVisible(false);

        // Max Idle Time
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        formPanel.add(new JLabel("Max Idle Time (sec):"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(300, 0, 3600, 10);
        idleTimeSpinner = new JSpinner(spinnerModel);
        formPanel.add(idleTimeSpinner, gbc);

        mainPanel.add(formPanel, BorderLayout.NORTH);
        
        // Permissions Panel
        JPanel permissionsPanel = createPermissionsPanel();
        mainPanel.add(permissionsPanel, BorderLayout.CENTER);

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
        // Get the preferred size to ensure text fits
        Dimension prefSize = button.getPreferredSize();
        
        // Set a minimum width to accommodate the text with padding
        int minWidth = Math.max(prefSize.width + 20, 80); // Add padding and minimum width
        int height = 32; // Standard button height
        
        // Set both preferred and minimum size for consistent height
        button.setPreferredSize(new Dimension(minWidth, height));
        button.setMinimumSize(new Dimension(minWidth, height));
    }

    private void populateFields(UserAccount user) {
        usernameField.setText(user.getUsername());
        usernameField.setEnabled(false); // Don't allow username changes in edit mode
        passwordField.setText(user.getPassword());
        confirmPasswordField.setText(user.getPassword());
        homeDirectoryField.setText(user.getHomeDirectory());
        writePermissionCheckBox.setSelected(user.isWritePermission());
        idleTimeSpinner.setValue(user.getMaxIdleTime());
        
        // Load permissions
        FTPPermissions perms = user.getPermissions();
        readCheckBox.setSelected(perms.isCanRead());
        writeCheckBox.setSelected(perms.isCanWrite());
        deleteCheckBox.setSelected(perms.isCanDelete());
        renameCheckBox.setSelected(perms.isCanRename());
        createDirCheckBox.setSelected(perms.isCanCreateDirectory());
        removeDirCheckBox.setSelected(perms.isCanRemoveDirectory());
        listCheckBox.setSelected(perms.isCanList());
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

        char[] passwordChars = passwordField.getPassword();
        char[] confirmPasswordChars = confirmPasswordField.getPassword();
        String password = new String(passwordChars);
        String confirmPassword = new String(confirmPasswordChars);
        
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Password cannot be empty",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            // Clear password arrays for security
            java.util.Arrays.fill(passwordChars, ' ');
            java.util.Arrays.fill(confirmPasswordChars, ' ');
            JOptionPane.showMessageDialog(this,
                "Passwords do not match",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Clear password arrays after use
        java.util.Arrays.fill(passwordChars, ' ');
        java.util.Arrays.fill(confirmPasswordChars, ' ');

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

        int maxIdleTime = (Integer) idleTimeSpinner.getValue();
        
        // Create permissions object from checkboxes
        FTPPermissions permissions = new FTPPermissions();
        permissions.setCanRead(readCheckBox.isSelected());
        permissions.setCanWrite(writeCheckBox.isSelected());
        permissions.setCanDelete(deleteCheckBox.isSelected());
        permissions.setCanRename(renameCheckBox.isSelected());
        permissions.setCanCreateDirectory(createDirCheckBox.isSelected());
        permissions.setCanRemoveDirectory(removeDirCheckBox.isSelected());
        permissions.setCanList(listCheckBox.isSelected());

        userAccount = new UserAccount(username, password, homeDirectory, permissions, maxIdleTime);
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
