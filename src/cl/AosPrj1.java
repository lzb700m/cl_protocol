package cl;

import java.io.FileNotFoundException;
import java.io.IOException;

import utilities.ConfigParser;

public class AosPrj1 {

	/**
	 * The main program takes exactly 2 arguments
	 * 
	 * @param args
	 *            : args[0] - node id for the machine running this instance;
	 *            args[1] - path of the configuration file
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws IOException,
			InterruptedException {
		if (args.length < 2) {
			System.out.println("Not enough arguments. Program terminated.");
			System.exit(1);
		}

		int nodeId = Prj1Config.SNAPSHOT_INIT_BY;
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

		NetworkNode node = new NetworkNode(nodeId, identifier,
				cp.getSystemSize(), cp.getMinPerActive(), cp.getMaxPerActive(),
				cp.getMinSendDelay(), cp.getSnapShotDelay(), cp.getMaxNumber(),
				cp.getHostMap(), cp.getPortMap(), cp.getNeighborMap(), parent);
		Thread nodeThread = new Thread(node, "NetworkNode");
		nodeThread.start();
		nodeThread.join();

		System.out.println("Node " + nodeId + " halts.");
		System.exit(0);
	}
}
