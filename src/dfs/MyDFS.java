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
	
	MyDBufferCache bufferCache;
	DFileID[] fileIDs;
	INode[] iNodes;
	int[] freeMap;
	
	private static MyDFS myInstance;
	
	
	public static MyDFS getInstance() {
		if (myInstance == null) {
			myInstance = new MyDFS();
		} 
		return myInstance;
	}
	
	private MyDFS(String volName, boolean format) {
		super(volName, format);
		bufferCache = MyDBufferCache.getInstance(); 
		fileIDs = new DFileID[Constants.MAX_NUM_FILES];
		iNodes = new INode[Constants.MAX_NUM_FILES];
		freeMap = new int[Constants.NUM_OF_BLOCKS-Constants.BLOCK_OFFSET]; // Block 0, 1-8 reserved 
		if (format) format();
		this.readINodeRegion();
	}
	
	private MyDFS(boolean format) {
		this(Constants.vdiskName,format);
	}

	private MyDFS() {
		this(Constants.vdiskName,false);
	}
	
	private void readINodeRegion() {
		int fileID = 0;
		for (int i = 0; i < iNodes.length/Constants.INODES_PER_BLOCK; ++i) {
			MyDBuffer dbuf = (MyDBuffer) bufferCache.getBlock(i+1); // i+1 because block 0 reserved 
			if (!dbuf.checkValid()) {
				dbuf.startFetch();
				dbuf.waitValid();
			}
			System.out.println("MyDFS.readRegion() GETTING BLOCK " + (i+1));
			if (!dbuf.checkValid()) {
				dbuf.startFetch();
				dbuf.waitValid();
			}
			byte[] bufferBytes = dbuf.getBuffer();
			for (int k = 0; k < Constants.INODES_PER_BLOCK; ++k, ++fileID) {
				INode iNode = new INode(Arrays.copyOfRange(
						bufferBytes, k*Constants.INODE_SIZE, (k+1)*Constants.INODE_SIZE));
				if (iNode.isNotUsed()) {
					System.out.println("MyDFS.readINodeRegion(): NOOOO");
					continue;
				} else {
					System.out.println("MyDFS.readINodeRegion(): YESSS");
					System.out.println("- Got iNode for fileID: " + fileID);
					// Convert bytes into iNode information
					populateFreeMap(iNode, fileID, bufferBytes);
				}
			}
		}
	}
	
	private void populateFreeMap(INode iNode, int fileID, byte[] bufferBytes) {
		iNodes[fileID] = iNode;
		fileIDs[fileID] = new DFileID(fileID);
		int[] blocks = iNode.getBlockArray();
		for (int n = 0; n < blocks.length; ++n) {
			if (n == blocks.length-1) {
				INode nestedNode = new INode(Arrays.copyOfRange(
						bufferBytes, blocks[n], blocks[n]+Constants.INODE_SIZE));
				nestedNode.setDFileID(new DFileID(blocks[n]/Constants.INODE_SIZE));
				++fileID;
				if (fileID >= fileIDs.length) {
					return;
				}
				populateFreeMap(nestedNode, fileID, bufferBytes);
				break;
			}
			freeMap[blocks[n]] = 1; // Not free, -offset account for block 0 and inode region
		}
	}

	
	@Override
	public boolean format() {
		System.out.println("FORMATING IN DFS");
		bufferCache.flush();
		return MyVirtualDisk.format();
	}

	public boolean fileExists(DFileID dfid) {
		return fileIDs[dfid.getInt()] != null ? true: false;
	}
	
	@Override
	public DFileID createDFile() {
		// Simple implementation for now: just file closest available ID
		for (int i = 0; i < fileIDs.length; ++i) {
			if (fileIDs[i] == null) {
				fileIDs[i] = new DFileID(i);
				System.out.println("MyDFS.createFile() CREATING FILE: " + i);
				// Create INode
				INode iNode = new INode(fileIDs[i]);
				iNodes[i] = iNode;
				DBuffer dbuf = bufferCache.getBlock(i/Constants.INODES_PER_BLOCK + 1);
				if (!dbuf.checkValid()) {
					dbuf.startFetch();
					dbuf.waitValid();
				}
				iNode.writeToBuffer(dbuf);
				return fileIDs[i];
			}
		}
		throw new DFSException("Maximum number of files reached.");
	}

	@Override
	public void destroyDFile(DFileID dFID) {
		for (int i = 0; i < fileIDs.length; ++i) {
			if (fileIDs[i] == dFID) {
				fileIDs[i] = null;
				INode iNode = iNodes[i];
				iNode.clearINode();
				DBuffer dbuf = bufferCache.getBlock(i % Constants.INODES_PER_BLOCK);
				iNode.writeToBuffer(dbuf);
				iNodes[i]=null;
			}
		}
	}
	
	public int getNextFreeBlock() throws DFSException {
		for (int i = 0; i < freeMap.length; ++i) {
			if (freeMap[i] == 0) {
				freeMap[i] = 1;
				return i+Constants.BLOCK_OFFSET; // includes block 0 and inode region
			}
		}
		throw new DFSException("No more free blocks.");
	}
	
	@Override
	public int read(DFileID dFID, byte[] buffer, int startOffset, int count) {
		int totalBytesRead = 0;
		// REFACTOR HERE
		INode iNode = iNodes[dFID.getInt()];
		if (iNode == null) {
			System.out.println("MyDFS.read(): FILE DOES NOT EXIST");
		} else {
			int[] blocks = iNode.getBlockArray();
			System.out.println(Arrays.toString(blocks));
			for (int i = 0; i < blocks.length; ++i) {
				System.out.println("MyDFS.read() block: " + blocks[i]);
				if (blocks[i] == 0) continue; 
				DBuffer dbuf = bufferCache.getBlock(blocks[i]);
				if (!dbuf.checkValid()) {
					dbuf.startFetch();
					dbuf.waitValid();
				}
				int numBytesRead = count;
				if (numBytesRead > Constants.BLOCK_SIZE) {
					numBytesRead = Constants.BLOCK_SIZE;
					count -= numBytesRead;
				}
				int bytesRead = dbuf.read(buffer, startOffset, numBytesRead);
				totalBytesRead += bytesRead;
				bufferCache.releaseBlock(dbuf);
				System.out.println(bytesRead);
				if (count == totalBytesRead) break;  
			}
		}
		System.out.println(new String(buffer));
		System.out.println("MyDFS.read(): " + totalBytesRead);
		return totalBytesRead;
	}

	@Override
	public int write(DFileID dfid, byte[] buffer, int startOffset, int count) { 
		int totalBytesWritten = 0;
		int fileID = dfid.getInt();
		INode iNode = iNodes[dfid.getInt()];
		if (iNode == null) {
			System.out.println("MyDFS.write() FILE DOES NOT EXIST");
		} else {
			int[] blocks = iNode.getBlockArray();
			int startBlock = startOffset / Constants.BLOCK_SIZE;
			startOffset = startOffset % Constants.BLOCK_SIZE;
			for (int i = startBlock; i < blocks.length; ++i) {
				if (blocks[i] == 0) {
					blocks[i] = this.getNextFreeBlock();
					iNode.addBlock(blocks[i]);
					if (blocks[i] == 0) System.out.println("MyDFS.write() NO MORE BLOCKS AVAILABLE");
					else {
						DBuffer dbuf = bufferCache.getBlock(fileID/Constants.INODES_PER_BLOCK + 1);
						if (!dbuf.checkValid()) {
							dbuf.startFetch();
							dbuf.waitValid();
						}
						iNode.writeToBuffer(dbuf);
					}
				}

				int numBytesWrite = count;
				if (numBytesWrite > Constants.BLOCK_SIZE) {
					count -= Constants.BLOCK_SIZE;
					numBytesWrite = Constants.BLOCK_SIZE;
				}

				DBuffer dbuf = bufferCache.getBlock(blocks[i]);
				int bytesWritten = dbuf.write(buffer, startOffset, numBytesWrite);
				if (bytesWritten == -1) System.err.println("MyDFS.write() WRITE FAILED");
				totalBytesWritten += bytesWritten;
				iNode.increaseFileSize(bytesWritten);
				iNode.writeToBuffer(bufferCache.getBlock(iNode.getDFileID().getInt()/Constants.INODES_PER_BLOCK + 1));
				dbuf.startPush();
				dbuf.waitClean();
				bufferCache.releaseBlock(dbuf);
				System.out.println("MyDFS.write() PUSHED: " + dbuf.getBlockID());
				if (count == bytesWritten) {
					break;
				} else {
					count -= bytesWritten;
					startOffset = 0;
				}
			}
		}
		System.out.println("MyDFS.write() wrote num bytes: " + totalBytesWritten);
		
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
		for (INode iNode: iNodes) {
			if (iNode != null) {
				dFiles.add(iNode.getDFileID());
			}
		}
		return dFiles;
	}
	
	public void sync() {
		bufferCache.sync();
	}
	
	public class DFSException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		DFSException(String message) {
			super(message);
		}
	}

	@Override
	public void finalize() throws Throwable {
		this.sync();
		super.finalize();
	}
	
}	




