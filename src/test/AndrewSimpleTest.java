package test;

import java.io.FileNotFoundException;
import java.io.IOException;

import common.DFileID;
import dfs.myDFS;



public class AndrewSimpleTest {


	public static void main(String[] args) throws FileNotFoundException, IOException {

		myDFS dfs = new myDFS();	
		String s = "Hello DeFiler World!"; 
		byte[] sBytes = s.getBytes();
		byte[] rBytes = new byte[sBytes.length];
		DFileID myFileID = dfs.createDFile();
		dfs.write(myFileID, sBytes, 0, sBytes.length);

		dfs.read(new DFileID(0), rBytes, 0, rBytes.length);
		String sp = new String(rBytes);

		System.out.println(sp);
		System.out.println("COMPLETE");

	}






}