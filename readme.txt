##############################################
# Please do not modify the format of this file
################
# Team Info
################

Name1: Matthew Tse
NetId1: mst17

Name2: Andrew Shim	
NetId2:	aks35

Name3: Afanasiy Yermakov
NetId3: asy9

###############
# Time spent
###############

NetId1: 10 hours 
NetId2: 10 hours 
NetId3: 10 hours 

################
# Files to submit
# Design phase: 
################

README #A plain txt file or a pdf file; Fill-in the design/implementation section

################
# Files to submit
# Final phase: 
################

README #Revised design/implementation details section; updated hours

An executable *.jar file including the source code (*.java files) #Include the test case files when appropriate.

####################################
# Design/Implementation details
####################################

This section should contain the implementation details and a overview of the
results. 

You are required to provide a good README document along with the
implementation details. In particular, you can pseudocode to describe your
implementation details where necessary. However that does not mean to
copy/paste your Java code. Rather, provide clear and concise text/pseudocode
describing the primary algorithms (for e.g., scheduling choices you used) and
any special data structures in your implementation. We expect the design and
implementation details to be 3-4 pages. A plain textfile is encouraged.
However, a pdf is acceptable. No other forms are permitted.

In case of lab is limited in some functionality, you should provide
the details to maximize your partial credit.

////////////////
Upper Layer: DFS
////////////////

    MyDFS.java extends DFS.java.  
    	//implemented as a singleton, when trying to instantiate a new copy, will return the existing static copy if it exists.  This is because if we can't have multiple DFS' concurrently writing and reading

        DBufferCache buffer cache; //its only connection to the underlying disk		      
	DFileID[] fileIDs;  //an array of fileIDs, filled in ascending index order

	INode[] iNodes;  //List of INodes that correspond to fileIDs, the 0th index INode corresponds to the 0th index fileID

	int[] freeMap;  //an array that corresponds to blockID's.  It tells whether a particular blockID is taken or free.

	readINodeRegion() //called when MyDFS is initialized, attempts to read all the preexisting data in the VDF, and update the state of the arrays fileIDs, iNodes, and freeMap to take into account previously stored data

	DFileID createDFile()  //creates a new entry in the fileIDs array, also creates a new entry in the iNodes array

	destroyDFile(DFileID dFID) //finds that fildID, destroys the entry in fileIDs array, clears the INode, sets the entry in iNodes array to null

	read(....) //gets the iNode associated with the DFID, logic to get the associated blocks is located in the iNode class.  With those blocks, we get DBuffers from the DBufferCache, and call the read function in those DBuffers

	write(....) //gets the iNode associated to the DFID, gets the associated blocks through the iNode(logic in iNode class).  Grabs a DBuffer from the BufferCache using the blockID, calls the write function from that DBuffer

    INode.java
	Abstraction that connects a File(represented by DFILEID) with the associated blocks that are used for that file's storage.  The first few bytes of the INode store information, bytes 0-3 for fileID, and bytes 4-7 for fileSize.  The rest of the bytes are used for the blocks.
////add more

    

/////////////////
Middle Layer:
///////////////

    MyDBufferCache.java
	//also implemented as a singleton.  When the DFS instantiates a DBufferCache, it will receive the already statically created cache if it exists, otherwise it will instantiate a new one.  

    	RestrictedQueue<MyDBuffer> bufferQueue; //We will instantiate a set number of DBuffers at the start, and change the blocks that these set DBuffers point to, therefore we do not have to constantly create and destroy new DBuffers

    	DBuffer getBlock(int blockID) //given a blockID, return a DBuffer that is connected with that blockID.  First check if a DBuffer in the cache has that blockID, if so return it.  If not, evict that blockID(concurrently wait until the most unpopular DBuffer is free, then set that DBuffer blockid to the blockID needed)

    	void sync() //loop through all the DBuffers in the bufferQueue, starts Push on all of them.  Then has those buffers wait on a clean().

    	synchronized void signalBufferRead() //notify that a buffer is ready

    	synchronized void waitOnBuffers() //wait until a buffer is free


    RestrictedQueue.java
	//simply a queue that cannot be increased beyond a fixed size, useful since our DBufferCache is of fixed size.  We modified the add functions, so it does not add after the queue has reached a fixed capacity



    MyDBuffer.java

	byte[] myBuffer //the byte array for the block that this buffer is currently associated with

	int blockID //the blockID that this DBuffer is currently associated with

	Object cleanSignal, validSignal //synchronization lock objects.  ValidSignal tells whether the current content stored in this Buffer is valid(correct in relation to the disk and other writers).  CleanSignal tells us if this buffer is cleanly synchronized with the VDF underneath or not.

	boolean isHeld, isValid, isClean, isFetching, isPushing, isPinned //various booleans for synchronizing the various methods that are called upon the DBuffer.  Self explanatory names

	MyVirtualDisk VDF //link to the singleton VDF

	int read(....) //read to a buffer if the above booleans are satisfied

	int write(....) //write to a buffer if a combination of the above booleans are satisfied




/////////////////////////
Lower Layer: Virtual Disk
//////////////////////////

  MyVirtualDisk.java
    //implemented as a singleton.  When called, it will return the currently existing static VDF, or instantiate a new one

    Queue<Request> requestQueue // a queue of requests

    void startRequest(...) //in a synchronized block, create a new request with the current buffer and an operation(either read or write), and add the request to the queue

    void run() // implemented as a thread, it constantly checks the requestQueue for new requests, and serves them appropriately.  If the queue is empty, wait().  If the queue has elements, in a synchronized block, poll() the first element off the queue, and either read or write it.

    void format() //return a new instance of MyVirtualDisk(true), passing the true boolean causes the VirtualDisk.java class to zero out all the data and format the disk.


  Request.java
    a helper class that encapsulates the data behind each request, such as the buffer the request comes from, and whether it is a read or write operation.

//////////////
Testing
////////////

    JUnitTests.java
	//collection of JUnit tests

	testBasicWriteThenRead() //very simply creates a byte[] writes it, then reads it and checks equality

	testCreateAndDelete() //create a DFile, delete it, check that DFS is clean of it
	testOffset() //write using an offset integer, and read using an offset

	testPersistence() //test that the VDF contents persist.  Instantiate a DFS, write something, instantiate another DFS and look for that previously written file

	testMaxSizeOfDFS() //write up to the max number of files in the DFS

	testConcurrentReadingClients() //create multiple reader threads, that read concurrently from the DFS, check that they all finish without deadlocks

	testAsynchronousWritingClients() //create multiple writer threads that write to the DFS, check that hey all finish without deadlocks

	testAsynchronousReadingAndWriting() //create multiple reader and writer threads, check that they all finish without deadlocks

	testVeryLargeFiles() //create a very large file, up to the max file size, and check that it works correctly

	testFormat() //check that passing in format=true will actually format the underlying disk correctly

	testSpaceIsRecycled() //writes files such that their size fills up the entire VDF, then deletes them all.  Then rewrites all of them again, check that the size of the written files matches the intended size(i.e. write didn't fail prematurely due to not enough space).  If space is recycled correctly, this should work.  


    WriterClient.java
	thread class that writes a certain number of files to the DFS, then ends and signals to the tester that it completed

    ReaderWriter.java
	a thread class that reads all the DFiles from the DFS n times, then ends and signals the tester that it has completed

####################################
# Feedback on the lab
####################################

How did you find the lab?

##################################
# Additional comments
##################################

Anything else you would like us to evaluate your lab.
List of collaborators involved including any online references/citations.
