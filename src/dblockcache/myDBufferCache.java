package dblockcache;

import java.util.HashMap;
import java.util.LinkedList;

public class myDBufferCache extends DBufferCache{

	private final int cacheSize;
	private HashMap<Integer,DBuffer> cache;
	private LinkedList<Integer> queue;

	public myDBufferCache(int cacheSize) {
		super(cacheSize);
		this.cacheSize=cacheSize;
		cache= new HashMap<Integer,DBuffer>();
		queue= new LinkedList<Integer>();
	}

	@Override
	public DBuffer getBlock(int blockID) {
		//check if blockId is in the cache
		if(cache.containsKey(blockID))
		{
			//update the list:
			queue.remove(blockID);
			queue.addLast(blockID);
			return cache.get(blockID);

		}
		//remove a block from cache
		cache.put(queue.removeFirst(),null);

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
