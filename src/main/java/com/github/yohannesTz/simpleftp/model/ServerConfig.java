package com.github.yohannesTz.simpleftp.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for the FTP server
 */
public class ServerConfig {
    private int port;
    private String serverAddress;
    private int maxLogins;
    private boolean anonymousEnabled;
    private String baseFolder;
    private List<UserAccount> users;

    public ServerConfig() {
        this.port = 2121;
        this.serverAddress = "0.0.0.0";
        this.maxLogins = 10;
        this.anonymousEnabled = false;
        this.baseFolder = System.getProperty("user.home") + "/ftp";
        this.users = new ArrayList<>();
        
        // Add a default admin user
        users.add(new UserAccount("admin", "admin", 
                baseFolder, true, 300));
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public int getMaxLogins() {
        return maxLogins;
    }

    public void setMaxLogins(int maxLogins) {
        this.maxLogins = maxLogins;
    }

    public boolean isAnonymousEnabled() {
        return anonymousEnabled;
    }

    public void setAnonymousEnabled(boolean anonymousEnabled) {
        this.anonymousEnabled = anonymousEnabled;
    }

    public String getBaseFolder() {
        return baseFolder;
    }

    public void setBaseFolder(String baseFolder) {
        this.baseFolder = baseFolder;
    }

    public List<UserAccount> getUsers() {
        return users;
    }

    public void addUser(UserAccount user) {
        this.users.add(user);
    }

    public void removeUser(UserAccount user) {
        this.users.remove(user);
    }

    public void updateUser(UserAccount oldUser, UserAccount newUser) {
        int index = users.indexOf(oldUser);
        if (index != -1) {
            users.set(index, newUser);
        }
    }
}

