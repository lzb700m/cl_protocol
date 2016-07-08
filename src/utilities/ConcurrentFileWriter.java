package utilities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Concurrent class for writing files
 * 
 * @author LiP
 *
 */
public class ConcurrentFileWriter implements Runnable {
	private final File file;
	private final Writer out;
	private final BlockingQueue<String> queue;
	private volatile boolean stopped;

	public ConcurrentFileWriter(File file, BlockingQueue<String> queue)
			throws IOException {
		this.file = file;
		this.out = new BufferedWriter(new FileWriter(this.file));
		this.queue = queue;
		this.stopped = false;
	}

	public void close() {
		stopped = true;
	}

	@Override
	public void run() {
		while (!stopped) {
			try {
				String item = queue.poll(100, TimeUnit.MICROSECONDS);
				if (item != null) {
					try {
						out.write(item);
					} catch (IOException ex) {
						System.out.println("Thread-"
								+ Thread.currentThread().getName()
								+ " Error writing file.");
					}
				}
			} catch (InterruptedException ex) {
				System.out.println("Thread-" + Thread.currentThread().getName()
						+ " is interrupted.");
				Thread.currentThread().interrupt();
			}
		}

		while (!queue.isEmpty()) {
			try {
				out.write(queue.poll());
			} catch (IOException ex) {
				System.out.println("Thread-" + Thread.currentThread().getName()
						+ " Error writing file.");
			}
		}
		try {
			out.close();
		} catch (IOException ex) {
			System.out.println("Thread-" + Thread.currentThread().getName()
					+ " Error closing BufferedWriter.");
			Thread.currentThread().interrupt();
		}
	}
}
