package swordfishsync.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import swordfishsync.domain.FeedProvider;

public class FileSystemUtils {

    private static final Logger log = LoggerFactory.getLogger(FileSystemUtils.class);

	public static void setFilePermissions(File file, FeedProvider feedProvider) throws IOException {
		String currentUser = System.getProperty("user.name");

		File parentFolder = new File(feedProvider.getDownloadDirectory());

	    FileOwnerAttributeView view = Files.getFileAttributeView(file.toPath(), FileOwnerAttributeView.class);
	    UserPrincipal userPrincipal = view.getOwner();
	    
	    String fileOwner = userPrincipal.getName();
	    
	    if (currentUser.equals(fileOwner)) {
			log.debug("Setting file permissions on file canonical path " + file.getCanonicalPath());

			GroupPrincipal groupPrincipal = Files.readAttributes(parentFolder.toPath(), PosixFileAttributes.class).group();

			log.debug("Using groupPrincipal [" + groupPrincipal + "]");

			// set group ownership
			Files.getFileAttributeView(file.toPath(), PosixFileAttributeView.class).setGroup(groupPrincipal);

			// Use PosixFilePermission to set file permissions
			Set<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
			//add owners permission
			perms.add(PosixFilePermission.OWNER_READ);
			perms.add(PosixFilePermission.OWNER_WRITE);
			perms.add(PosixFilePermission.OWNER_EXECUTE);
			//add group permissions
			perms.add(PosixFilePermission.GROUP_READ);
			perms.add(PosixFilePermission.GROUP_WRITE);
			perms.add(PosixFilePermission.GROUP_EXECUTE);
			//add others permissions
			perms.add(PosixFilePermission.OTHERS_READ);
    		perms.add(PosixFilePermission.OTHERS_EXECUTE);

			Files.setPosixFilePermissions(file.toPath(), perms);
	    } else {
			log.debug("File user [" + fileOwner + "] does not match current user [" + currentUser + "], skipping permissions change.");
	    }
	}

}
