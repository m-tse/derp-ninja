package test;

import common.Constants;
import common.DFileID;

import dfs.MyDFS;

public class Tester {

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

	public static void create() {
		MyDFS dfs = new MyDFS();

		DFileID[] fileIDs = new DFileID[Constants.MAX_NUM_FILES];
		for (int i = 0; i < fileIDs.length; ++i) {
			DFileID dfid = new DFileID(i);
			if (dfs.fileExists(dfid)) fileIDs[i] = dfid;
			else fileIDs[i] = dfs.createDFile();
		}

		for (DFileID fID: fileIDs) {
			byte[] buffer = ("Hello Devil Filer!" + fID).getBytes();
			dfs.write(fID, buffer, 0, buffer.length);
		}

		for (DFileID fID: fileIDs) {
			byte[] buffer = new byte[100];
			dfs.read(fID, buffer, 0, buffer.length);
			System.out.println("STRING: " + new String(buffer));
		}
	}

	public static void after() {
		MyDFS dfs = new MyDFS();
		System.out.println(dfs.listAllDFiles().toString());
		for (DFileID fID: dfs.listAllDFiles()) {
			byte[] buffer = new byte[100];
			dfs.read(fID, buffer, 0, buffer.length);
			System.out.println("STRING: " + new String(buffer));
		}
	}

	public static void main(String[] args) {
		create();
		after();
	}
	
	
	
	
}
