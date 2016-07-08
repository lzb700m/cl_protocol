package utilities;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;

import cl.Prj1Config;

/**
 * Configuration file parser: parse information from configuration file, ignore
 * all comments; build structured information for easier extraction; build
 * spanning tree for converge-cast snapshots
 * 
 * @author LiP
 *
 */

public class ConfigParser {

	private String path;
	private int systemSize;
	private int minPerActive;
	private int maxPerActive;
	private int minSendDelay;
	private int snapShotDelay;
	private int maxNumber;
	private String identifier; // configuration file name
	private int[] parents;

	Map<Integer, String> hostMap;
	Map<Integer, Integer> portMap;
	Map<Integer, List<Integer>> neighborMap;

	public ConfigParser(String filePath) throws FileNotFoundException {
		this.path = filePath;
		hostMap = new HashMap<>();
		portMap = new HashMap<>();
		neighborMap = new HashMap<>();
		parse();
	}

	private void parse() throws FileNotFoundException {
		StringBuilder withoutComment = new StringBuilder();
		Scanner sc = new Scanner(new File(path));

		while (sc.hasNext()) {
			String line = sc.nextLine();
			if (line.indexOf(Prj1Config.CONFIGURATION_COMMENT) != -1) {
				line = line.substring(0,
						line.indexOf(Prj1Config.CONFIGURATION_COMMENT));
			}
			line = line.trim();
			if (line != null && line.length() != 0) {
				withoutComment.append(line + "\n");
			}
		}
		sc.close();
		sc = new Scanner(withoutComment.toString());
		String[] global = sc.nextLine().split("\\s+");
		systemSize = Integer.parseInt(global[0]);
		minPerActive = Integer.parseInt(global[1]);
		maxPerActive = Integer.parseInt(global[2]);
		minSendDelay = Integer.parseInt(global[3]);
		snapShotDelay = Integer.parseInt(global[4]);
		maxNumber = Integer.parseInt(global[5]);

		int[] lineToNode = new int[systemSize];
		for (int i = 0; i < systemSize; i++) {
			String[] line = sc.nextLine().split("\\s+");
			lineToNode[i] = Integer.parseInt(line[0]);
			hostMap.put(lineToNode[i], line[1] + Prj1Config.HOST_SUFFIX);
			portMap.put(lineToNode[i], Integer.parseInt(line[2]));
		}

		for (int i = 0; i < systemSize; i++) {
			String[] line = sc.nextLine().split("\\s+");
			List<Integer> neighbors = new ArrayList<>();
			for (String s : line) {
				neighbors.add(Integer.parseInt(s));
			}
			neighborMap.put(lineToNode[i], neighbors);
		}
		sc.close();

		// extract configuration filename as identifier for output files
		identifier = "";
		int lastSlashIndex = path.lastIndexOf("/");
		int dotIndex = path.indexOf(".", lastSlashIndex + 1);
		if (dotIndex != -1) {
			identifier += path.substring(lastSlashIndex + 1, dotIndex);
		} else {
			identifier += path.substring(lastSlashIndex + 1);
		}

		spanningTreeGen(Prj1Config.SNAPSHOT_INIT_BY, neighborMap);
	}

	/*
	 * generate spanning tree using breadth first search
	 */
	private void spanningTreeGen(int root, Map<Integer, List<Integer>> graph) {
		Set<Integer> visited = new HashSet<>();
		Queue<Integer> queue = new LinkedList<>();
		parents = new int[systemSize];

		parents[root] = root;
		queue.offer(root);
		visited.add(root);

		while (!queue.isEmpty()) {
			int cur = queue.poll();
			List<Integer> children = graph.get(cur);
			for (int child : children) {
				if (!visited.contains(child)) {
					parents[child] = cur;
					queue.offer(child);
					visited.add(child);
				}
			}
		}
	}

	public int getSystemSize() {
		return systemSize;
	}

	public int getMinPerActive() {
		return minPerActive;
	}

	public int getMaxPerActive() {
		return maxPerActive;
	}

	public int getMinSendDelay() {
		return minSendDelay;
	}

	public int getSnapShotDelay() {
		return snapShotDelay;
	}

	public int getMaxNumber() {
		return maxNumber;
	}

	public Map<Integer, String> getHostMap() {
		return hostMap;
	}

	public Map<Integer, Integer> getPortMap() {
		return portMap;
	}

	public Map<Integer, List<Integer>> getNeighborMap() {
		return neighborMap;
	}

	public int[] getParents() {
		return parents;
	}

	public String getIdentifier() {
		return identifier;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(systemSize + " ");
		sb.append(minPerActive + " ");
		sb.append(maxPerActive + " ");
		sb.append(minSendDelay + " ");
		sb.append(snapShotDelay + " ");
		sb.append(maxNumber + "\n");
		sb.append(hostMap + "\n");
		sb.append(portMap + "\n");
		sb.append(neighborMap + "\n");
		sb.append(Arrays.toString(parents));

		return sb.toString();
	}
}