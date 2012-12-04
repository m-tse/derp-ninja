package dfs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import virtualdisk.MyVirtualDisk;

import common.Constants;
import common.DFileID;
import common.INode;

import dblockcache.DBuffer;
import dblockcache.MyDBuffer;
import dblockcache.MyDBufferCache;

public class MyDFS extends DFS {

	// TODO: Use different structure for fileIDs
	// TODO: Surpass max file size
	// TODO: Update inode size

	private static MyDFS myInstance;

	public static MyDFS getInstance() {
		if (myInstance == null) {
			myInstance = new MyDFS();
		}
		return myInstance;
	}

	MyDBufferCache bufferCache;
	DFileID[] fileIDs;
	INode[] iNodes;
	int[] freeMap;

	private MyDFS(String volName, boolean format) {
		super(volName, format);
		bufferCache = MyDBufferCache.getInstance();
		fileIDs = new DFileID[Constants.MAX_NUM_FILES];
		iNodes = new INode[Constants.MAX_NUM_FILES];
		freeMap = new int[Constants.NUM_OF_BLOCKS - Constants.BLOCK_OFFSET]; // Block
																				// 0,
																				// 1-8
																				// reserved
		if (format)
			format();
		else
			this.readINodeRegion();
	}

	private MyDFS(boolean format) {
		this(Constants.vdiskName, format);
	}

	private MyDFS() {
		this(Constants.vdiskName, false);
	}

	private void readINodeRegion() {
		int fileID = 0;
		for (int i = 0; i < iNodes.length / Constants.INODES_PER_BLOCK; ++i) {
			MyDBuffer dbuf = (MyDBuffer) bufferCache.getBlock(i + 1); // i+1
																		// because
																		// block
																		// 0
																		// reserved
			if (!dbuf.checkValid()) {
				dbuf.startFetch();
				dbuf.waitValid();
			}
			// System.out.println("MyDFS.readRegion() GETTING BLOCK " + (i+1));
			if (!dbuf.checkValid()) {
				dbuf.startFetch();
				dbuf.waitValid();
			}
			byte[] bufferBytes = dbuf.getBuffer();
			for (int k = 0; k < Constants.INODES_PER_BLOCK; ++k, ++fileID) {
				INode iNode = new INode(Arrays.copyOfRange(bufferBytes, k
						* Constants.INODE_SIZE, (k + 1) * Constants.INODE_SIZE));
				if (iNode.isNotUsed()) {
					// System.out.println("MyDFS.readINodeRegion(): NOOOO");
					continue;
				} else {
					// System.out.println("MyDFS.readINodeRegion(): YESSS");
					// System.out.println("- Got iNode for fileID: " + fileID);
					// Convert bytes into iNode information
					iNodes[fileID] = iNode;
					fileIDs[fileID] = new DFileID(fileID);
					for (int n : iNode.getBlockArray()) {
						if (n == 0)
							continue;
						freeMap[n - Constants.BLOCK_OFFSET] = 1; // Not free,
																	// -offset
																	// account
																	// for block
																	// 0 and
																	// inode
																	// region
					}
				}
			}
		}
	}

	@Override
	public boolean format() {
		// System.out.println("FORMATING IN DFS");
		bufferCache = MyDBufferCache.getInstance();
		fileIDs = new DFileID[Constants.MAX_NUM_FILES];
		iNodes = new INode[Constants.MAX_NUM_FILES];
		freeMap = new int[Constants.NUM_OF_BLOCKS - Constants.BLOCK_OFFSET]; // Block
																				// 0,
																				// 1-8
																				// reserved
		bufferCache.flush();

		return MyVirtualDisk.format();
	}

	public boolean fileExists(DFileID dfid) {
		return fileIDs[dfid.getInt()] != null ? true : false;
	}

	@Override
	public DFileID createDFile() {
		/*
		 * for (int i = 0; i < fileIDs.length; ++i) { if (fileIDs[i]!= null)
		 * System.out.println(fileIDs[i].toString()); }
		 */
		// Simple implementation for now: just file closest available ID
		for (int i = 0; i < fileIDs.length; ++i) {
			if (fileIDs[i] == null) {
				fileIDs[i] = new DFileID(i);
				System.out.println("MyDFS.createFile() CREATING FILE: " + i);
				// Create INode
				INode iNode = new INode(fileIDs[i]);
				iNodes[i] = iNode;
				DBuffer dbuf = bufferCache.getBlock(i
						/ Constants.INODES_PER_BLOCK + 1);
				if (!dbuf.checkValid()) {
					dbuf.startFetch();
					dbuf.waitValid();
				}
				// writeToBuffer(iNode, dbuf);
				return fileIDs[i];
			}
		}
		System.err.println("RETURNING A NULL FILE ID!");
		return null;
	}

	@Override
	public void destroyDFile(DFileID dFID) {
		for (int i = 0; i < fileIDs.length; ++i) {
			if (fileIDs[i] == dFID) {
				fileIDs[i] = null;
				INode iNode = iNodes[i];
				iNode.clearINode();
				DBuffer dbuf = bufferCache.getBlock(i
						% Constants.INODES_PER_BLOCK);
				writeToBuffer(iNode, dbuf);
				iNodes[i] = null;
			}
		}
	}

	public int getNextFreeBlock() {
		for (int i = 0; i < freeMap.length; ++i) {
			if (freeMap[i] == 0) {
				freeMap[i] = 1;
				return i + Constants.BLOCK_OFFSET; // includes block 0 and inode
													// region
			}
		}
		return 0;
	}

	@Override
	public int read(DFileID dFID, byte[] buffer, int startOffset, int count) {


		int currentIndexInReadToBuffer = startOffset;
		INode currentINode = iNodes[dFID.getInt()];
		if (currentINode == null) {
			System.out.println("MyDFS.read(): FILE DOES NOT EXIST");
			return 0;
		}
		int iNodeBlockIndex = 0;

		while (count > 0) {
			System.out.println("block index just increasd while reading");
			if (iNodeBlockIndex == currentINode.blockIDs.length) { // at the
																	// very last
																	// block of
																	// this
																	// iNode, go
																	// to a new
																	// iNode,
																	// and reset
																	// the
																	// indexes
				System.out.println("READING FROM LINKED INODE");
				currentINode = currentINode.getNextINode();
				if (currentINode == null) {
					System.err.println("BADLY WRITTEN FILE, CANNOT READ");
					return currentIndexInReadToBuffer-startOffset;
				}
				iNodeBlockIndex = 0;
			}

			int bytesToBeRead = count;
			if (bytesToBeRead > Constants.MAX_FILE_BLOCK_SIZE)
				bytesToBeRead = Constants.MAX_FILE_BLOCK_SIZE;

			DBuffer dbuf = bufferCache
					.getBlock(currentINode.blockIDs[iNodeBlockIndex]);
			if (!dbuf.checkValid()) {
				dbuf.startFetch();
				dbuf.waitValid();
			}
			int bytesRead = dbuf.read(buffer, currentIndexInReadToBuffer, bytesToBeRead);
			if (bytesRead == -1)
				System.err.println("MyDFS.read() READ FAILED");
			currentIndexInReadToBuffer += bytesRead;
			iNodeBlockIndex++;
			count -= bytesRead;

			bufferCache.releaseBlock(dbuf);
			// System.out.println("MyDFS.write() PUSHED: " + dbuf.getBlockID());

		}
		System.out.println(new String(buffer));
		// System.out.println("MyDFS.read(): " + totalBytesRead);
		return currentIndexInReadToBuffer-startOffset;
	}

	@Override
	public int write(DFileID dfid, byte[] buffer, int startOffset, int count) {
		int totalBytesWritten = 0;

		int currentIndex = startOffset;
		INode currentINode = iNodes[dfid.getInt()];
		int iNodeBlockIndex = 0;

		if (currentINode == null) {
			System.out.println("MyDFS.write() FILE DOES NOT EXIST");
			return 0;
		}

		while (count > 0) {
			System.out.println("blockINDEX just increased while writing");

			if (iNodeBlockIndex == currentINode.blockIDs.length) { // at the
																	// very last
																	// block of
																	// this
																	// iNode, go
																	// to a new
																	// iNode,
																	// and reset
																	// the
																	// indexes
				System.out.println("LINKING TO A NEW INODE");
				currentINode = currentINode.getCreateNextINode();
				iNodeBlockIndex = 0;
			}

			if (currentINode.blockIDs[iNodeBlockIndex] == 0) { // currentBlock
																// has no
																// associated
																// blockID yet
				int newFreeBlock = getNextFreeBlock();
				if (newFreeBlock == 0)
					System.err
							.println("MyDFS.write() NO MORE BLOCKS AVAILABLE");
				currentINode.blockIDs[iNodeBlockIndex] = getNextFreeBlock();
			}

			int bytesToBeWritten = count;
			if (bytesToBeWritten > Constants.MAX_FILE_BLOCK_SIZE)
				bytesToBeWritten = Constants.MAX_FILE_BLOCK_SIZE;

			DBuffer dbuf = bufferCache
					.getBlock(currentINode.blockIDs[iNodeBlockIndex]);

			int bytesWritten = dbuf.write(buffer, currentIndex,
					bytesToBeWritten);
			if (bytesWritten == -1)
				System.err.println("MyDFS.write() WRITE FAILED");

			iNodeBlockIndex++;
			count -= bytesWritten;
			totalBytesWritten += bytesWritten;
			currentINode.increaseFileSize(bytesWritten); // note write a new
															// filesize function
															// for INode taking
															// into account
															// linked list
			dbuf.startPush();
			dbuf.waitClean();
			bufferCache.releaseBlock(dbuf);
			// System.out.println("MyDFS.write() PUSHED: " + dbuf.getBlockID());

		}
		// System.out.println("MyDFS.write() wrote num bytes: "
		// + totalBytesWritten);

		return totalBytesWritten;
	}

	@Override
	public int sizeDFile(DFileID dFID) {
		INode iNode = iNodes[dFID.getInt()];
		return iNode.getFileSize();
	}

	@Override
	public List<DFileID> listAllDFiles() {
		List<DFileID> dFiles = new ArrayList<DFileID>();
		for (INode iNode : iNodes) {
			if (iNode != null) {
				dFiles.add(iNode.getDFileID());
			}
		}
		return dFiles;
	}

	public void sync() {
		bufferCache.sync();
	}

	@Override
	protected void finalize() {
		this.sync();
		try {
			super.finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// INode functions
	public void writeToBuffer(INode inode, DBuffer dbuf) {
		byte[] iNodeBytes = inode.getBytes();
		byte[] bufferBytes = dbuf.getBuffer();
		int iNodeOffset = Constants.INODE_SIZE
				* (inode.getDFileID().getInt() % Constants.INODES_PER_BLOCK);
		for (int i = 0; i < iNodeBytes.length; ++i) {
			bufferBytes[i + iNodeOffset] = iNodeBytes[i];
		}
		try {
			dbuf.write(bufferBytes, 0, bufferBytes.length);
			dbuf.startPush();
			dbuf.waitClean();
			System.out.println("INode.writeToDisk(): " + iNodeOffset);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

	}

}
