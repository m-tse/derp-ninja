package dblockcache;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import common.Constants.DiskOperationType;

import virtualdisk.MyVirtualDisk;

public class myDBufferCache extends DBufferCache{

	private final int cacheSize;
	private MyVirtualDisk disk;
	private HashMap<Integer,DBuffer> cache;
	//we define the head (First) element to be the one in line for eviction
	//we define the tail (last) element to be the latest used
	private LinkedList<Integer> queue;

	public myDBufferCache(int cacheSize) throws FileNotFoundException, IOException {
		super(cacheSize);
		this.cacheSize=cacheSize;
		cache= new HashMap<Integer,DBuffer>();
		queue= new LinkedList<Integer>();
		disk=MyVirtualDisk.getInstance();
	}

	//TODO: do we need to make sure buffer is filled before returning it?
	@Override
	public DBuffer getBlock(int blockID) throws IllegalArgumentException, IOException {
		//check if blockId is in the cache
		if(cache.containsKey(blockID))
		{
			//update the queue:
			queue.remove(blockID);
			queue.addLast(blockID);
			return cache.get(blockID);

		}
		//otherwise load the block into the cache
		myDBuffer buf=new myDBuffer(blockID);
		disk.startRequest(buf, DiskOperationType.READ);
		//first check if we need to evict
		if(cache.size()>=cacheSize)
		{
			//cache is full
			int evict=queue.removeLast();
			//TODO: need to make sure the buffer isnt open for I/O right now
			DBuffer evictBuf=cache.get(evict);
			//TODO: make sure it's clean or whatever
			disk.startRequest(evictBuf, DiskOperationType.WRITE);
			cache.remove(evict);
		}
		cache.put(blockID, buf);
		

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
