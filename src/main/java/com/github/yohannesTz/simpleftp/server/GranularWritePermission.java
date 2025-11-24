package com.github.yohannesTz.simpleftp.server;

import com.github.yohannesTz.simpleftp.model.FTPPermissions;
import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.AuthorizationRequest;

/**
 * Custom write permission that supports granular FTP operations
 */
public class GranularWritePermission implements Authority {
    private final FTPPermissions permissions;
    
    public GranularWritePermission(FTPPermissions permissions) {
        this.permissions = permissions;
    }
    
    @Override
    public AuthorizationRequest authorize(AuthorizationRequest request) {
        String requestType = request.getClass().getSimpleName();
        
        // Handle different request types based on our granular permissions
        switch (requestType) {
            case "WriteRequest":
                return permissions.isCanWrite() ? request : null;
            case "DeleteRequest":
                return permissions.isCanDelete() ? request : null;
            case "RenameRequest":
                return permissions.isCanRename() ? request : null;
            case "MkdirRequest":
                return permissions.isCanCreateDirectory() ? request : null;
            case "RmdirRequest":
                return permissions.isCanRemoveDirectory() ? request : null;
            case "ListRequest":
                return permissions.isCanList() ? request : null;
            case "DownloadRequest":
            case "RetrRequest":
                return permissions.isCanRead() ? request : null;
            default:
                // For unknown requests, check if user has any write permission
                return permissions.hasWriteAccess() ? request : null;
        }
    }
    
    @Override
    public boolean canAuthorize(AuthorizationRequest request) {
        return true; // We handle all request types
    }
}

