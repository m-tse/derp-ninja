package test;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

import common.Constants;
import common.DFileID;

import dfs.DFS;
import dfs.myDFS;

public class JUnitTests {
	/*
	 * REQUIREMENTS
	 * 
	 *  1. Work correctly with multiple client threads
	 *  2. Free of races, starvation, crashes, deadlocks, storage leaks
	 *  3. Recycle memory and storage correctly
	 *  4. Consistent file model
	 *  	- DFile continues to exist until destroyed
	 *  	- One DFile per DFileID
	 *  	- Read returns data that was most recently written 
	 */
	
	
	
	@Test
	public void test() throws FileNotFoundException, IOException {
		JUnitTests j = new JUnitTests();
	
		
		testBasicWriteThenRead();
		testMaxSizeOfDFS();
		testCreateAndDelete();
//		testVeryLargeFiles();
//		testOffset();
//		testFormat();
//		testConcurrentWriting();
//		testConcurrentReading();
		
	}
	
	public void testBasicWriteThenRead() throws FileNotFoundException, IOException{
		DFS myDFS = new myDFS(true); //start off by formatting the drive
		DFileID newDFileID = myDFS.createDFile();
		String helloString = "Hello World!";
		byte[] WrittenFromBuffer = helloString.getBytes();
		myDFS.write(newDFileID, WrittenFromBuffer, 0, WrittenFromBuffer.length);
		//HelloWorld String was written to a DFile
		
		byte[] readBuffer = new byte[WrittenFromBuffer.length];
		myDFS.read(newDFileID, readBuffer, 0, readBuffer.length);
		//check that the read buffer equals the buffer we wrote with
		assertTrue(readBuffer==WrittenFromBuffer);
		
		
		//Now test that multiple DFiles can be stored correctly and read/written from

		
		
	}
	
	public void testMaxSizeOfDFS() throws FileNotFoundException, IOException{
		DFS myDFS = new myDFS(true);
		DFileID[] dfileIDs = new DFileID[Constants.MAX_NUM_FILES];
		//write them all
		for(int i = 0;i<dfileIDs.length;i++){
			dfileIDs[i]=myDFS.createDFile();
			String writeString = "Hello DFS!"+ Integer.toString(i);
			byte[] toWriteBuffer = writeString.getBytes();
			myDFS.write(dfileIDs[i], toWriteBuffer, 0, toWriteBuffer.length);
		}
		
		//now read them all and check equality
		for(int i = 0;i<dfileIDs.length;i++){
			byte[] toReadBuffer = new byte[myDFS.sizeDFile(dfileIDs[i])];
			myDFS.read(dfileIDs[i], toReadBuffer, 0, toReadBuffer.length);
			
			String testString = "Hello DFS!"+Integer.toString(i);
			byte[] equalityBuffer = testString.getBytes();
			assertTrue(toReadBuffer==equalityBuffer);
		}
		
		
	}
	
	public void testCreateAndDelete() throws FileNotFoundException, IOException{
		DFS myDFS = new myDFS(true);
		DFileID newDFileID = myDFS.createDFile();
	}

}
