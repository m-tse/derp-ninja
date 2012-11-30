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
	//the index i DfileID corresponds to the index i Inode
	DBufferCache _DBufferCache;
	
	public myDFS(String volname, boolean format) throws FileNotFoundException, IOException{
		super(volname, format);
		_DFileIDs = new DFileID[Constants.MAX_NUM_FILES];
		_INodes = new INode[Constants.MAX_NUM_FILES];
		_DBufferCache = new myDBufferCache(Constants.CACHE_SIZE);
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
		DFileID returnfile=null;
		for(int i = 0;i<_DFileIDs.length;i++){
			if(_DFileIDs[i]==null){
				_DFileIDs[i] = new DFileID(i);
				returnfile = _DFileIDs[i];
				_INodes[i] = new INode(_DFileIDs[i]);
			}
		}
		// TODO Auto-generated method stub
		return returnfile;
	}

	@Override
	public void destroyDFile(DFileID dFID) {
		for(int i = 0;i<_DFileIDs.length;i++){
			if(_DFileIDs[i]==dFID){
				_DFileIDs[i]=null;
				_INodes[i]=null;
				//maybe this works?
			}
		}
		
	}

	@Override
	public int read(DFileID dFID, byte[] buffer, int startOffset, int count) {
		
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int write(DFileID dFID, byte[] buffer, int startOffset, int count) {
		INode inode = _INodes[dFID.getDFID()];
		
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int sizeDFile(DFileID dFID) {
		return _INodes[dFID.getDFID()].getSize();
	}

	@Override
	public List<DFileID> listAllDFiles() {
		ArrayList<DFileID> dfiles = new ArrayList<DFileID>();
		for (DFileID s:_DFileIDs) dfiles.add(s);
		return dfiles;
	}

}
