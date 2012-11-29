package test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import common.DFileID;
import common.INode;

public class INodeToBytesTest {

	
	public static void writeFile() throws IOException {
		
		RandomAccessFile file = new RandomAccessFile("test.dat", "rws");
		file.setLength(100);
		INode inode = new INode(new DFileID(1));
		inode.setFileSize(10);
		int[] blocks = {1,2,3,4,5};
		inode.setBlockArray(blocks);
		
		byte[] iNodeBytes = new byte[28];
		int index = 0;
		ByteBuffer bytes;
		byte[] result;
		
		bytes = ByteBuffer.allocate(4);
		bytes.putInt(inode.getFileSize());
		result = bytes.array();
		for (byte b: result) {
			iNodeBytes[index] = b;
			++index;
		}

		bytes = ByteBuffer.allocate(4);
		bytes.putInt(inode.getFileSize());
		result = bytes.array();
		for (byte b: result) {
			iNodeBytes[index] = b;
			++index;
		}
		
		for (int i: blocks) {
			bytes = ByteBuffer.allocate(4);
			bytes.putInt(i);
			result = bytes.array();
			for (byte b: result) {
				iNodeBytes[index] = b;
				++index;
			}
		}
		
		System.out.println(index);
		file.write(iNodeBytes);
		file.close();
	}
	
	
	public static void readFile() throws IOException {
		
		RandomAccessFile file = new RandomAccessFile("test.dat", "rws");
		byte[] iNodeBytes = new byte[28];
		file.read(iNodeBytes);
		
		byte[] fileSize1 = new byte[4];
		byte[] fileSize2 = new byte[4];
		for (int i = 0 ; i < 4; ++i) {
			fileSize1[i] = iNodeBytes[i];
		}
		ByteBuffer bb;
		bb = ByteBuffer.wrap(fileSize1);
		int fs1 = bb.getInt(); 
		
		for (int i = 4 ; i < 8; ++i) {
			fileSize2[i-4] = iNodeBytes[i];
		}
		bb = ByteBuffer.wrap(fileSize2);
		int fs2 = bb.getInt(); 
		
		int[] iblocks = new int[5];
		int index = 0;
		byte[] blocks = new byte[4];
		for (int i = 8 ; i < iNodeBytes.length; ++i) {
			blocks[i%4] = iNodeBytes[i];
			if ((i+1) % 4 == 0) {
				bb = ByteBuffer.wrap(blocks);
				int inte = bb.getInt(); 
				iblocks[index] = inte; 
				index++;
				blocks = new byte[4];
			}
		}
		
		for (int i : iblocks) {
			System.out.println(i);
		}
		
	}
	
	
	
	public static void main(String[] args) throws IOException {
//		writeFile();
//		readFile();
		System.out.println(5 / 2);
		System.out.println(4 / 3);
	}


	
}
