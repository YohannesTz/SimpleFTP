package com.github.yohannesTz.simpleftp.server;

import com.github.yohannesTz.simpleftp.model.FTPPermissions;
import com.github.yohannesTz.simpleftp.model.ServerConfig;
import com.github.yohannesTz.simpleftp.model.UserAccount;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the FTP Server lifecycle
 */
public class FTPServerManager {
    private FtpServer server;
    private ServerConfig config;
    private boolean running;
    private List<ServerStatusListener> listeners;

    public FTPServerManager(ServerConfig config) {
        this.config = config;
        this.running = false;
        this.listeners = new ArrayList<>();
    }

    public interface ServerStatusListener {
        void onStatusChange(boolean running, String message);
    }

    public void addStatusListener(ServerStatusListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners(boolean running, String message) {
        for (ServerStatusListener listener : listeners) {
            listener.onStatusChange(running, message);
        }
    }

    public void startServer() throws FtpException {
        if (running) {
            throw new FtpException("Server is already running");
        }

        FtpServerFactory serverFactory = new FtpServerFactory();

        // Configure listener
        ListenerFactory listenerFactory = new ListenerFactory();
        listenerFactory.setPort(config.getPort());
        listenerFactory.setServerAddress(config.getServerAddress());
        serverFactory.addListener("default", listenerFactory.createListener());

        // Configure user manager
        PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
        UserManager userManager = userManagerFactory.createUserManager();

        // Add users from config
        for (UserAccount userAccount : config.getUsers()) {
            BaseUser user = new BaseUser();
            user.setName(userAccount.getUsername());
            user.setPassword(userAccount.getPassword());
            user.setHomeDirectory(userAccount.getHomeDirectory());
            
            // Ensure home directory exists
            File homeDir = new File(userAccount.getHomeDirectory());
            if (!homeDir.exists()) {
                homeDir.mkdirs();
            }

            List<Authority> authorities = new ArrayList<>();
            
            // Use granular permissions if available
            FTPPermissions permissions = userAccount.getPermissions();
            if (permissions.hasWriteAccess()) {
                // Add our custom granular permission
                authorities.add(new GranularWritePermission(permissions));
            } else {
                // For read-only users, still add granular permission
                // so read/list permissions can be enforced
                authorities.add(new GranularWritePermission(permissions));
            }
            
            user.setAuthorities(authorities);
            user.setMaxIdleTime(userAccount.getMaxIdleTime());

            try {
                userManager.save(user);
            } catch (FtpException e) {
                throw new FtpException("Failed to save user: " + userAccount.getUsername(), e);
            }
        }

        serverFactory.setUserManager(userManager);

        // Create and start server
        server = serverFactory.createServer();
        server.start();
        running = true;
        
        notifyListeners(true, "Server started on port " + config.getPort());
    }

    public void stopServer() {
        if (server != null && running) {
            server.stop();
            running = false;
            notifyListeners(false, "Server stopped");
        }
    }

    public boolean isRunning() {
        return running;
    }

    public ServerConfig getConfig() {
        return config;
    }

    public void updateConfig(ServerConfig newConfig) {
        boolean wasRunning = running;
        if (wasRunning) {
            stopServer();
        }
        this.config = newConfig;
        if (wasRunning) {
            try {
                startServer();
            } catch (FtpException e) {
                notifyListeners(false, "Failed to restart server: " + e.getMessage());
            }
        }
    }
}

