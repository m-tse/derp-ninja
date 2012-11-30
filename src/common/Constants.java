package common;
/*
 * This class contains the global constants used in DFS
 */

public class Constants {

	public static final int NUM_OF_BLOCKS = 16384; // 2^14
	public static final int BLOCK_SIZE = 1024; // 1kB
	public static final int MAX_NUM_FILES = 512;
	public static final int INODE_SIZE = 256;

	/* DStore Operation types */
	public enum DiskOperationType {
		READ, WRITE
	};

	/* Virtual disk file/store name */
	public static final String vdiskName = "DSTORE.dat";
}
