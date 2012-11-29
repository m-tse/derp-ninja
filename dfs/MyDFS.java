package dfs;

import java.io.FileNotFoundException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import virtualdisk.MyVirtualDisk;
import virtualdisk.VirtualDisk;

import common.Constants;
import common.DFileID;
import common.INode;
import dblockcache.DBuffer;
import dblockcache.DBufferCache;
import dblockcache.MyDBufferCache;

public class MyDFS extends DFS {
	
	DBufferCache _bufferCache;
	DFileID[] _fileIDs;
	INode[] _iNodes;
	int[] _freeMap;
	
	
	public MyDFS(String volName, boolean format) throws FileNotFoundException, IOException {
		super(volName, format);
		_bufferCache = new MyDBufferCache(Constants.NUM_OF_BLOCKS/2);
		_fileIDs = new DFileID[Constants.MAX_NUM_FILES];
		_iNodes = new INode[Constants.MAX_NUM_FILES];
		_freeMap = new int[Constants.NUM_OF_BLOCKS-Constants.BLOCK_OFFSET]; // Block 0, 1-8 reserved 
		this.readINodeRegion();
	}
	
	public MyDFS(boolean format) throws FileNotFoundException, IOException {
		this(Constants.vdiskName,format);
	}

	public MyDFS() throws FileNotFoundException, IOException {
		this(Constants.vdiskName,false);
	}
	
	private void readINodeRegion() throws IllegalArgumentException, FileNotFoundException, IOException {
		for (int i = 0; i < _iNodes.length/Constants.INODES_PER_BLOCK; ++i) {
			byte[] iNodeBytes = new byte[Constants.INODE_SIZE];
			DBuffer buffer = _bufferCache.getBlock(i); 
			for (int k = 0; k < Constants.INODES_PER_BLOCK; ++k) {
				buffer.read(iNodeBytes, k*Constants.INODE_SIZE, (k+1)*Constants.INODE_SIZE);
				INode iNode = new INode(iNodeBytes);
				if (iNode.isNotUsed()) {
					System.out.println("MyDFS.readINodeRegion(): NOOOO");
					continue;
				} else {
					System.out.println("MyDFS.readINodeRegion(): YESSS");
					// Convert bytes into iNode information
					_iNodes[i] = iNode;
					for (int n: iNode.getBlockArray()) {
						_freeMap[n] = 1; // Not free
					}
				}
			}
				
		}
	}
	
	@Override
	public boolean format() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DFileID createDFile() {
		// Simple implementation for now: just file closest available ID
		for (int i = 0; i < _fileIDs.length; ++i) {
			if (_fileIDs[i] == null) {
				_fileIDs[i] = new DFileID(i);
				
				// Create INode
				INode iNode = new INode(_fileIDs[i]);
				_iNodes[i] = iNode;
				iNode.writeToDisk(_bufferCache);
				return _fileIDs[i];
			}
		}
		return null;
	}

	@Override
	public void destroyDFile(DFileID dFID) {
		// Improve implementation: slow algorithm right now
		for (int i = 0; i < _fileIDs.length; ++i) {
			if (_fileIDs[i] == dFID) {
				_fileIDs[i] = null;
			}
		}
	}
	
	public int getNextFreeBlock() {
		for (int i: _freeMap) {
			if (i == 0) {
				return i+Constants.BLOCK_OFFSET;
			}
		}
		return 0;
	}
	
	@Override
	public int read(DFileID dFID, byte[] buffer, int startOffset, int count) 
			throws IllegalArgumentException, FileNotFoundException, IOException {
		int totalBytesRead = 0;
		// REFACTOR HERE
		INode iNode = _iNodes[dFID.getInt()];
		if (iNode == null) {
			System.out.println("FILE DOES NOT EXIST");
		} else {
			int[] blocks = iNode.getBlockArray();
			int startBlock = startOffset / Constants.BLOCK_SIZE;
			startOffset = startOffset % Constants.BLOCK_SIZE;
			for (int i = startBlock; i < blocks.length; ++i) {
				System.out.println("MyDFS.read(): " + blocks[i]);
				if (blocks[i] == 0) {
					continue;
				}
				DBuffer dBuffer = _bufferCache.getBlock(blocks[i]);
				int bytesRead = dBuffer.read(buffer, startOffset, count);
				totalBytesRead += bytesRead;
				if (count == bytesRead) {
					break;
				} else {
					count -= bytesRead;
					startOffset = 0;
				}
			}
		}
		System.out.println("MyDFS.read(): " + totalBytesRead);
		return totalBytesRead;
	}

	@Override
	public int write(DFileID dFID, byte[] buffer, int startOffset, int count) throws IllegalArgumentException, FileNotFoundException, IOException {
		int totalBytesWritten = 0;
		INode iNode = _iNodes[dFID.getInt()];
		if (iNode == null) {
			System.out.println("FILE DOES NOT EXIST");
		} else {
			int[] blocks = iNode.getBlockArray();
			int startBlock = startOffset / Constants.BLOCK_SIZE;
			startOffset = startOffset % Constants.BLOCK_SIZE;
			for (int i = startBlock; i < blocks.length; ++i) {
				if (blocks[i] == 0) {
					blocks[i] = this.getNextFreeBlock();
					iNode.addBlock(blocks[i]);
					if (blocks[i] == 0) {
						System.out.println("NO MORE BLOCKS AVAILABLE");
					} else {
						iNode.writeToDisk(_bufferCache);
					}
				}
				DBuffer dBuffer = _bufferCache.getBlock(blocks[i]);
				int bytesWritten = dBuffer.write(buffer, startOffset, count);
				totalBytesWritten += bytesWritten;
				if (count == bytesWritten) {
					break;
				} else {
					count -= bytesWritten;
					startOffset = 0;
				}
			}
		}
		System.out.println("MyDFS.write(): " + totalBytesWritten);
		return totalBytesWritten;
	}

	@Override
	public int sizeDFile(DFileID dFID) {
		INode iNode = _iNodes[dFID.getInt()];	
		return iNode.getFileSize();
	}

	@Override
	public List<DFileID> listAllDFiles() {
		List<DFileID> dFiles = new ArrayList<DFileID>();
		for (INode iNode: _iNodes) {
			if (iNode != null) {
				dFiles.add(iNode.getDFileID());
			}
		}
		return dFiles;
	}
	
}	
	

