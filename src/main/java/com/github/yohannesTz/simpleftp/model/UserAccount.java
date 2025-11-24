package com.github.yohannesTz.simpleftp.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Model class representing an FTP user account
 */
public class UserAccount implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username;
    private String password;
    private String homeDirectory;
    private boolean writePermission; // Legacy field - kept for compatibility
    private FTPPermissions permissions;
    private int maxIdleTime;

    public UserAccount(String username, String password, String homeDirectory, 
                      boolean writePermission, int maxIdleTime) {
        this.username = username;
        this.password = password;
        this.homeDirectory = homeDirectory;
        this.writePermission = writePermission;
        this.maxIdleTime = maxIdleTime;
        
        // Initialize permissions based on legacy writePermission
        if (writePermission) {
            this.permissions = FTPPermissions.fullAccess();
        } else {
            this.permissions = FTPPermissions.readOnly();
        }
    }
    
    public UserAccount(String username, String password, String homeDirectory, 
                      FTPPermissions permissions, int maxIdleTime) {
        this.username = username;
        this.password = password;
        this.homeDirectory = homeDirectory;
        this.permissions = permissions;
        this.writePermission = permissions.hasWriteAccess();
        this.maxIdleTime = maxIdleTime;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHomeDirectory() {
        return homeDirectory;
    }

    public void setHomeDirectory(String homeDirectory) {
        this.homeDirectory = homeDirectory;
    }

    public boolean isWritePermission() {
        return writePermission;
    }

    public void setWritePermission(boolean writePermission) {
        this.writePermission = writePermission;
        // Update permissions object to maintain consistency
        if (writePermission && permissions != null) {
            permissions = FTPPermissions.fullAccess();
        } else if (!writePermission && permissions != null) {
            permissions = FTPPermissions.readOnly();
        }
    }
    
    public FTPPermissions getPermissions() {
        if (permissions == null) {
            // Migrate from legacy writePermission
            if (writePermission) {
                permissions = FTPPermissions.fullAccess();
            } else {
                permissions = FTPPermissions.readOnly();
            }
        }
        return permissions;
    }
    
    public void setPermissions(FTPPermissions permissions) {
        this.permissions = permissions;
        this.writePermission = permissions.hasWriteAccess();
    }

    public int getMaxIdleTime() {
        return maxIdleTime;
    }

    public void setMaxIdleTime(int maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAccount that = (UserAccount) o;
        return Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return username + " (" + homeDirectory + ")";
    }
}

