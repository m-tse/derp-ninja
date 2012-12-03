package dblockcache;

import virtualdisk.MyVirtualDisk;

import common.Constants;

public class MyDBuffer extends DBuffer {

	private byte[] myBuffer; // Corresponds to information stored in respective block
	private int blockID;
	private Object cleanSignal, validSignal;
	private boolean isHeld, isValid, isClean, isFetching, isPushing, isPinned;
	private MyVirtualDisk VDF;
	
	public MyDBuffer() {
		myBuffer = new byte[Constants.BLOCK_SIZE];
		blockID = 0; // Indication that buffer is unused
		cleanSignal = new Object();
		validSignal = new Object();
		isHeld = false;
		isValid = false;
		isClean = false;
		isFetching = false;
		isPushing = false;
		isPinned = false;
		VDF = MyVirtualDisk.getInstance();
	}

	public MyDBuffer(boolean pinned) {
		this();
		this.isPinned = pinned;
	}
	
	public boolean isPinned() {
		return isPinned;
	}
	
	public void setBlockID(int blockID) {
		this.blockID = blockID;
	}

	public void clearBuffer() {
		myBuffer = new byte[Constants.BLOCK_SIZE];
		isHeld = false;
		isValid = false;
		isClean = false;
		isFetching = false;
		isPushing = false;
		isPinned = false;
	}
	
	@Override
	public void startFetch() {
		try {
			isFetching = true;
			VDF.startRequest(this, Constants.DiskOperationType.READ);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void startPush() {
		try {
			isPushing = true;
			VDF.startRequest(this, Constants.DiskOperationType.WRITE);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean checkValid() {
		return isValid;
	}

	@Override
	public boolean waitValid() {
		while (isFetching && !isValid) {
			try {
				synchronized(validSignal) {
					validSignal.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	@Override
	public boolean checkClean() {
		return isClean;
	}

	@Override
	public boolean waitClean() {
		while (isPushing && !isClean) {
			try {
				synchronized(cleanSignal) {
					cleanSignal.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public boolean isBusy() {
		return isHeld || isFetching || isPushing;
	}

	public boolean cannotRead() {
		if (!isHeld && !isPinned) {
			return true;
		}
		if (!isValid) {
			System.out.println("MyDBuffer.read() Buffer invalid");
			return true;
		}	
		return false;
	}

	@Override
	public int read(byte[] buffer, int startOffset, int count) {
		if (cannotRead()) return -1;
		int bytesRead = 0;
		for (int i = 0; i < count; ++i, ++bytesRead) {
			buffer[i] = myBuffer[i+startOffset];
		}
		return bytesRead;
	}

	public boolean cannotWrite() {
		if (!isHeld && !isPinned) {
			System.out.println("MyDBuffer.write Buffer NOT HELD");
			return true;
		}
		return false;
	}
	
	@Override
	public int write(byte[] buffer, int startOffset, int count) {
		if (cannotWrite()) return -1;
		int bytesWritten = 0;
		for (int i = 0; i < count; ++i, ++bytesWritten) {
			if (i >= myBuffer.length || i >= buffer.length) {
				System.out.println("MyDBuffer.write() OUT OF BOUNDS");
				break;
			}
			myBuffer[i] = buffer[startOffset+i]; 
		}
		isClean = false;
		return bytesWritten;
	}

	@Override
	public void ioComplete() {
		if (isFetching) {
			isFetching = false;
			isValid = true;
			synchronized(validSignal) {
				validSignal.notify();
			}
		} else if (isPushing) {
			isPushing = false;
			isClean = true;
			synchronized(cleanSignal) {
				cleanSignal.notify();
			}
		}
		MyDBufferCache cache = MyDBufferCache.getInstance();
		if (cache.waitingOnBuffers()) {
			cache.signalBufferReady();
		}
	}

	@Override
	public int getBlockID() {
		return new Integer(blockID);
	}

	@Override
	public byte[] getBuffer() {
		return myBuffer;
	}
	
	public void holdBuffer() {
		isHeld = true;
	}
	
	public void releaseBuffer() {
		isHeld = false;
	}

}
