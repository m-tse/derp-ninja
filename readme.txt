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
NetId3: 

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

    

/////////////
Middle Layer:
/////////////

  MyDBufferCache.java
    //also implemented as a singleton.  When the DFS instantiates a DBufferCache, it will receive the already statically created cache if it exists, otherwise it will instantiate a new one.  
    HashMap<Integer, MyDBuffer> bufferMemory; //
    RestrictedQueue<MyDBuffer> bufferQueue; //    
    boolean allBuffersBusy;
    private static final int NUMRESERVED_BUFFERS; //



  MyDBuffer.java
    




/////////////////////////
Lower Layer: Virtual Disk
//////////////////////////

  MyVirtualDisk.java
    MyVirtualDisk implements Runnable, so it is a thread.  In its run() function, it constantly checks the requestQueue for new requests, and serves them appropriately.  

  Singleton Design Pattern
  We used the singleton design pattern on the Virtual Disk, for the purpose of maintaining only 1 instance of the virtual disk at all times.  When another class tries to get an instance of VirtualDisk, VirtualDisk will return the current statically stored Virtual Disk, or a newly generated one if one does not exist yet.

  Request.java
    a helper class that encapsulates the data behind each request, such as the buffer the request comes from, and whether it is a read or write operation.




####################################
# Feedback on the lab
####################################

How did you find the lab?

##################################
# Additional comments
##################################

Anything else you would like us to evaluate your lab.
List of collaborators involved including any online references/citations.
