package common;

public class INode {
	private DFileID _DFileID;
	private int _Size;
	private int[] _blocks;
	public INode(DFileID dfID){
		_DFileID=dfID;
		_Size=Constants.INODE_SIZE;
		_blocks=new int[_Size];
	}
	
	public int getSize(){
		return _Size;
	}
}
