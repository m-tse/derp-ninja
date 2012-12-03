package dblockcache;

import java.util.HashMap;

import common.Constants;

public class MyDBufferCache extends DBufferCache {

	
	private RestrictedQueue<MyDBuffer> bufferQueue; // Custom class: queue of restricted size
	private boolean allBuffersBusy;
	private static final int NUMRESERVED_BUFFERS = 8;

	private static MyDBufferCache myInstance;
	
	public static MyDBufferCache getInstance() {
		if (myInstance == null) {
			myInstance = new MyDBufferCache(Constants.CACHE_SIZE);
		}
		return myInstance;
	}
	
	private MyDBufferCache(int cacheSize) { // cacheSize = number of blocks 
		super(cacheSize);
		
		bufferQueue = new RestrictedQueue<MyDBuffer>(cacheSize);
		// Need to 'pin' buffers that we will always need: iNode and freeMap buffers
		for (int i = 0; i < NUMRESERVED_BUFFERS; ++i) {
			MyDBuffer iNodeBuffer = new MyDBuffer(true);
			iNodeBuffer.setBlockID(i);
			iNodeBuffer.holdBuffer();
			bufferQueue.add(iNodeBuffer);
		}
		for (int i = NUMRESERVED_BUFFERS; i < cacheSize; ++i) {
			bufferQueue.add(new MyDBuffer());
		}
		allBuffersBusy = false;
		/*
		 * Vamsi - should init all buffers beforehand and then set
		 * the blockIDs as needed
		 * Also, should not use any other memory than buffer cache?
		 * ...seriously?
		 */
	}
	
	/*
	 * Flush out the cache.
	 */
	public void flush() {
		for (MyDBuffer dbuf: bufferQueue) {
			dbuf.setBlockID(0);
			dbuf.clearBuffer();
		}
	}

	public MyDBuffer checkInCache(int blockID) {
		for (MyDBuffer dbuf: bufferQueue) {
			if (dbuf.getBlockID() == blockID) return dbuf;
		}
		return null;
	}

	public MyDBuffer getAvailableBuffer() {
		for (MyDBuffer dbuf: bufferQueue) {
			if (dbuf.getBlockID() == 0) {
				return dbuf;
			}
		}
		return null;
	}

	@Override
	public DBuffer getBlock(int blockID) {
		// TODO: Need to synchronize this method
		// See if block in buffer
		MyDBuffer retrieved;
		if ((retrieved = checkInCache(blockID)) != null) return retrieved; 
		// else
		if ((retrieved = getAvailableBuffer()) != null) { 
			retrieved.setBlockID(blockID);
			retrieved.holdBuffer();
			return retrieved;
		} // else need to EVICT 
		MyDBuffer evictee = bufferQueue.poll();
		if (evictee.isBusy() || evictee.isPinned()) { 
			// If first i.e. most unpopular buffer is busy then all are busy
			// Wait for one to free up ?
			// For now just wait for evictee
			allBuffersBusy = true;
			waitOnBuffers();
		} 
		evictee.clearBuffer();
		evictee.setBlockID(blockID);
		evictee.holdBuffer();
		bufferQueue.add(evictee);
		return evictee;
	}

	public boolean waitingOnBuffers() {
		return allBuffersBusy;
	}
	
	
	public synchronized void waitOnBuffers() {
		while(allBuffersBusy) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void signalBufferReady() {
		allBuffersBusy = false;
		this.notify();
	}
	
	@Override
	public void releaseBlock(DBuffer buf) {
		// Err this is fishy - Andrew
		if (buf.getClass().isInstance(MyDBuffer.class)) {
			((MyDBuffer) buf).releaseBuffer();
		}
	}

	@Override
	public void sync() {
		for (MyDBuffer buf: bufferQueue) {
			if (!buf.checkClean()) {
				buf.startPush();
				buf.waitClean();
			}
		}
		
	}
}
