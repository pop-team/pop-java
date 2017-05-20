package popjava.util;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;

/**
 * Watch a Directory for changes
 *
 * @see http://www.rgagnon.com/javadetails/java-detect-file-modification-event.html
 */
public class WatchDirectory implements Runnable {

	public static abstract class WatchMethod {
		public void create(String s) {}
		public void delete(String s) {}
		public void modify(String s) {}
	}
	
	private Path myDir;
	private WatchService watcher;
	private WatchMethod method;
	
	private boolean running = true;
	
	public WatchDirectory(String path, WatchMethod method) {
		try {
			myDir = Paths.get(path);
			watcher = myDir.getFileSystem().newWatchService();
			myDir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);
			this.method = method;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		while (running) {
			try {
				WatchKey watchKey = watcher.take();
				List<WatchEvent<?>> events = watchKey.pollEvents();
				for (WatchEvent<?> event : events) {
					if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
						method.create(event.context().toString());
					}
					else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
						method.delete(event.context().toString());
					}
					else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
						method.modify(event.context().toString());
					}
				}
				watchKey.reset();
			} catch (Exception e) {
				LogWriter.writeDebugInfo("[WatchDirectory] Error: " + e.toString());
			}
		}
	}
}
