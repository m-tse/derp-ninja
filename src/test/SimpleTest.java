package test;

import java.io.FileNotFoundException;
import java.io.IOException;

import dblockcache.DBuffer;
import dblockcache.MyDBufferCache;

public class SimpleTest {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException 
	{
//		
//		MyVirtualDisk disk=MyVirtualDisk.getInstance();
//		myDBufferCache cache=new myDBufferCache(4);
//		DBuffer buf=cache.getBlock(0);
//		byte[] buffer=new byte[1024];
//		for(int x=0;x<256;x++)
//			buffer[x]=(byte) x;
//		buf.write(buffer, 0, 256);
//		//System.out.println("about to start push");
//		buf.startPush();
//		buf.waitClean();
//		
		/*
		byte[] buffer1=new byte[1024];
		System.out.println(buf.read(buffer1, 0, 256));
		cache.sync();
		for(int x=0;x<512;x++)
			System.out.println(buffer1[x]);
			*/
		
		
		byte[] buffer1=new byte[1024];
		
		//MyDBufferCache cache=new MyDBufferCache(4);
//		DBuffer buf=cache.getBlock(0);
//		//byte[] buffer=new byte[1024];
//		buf.startFetch();
//		buf.waitValid();
//		System.out.println(buf.read(buffer1, 0, 256));
//		cache.sync();
//		for(int x=0;x<512;x++)
//			System.out.println(buffer1[x]);
			
	}

}
