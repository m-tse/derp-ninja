package dblockcache;

import virtualdisk.MyVirtualDisk;

import common.Constants;

public class MyDBuffer extends DBuffer {

	private byte[] _blockBytes; // Corresponds to information stored in respective block
	private int _blockID;
	private boolean _held, _valid, _dirty, _waitFetch, _waitPush;
	private MyVirtualDisk _VDF;
	
	
	public MyDBuffer(int blockID) {
		_blockID = blockID;
		_blockBytes = new byte[Constants.BLOCK_SIZE];
		_held = false;
		_valid = false;
		_VDF = MyVirtualDisk.getInstance();
	}
	
	@Override
	public void startFetch() {
		try {
			_waitFetch = true;
			_VDF.startRequest(this, Constants.DiskOperationType.READ);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void startPush() {
		try {
			_waitPush = true;
			_VDF.startRequest(this, Constants.DiskOperationType.WRITE);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean checkValid() {
		return _valid;
	}

	@Override
	public boolean waitValid() {
		while (_waitFetch) {
			try {
				synchronized(this) {
					this.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	@Override
	public boolean checkClean() {
		return !_dirty;
	}

	@Override
	public boolean waitClean() {
		while (_waitPush) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public boolean isBusy() {
		return _held || _waitFetch || _waitPush;
	}

	@Override
	public int read(byte[] buffer, int startOffset, int count) {
		if (!_held) {
			return -1;
		}
		int bytesRead = 0;
		if (!_valid) {
			System.out.println("WAITING");
			waitValid();
		}	
		for (int i = startOffset; i < count; ++i, ++bytesRead) {
			buffer[i] = _blockBytes[i-startOffset];
		}
		return bytesRead;
	}

	@Override
	public int write(byte[] buffer, int startOffset, int count) {
		if (!_held) {
			return -1;
		}
		int bytesWritten = 0;
		for (int i = startOffset; i < count; ++i, ++bytesWritten) {
			_blockBytes[i-startOffset] = buffer[i]; 
		}
		_dirty = true;
		return bytesWritten;
	}

	@Override
	public void ioComplete() {
		if (_waitFetch) {
			_valid = true;
			_waitFetch = false;
		} else if (_waitPush) {
			_waitPush = false;
		}
		synchronized(this) {
			this.notify();
		}
	}

	@Override
	public int getBlockID() {
		return new Integer(_blockID);
	}

	@Override
	public byte[] getBuffer() {
		return _blockBytes;
	}
	
	public void holdBuffer() {
		_held = true;
	}
	
	public void releaseBuffer() {
		_held = false;
	}

}
