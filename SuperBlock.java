/*
* Edited By Chris Knakal and Hyungjin Lee
* CSS 430 Program 5
*/

import java.util.Queue;

class SuperBlock
{
   private final int MAX_INODES = 64;
   public int totalBlocks; // the number of disk blocks
   public int totalInodes; // the number of inodes
   public int freeList;    // the block number of the free list's head


    // Constructor
   public SuperBlock(int diskSize)
   {
       byte[] superBlock = new byte[Disk.blockSize];
       SysLib.rawread(0, superBlock);
       totalBlocks = SysLib.bytes2int(superBlock, 0);
       totalInodes = SysLib.bytes2int(superBlock, 4);
       freeList = SysLib.bytes2int(superBlock, 8);

       if (totalBlocks != diskSize || totalInodes <= 0)
       {
           totalBlocks = diskSize;
           format(MAX_INODES);
       }
   }

   // Constructor: grab ???

   // Formats the disk
   // fileCount: number of inode blocks that the disk needs to free up
   void format (int fileCount)
   {
       totalBlocks = 1000;
       totalInodes = fileCount;
       freeList = (fileCount / 16) + 2;
       for (short i = 0; i < totalInodes; i++)
       {
           Inode inode = new Inode();
           inode.flag = 0;
           inode.toDisk(i);
       }

       // create the freeList linked list
       byte[] block = new byte[Disk.blockSize];
       for (int i = freeList; i < totalBlocks - 1; i++)
       {
           // get this block from disk
           SysLib.rawread(i, block);
           // set its first four bytes to point to the next free block
           SysLib.int2bytes(i + 1, block, 0);
           // write block back to disk
           SysLib.rawwrite(i, block);
       }
       // set the last block's pointer to -1
       setLastBlock(block);

       // write the formatted superBlock to disk
       sync();
   }

    // Writes totalBlocks, totalInodes, and the freeList back to the Disk
    void sync ()
    {
        // create a new byte array to be copied to disk
        byte[] superBlock = new byte[Disk.blockSize];

        // write totalBlocks to the block
        SysLib.int2bytes(totalBlocks, superBlock, 0);
        // write totalInodes to the block
        SysLib.int2bytes(totalInodes, superBlock, 4);
        // write the freeList to the block
        SysLib.int2bytes(freeList, superBlock, 8);

        // write the superBlock to disk
        SysLib.rawwrite(0, superBlock);

    }

    // Finds and returns the blockID of the next free block from the freeList
    int getBlock()
    {
        int index = freeList;
        if (index != -1)
        {
            byte[] block = new byte[Disk.blockSize];
            // read in the next-next free block
            SysLib.rawread(index, block);
            // set the new next free block
            freeList = SysLib.bytes2int(block, 0);

            // clear the return block's data
            SysLib.int2bytes(0, block, 0);
            SysLib.rawwrite(index, block);
        }
        return index;
    }

    // Add the blockID to the end of the freeList
    boolean returnBlock (int blockID)
    {
        if (blockID < totalBlocks && blockID > 0)
        {
            byte[] readBlock = new byte[Disk.blockSize];
            byte[] writeBlock = new byte[Disk.blockSize];

            // set writeBlock's next free block to -1
            SysLib.int2bytes(-1, writeBlock, 0);

            if (freeList == -1) // the free list was empty. point to blockID as head
            {
                freeList = blockID;
                SysLib.rawwrite(blockID, writeBlock);
                return true;
            }
            else
            {
                int next = freeList;
                int current = next;

                // start reading from the freeList until you find the block with
                //  -1 as the next free block
                while (next != -1) {
                    SysLib.rawread(next, readBlock);
                    current = next; // set current block to the one we just read
                    next = SysLib.bytes2int(readBlock, 0); // next block to read
                }

                // we found the last free block
                // set the current block to point to blockID
                SysLib.int2bytes(blockID, readBlock, 0);
                SysLib.rawwrite(current, readBlock);

                // write writeBlock to disk
                SysLib.rawwrite(blockID, writeBlock);
                return true;
            }
        }
        return false;
    }

    // Sets the freeList to the passed in int
    boolean setFreeList(int index)
    {
        if (index > 0 && index < 1000)
        {
            freeList = index;
            return true;
        }
        else
        {
            return false;
        }
    }

    // Sets the last block's free pointer during formatting
    private void setLastBlock(byte[] block)
    {
        // make it point to a non existant disk block
        SysLib.int2bytes(-1, block, 0);
        // write last disk block back to disk
        SysLib.rawwrite(totalBlocks - 1, block);
    }
}
