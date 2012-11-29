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
	
	public static MyVirtualDisk getInstance() throws FileNotFoundException, IOException {
		if (myInstance == null) {
			myInstance = new MyVirtualDisk();
		}
		return myInstance;
	}
	
	/*
	 * MyVirtualDisk Constructors
	 */
	
	Queue<Request> _requestQueue;
	
	private MyVirtualDisk(String volName, boolean format) throws FileNotFoundException,
			IOException {
		super(volName, format);
		_requestQueue = new LinkedList<Request>();
		new Thread(this).start();
	}
	
	private MyVirtualDisk(boolean format) throws FileNotFoundException,
	IOException {
		this(Constants.vdiskName, format);
	}
	
	private MyVirtualDisk() throws FileNotFoundException, IOException {
		this(Constants.vdiskName, false);
	}
	
	@Override
	public void startRequest(DBuffer buf, DiskOperationType operation)
			throws IllegalArgumentException, IOException {
		Request nextRequest = new Request(buf, operation);
		_requestQueue.add(nextRequest);
	}

	@Override
	public void run() {
		while (true) {
			if (!_requestQueue.isEmpty()) {
				Request nextRequest = _requestQueue.poll();
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
			}
		}
		
	}

}