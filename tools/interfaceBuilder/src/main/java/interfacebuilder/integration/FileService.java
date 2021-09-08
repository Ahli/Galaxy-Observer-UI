// This is an open source non-commercial project. Dear PVS-Studio, please check it.
// PVS-Studio Static Code Analyzer for C, C++ and C#: http://www.viva64.com

package interfacebuilder.integration;

import com.ahli.util.FileCountingVisitor;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FileService {
	
	public FileService() {
		// explicit default constructor
	}
	
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
	public void copyFileOrDirectory(final Path source, final Path target) throws IOException {
		if (Files.isDirectory(source)) {
			// create folder if not existing
			if (!Files.exists(target)) {
				Files.createDirectories(target);
			}
			
			// copy all contained files recursively
			try (final Stream<Path> stream = Files.walk(source)) {
				stream.forEach(file -> {
					try {
						Files.copy(file, target.resolve(source.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
					} catch (final IOException e) {
						throw new FileServiceException(e.getMessage(), e);
					}
				});
			}
			
		} else {
			// copy a file
			Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
		}
	}
	
	/**
	 * Shortens the path to become a valid directory. Returns null if no valid directory exists.
	 *
	 * @param path
	 * @return
	 */
	public File cutTillValidDirectory(final String path) {
		File f = new File(path);
		if (!f.isDirectory()) {
			final char sep = File.separatorChar;
			int i;
			String pathTmp = path;
			do {
				i = pathTmp.lastIndexOf(sep);
				if (i != -1) {
					pathTmp = pathTmp.substring(0, i);
					//noinspection ObjectAllocationInLoop
					f = new File(pathTmp);
				} else {
					f = null;
					break;
				}
			} while (!f.isDirectory());
		}
		return f;
	}
	
	/**
	 * Returns the size of the specified directory in bytes.
	 *
	 * @param path
	 * @return size of all contained files in bytes
	 * @throws IOException
	 */
	public long getDirectorySize(final Path path) throws IOException {
		final long size;
		try (final Stream<Path> walk = Files.walk(path)) {
			size = walk.filter(p -> p.toFile().isFile()).mapToLong(p -> p.toFile().length()).sum();
		}
		return size;
	}
	
	/**
	 * Cleans a directory without deleting it.
	 *
	 * @param directory
	 * @throws IOException
	 */
	public void cleanDirectory(final Path directory) throws IOException {
		for (int i = 500; i > 0; --i) {
			try {
				FileSystemUtils.deleteRecursively(directory);
				Files.createDirectories(directory);
				return;
			} catch (final IOException e) {
				if (i <= 1) {
					throw e;
				} else {
					try {
						Thread.sleep(5);
					} catch (final InterruptedException e1) {
						e1.printStackTrace();
						Thread.currentThread().interrupt();
					}
				}
			}
		}
	}
	
	/**
	 * Checks if a directory is empty or not.
	 *
	 * @param directory
	 * @return true, if the directory is empty
	 * @throws IOException
	 * 		if it is not a directory or another problem occurred
	 */
	public boolean isEmptyDirectory(final Path directory) throws IOException {
		return getFileCountOfDirectory(directory) <= 0;
	}
	
	/**
	 * Returns the count of files within the specified directory.
	 *
	 * @param path
	 * @return count of files in directory and subdirectories
	 * @throws IOException
	 */
	public int getFileCountOfDirectory(final Path path) throws IOException {
		if (!Files.exists(path)) {
			return 0;
		}
		final FileCountingVisitor fileVisitor = new FileCountingVisitor();
		Files.walkFileTree(path, fileVisitor);
		return fileVisitor.getCount();
	}
	
	/**
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public List<Path> getFilesOfDirectory(final Path path) throws IOException {
		final FileListingVisitor fileVisitor = new FileListingVisitor();
		Files.walkFileTree(path, fileVisitor);
		return fileVisitor.getFilePaths();
	}
	
	private static class FileServiceException extends RuntimeException {
		
		@Serial
		private static final long serialVersionUID = -5030478360016607662L;
		
		public FileServiceException(final String message, final IOException e) {
			super(message, e);
		}
	}
	
	private static final class FileListingVisitor extends SimpleFileVisitor<Path> {
		private List<Path> paths = new ArrayList<>();
		
		@Override
		public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
			paths.add(file);
			return FileVisitResult.CONTINUE;
		}
		
		public List<Path> getFilePaths() {
			return paths;
		}
		
		public void resetFiles() {
			paths = new ArrayList<>();
		}
	}
}
