package virtualdisk;
import common.Constants;
import common.Constants.DiskOperationType;

import dblockcache.DBuffer;


public class Request {

	private DBuffer _buf;
	private DiskOperationType _operation;
	
	public Request(DBuffer buf, DiskOperationType operation) {
		_buf = buf;
		_operation = operation;
	}
	
	public boolean isRead() {
		return _operation.equals(Constants.DiskOperationType.READ);
	}
	
	public boolean isWrite() {
		return _operation.equals(Constants.DiskOperationType.WRITE);
	}
	
	public DBuffer getBuffer() {
		return _buf;
	}
	
}
