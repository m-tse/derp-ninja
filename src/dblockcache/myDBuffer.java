package dblockcache;

import java.io.IOException;

import common.Constants;
import common.Constants.DiskOperationType;

import virtualdisk.MyVirtualDisk;
//TODO: what if we have two or more disk operations in progress????
public class myDBuffer extends DBuffer{
	private final MyVirtualDisk disk=MyVirtualDisk.getInstance();
	private byte[] myBuffer;
	private Object cleanSignal;
	private Object validSignal;
	private final int blockID;
	private boolean isValid;
	private boolean isClean;
	//private boolean isHeld; //not needed: this is checked on cache level
	private boolean isInProgress;
	public myDBuffer(int blockID)
	{
		this.blockID=blockID;
		myBuffer=new byte[Constants.BLOCK_SIZE];
		cleanSignal=new Object();
		validSignal=new Object();
		isValid=false;
		isClean=false;
		isInProgress=false;
		
	}
	/** Start an asynchronous fetch of associated block from the volume  */
	public void startFetch(){
		//this is just starting a read request from the disk
		try {
			//i/o op initiated to the disk
			isInProgress=true;
			//since we are fetching new data, the current version is no longer valid
			isValid=false;
			//not clean because not valid
			isClean=false;
			disk.startRequest(this, DiskOperationType.READ);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void startPush() {
		//no need to write if it's already synced!
		if(isClean)
			return;
		
		try {
			// i/o op initiated on disk
			isInProgress=true;
			//will be clean once IO completes
			//is already valid
			disk.startRequest(this, DiskOperationType.WRITE);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean checkValid() {
		return isValid;
	}

	@Override
	public boolean waitValid(){
		synchronized(validSignal)
		{
			while(!isValid)
			{
				try {
					validSignal.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
					return false;
				}
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
		synchronized(cleanSignal)
		{
			while(!isClean)
			{
				try {
					cleanSignal.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean isBusy() {
		return (!isInProgress);
	}

	@Override
	public int read(byte[] buffer, int startOffset, int count) {
		if(!isValid) 
			return -1;
		//check that buffer can hold the requested data
		if(startOffset+count>buffer.length)
			return -1;
		//check that count is within limits
		if(count>Constants.BLOCK_SIZE)
			return -1;
		//do the copy
		for(int x=0;x<count;x++)
		{
			buffer[startOffset+x]=myBuffer[x];
		}
		return count;
	}

	@Override
	public int write(byte[] buffer, int startOffset, int count) {
		//check that we can fit the data
		if(count>Constants.BLOCK_SIZE)
			return -1;
		//check the validity of parameters
		if(startOffset+count>buffer.length)
			return -1;
		//mark dirty!
		isClean=false;
		//we are now valid, if not so already
		isValid=true;
		validSignal.notifyAll();
		
		for(int x=0;x<count;x++)
		{
			myBuffer[x]=buffer[startOffset+x];
		}
		return count;
		
	}

	@Override
	public void ioComplete() {
		isInProgress=false;
		isValid=true;
		isClean=true;
		cleanSignal.notifyAll();
		validSignal.notifyAll();
		
	}

	@Override
	public int getBlockID() {
		// TODO Auto-generated method stub
		return blockID;
	}

	@Override
	public byte[] getBuffer() {
		return myBuffer;
	}

}
