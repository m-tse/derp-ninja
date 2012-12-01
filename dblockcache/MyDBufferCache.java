package dblockcache;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class MyDBufferCache extends DBufferCache {

	private HashMap<Integer, MyDBuffer> _bufferMemory;
	private RestrictedQueue<MyDBuffer> _bufferQueue; // Custom class: queue of restricted size
	
	
	public MyDBufferCache(int cacheSize) { // cacheSize = number of blocks 
		super(cacheSize);
		_bufferMemory = new HashMap<Integer, MyDBuffer>();
		_bufferQueue = new RestrictedQueue<MyDBuffer>(cacheSize);
	}

	@Override
	public DBuffer getBlock(int blockID) {
		++blockID; // Block 0 is reserved
		MyDBuffer retrievedBuffer = null;
		if (!_bufferMemory.keySet().contains(blockID)) {
			retrievedBuffer = new MyDBuffer(blockID);
			if (_bufferMemory.size() == super.getCacheSize()) { // Need to evict one 
				Iterator<MyDBuffer> find = _bufferQueue.iterator();
				DBuffer unpopularBuffer = null;
				while (find.hasNext()) {
					unpopularBuffer = find.next();
					if (unpopularBuffer.isBusy()) continue;
					else break;
				}
				if (unpopularBuffer == null) { 
					System.err.println("ALL BUFFERS BUSY");
				} else { // Evict
					_bufferMemory.remove(unpopularBuffer);
					_bufferQueue.remove(unpopularBuffer);
				}
			} else if (_bufferMemory.size() > super.getCacheSize()) { // ERROR
				System.err.println("CACHE OVERFLOW");
			}
			_bufferMemory.put(blockID, retrievedBuffer);
			_bufferQueue.add(retrievedBuffer);
			retrievedBuffer.holdBuffer();
		} else {
			retrievedBuffer = _bufferMemory.get(blockID);
			retrievedBuffer.holdBuffer();
			_bufferQueue.remove(retrievedBuffer);
			_bufferQueue.add(retrievedBuffer);
		}
		return retrievedBuffer;
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
		for (MyDBuffer buf: _bufferQueue) {
			if (!buf.checkClean()) {
				buf.startPush();
				buf.waitClean();
			}
		}
		
	}
}
