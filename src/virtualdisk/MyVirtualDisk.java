package virtualdisk;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import common.Constants;
import common.Constants.DiskOperationType;

import dblockcache.DBuffer;

public class MyVirtualDisk extends VirtualDisk implements Runnable {

	/*
	 * Singleton methods
	 */
	private static MyVirtualDisk myInstance;
	private static Thread myThread;
	private static boolean isRunning, isComplete;
	private static Object doneSignal;

	public synchronized static MyVirtualDisk getInstance() {
		if (myInstance == null) {
			try {
				myInstance = new MyVirtualDisk();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return myInstance;
	}

	public static boolean format() {
		try {
			if (myInstance == null) {
				stopRunning();
			}
			myInstance = new MyVirtualDisk(true);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	private static void stopRunning() {
		isRunning = false;
		while(!isComplete) {
			synchronized(doneSignal) {
				try {
					doneSignal.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}



	/*
	 * MyVirtualDisk Constructors
	 */
	private LinkedList<Request> requestQueue;

	private MyVirtualDisk(String volName, boolean format) throws FileNotFoundException,
	IOException {
		super(volName, format);
		requestQueue = new LinkedList<Request>();
		isRunning = true;
		isComplete = false;
		doneSignal = new Object();
		myThread = new Thread(this);
		myThread.setDaemon(true);
		myThread.start();

	}

	private MyVirtualDisk(boolean format) throws FileNotFoundException,
	IOException {
		this(Constants.vdiskName, format);
	}

	private MyVirtualDisk() throws FileNotFoundException, IOException {
		this(Constants.vdiskName, false);
	}

	@Override
	public void startRequest(DBuffer buf, DiskOperationType operation) {
		Request nextRequest = new Request(buf, operation);
		synchronized(this) {
			
			boolean insertedWrite=false;
			//make sure that writes get prioritized over reads for the same data
			if(operation==DiskOperationType.WRITE)
			{
				int index=0;
				for(Request r:requestQueue)
				{
					if(r.getBuffer().getBlockID()==buf.getBlockID() && r.isRead())
					{
						requestQueue.add(index, nextRequest);
						insertedWrite=true;
						break;
					}
					index++;
				}
			}
			if(!insertedWrite)
				requestQueue.add(nextRequest);

			this.notify();
		}
	}

	@Override
	public void run() {
		while (isRunning) {
			synchronized(this) {
				while (requestQueue.isEmpty()) {
					try {
						this.wait();
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			while (!requestQueue.isEmpty()) {
				Request nextRequest;
				synchronized(this) {
					nextRequest = requestQueue.poll();
				}
				if (nextRequest.isRead()) {
					try {
						super.readBlock(nextRequest.getBuffer());
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else if (nextRequest.isWrite()) {
					try {
						super.writeBlock(nextRequest.getBuffer());
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					System.err.println("UKNOWN OPERATION");
				}
				nextRequest.getBuffer().ioComplete();
			}
		}
		synchronized(doneSignal) {
			isComplete = true;
			doneSignal.notify();
		}
	}
}

