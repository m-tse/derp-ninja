package virtualdisk;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import common.Constants;
import common.Constants.DiskOperationType;
import dblockcache.DBuffer;

public class MyVirtualDisk extends VirtualDisk implements Runnable
{
	private static MyVirtualDisk Instance;
	private Queue<Operation> operations;
	public synchronized static MyVirtualDisk getInstance() 
	{
		if(Instance==null)
		{
			try {
				Instance=new MyVirtualDisk();
				Thread t=new Thread(Instance);
				t.setDaemon(true);
				t.start();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return Instance;
	}
	
	
	
	private MyVirtualDisk()throws FileNotFoundException, IOException {
		super(Constants.vdiskName, false);
		operations=new LinkedList<Operation>();
	}



	@Override
	public void run() 
	{
		while(true)
		{
			synchronized(this)
			{
				//wait for operations to appear
				while(operations.size()==0)
				{
					try {
						System.out.println("disk waiting for an operation!");
						this.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			//clear the list of operations
			while(!operations.isEmpty())
			{
				System.out.println("starting operation");
				Operation curr=operations.poll();
				if(curr.getType()==DiskOperationType.READ)
				{
					try {
						readBlock(curr.getBuffer());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else
				{
					try {
						writeBlock(curr.getBuffer());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				curr.getBuffer().ioComplete();
				System.out.println("operation complete");
			}
		}
	}



	@Override
	public void startRequest(DBuffer buf, DiskOperationType operation) throws IllegalArgumentException,
	IOException
	{
		synchronized(this)
		{
			Operation op=new Operation(operation,buf);
			operations.add(op);
			this.notifyAll();
			System.out.println("operation added, notifying waiting threads!");
		}
		
		
	}

	private class Operation
	{
		private DiskOperationType opType;
		private DBuffer buffer;
		public Operation(DiskOperationType op,DBuffer buf)
		{
			buffer=buf;
			opType = op;
		}
		public DiskOperationType getType()
		{
			return opType;
		}
		public DBuffer getBuffer()
		{
			return buffer;
		}
		
	}
}
