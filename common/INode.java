package common;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import dblockcache.DBuffer;
import dblockcache.DBufferCache;

public class INode {

	// For now, having INodes created at fixed constant size
	private int[] _blocks;  
	private int _fileSize;
	private DFileID _fileID;
	
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
		_fileID = new DFileID(bb.getInt());

		// Retrieve file size
		for (int i = 4 ; i < 8; ++i) {
			fileSizeBytes[i-4] = iNodeBytes[i];
		}
		bb = ByteBuffer.wrap(fileSizeBytes);
		_fileSize = bb.getInt();

		// Retrieve blocks
		_blocks = new int[Constants.MAX_FILE_BLOCK_SIZE];
		int index = 0;
		byte[] blockBytes = new byte[4];
		for (int i = 8 ; i < iNodeBytes.length; ++i) {
			blockBytes[i%4] = iNodeBytes[i];
			if ((i+1) % 4 == 0) {
				bb = ByteBuffer.wrap(blockBytes);
				int blockNum = bb.getInt(); 
				_blocks[index] = blockNum; 
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
		bb.putInt(_fileID.getInt());
		result = bb.array();
		for (byte b: result) {
			iNodeBytes[index] = b;
			++index;
		}		
		
		// File size
		bb = ByteBuffer.allocate(4);
		bb.putInt(_fileSize);
		result = bb.array();
		for (byte b: result) {
			iNodeBytes[index] = b;
			++index;
		}		
		
		// Blocks 
		for (int i: _blocks) {
			bb = ByteBuffer.allocate(4);
			bb.putInt(i);
			result = bb.array();
			for (byte b: result) {
				iNodeBytes[index] = b;
				++index;
			}
		}
				
		System.out.println("getBytes() in INode.java: " + index);
		return iNodeBytes;
	}
	
	public INode(DFileID fileID) {
		_blocks = new int[Constants.MAX_FILE_BLOCK_SIZE]; // 62*4 bytes = 248 bytes
		_fileSize = 0; // 4 bytes
		_fileID = fileID; // 4 bytes
	}
	
	public void increaseFileSize(int amount) {
		_fileSize += amount;
	}
	
	public void decreateFileSize(int amount) {
		_fileSize -= amount;
	}
	
	
	public DFileID getDFileID() {
		return _fileID;
	}
	
	
	public int getFileSize() {
		return new Integer(_fileSize);
	}
	
	public void setFileSize(int fileSize) {
		_fileSize = fileSize;
	}
	
	public int[] getBlockArray() {
		return Arrays.copyOf(_blocks, _blocks.length);
	}
	
	
	public void setBlockArray(int[] blocks) {
		_blocks = blocks;
	}
	
	public void addBlock(int newBlock) {
		for (int i = 0; i < _blocks.length; ++i) {
			if (_blocks[i] == 0) {
				_blocks[i] = newBlock;
				return;
			}
		}
		System.err.println("Could not add block to inode, no more space");
	}
	
	public void removeBlock(int oldBlock) {
		for (int i = 0; i < _blocks.length; ++i) {
			if (_blocks[i] == oldBlock) {
				_blocks[i] = 0;
				return;
			}
		}
		System.err.println("Could not find block in inode");
	}

	public void writeToDisk(DBufferCache bufferCache) {
		byte[] iNodeBytes = this.getBytes();
		int blockID = _fileID.getInt() / Constants.INODES_PER_BLOCK;
		int iNodeOffset = Constants.INODE_SIZE * (_fileID.getInt() % Constants.INODES_PER_BLOCK);
		DBuffer dBuffer = bufferCache.getBlock(blockID);
		try {
			dBuffer.write(iNodeBytes, iNodeOffset, iNodeBytes.length);
			dBuffer.startPush();
			System.out.println("INode.writeToDisk(): " + blockID);
			System.out.println("INode.writeToDisk(): " + iNodeOffset);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

	}

	public void clearINode() {
		Arrays.fill(_blocks, 0);
		_fileID.clearFileID();
		_fileSize = 0;
	}
	
	
	public boolean isNotUsed() {
		for (int i: _blocks) {
			if (i != 0) {
				return false;
			}
		}
		return true;
	}
	
}
