package common;
/*
 * This class contains the global constants used in DFS
 */

public class Constants {

	public static final int NUM_OF_BLOCKS = 16384; // 2^14
	public static final int BLOCK_SIZE = 1024; // 1kB

	/* BEGIN: For TESTING purposes */

	/* Our constants */
	public static final int MAX_NUM_FILES = 32; // This many inodes
	public static final int MAX_FILE_BLOCK_SIZE = 62; //Max num of blocks per file
	public static final int INODE_SIZE = 256;
	public static final int INODE_REGION_SIZE_BYTES = INODE_SIZE * MAX_NUM_FILES;
	public static final int INODE_REGION_SIZE_BLOCKS = INODE_REGION_SIZE_BYTES / BLOCK_SIZE;
	public static final int INODES_PER_BLOCK = BLOCK_SIZE/INODE_SIZE; 
	public static final int BLOCK_OFFSET = INODE_REGION_SIZE_BLOCKS + 1;

	/* END: For TESTING purposes */


	/* DStore Operation types */
	public enum DiskOperationType {
		READ, WRITE
	};

	/* Virtual disk file/store name */
	public static final String vdiskName = "DSTORE.dat";



}