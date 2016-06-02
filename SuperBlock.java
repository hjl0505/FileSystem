class SuperBlock
{
   public int totalBlocks; // the number of disk blocks
   public int totalInodes; // the number of inodes
   public int freeList;    // the block number of the free list's head

    // Constructor
   public SuperBlock(int diskSize)
   {

   }

   // Constructor: grab

    // Formats the disk
    // fileCount: number of inode blocks that the disk needs to free up
   void format (int fileCount)
   {

   }

    // Writes totalBlocks, totalInodes, and the freeList back to the Disk
    void sync ()
    {

    }

    // Finds and returns the blockID of the next free block from the freeList
    int getBlock()
    {
        return 0;
    }

    // Add the blockID to the end of the freeList
    boolean returnBlock (int blockID)
    {
        if (blockID < totalBlocks)
        {
            // do something
            return true;
        }
        return false;
    }
}

/*
* Questions:
*
*
*/
