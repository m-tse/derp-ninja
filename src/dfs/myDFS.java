package dfs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import common.Constants;
import common.DFileID;
import common.INode;
import dblockcache.DBufferCache;
import dblockcache.myDBufferCache;

public class myDFS extends DFS{
	DFileID[] _DFileIDs;
	INode[] _INodes;
	DBufferCache _DBufferCache;
	
	public myDFS(String volname, boolean format) throws FileNotFoundException, IOException{
		super(volname, format);
		_DFileIDs = new DFileID[Constants.MAX_NUM_FILES];
		_INodes = new INode[Constants.MAX_NUM_FILES];
		_DBufferCache = new myDBufferCache();
	}
	
	public myDFS(boolean format) throws FileNotFoundException, IOException{
		this(Constants.vdiskName, format);
	}
	
	public myDFS() throws FileNotFoundException, IOException{
		this(Constants.vdiskName, false);// should it be initialized to false?
	}
	@Override
	public boolean format() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DFileID createDFile() {

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void destroyDFile(DFileID dFID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int read(DFileID dFID, byte[] buffer, int startOffset, int count) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int write(DFileID dFID, byte[] buffer, int startOffset, int count) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int sizeDFile(DFileID dFID) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<DFileID> listAllDFiles() {
		ArrayList<DFileID> dfiles = new ArrayList<DFileID>();
		for (DFileID s:_DFileIDs) dfiles.add(s);
		return dfiles;
	}

}
