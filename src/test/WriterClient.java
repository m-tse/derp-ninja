package test;



import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import common.DFileID;

import dfs.DFS;

public class WriterClient implements Runnable {
	DFS myDFS;
	ArrayList<Integer> completeCounter;
	int myID;

	public WriterClient(int id, DFS dfs, ArrayList<Integer> cc) {
		myDFS = dfs;
		completeCounter=cc;
		myID=id;
	}

	@Override
	public void run() {
		for (int i = 0; i < 5; i++) {
			String writeString = "WriterClient:"+Integer.toString(myID)+" "+Integer.toString(i);
			byte[] buffer = writeString.getBytes();
			DFileID dfid = myDFS.createDFile();
			try {
				myDFS.write(dfid, buffer, 0, buffer.length);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			byte[] readToBuffer = new byte[buffer.length];
			try {
				myDFS.read(dfid, readToBuffer, 0, buffer.length);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(!new String(readToBuffer).equals(new String(buffer))){
				break;
			}
		}
		
		notifyTester();
		
		//somehow signal the tester
	}
	
	public synchronized void notifyTester(){
		completeCounter.add(0);
	}

}
