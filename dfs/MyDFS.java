package dfs;

import java.util.ArrayList;
import java.util.List;

import virtualdisk.MyVirtualDisk;

import common.Constants;
import common.DFileID;
import common.INode;

import dblockcache.DBuffer;
import dblockcache.DBufferCache;
import dblockcache.MyDBufferCache;

public class MyDFS extends DFS {
	
	// TODO: Use different structure for fileIDs
	// TODO: Surpass max file size
	// TODO: Update inode size
	
	
	DBufferCache _bufferCache;
	DFileID[] _fileIDs;
	INode[] _iNodes;
	int[] _freeMap;
	
	
	public MyDFS(String volName, boolean format) {
		super(volName, format);
		_bufferCache = new MyDBufferCache(Constants.NUM_OF_BLOCKS/2);
		_fileIDs = new DFileID[Constants.MAX_NUM_FILES];
		_iNodes = new INode[Constants.MAX_NUM_FILES];
		_freeMap = new int[Constants.NUM_OF_BLOCKS-Constants.BLOCK_OFFSET]; // Block 0, 1-8 reserved 
		if (format) format();
		this.readINodeRegion();
	}
	
	public MyDFS(boolean format) {
		this(Constants.vdiskName,format);
	}

	public MyDFS() {
		this(Constants.vdiskName,false);
	}
	
	private void readINodeRegion() {
		int fileID = 0;
		for (int i = 0; i < _iNodes.length/Constants.INODES_PER_BLOCK; ++i) {
			byte[] iNodeBytes = new byte[Constants.INODE_SIZE];
			DBuffer buffer = _bufferCache.getBlock(i+1); // i+1 because block 0 reserved 
			buffer.startFetch();
			for (int k = 0; k < Constants.INODES_PER_BLOCK; ++k, ++fileID) {
				buffer.read(iNodeBytes, k*Constants.INODE_SIZE, Constants.INODE_SIZE);
				INode iNode = new INode(iNodeBytes);
				if (iNode.isNotUsed()) {
					System.out.println("MyDFS.readINodeRegion(): NOOOO");
					continue;
				} else {
					System.out.println("MyDFS.readINodeRegion(): YESSS");
					System.out.println("- Got iNode for fileID: " + fileID);
					// Convert bytes into iNode information
					_iNodes[i] = iNode;
					_fileIDs[fileID] = new DFileID(i);
					for (int n: iNode.getBlockArray()) {
						_freeMap[n] = 1; // Not free
					}
				}
			}
		}
	}
	
	@Override
	public boolean format() {
		System.out.println("FORMATING");
		return MyVirtualDisk.format();
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
		for (int i = 0; i < _fileIDs.length; ++i) {
			if (_fileIDs[i] == dFID) {
				_fileIDs[i] = null;
				INode iNode = _iNodes[i];
				iNode.clearINode();
				iNode.writeToDisk(_bufferCache);
			}
		}
	}
	
	public int getNextFreeBlock() {
		for (int i = 0; i < _freeMap.length; ++i) {
			if (_freeMap[i] == 0) {
				_freeMap[i] = 1;
				return i+Constants.BLOCK_OFFSET;
			}
		}
		return 0;
	}
	
	@Override
	public int read(DFileID dFID, byte[] buffer, int startOffset, int count) {
		int totalBytesRead = 0;
		// REFACTOR HERE
		INode iNode = _iNodes[dFID.getInt()];
		if (iNode == null) {
			System.out.println("MyDFS.read(): FILE DOES NOT EXIST");
		} else {
			int[] blocks = iNode.getBlockArray();
			System.out.println(blocks);
			int startBlock = startOffset / Constants.BLOCK_SIZE;
			startOffset = startOffset % Constants.BLOCK_SIZE;
			for (int i = startBlock; i < blocks.length; ++i) {
				System.out.println("MyDFS.read(): " + blocks[i]);
				if (blocks[i] == 0) continue; 
				DBuffer dBuffer = _bufferCache.getBlock(blocks[i]);
				dBuffer.startFetch();
				int bytesRead = dBuffer.read(buffer, startOffset, count);
				totalBytesRead += bytesRead;
				if (count == totalBytesRead) break;  
				else startOffset = 0; // Only need to do this once though
			}
		}
		System.out.println(new String(buffer));
		System.out.println("MyDFS.read(): " + totalBytesRead);
		return totalBytesRead;
	}

	@Override
	public int write(DFileID dFID, byte[] buffer, int startOffset, int count) { 
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
					if (blocks[i] == 0) System.out.println("NO MORE BLOCKS AVAILABLE");
					else iNode.writeToDisk(_bufferCache);
				}
				DBuffer dBuffer = _bufferCache.getBlock(blocks[i]);
				int bytesWritten = dBuffer.write(buffer, startOffset, count);
				if (bytesWritten == -1) System.err.println("WRITE FAILED");
				totalBytesWritten += bytesWritten;
				iNode.increaseFileSize(bytesWritten);
				dBuffer.startPush();
				System.out.println("PUSHED: " + dBuffer.getBlockID());
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
				System.out.println("HELLOO");
				dFiles.add(iNode.getDFileID());
			}
		}
		return dFiles;
	}
	
	public void sync() {
		_bufferCache.sync();
	}
	
	
}	
	

