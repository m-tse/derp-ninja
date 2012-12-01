package common;

/* typedef DFileID to int */
public class DFileID {
	private int _dFID;
	
	public DFileID(int dFID) {
		_dFID = dFID;
	}
	
	public int getInt() {
		return new Integer(_dFID);
	}
	
	public void clearFileID() {
		_dFID = 0;
	}
	
	
}
