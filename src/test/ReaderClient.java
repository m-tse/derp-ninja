package test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import common.DFileID;

import dfs.DFS;

public class ReaderClient implements Runnable {
	DFS myDFS;
	ArrayList<Integer> completeCounter;

	public ReaderClient(DFS dfs, ArrayList<Integer> cc) {
		myDFS = dfs;
		completeCounter=cc;
	}

	@Override
	public void run() {
		for (int i = 0; i < 1; i++) {
			List<DFileID> fileList = myDFS.listAllDFiles();
			for (DFileID d : fileList) {
				byte[] readToArray = new byte[myDFS.sizeDFile(d)];
				try {
					myDFS.read(d, readToArray, 0, readToArray.length);
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
			}
		}
		
		notifyTester();
		
		//somehow signal the tester
	}
	
	public synchronized void notifyTester(){
		completeCounter.add(0);
	}

}
