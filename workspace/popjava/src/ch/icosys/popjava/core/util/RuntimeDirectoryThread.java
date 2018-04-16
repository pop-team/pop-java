package popjava.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

/**
 * Broker cleanup after itself on exit
 *
 * @author Davide Mazzoleni
 */
public class RuntimeDirectoryThread extends Thread {

	private final Path origin;
	private Path basePath;
	private boolean done = false;

	public RuntimeDirectoryThread(String id) {
		Objects.requireNonNull(id);
		
		this.origin = Paths.get(".").toAbsolutePath();
		this.basePath = Paths.get(id).toAbsolutePath();
		init(id);
	}

	public RuntimeDirectoryThread(File dir) {
		Objects.requireNonNull(dir);
		
		this.origin = Paths.get(".").toAbsolutePath();
		this.basePath = dir.toPath().toAbsolutePath();
		init(basePath.getFileName().toString());
	}

	private void init(String id) {
		Configuration conf = Configuration.getInstance();
		// create directories
		try {
			basePath = Files.createDirectories(basePath);
		} catch(IOException e) {
			try {
				basePath = Files.createTempDirectory(String.format("popjava-%s", id));
			} catch(IOException ex) {
				throw new RuntimeException("Broker couldn't create the object directory.");
			}
		}
		// change SSL configuration
		conf.setSSLTemporaryCertificateDirectory(basePath.toFile());
	}

	@Override
	public void run() {
		try {
			cleanup();
		} catch (IOException e) {
			LogWriter.writeDebugInfo("[Broker] A problem occurred when cleaning up: %s", e.getMessage());
		}
	}
	
	public void addCleanupHook() {		
		// set exit cleanup
		Runtime.getRuntime().addShutdownHook(this);
	}
	
	public void removeCleanupHook() {
		// remove exit cleanup
		Runtime.getRuntime().removeShutdownHook(this);
	}

	public void cleanup() throws IOException {
		if (!done) {
			// back to the original path
			System.setProperty("user.dir", origin.toString());
			// remove object dir and content
			if (basePath != null) {
				Files.walkFileTree(basePath, new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						Files.delete(file);
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
						Files.delete(dir);
						return FileVisitResult.CONTINUE;
					}
				});
			}
			done = true;
		}
	}

	public Path getBasePath() {
		return basePath;
	}

}
