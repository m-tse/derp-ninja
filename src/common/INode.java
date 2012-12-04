package common;

import java.nio.ByteBuffer;
import java.util.Arrays;

import dblockcache.DBuffer;
import dblockcache.MyDBufferCache;
import dfs.DFS;

public class INode {
	// For now, having INodes created at fixed constant size
	public int[] blockIDs;  
	private int fileSize;
	private DFileID fileID;
	private INode nextINode;
	
	
	// Pointer to last available position on disk
	private static int endOffset = Constants.INODE_REGION_SIZE_BYTES; 
	
	// INode byte format
	/*
	 * Bytes 0-3: fileId 
	 * Bytes 4-7: fileSize  
	 * Bytes 8-255: Block array 
	 */
	
	/*
	 * Use the empty constructor when creating INodes that connect in a linked list, not to represent the head of a file
	 */
	public INode(){
		blockIDs = new int[Constants.MAX_FILE_BLOCK_SIZE];
	}
	public INode(byte[] iNodeBytes) {
		byte[] fileIdBytes = new byte[4];
		byte[] fileSizeBytes = new byte[4];
		ByteBuffer bb;
		
		// Retrieve file ID
		for (int i = 0 ; i < 4; ++i) {
			fileIdBytes[i] = iNodeBytes[i];
		}
		bb = ByteBuffer.wrap(fileIdBytes);
		fileID = new DFileID(bb.getInt());

		// Retrieve file size
		for (int i = 4 ; i < 8; ++i) {
			fileSizeBytes[i-4] = iNodeBytes[i];
		}
		bb = ByteBuffer.wrap(fileSizeBytes);
		fileSize = bb.getInt();

		// Retrieve blocks
		blockIDs = new int[Constants.MAX_FILE_BLOCK_SIZE];
		int index = 0;
		byte[] blockBytes = new byte[4];
		for (int i = 8 ; i < iNodeBytes.length; ++i) {
//			if (i == iNodeBytes.length-1) {
//				if (i != 0) {
//					this.next = createNestedNode(i);
//				} else this.next = null;
//			}
			blockBytes[i%4] = iNodeBytes[i];
			if ((i+1) % 4 == 0) {
				bb = ByteBuffer.wrap(blockBytes);
				int blockNum = bb.getInt(); 
				blockIDs[index] = blockNum; 
				index++;
				blockBytes = new byte[4];
			}
		}
	}
	

	
	public byte[] getBytes() {
		byte[] iNodeBytes = new byte[Constants.INODE_SIZE];
		ByteBuffer bb;
		byte[] result;
		int index = 0;
		
		// FileID
		bb = ByteBuffer.allocate(4);
		bb.putInt(fileID.getInt());
		result = bb.array();
		for (byte b: result) {
			iNodeBytes[index] = b;
			++index;
		}		
		
		// File size
		bb = ByteBuffer.allocate(4);
		bb.putInt(fileSize);
		result = bb.array();
		for (byte b: result) {
			iNodeBytes[index] = b;
			++index;
		}		
		
		// Blocks 
		for (int i: blockIDs) {
			bb = ByteBuffer.allocate(4);
			bb.putInt(i);
			result = bb.array();
			for (byte b: result) {
				iNodeBytes[index] = b;
				++index;
			}
		}
				
		System.out.println("getBytes() in INode.java: " + fileID.getInt());
		return iNodeBytes;
	}
	
	public INode(DFileID fileID) {
		blockIDs = new int[Constants.MAX_FILE_BLOCK_SIZE]; // 62*4 bytes = 248 bytes
		fileSize = 0; // 4 bytes
		this.fileID = fileID; // 4 bytes

	}
	
	public void increaseFileSize(int amount) {
		fileSize += amount;
	}
	
	public void decreaseFileSize(int amount) {
		fileSize -= amount;
	}
	
	public DFileID getDFileID() {
		return fileID;
	}
	
	public int getFileSize() {
		return new Integer(fileSize);
	}
	
	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}
	
	public int[] getBlockArray() {
		return Arrays.copyOf(blockIDs, blockIDs.length);
	}
	
	
	public void setBlockArray(int[] blocks) {
		this.blockIDs = blocks;
	}
	
	
	public void addBlock(int newBlock) {
		for (int i = 0; i < blockIDs.length; ++i) {
			if (i == blockIDs.length-1){
//				createIndirectBlock(newBlock); // Reserve last block for inode pointer - large files
				break;
			}
			if (blockIDs[i] == 0) {
				blockIDs[i] = newBlock;
				return;
			}
		}
		// Code for larger files - use recursion?
//		if (next == null) {
//			next = new INode(this.getDFileID());
//			endOffset -= Constants.INODE_SIZE;
//			while (!createNestedNode(endOffset).isNotUsed()) {
//				endOffset -= Constants.INODE_SIZE;
//				if (endOffset <= 0) {
//					System.err.println("Cannot create anymore nodes");
//					return;
//				}
//			}
//			blocks[blocks.length-1] = endOffset;
//		} 
//		next.addBlock(newBlock);
	}

	
	public void removeBlock(int oldBlock) {
		for (int i = 0; i < blockIDs.length; ++i) {
			if (blockIDs[i] == oldBlock) {
				blockIDs[i] = 0;
				return;
			}
		}
		System.err.println("Could not find block in inode");
	}

	public INode getCreateNextINode(){
		if(nextINode==null){
			nextINode=new INode();
		}
		return nextINode;
	}
	public INode getNextINode(){
		if(nextINode==null){
			nextINode=new INode();
		}
		return nextINode;
	}

	public void clearINode() {
		Arrays.fill(blockIDs, 0);
		fileID.clearFileID();
		fileSize = 0;
	}
	
	public boolean isNotUsed() {
		for (int i: blockIDs) {
			if (i != 0) {
				return false;
			}
		}
		return true;
	}
	
}
