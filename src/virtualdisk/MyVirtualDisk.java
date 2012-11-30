package virtualdisk;

import java.io.FileNotFoundException;
import java.io.IOException;

import common.Constants;

public class MyVirtualDisk extends VirtualDisk
{
	private static String volName;
	private static MyVirtualDisk Instance;
	public synchronized static MyVirtualDisk getInstance() 
	{
		if(Instance==null)
		{
			try {
				Instance=new MyVirtualDisk();
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
		super(volName, false);
	
	}

	
}
