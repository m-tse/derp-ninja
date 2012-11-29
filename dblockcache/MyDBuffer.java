package dblockcache;

import java.io.FileNotFoundException;
import java.io.IOException;

import virtualdisk.MyVirtualDisk;
import common.Constants;

public class MyDBuffer extends DBuffer {

	byte[] _blockBytes; // Corresponds to information stored in respective block
	int _blockID;
	
	
	public MyDBuffer(int blockID) {
		_blockID = blockID;
		_blockBytes = new byte[Constants.BLOCK_SIZE];
	}
	
	@Override
	public void startFetch() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startPush() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean checkValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean waitValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean checkClean() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean waitClean() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isBusy() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int read(byte[] buffer, int startOffset, int count) throws IllegalArgumentException, FileNotFoundException, IOException {
		MyVirtualDisk.getInstance().startRequest(this, Constants.DiskOperationType.READ);
		int bytesRead = 0;
		// After ioComplete
		for (int i = startOffset; i < count; ++i, ++bytesRead) {
			buffer[i%Constants.INODE_SIZE] = _blockBytes[i];
		}
		return bytesRead;
	}

	@Override
	public int write(byte[] buffer, int startOffset, int count) throws IllegalArgumentException, FileNotFoundException, IOException {
		// Must copy over buffer before this time ?
		int bytesWritten = 0;
		// NOT taking int count into account right now
		for (int i = startOffset; i < count; ++i, ++bytesWritten) {
			_blockBytes[i] = buffer[i]; 
		}
		MyVirtualDisk.getInstance().startRequest(this, Constants.DiskOperationType.WRITE);
		// After ioCompelte;
		return bytesWritten;
	}

	@Override
	public void ioComplete() {
		// TODO Auto-generated method stub
	}

	@Override
	public int getBlockID() {
		return new Integer(_blockID);
	}

	@Override
	public byte[] getBuffer() {
		return _blockBytes;
	}

}
