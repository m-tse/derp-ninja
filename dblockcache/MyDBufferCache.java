package dblockcache;

import java.util.HashMap;

import common.Constants;

public class MyDBufferCache extends DBufferCache {

	private DBuffer[] _buffers;
	private HashMap<Integer, DBuffer> _bufferMemory;
	
	
	public MyDBufferCache(int cacheSize) {
		super(cacheSize);
		_buffers = new DBuffer[cacheSize];
		_bufferMemory = new HashMap<Integer, DBuffer>();
//		for (int i = 0; i < cacheSize; ++i) {
//			_bufferMemory.put(i, new byte[Constants.BLOCK_SIZE]);
//		}
	}

	int count = 0 ;
	@Override
	public DBuffer getBlock(int blockID) {
		DBuffer retrievedBlock = null;
		if (!_bufferMemory.keySet().contains(blockID)) {
			System.out.printf("%d: BLOCK ID DOES NOT EXIST\n", count);
			count++;
			retrievedBlock = new MyDBuffer(blockID);
		} else {
			retrievedBlock = _bufferMemory.get(blockID);
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
