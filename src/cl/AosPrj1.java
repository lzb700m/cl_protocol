package cl;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import utilities.ConfigParser;

public class AosPrj1 {
	static final Set<Integer> ACTIVE_NODES = new HashSet<>(Arrays.asList(0));
	static final SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSS");

	/**
	 * The main program takes exactly 2 arguments
	 * 
	 * @param args
	 *            : args[0] - node id; args[1] - path of the configuration file
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Not enough arguments. Program terminated.");
			System.exit(1);
		}

		int nodeId = ConfigParser.SNAPSHOT_INIT_BY;
		try {
			nodeId = Integer.parseInt(args[0]);
		} catch (NumberFormatException ex) {
			System.out
					.println("Invalid argument, node ID must be an integer. Program terminated.");
			System.exit(1);
		}

		String configFilePath = args[1];
		ConfigParser cp = null;
		try {
			cp = new ConfigParser(configFilePath);
		} catch (FileNotFoundException ex) {
			System.out.println("Invalid argument, configuration file "
					+ configFilePath + " not found. Program terminated.");
			System.exit(1);
		}

		String identifier = cp.getIdentifier();
		int parent = cp.getParents()[nodeId];

		System.out.println(cp);
		System.out.println(identifier);
		System.out.println(parent);

		// ConcurrentFileWriter logger = null;
		// ConcurrentFileWriter outputWriter = null;
		//
		// try {
		// File logFile = new File("logs/log-" + identifier + "-" + nodeId
		// + ".txt");
		// File outputFile = new File("outputs/" + identifier + "-" + nodeId
		// + ".txt");
		// logger = new ConcurrentFileWriter(logFile);
		// outputWriter = new ConcurrentFileWriter(outputFile);
		// } catch (IOException ex) {
		// System.out.println("Can not initiate file. Program terminated.");
		// System.exit(1);
		// }
		//

		//
		// logger.open();
		// outputWriter.open();
		//
		// logger.appendlog("[Node-" + nodeId + " Thread-"
		// + Thread.currentThread().getName() + "] starting network node");
		// NetworkNode node = new NetworkNode(logger, outputWriter, nodeId,
		// cp.getSystemSize(), cp.getMinPerActive(), cp.getMaxPerActive(),
		// cp.getMinSendDelay(), cp.getSnapShotDelay(), cp.getMaxNumber(),
		// cp.getHostMap(), cp.getPortMap(), cp.getNeighborMap(), parent);
		// Thread network = new Thread(node, "Coordinator");
		// network.start();
		//
		// try {
		// network.join();
		// } catch (InterruptedException ex) {
		// ex.printStackTrace();
		// }
		//
		// outputWriter.close();
		// logger.appendlog("[Node-" + nodeId + " Thread-"
		// + Thread.currentThread().getName() + "]: system halts.");
		// logger.close();

		System.exit(0);
	}
}
