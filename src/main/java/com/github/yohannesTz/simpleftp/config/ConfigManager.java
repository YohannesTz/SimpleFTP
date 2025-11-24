package com.github.yohannesTz.simpleftp.config;

import com.github.yohannesTz.simpleftp.model.ServerConfig;
import com.github.yohannesTz.simpleftp.model.UserAccount;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Manages saving and loading of application configuration
 */
public class ConfigManager {
    private static final String CONFIG_DIR = System.getProperty("user.home") + "/.simpleftp";
    private static final String CONFIG_FILE = CONFIG_DIR + "/config.properties";
    private static final String USERS_FILE = CONFIG_DIR + "/users.dat";
    
    /**
     * Saves server configuration and user accounts
     */
    public static void saveConfig(ServerConfig config, String currentTheme) {
        try {
            // Create config directory if it doesn't exist
            File configDir = new File(CONFIG_DIR);
            if (!configDir.exists()) {
                configDir.mkdirs();
            }
            
            // Save server configuration
            Properties props = new Properties();
            props.setProperty("server.port", String.valueOf(config.getPort()));
            props.setProperty("server.address", config.getServerAddress());
            props.setProperty("server.maxLogins", String.valueOf(config.getMaxLogins()));
            props.setProperty("server.anonymousEnabled", String.valueOf(config.isAnonymousEnabled()));
            props.setProperty("server.baseFolder", config.getBaseFolder());
            props.setProperty("ui.theme", currentTheme);
            
            try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
                props.store(fos, "Simple FTP Server Configuration");
            }
            
            // Save user accounts
            saveUsers(config.getUsers());
            
        } catch (IOException e) {
            System.err.println("Failed to save configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Loads server configuration and user accounts
     */
    public static ConfigData loadConfig() {
        ConfigData data = new ConfigData();
        
        try {
            File configFile = new File(CONFIG_FILE);
            if (!configFile.exists()) {
                // Return default configuration
                return data;
            }
            
            // Load server configuration
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
                props.load(fis);
            }
            
            ServerConfig config = new ServerConfig();
            
            // Clear default admin user if loading from config
            config.getUsers().clear();
            
            config.setPort(Integer.parseInt(props.getProperty("server.port", "2121")));
            config.setServerAddress(props.getProperty("server.address", "0.0.0.0"));
            config.setMaxLogins(Integer.parseInt(props.getProperty("server.maxLogins", "10")));
            config.setAnonymousEnabled(Boolean.parseBoolean(props.getProperty("server.anonymousEnabled", "false")));
            config.setBaseFolder(props.getProperty("server.baseFolder", System.getProperty("user.home") + "/ftp"));
            
            data.serverConfig = config;
            data.theme = props.getProperty("ui.theme", "Flat Light");
            
            // Load user accounts
            List<UserAccount> users = loadUsers();
            for (UserAccount user : users) {
                config.addUser(user);
            }
            
            // If no users loaded, add default admin
            if (config.getUsers().isEmpty()) {
                config.addUser(new UserAccount("admin", "admin", 
                    config.getBaseFolder(), true, 300));
            }
            
        } catch (Exception e) {
            System.err.println("Failed to load configuration: " + e.getMessage());
            e.printStackTrace();
        }
        
        return data;
    }
    
    /**
     * Saves user accounts to file
     */
    private static void saveUsers(List<UserAccount> users) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeInt(users.size());
            for (UserAccount user : users) {
                oos.writeUTF(user.getUsername());
                oos.writeUTF(user.getPassword());
                oos.writeUTF(user.getHomeDirectory());
                oos.writeBoolean(user.isWritePermission());
                oos.writeInt(user.getMaxIdleTime());
            }
        }
    }
    
    /**
     * Loads user accounts from file
     */
    private static List<UserAccount> loadUsers() {
        List<UserAccount> users = new ArrayList<>();
        
        try {
            File usersFile = new File(USERS_FILE);
            if (!usersFile.exists()) {
                return users;
            }
            
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USERS_FILE))) {
                int count = ois.readInt();
                for (int i = 0; i < count; i++) {
                    String username = ois.readUTF();
                    String password = ois.readUTF();
                    String homeDirectory = ois.readUTF();
                    boolean writePermission = ois.readBoolean();
                    int maxIdleTime = ois.readInt();
                    
                    users.add(new UserAccount(username, password, homeDirectory, 
                        writePermission, maxIdleTime));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load users: " + e.getMessage());
        }
        
        return users;
    }
    
    /**
     * Deletes all saved configuration
     */
    public static void deleteConfig() {
        try {
            Files.deleteIfExists(Paths.get(CONFIG_FILE));
            Files.deleteIfExists(Paths.get(USERS_FILE));
        } catch (IOException e) {
            System.err.println("Failed to delete configuration: " + e.getMessage());
        }
    }
    
    /**
     * Checks if configuration exists
     */
    public static boolean configExists() {
        return new File(CONFIG_FILE).exists();
    }
    
    /**
     * Container for loaded configuration data
     */
    public static class ConfigData {
        public ServerConfig serverConfig = new ServerConfig();
        public String theme = "Flat Light";
    }
}

