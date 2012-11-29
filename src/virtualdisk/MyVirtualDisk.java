package virtualdisk;

import java.io.FileNotFoundException;
import java.io.IOException;

import common.Constants;

public class MyVirtualDisk extends VirtualDisk
{
	private static String volName;
	private static MyVirtualDisk Instance;
	public synchronized static MyVirtualDisk getInstance() throws FileNotFoundException, IOException
	{
		if(Instance==null)
		{
			Instance=new MyVirtualDisk();
		}
		return Instance;
	}
	
	
	
	private MyVirtualDisk()throws FileNotFoundException, IOException {
		super(volName, false);
	
	}

	
}
