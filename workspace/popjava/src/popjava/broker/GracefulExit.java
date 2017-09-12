package popjava.broker;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import popjava.util.LogWriter;

/**
 * Broker cleanup after itself on exit
 *
 * @author Davide Mazzoleni
 */
public class GracefulExit extends Thread {

	private final Path origin;
	private final Path basePath;

	public GracefulExit(Path origin, Path basePath) {
		Objects.requireNonNull(origin);
		Objects.requireNonNull(basePath);
		
		this.origin = origin;
		this.basePath = basePath;
	}

	@Override
	public void run() {
		try {
			cleanup();
		} catch (IOException e) {
			LogWriter.writeDebugInfo("[Broker] A problem occurred when cleaning up: %s", e.getMessage());
		}
	}

	private void cleanup() throws IOException {
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
	}
}
