package virtualdisk;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import common.Constants;
import common.Constants.DiskOperationType;

import dblockcache.DBuffer;

public class MyVirtualDisk extends VirtualDisk {
	
	/*
	 * Singleton methods
	 */
	private static MyVirtualDisk myInstance;
	
	public static MyVirtualDisk getInstance() throws FileNotFoundException, IOException {
		if (myInstance == null) {
			myInstance = new MyVirtualDisk();
		}
		return myInstance;
	}
	
	
	/*
	 * MyVirtualDisk Constructors
	 */
	private MyVirtualDisk(String volName, boolean format) throws FileNotFoundException,
			IOException {
		super(volName, format);
	}
	
	private MyVirtualDisk(boolean format) throws FileNotFoundException,
	IOException {
		super(format);
	}
	
	private MyVirtualDisk() throws FileNotFoundException, IOException {
		super();
	}
	
	@Override
	public void startRequest(DBuffer buf, DiskOperationType operation)
			throws IllegalArgumentException, IOException {
		if (Constants.DiskOperationType.READ == operation) {
			this.readBlock(buf);
		} else if (Constants.DiskOperationType.WRITE == operation) {
			this.writeBlock(buf);
		} else {
			// SHOULD NOT GET HERE
			// Only something other than READ/WRITE
		}
	}


}