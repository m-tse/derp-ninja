package test;

import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import common.Constants;
import common.DFileID;

import dfs.DFS;
import dfs.MyDFS;

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
	

	
//	@Test
	public void testBasicWriteThenRead() throws FileNotFoundException, IOException{
		DFS myDFS = new MyDFS(true); //start off by formatting the drive
		DFileID newDFileID = myDFS.createDFile();
		String helloString = "Hello World!";
		byte[] WrittenFromBuffer = helloString.getBytes();
		myDFS.write(newDFileID, WrittenFromBuffer, 0, WrittenFromBuffer.length);
		//HelloWorld String was written to a DFile
		
		byte[] readBuffer = new byte[WrittenFromBuffer.length];
		myDFS.read(newDFileID, readBuffer, 0, readBuffer.length);
		//check that the read buffer equals the buffer we wrote with
		assertTrue(new String(readBuffer).equals(new String(WrittenFromBuffer)));
		
		
		//Now test that multiple DFiles can be stored correctly and read/written from

		
		
	}

	@Test
	public void testCreateAndDelete() throws FileNotFoundException, IOException{
		DFS myDFS = new MyDFS(true);
		int baseSize = myDFS.listAllDFiles().size();
		assertTrue(myDFS.listAllDFiles().size()==baseSize); //myDFS listDFile should be empty at start
		DFileID newDFileID = myDFS.createDFile();
		assertTrue(myDFS.listAllDFiles().size()==baseSize+1); //myDFS should now have 1 DFile
		myDFS.destroyDFile(newDFileID);
		assertTrue(myDFS.listAllDFiles().size()==baseSize); //myDFS should be back to 0 DFiles now
		
		DFileID[] dfileids = new DFileID[Constants.MAX_NUM_FILES];
		for(int i = 0;i<dfileids.length;i++)	dfileids[i]=myDFS.createDFile();
		assertTrue(myDFS.listAllDFiles().size()==Constants.MAX_NUM_FILES); //should exist the max number of dfiles
		
		for(int i = 0;i<dfileids.length;i++){
			myDFS.destroyDFile(dfileids[i]);
		}
		assertTrue(myDFS.listAllDFiles().size()==0); //should be no dfiles now
		
		
		
	}
	
	@Test
	public void testOffset() throws FileNotFoundException, IOException{
		DFS myDFS = new MyDFS(false);
		String testString = "asdfasdfasdf";
		byte[] writeFromBuffer = testString.getBytes();
//		int bytesPerChar = writeFromBuffer.length/testString.length();
		int offset = 5;
		DFileID dfid = myDFS.createDFile();
		myDFS.write(dfid, writeFromBuffer, 0, writeFromBuffer.length);
		byte[] readToBuffer = new byte[writeFromBuffer.length-offset];
		myDFS.read(dfid, readToBuffer, offset, readToBuffer.length-offset);
		
//		String readToString = new String(readToBuffer);
//		String writeFromBufferString = new String(writeFromBuffer);
		
		for(int i = 0;i<readToBuffer.length-offset;i++){
			assertTrue(readToBuffer[i]==writeFromBuffer[i+offset]);
		}
	}
	@Test
	public void testPersistence() throws FileNotFoundException, IOException{
		DFS dfs1 = new MyDFS();
		byte[] buffer = "asdfasdfasdf".getBytes();
		DFileID dfid = dfs1.createDFile();
		dfs1.write(dfid, buffer, 0, buffer.length);
		
		//now instantiate another dfs, and check persistence of the first file
		DFS dfs2 = new MyDFS();
		List<DFileID> dfileids = dfs2.listAllDFiles();
		assertTrue(dfileids.size()==1); //there should already exist 1 dfile
		DFileID firstDFile = dfileids.get(0);
		byte[] readToBuffer = new byte[dfs2.sizeDFile(firstDFile)];
		dfs2.read(firstDFile, readToBuffer, 0, readToBuffer.length);
		assertTrue(buffer.equals(readToBuffer));
		
		
	}
	
	@Test
	public void testMaxSizeOfDFS() throws FileNotFoundException, IOException{
		DFS myDFS = new MyDFS(true);
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
			assertTrue(new String(toReadBuffer).equals(new String(equalityBuffer)));
		}
		
		
	}	
	
	@Test
	public void testConcurrentReadingClients(){
		DFS myDFS = new MyDFS(true);
		for(int i = 0;i<10;i++){
			ReaderClient r = new ReaderClient(myDFS);
		}
		//check that they all finished with no deadlocks
	}
//	
//	@Test
//	public void testVeryLargeFiles(){
//		
//	}
//
//	@Test
//	public void testFormat(){
//		
//	}
//
//
//	@Test
//	public void testReadReturnsMostRecentlyWrittenData(){
//		
//	}
}
