package utilities;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import message.Message;
import cl.Prj1Config;

import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;

public class InputChannelManager implements Runnable {
	private final BlockingQueue<String> logBuffer;
	private final int nodeId;
	private final Map<Integer, String> hostMap;
	private final Map<Integer, Integer> portMap;
	private final List<Integer> neighbors;
	private volatile boolean initialized;
	private volatile boolean stopped;

	private final Map<Integer, SctpChannel> channels;
	private final BlockingQueue<Message> queue;

	public InputChannelManager(BlockingQueue<String> logBuffer, int id,
			Map<Integer, String> hosts, Map<Integer, Integer> ports,
			List<Integer> neighborList, BlockingQueue<Message> queue) {
		this.logBuffer = logBuffer;
		this.nodeId = id;
		this.hostMap = hosts;
		this.portMap = ports;
		this.neighbors = neighborList;
		this.initialized = false;
		this.stopped = false;
		this.channels = new HashMap<>();
		this.queue = queue;
	}

	public void close() {
		stopped = true;
	}

	@Override
	public void run() {
		while (!initialized) {
			// initiate SCTP connection
			// send handshake information
			setupChannel();
		}
		System.out.println("Node " + nodeId + " input channel established.");

		while (!stopped) {
			// pull data from all channels
			// put them in the queue
			retrieve();
		}

		// clean up
		for (int neighbor : neighbors) {
			try {
				log("[Node-" + nodeId + " Thread-"
						+ Thread.currentThread().getName()
						+ "] closing input communication channel with Node-"
						+ neighbor);
				channels.get(neighbor).close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private void setupChannel() {
		for (int neighborId : neighbors) {
			boolean connected = false;
			while (!connected) {
				SocketAddress serverAddress = new InetSocketAddress(
						hostMap.get(neighborId), portMap.get(neighborId));
				SctpChannel sctpChannel = null;
				try {
					sctpChannel = SctpChannel.open();
					sctpChannel.bind(new InetSocketAddress(neighborId
							+ Prj1Config.CHANNEL_BASE));
					sctpChannel.connect(serverAddress);
					sctpChannel.configureBlocking(false);
					channels.put(neighborId, sctpChannel);
					connected = true;

					log("[Node-" + nodeId + " Thread-"
							+ Thread.currentThread().getName()
							+ "] incomming channel from Node-" + neighborId
							+ " established");
				} catch (IOException ex) {
					log("[Node-" + nodeId + " Thread-"
							+ Thread.currentThread().getName()
							+ "] failed connecting to Node-" + neighborId
							+ ", retry...");
					try {
						sctpChannel.close();
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		handshake();
		log("[Node-" + nodeId + " Thread-" + Thread.currentThread().getName()
				+ "] all incomming channels initialized");
		initialized = true;
	}

	private void handshake() {
		for (int neighborId : neighbors) {
			log("[Node-" + nodeId + " Thread-"
					+ Thread.currentThread().getName()
					+ "] sending handshake message to Node-" + neighborId);
			ByteBuffer bBuffer = ByteBuffer
					.allocate(Prj1Config.MESSAGE_BUFFER_SIZE);
			MessageInfo mInfo = MessageInfo.createOutgoing(null, 0);
			bBuffer.put((Integer.toString(nodeId).getBytes()));
			bBuffer.flip();
			try {
				channels.get(neighborId).send(bBuffer, mInfo);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private Message byteToMessage(ByteBuffer byteBuffer) throws IOException,
			ClassNotFoundException {
		byteBuffer.position(0);
		byteBuffer.limit(Prj1Config.MESSAGE_BUFFER_SIZE);
		byte[] bufArr = new byte[byteBuffer.remaining()];
		byteBuffer.get(bufArr);
		ByteArrayInputStream in = new ByteArrayInputStream(bufArr);
		ObjectInputStream is = new ObjectInputStream(in);
		return (Message) is.readObject();
	}

	private void retrieve() {
		for (int origin : neighbors) {
			ByteBuffer bBuffer = ByteBuffer
					.allocate(Prj1Config.MESSAGE_BUFFER_SIZE);
			MessageInfo mInfo = null;
			try {
				mInfo = channels.get(origin).receive(bBuffer, null, null);
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			if (mInfo != null && mInfo.isComplete()) {
				Message message = null;
				try {
					message = byteToMessage(bBuffer);
				} catch (ClassNotFoundException | IOException ex) {
					ex.printStackTrace();
					continue;
				}

				queue.offer(message);
				log("[Node-" + nodeId + " Thread-"
						+ Thread.currentThread().getName()
						+ "] received message [" + message + "] to Node-"
						+ origin);
			}
		}
	}

	private void log(String log) {
		try {
			logBuffer.put(Prj1Config.SDF.format(System.currentTimeMillis())
					+ "  " + log + System.lineSeparator());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}