package cl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

import message.Message;
import utilities.ConcurrentFileWriter;
import utilities.InputChannelManager;
import utilities.OutputChannelManager;

public class NetworkNode implements Runnable {

	private final int nodeId;
	// global parameters
	private final String identifier;
	private final int systemSize;
	private final int minPerActive;
	private final int maxPerActive;
	private final int minSendDelay;
	private final int snapShotDelay;
	private final int maxNumber;
	private final Map<Integer, String> hostMap;
	private final Map<Integer, Integer> portMap;

	// converge cast to
	private final int parent;

	private final List<Integer> neighbors;
	int[] vectorClock;

	final BlockingQueue<Message> inChannelBuffer = new LinkedBlockingQueue<>();;
	final BlockingQueue<Message> outChannelBuffer = new LinkedBlockingQueue<>();;

	final BlockingQueue<String> logBuffer = new LinkedBlockingQueue<>();
	final BlockingQueue<String> outputBuffer = new LinkedBlockingQueue<>();

	// private final Map<Integer, SnapshotMessage> snapshots;
	// private final Map<Integer, Set<Integer>> receivedSnapShotsReq;
	// private final BlockingQueue<SnapshotMessage> gatheredSnapshotMessages;
	private boolean isHalt = false;

	public NetworkNode(int nodeId, String configFileName, int systemSize,
			int minPerActive, int maxPerActive, int minSendDelay,
			int snapShotDelay, int maxNumber, Map<Integer, String> hostMap,
			Map<Integer, Integer> portMap,
			Map<Integer, List<Integer>> neighborMap, int parentVal) {

		this.nodeId = nodeId;

		this.identifier = configFileName;
		this.systemSize = systemSize;
		this.minPerActive = minPerActive;
		this.maxPerActive = maxPerActive;
		this.minSendDelay = minSendDelay;
		this.snapShotDelay = snapShotDelay;
		this.maxNumber = maxNumber;
		this.hostMap = hostMap;
		this.portMap = portMap;

		this.parent = parentVal;

		this.neighbors = neighborMap.get(nodeId);
		vectorClock = new int[systemSize];

		isHalt = false;

		log("[Node-" + nodeId + " Thread-" + Thread.currentThread().getName()
				+ "] network node is setup");
		log(this.toString());
	}

	@Override
	public void run() {
		// setup file writer
		ConcurrentFileWriter logger = null;
		ConcurrentFileWriter outputWriter = null;
		try {
			logger = new ConcurrentFileWriter(new File(Prj1Config.ROOT_FOLDER
					+ "logs/log-" + identifier + "-" + nodeId + ".txt"),
					logBuffer);
			outputWriter = new ConcurrentFileWriter(new File(
					Prj1Config.ROOT_FOLDER + "outputs/" + identifier + "-"
							+ nodeId + ".txt"), outputBuffer);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// starts file writer thread
		Thread loggerThread = new Thread(logger, "Logger");
		Thread outputWriterThread = new Thread(outputWriter, "OutputWriter");
		loggerThread.start();
		outputWriterThread.start();

		// setup communication channel
		OutputChannelManager out = new OutputChannelManager(logBuffer, nodeId,
				portMap, neighbors, inChannelBuffer);
		InputChannelManager in = new InputChannelManager(outputBuffer, nodeId,
				hostMap, portMap, neighbors, outChannelBuffer);

		// start communication manager thread
		Thread outThread = new Thread(out, "OutChannels");
		Thread inThread = new Thread(in, "InChannels");
		outThread.start();
		inThread.start();

		// do the work
		// while (!isHalt) {
		//
		// }

		try {
			outThread.join();
			inThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		logger.close();
		outputWriter.close();

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("NODE:" + nodeId + " [");
		sb.append("NEIGHBORS:" + neighbors + ", ");
		sb.append("GLOBAL:" + systemSize + " ");
		sb.append(minPerActive + " ");
		sb.append(maxPerActive + " ");
		sb.append(minSendDelay + " ");
		sb.append(snapShotDelay + " ");
		sb.append(maxNumber + ", ");
		sb.append("ALLHOST:" + hostMap + ", ");
		sb.append("LISTEN_POST:" + portMap + "]");

		return sb.toString();
	}

	private void appendln(String content) {
		try {
			outputBuffer.put(content + System.lineSeparator());
		} catch (InterruptedException e) {
			e.printStackTrace();
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

	private void send(Message msg) {
		try {
			outChannelBuffer.put(msg);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private Message read() {
		Message msg = inChannelBuffer.poll();
		return msg;
	}

	private int rand(int min, int max) {
		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}
}
