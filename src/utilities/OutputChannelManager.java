package utilities;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import message.Message;
import cl.Prj1Config;

import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;
import com.sun.nio.sctp.SctpServerChannel;

public class OutputChannelManager implements Runnable {
	final BlockingQueue<String> logBuffer;
	private final int nodeId;
	private final Map<Integer, Integer> portMap;
	private final List<Integer> neighbors;
	private volatile boolean initialized;
	private volatile boolean stopped;

	private final Map<Integer, SctpChannel> channels;
	private final BlockingQueue<Message> queue;

	public OutputChannelManager(BlockingQueue<String> logBuffer, int id,
			Map<Integer, Integer> ports, List<Integer> neighborList,
			BlockingQueue<Message> queue) {
		this.logBuffer = logBuffer;
		this.nodeId = id;
		this.portMap = ports;
		this.neighbors = neighborList;
		this.initialized = false;
		this.stopped = false;
		channels = new HashMap<>();

		this.queue = queue;
	}

	public void close() {
		stopped = true;
	}

	@Override
	public void run() {
		while (!initialized) {
			// initialize SCTP connection
			// identify neighbors
			setupChannel();
		}
		System.out.println("Node " + nodeId + " output channel established.");

		while (!stopped) {
			// pull message from queue
			// send them into SCTP channels
			deliver();
		}

		// clean up
		for (int neighbor : channels.keySet()) {
			try {
				log("[Node-" + nodeId + " Thread-"
						+ Thread.currentThread().getName()
						+ "] closing output communication channel with Node-"
						+ neighbor);
				channels.get(neighbor).close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private void setupChannel() {
		Set<SctpChannel> outChannels = new HashSet<>();

		try {
			SctpServerChannel sctpServerChannel = SctpServerChannel.open();
			SocketAddress serverAddress = new InetSocketAddress(
					portMap.get(nodeId));
			sctpServerChannel.bind(serverAddress);
			while (outChannels.size() < neighbors.size()) {
				SctpChannel channel = sctpServerChannel.accept(); // block
				channel.configureBlocking(false); // this line of code is
													// IMPORTANT
				outChannels.add(channel);
			}
			sctpServerChannel.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		log("[Node-" + nodeId + " Thread-" + Thread.currentThread().getName()
				+ "] outgoing communication channels initialized");

		handshake(outChannels); // identify neighbor id
		initialized = true;
	}

	private void handshake(Set<SctpChannel> establishedChannels) {
		for (SctpChannel channel : establishedChannels) {
			boolean received = false;
			while (!received) {
				ByteBuffer bBuffer = ByteBuffer
						.allocate(Prj1Config.MESSAGE_BUFFER_SIZE);
				try {
					MessageInfo mInfo = channel.receive(bBuffer, null, null);
					if (mInfo != null && mInfo.isComplete()) {
						String message = byteToString(bBuffer);
						int neighborId = Integer.parseInt(message.trim());
						channels.put(neighborId, channel);
						received = true;
						log("[Node-" + nodeId + " Thread-"
								+ Thread.currentThread().getName()
								+ "] received handshake message from Node "
								+ neighborId);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		log("[Node-" + nodeId + " Thread-" + Thread.currentThread().getName()
				+ "] all outgoing channels identified." + channels.toString());
	}

	private String byteToString(ByteBuffer byteBuffer) {
		byteBuffer.position(0);
		byteBuffer.limit(Prj1Config.MESSAGE_BUFFER_SIZE);
		byte[] bufArr = new byte[byteBuffer.remaining()];
		byteBuffer.get(bufArr);
		return new String(bufArr);
	}

	private void deliver() {
		Message message = null;
		try {
			message = queue.poll(100, TimeUnit.MICROSECONDS);
			if (message != null) {
				ByteBuffer bBuffer = ByteBuffer
						.allocate(Prj1Config.MESSAGE_BUFFER_SIZE);
				MessageInfo mInfo = MessageInfo.createOutgoing(null, 0);
				bBuffer.put(message.getBytes());
				bBuffer.flip();
				try {
					channels.get(message.getDest()).send(bBuffer, mInfo);
					log("[Node-" + nodeId + " Thread-"
							+ Thread.currentThread().getName()
							+ "] sent message [" + message + "] to Node-"
							+ message.getDest());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException exIO) {
			exIO.printStackTrace();
		} catch (InterruptedException exIT) {
			exIT.printStackTrace();
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
