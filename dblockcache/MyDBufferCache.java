package dblockcache;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class MyDBufferCache extends DBufferCache {

	private HashMap<Integer, DBuffer> _bufferMemory;
	private Queue<DBuffer> _bufferQueue;
	
	
	public MyDBufferCache(int cacheSize) {
		super(cacheSize);
		_bufferMemory = new HashMap<Integer, DBuffer>();
		_bufferQueue = new LinkedList<DBuffer>();
	}

	@Override
	public DBuffer getBlock(int blockID) {
		++blockID; // Block 0 is reserved
		DBuffer retrievedBlock = null;
		if (!_bufferMemory.keySet().contains(blockID)) {
			retrievedBlock = new MyDBuffer(blockID);
			if (_bufferMemory.size() == super.getCacheSize()) { // Evict
				DBuffer unpopularBuffer = _bufferQueue.poll();
				_bufferMemory.remove(unpopularBuffer);
			} else if (_bufferMemory.size() > super.getCacheSize()) { // ERROR
				System.err.println("CACHE OVERFLOW");
			}
			_bufferMemory.put(blockID, retrievedBlock);
		} else {
			retrievedBlock = _bufferMemory.get(blockID);
			_bufferQueue.remove(retrievedBlock);
			_bufferQueue.add(retrievedBlock);
		}
		return retrievedBlock;
	}

	@Override
	public void releaseBlock(DBuffer buf) {
		// TODO Auto-generated method stub
	}

	@Override
	public void sync() {
		// TODO Auto-generated method stub
		
	}

}
