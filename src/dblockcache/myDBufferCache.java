package dblockcache;

import java.util.HashMap;
import java.util.LinkedList;

public class myDBufferCache extends DBufferCache{

	private final int cacheSize;
	private HashMap<Integer,DBuffer> cache;
	//we define the head (First) element to be the one in line for eviction
	//we define the tail (last) element to be the latest used
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
			//update the queue:
			queue.remove(blockID);
			queue.addLast(blockID);
			return cache.get(blockID);

		}
		//otherwise load the block into the cache
		//first check if we need to evict
		if(cache.size()<cacheSize)
		{
			//cache is not full yet
			
		}
		

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
