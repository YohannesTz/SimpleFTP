package com.github.yohannesTz.simpleftp.model;

import java.io.Serializable;

/**
 * Represents FTP permissions for a user account
 */
public class FTPPermissions implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private boolean canRead;
    private boolean canWrite;
    private boolean canDelete;
    private boolean canRename;
    private boolean canCreateDirectory;
    private boolean canRemoveDirectory;
    private boolean canList;
    
    public FTPPermissions() {
        // Default permissions: read and list only
        this.canRead = true;
        this.canWrite = false;
        this.canDelete = false;
        this.canRename = false;
        this.canCreateDirectory = false;
        this.canRemoveDirectory = false;
        this.canList = true;
    }
    
    /**
     * Creates permissions with all access
     */
    public static FTPPermissions fullAccess() {
        FTPPermissions perms = new FTPPermissions();
        perms.canRead = true;
        perms.canWrite = true;
        perms.canDelete = true;
        perms.canRename = true;
        perms.canCreateDirectory = true;
        perms.canRemoveDirectory = true;
        perms.canList = true;
        return perms;
    }
    
    /**
     * Creates read-only permissions
     */
    public static FTPPermissions readOnly() {
        FTPPermissions perms = new FTPPermissions();
        perms.canRead = true;
        perms.canWrite = false;
        perms.canDelete = false;
        perms.canRename = false;
        perms.canCreateDirectory = false;
        perms.canRemoveDirectory = false;
        perms.canList = true;
        return perms;
    }
    
    // Getters and Setters
    public boolean isCanRead() {
        return canRead;
    }
    
    public void setCanRead(boolean canRead) {
        this.canRead = canRead;
    }
    
    public boolean isCanWrite() {
        return canWrite;
    }
    
    public void setCanWrite(boolean canWrite) {
        this.canWrite = canWrite;
    }
    
    public boolean isCanDelete() {
        return canDelete;
    }
    
    public void setCanDelete(boolean canDelete) {
        this.canDelete = canDelete;
    }
    
    public boolean isCanRename() {
        return canRename;
    }
    
    public void setCanRename(boolean canRename) {
        this.canRename = canRename;
    }
    
    public boolean isCanCreateDirectory() {
        return canCreateDirectory;
    }
    
    public void setCanCreateDirectory(boolean canCreateDirectory) {
        this.canCreateDirectory = canCreateDirectory;
    }
    
    public boolean isCanRemoveDirectory() {
        return canRemoveDirectory;
    }
    
    public void setCanRemoveDirectory(boolean canRemoveDirectory) {
        this.canRemoveDirectory = canRemoveDirectory;
    }
    
    public boolean isCanList() {
        return canList;
    }
    
    public void setCanList(boolean canList) {
        this.canList = canList;
    }
    
    /**
     * Returns true if user has any write-related permission
     */
    public boolean hasWriteAccess() {
        return canWrite || canDelete || canRename || canCreateDirectory || canRemoveDirectory;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (canRead) sb.append("Read ");
        if (canWrite) sb.append("Write ");
        if (canDelete) sb.append("Delete ");
        if (canRename) sb.append("Rename ");
        if (canCreateDirectory) sb.append("CreateDir ");
        if (canRemoveDirectory) sb.append("RemoveDir ");
        if (canList) sb.append("List");
        return sb.toString().trim();
    }
}

