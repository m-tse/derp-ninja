package common;

import java.nio.ByteBuffer;
import java.util.Arrays;

import dblockcache.DBuffer;
import dblockcache.MyDBufferCache;

public class INode {

	// For now, having INodes created at fixed constant size
	private int[] blocks;  
	private int fileSize;
	private DFileID fileID;
	private INode next;
	
	// Pointer to last available position on disk
	private static int endOffset = Constants.INODE_REGION_SIZE_BYTES; 
	
	// INode byte format
	/*
	 * Bytes 0-3: fileId 
	 * Bytes 4-7: fileSize  
	 * Bytes 8-255: Block array 
	 */
	
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
		blocks = new int[Constants.MAX_FILE_BLOCK_SIZE];
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
				blocks[index] = blockNum; 
				index++;
				blockBytes = new byte[4];
			}
		}
	}
	
	public INode createNestedNode(int offset) {
		int blockNum = offset/Constants.BLOCK_SIZE;
		int blockOffset = offset-(blockNum*Constants.BLOCK_SIZE);
		MyDBufferCache cache = MyDBufferCache.getInstance();
		DBuffer dbuf = cache.getBlock(blockNum+1);
		byte[] blockBytes = new byte[Constants.BLOCK_SIZE];
		if (!dbuf.checkValid()) {
			dbuf.startFetch();
			dbuf.waitValid();
		}
		dbuf.read(blockBytes, blockOffset, Constants.INODE_SIZE);
		byte[] iNodeBytes = new byte[Constants.INODE_SIZE];
		for (int i = 0; i < iNodeBytes.length; ++i) {
			iNodeBytes[i] = blockBytes[i+blockOffset];
		}
		return new INode(iNodeBytes);
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
		for (int i: blocks) {
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
		blocks = new int[Constants.MAX_FILE_BLOCK_SIZE]; // 62*4 bytes = 248 bytes
		fileSize = 0; // 4 bytes
		this.fileID = fileID; // 4 bytes
		next = null;
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
		return Arrays.copyOf(blocks, blocks.length);
	}
	
	
	public void setBlockArray(int[] blocks) {
		this.blocks = blocks;
	}
	
	
	public void addBlock(int newBlock) {
		for (int i = 0; i < blocks.length; ++i) {
//			if (i == blocks.length-1) break; // Reserve last block for inode pointer - large files
			if (blocks[i] == 0) {
				blocks[i] = newBlock;
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
		for (int i = 0; i < blocks.length; ++i) {
			if (blocks[i] == oldBlock) {
				blocks[i] = 0;
				return;
			}
		}
		System.err.println("Could not find block in inode");
	}

	public void writeToBuffer(DBuffer dbuf) {
		byte[] iNodeBytes = this.getBytes();
		byte[] bufferBytes = dbuf.getBuffer();
		int iNodeOffset = Constants.INODE_SIZE * (fileID.getInt() % Constants.INODES_PER_BLOCK);
		for (int i = 0; i < iNodeBytes.length; ++i) {
			bufferBytes[i+iNodeOffset] = iNodeBytes[i];
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

	public void clearINode() {
		Arrays.fill(blocks, 0);
		fileID.clearFileID();
		fileSize = 0;
	}
	
	public boolean isNotUsed() {
		for (int i: blocks) {
			if (i != 0) {
				return false;
			}
		}
		return true;
	}
	
}
