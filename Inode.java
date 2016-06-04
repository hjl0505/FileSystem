// Edited By Chris Knakal and Hyungjin Lee
// CSS 430 Program 5
// 5/30/2016


public class Inode
{
   private final static int iNodeSize = 32;       // fix to 32 bytes
   public final static int directSize = 11;      // # direct pointers
   public static final int INODE_FULL = -1;
   public static final int INDIRECT_EMPTY = 0;
   public static final int INODE_AVAILABLE = 1;

   public int length;                             // file size in bytes
   public short count;                            // # file-table entries pointing to this
   public short flag;                             // 0 = unused, 1 = used, 2 = read, 3 = write, 4 = pendingDelete
   public short direct[] = new short[directSize]; // direct pointers
   public short indirect;                         // a indirect pointer

    // a default constructor
   Inode()
   {
      length = 0;
      count = 0;
      flag = 1;
      // initialize pointers to -1
      for ( int i = 0; i < directSize; i++ )
      {
          direct[i] = -1;
      }
      indirect = -1;
   }

    // retrieving inode from disk
   Inode(short iNumber)
   {
      // read in disk block where Inode lives
      // disk block = iNumber / 16
      byte[] data = new byte[Disk.blockSize];
      SysLib.rawread((iNumber / 16) + 1, data);

      // read Inodes 32 bytes within the disk block
      // Inode bytes within disk block start at (iNumber % 16) * 32
      int startIndex = (iNumber % 16) * 32;
      length = SysLib.bytes2int(data, startIndex);
      count = SysLib.bytes2short(data, startIndex + 4);
      flag = SysLib.bytes2short(data, startIndex + 6);
      for (int i = 0; i < directSize; i++)
      {
          direct[i] = SysLib.bytes2short(data, (startIndex + 8) + (2 * i));
      }
      indirect = SysLib.bytes2short(data, startIndex + 30);
   }

    // save to disk as the i-th inode
   int toDisk(short iNumber)
   {
      if (iNumber < 0)
      {
          return -1;
      }
      // Get the disk block where this Inode belongs
      // disk block = iNumber / 16
      byte[]data = new byte[Disk.blockSize];
      int block = ( iNumber/16 ) + 1;
      SysLib.rawread(block, data);

      // Inode bytes within disk block start at (iNumber % 16) * 32
      int startIndex = (iNumber % 16) * 32;
      SysLib.int2bytes(length, data, startIndex);
      SysLib.short2bytes(count, data, startIndex + 4);
      SysLib.short2bytes(flag, data, startIndex + 6);
      for (int i = 0; i < directSize; i++) // store direct pointers
      {
          SysLib.short2bytes(direct[i], data, (startIndex + 8) + (2 * i));
      }
      SysLib.short2bytes(indirect, data, startIndex + 30); // store indirect pointer
      SysLib.rawwrite(block, data);
      return 1;
   }

    //returns the block that contains the seekPtr
   int getBlockID (int seekPtr)
   {
       int tempID = seekPtr / Disk.blockSize;
       int blockID = -1;
       if (tempID < directSize)
       {
            blockID = direct[tempID];
            //SysLib.cout("Inode: Getting direct block: " + blockID + "\n");
            //SysLib.cout("Inode:   from direct[" + tempID + "]\n");
       }
       else if (indirect != -1)
       {
           //SysLib.cout("Inode: Getting indirect block " + blockID);
           byte[] indirectData = new byte[Disk.blockSize];
           SysLib.rawread(indirect, indirectData);
            int offset = (tempID - directSize) * 2;
            blockID = SysLib.bytes2short(indirectData, offset); // *short to int
       }
       //SysLib.cout("Inode: tempID = " + tempID + "\n");
       //SysLib.cout("Inode: Indirect points to disk block " + indirect + "\n");
       //SysLib.cout("Inode: Returning Block ID " + blockID + ".\n");
       return blockID;
   }

    // Add a free block to the inode
    // Returns the status of the inode
    int addBlock (int seekPtr, short freeBlock)
    {
        int tempID = (seekPtr / Disk.blockSize);
        if(tempID < directSize)
        {
            direct[tempID] = freeBlock; // add to direct block
        }
        else if (indirect == -1)
        {
            //indirect = freeBlock;  BADDDDDDDDDD. SOOOO BADDDDDDDD. BUGGGGGGGGGGGGGGGGG.
            return INDIRECT_EMPTY;
        }
        else
        {
            byte[] indirectData = new byte[Disk.blockSize];
            SysLib.rawread(indirect, indirectData);
            int offset = 0;
            short blockID = SysLib.bytes2short(indirectData, offset);
            while(blockID != -1)
            {
                if (offset >= Disk.blockSize)
                {
                  return INODE_FULL; // Indirect block is full
                }

                offset += 2;
                blockID = SysLib.bytes2short(indirectData, offset);
            }

            SysLib.short2bytes(freeBlock, indirectData, offset); // add block to indirect block
            SysLib.rawwrite(indirect, indirectData);
        }
        return INODE_AVAILABLE;
    }

    byte[] removeIndirectData ()
    {
        SysLib.cout("Inode: Remove Indirect Data called \n");
        if (indirect != -1)
        {
            byte[] indirectData = new byte[Disk.blockSize];
            SysLib.rawread(indirect, indirectData);
            indirect = -1;
            return indirectData;
        }
        return null;
    }

    // Add an indirect block
    // Returns false if indirect block is not added properly
    boolean addIndirectBlock(short blockID) {
        // Check to see if direct pointers are set first
        for (int i = 0; i < directSize; i++)
        {
            if (direct[i] == -1)
            {
                SysLib.cout("DIRECT BLOCK ERROR\n");
                return false;
            }
        }
        if (indirect != -1)
        {
            SysLib.cout("Inode: INDIRECT BLOCK ERROR\n");
            return false; // indirect is already set
        }
        else
        {
            indirect = blockID;
            byte[] data = new byte[Disk.blockSize];

            for (int i = 0; i < Disk.blockSize / 2; i++) // initialize indirect pointers to -1
            {
                SysLib.short2bytes((short) -1, data, i * 2);
            }

            SysLib.rawwrite(blockID, data);
            return true;
        }
    }
}
