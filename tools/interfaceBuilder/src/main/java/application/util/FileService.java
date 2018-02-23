package application.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class FileService {
	
	/**
	 * Copies a file or directory.
	 *
	 * @param source
	 * 		source location
	 * @param target
	 * 		target location
	 * @throws IOException
	 * 		when something goes wrong
	 */
	public void copyFileOrDirectory(final File source, final File target) throws IOException {
		if (source.isDirectory()) {
			// create folder if not existing
			if (!target.exists() && !target.mkdir()) {
				final String msg = "Could not create directory " + target.getAbsolutePath(); //$NON-NLS-1$
				throw new IOException(msg);
			}
			
			// copy all contained files recursively
			final String[] fileList = source.list();
			if (fileList == null) {
				final String msg = "Source directory's files returned null"; //$NON-NLS-1$
				throw new IOException(msg);
			}
			for (final String file : fileList) {
				final File srcFile = new File(source, file);
				final File destFile = new File(target, file);
				// Recursive traversal of directories
				copyFileOrDirectory(srcFile, destFile);
			}
		} else {
			// copy a file
			Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}
	
	/**
	 * Shortens the path to become a valid directory. Returns null if no valid directory exists.
	 *
	 * @param path
	 * @return
	 */
	public File cutTillValidDirectory(String path) {
		File f = new File(path);
		if (!f.isDirectory()) {
			final char sep = File.separatorChar;
			int i;
			do {
				i = path.lastIndexOf(sep);
				if (i != -1) {
					path = path.substring(0, i);
					f = new File(path);
				} else {
					f = null;
					break;
				}
			} while (!f.isDirectory());
		}
		return f;
	}
}
