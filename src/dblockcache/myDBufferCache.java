package dblockcache;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import common.Constants.DiskOperationType;

import virtualdisk.MyVirtualDisk;

public class myDBufferCache extends DBufferCache{

	private final int cacheSize;
	private MyVirtualDisk disk;
	private HashMap<Integer,DBuffer> cache;
	private HashSet<Integer> heldBlocks;
	//we define the head (First) element to be the one in line for eviction
	//we define the tail (last) element to be the latest used
	private LinkedList<Integer> queue;

	public myDBufferCache(int cacheSize) throws FileNotFoundException, IOException {
		super(cacheSize);
		this.cacheSize=cacheSize;
		cache= new HashMap<Integer,DBuffer>(cacheSize);
		heldBlocks=new HashSet<Integer>();
		queue= new LinkedList<Integer>();
		disk=MyVirtualDisk.getInstance();
	}

	//TODO: 
	@Override
	public DBuffer getBlock(int blockID) {
		//check if blockId is in the cache
		if(cache.containsKey(blockID))
		{
			/*
			//update the queue:
			queue.remove(blockID);
			queue.addLast(blockID);
			*/
			//hold the buffer
			heldBlocks.add(blockID);
			queue.remove(blockID);//remove from eviction queue because this block is held!
			return cache.get(blockID);

		}
		//otherwise load the block into the cache
		myDBuffer buf=new myDBuffer(blockID);
		heldBlocks.add(blockID);
		
		//first check if we need to evict
		if(cache.size()>=cacheSize)
		{
			//cache is full, find block to evict
			int evicted=-1;//a null value
			LinkedList<Integer> evictionCandidates=new LinkedList<Integer>();
			//we search for an unbusy block, pushing the busy ones onto a stack
			while(!queue.isEmpty())
			{
				int evict=queue.removeFirst();
				if(cache.get(evict).isBusy()||heldBlocks.contains(evict))
					evictionCandidates.push(evict);
				else
				{
					evicted=evict;
					break;
				}
			}
			while(!evictionCandidates.isEmpty())
			{
				queue.addFirst(evictionCandidates.pop());
			}
			//what if we didnt find any block to evict??
			if(evicted==-1)
			{

			}
			//now we have an eviction candidate which is not busy	
			DBuffer evictBuf=cache.get(evicted);
			if(!evictBuf.checkClean())
			{
				evictBuf.startPush();
				evictBuf.waitClean();
			}
			cache.remove(evicted);
			//the evicted buffer has been written to disk and kicked from the cache!
		}
		//put in the new buffer
		cache.put(blockID, buf);
		return buf;
	}

	@Override
	public void releaseBlock(DBuffer buf) 
	{
		heldBlocks.remove(buf.getBlockID());
		//we add it to the eviction queue once the block is released by the DFS
		queue.addLast(buf.getBlockID());
	}

	@Override
	public void sync() 
	{
		for(DBuffer buf:cache.values())
		{
			if(!buf.checkClean())
			{
				buf.startPush();
				buf.waitClean();//we'll just wait here
			}
		}

	}
	
	
	public class CacheException extends RuntimeException
	{
		//herp derp?
		private static final long serialVersionUID = 1L;

		CacheException(String message)
		{
			super(message);
		}

	}

}
