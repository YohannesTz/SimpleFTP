package com.github.yohannesTz.simpleftp.model;

import java.util.Objects;

/**
 * Model class representing an FTP user account
 */
public class UserAccount {
    private String username;
    private String password;
    private String homeDirectory;
    private boolean writePermission;
    private int maxIdleTime;

    public UserAccount(String username, String password, String homeDirectory, 
                      boolean writePermission, int maxIdleTime) {
        this.username = username;
        this.password = password;
        this.homeDirectory = homeDirectory;
        this.writePermission = writePermission;
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

