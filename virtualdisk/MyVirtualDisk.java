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
	
	public static MyVirtualDisk getInstance() {
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
			myInstance = new MyVirtualDisk(true);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	/*
	 * MyVirtualDisk Constructors
	 */
	private Queue<Request> requestQueue;
	
	private MyVirtualDisk(String volName, boolean format) throws FileNotFoundException,
			IOException {
		super(volName, format);
		requestQueue = new LinkedList<Request>();
		Thread t = new Thread(this);
		t.setDaemon(true);
		t.start();
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
			requestQueue.add(nextRequest);
			this.notify();
		}
	}

	@Override
	public void run() {
		while (true) {
			while (requestQueue.isEmpty()) {
				try {
					synchronized(this) {
						this.wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
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
	}

}