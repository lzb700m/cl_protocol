package cl;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Prj1Config {
	public static final String ROOT_FOLDER = "./workspace/cl_protocol/src/";
	public static final SimpleDateFormat SDF = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.SSS");
	public static final String CONFIGURATION_COMMENT = "#";
	public static final String HOST_SUFFIX = ".utdallas.edu";
	public static final int CHANNEL_BASE = 40000;
	public static final int MESSAGE_BUFFER_SIZE = 1000;
	public static final Set<Integer> ACTIVE_NODES = new HashSet<>(
			Arrays.asList(0));
	public static final int SNAPSHOT_INIT_BY = 0;
}
