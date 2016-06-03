// Edited By Chris Knakal and Hyungjin Lee
// CSS 430 Program 5
// 5/30/2016


public class Inode
{
   private final static int iNodeSize = 32;       // fix to 32 bytes
   public final static int directSize = 11;      // # direct pointers

   public int length;                             // file size in bytes
   public short count;                            // # file-table entries pointing to this
   public short flag;                             // 0 = unused, 1 = used, 2 = pendingDelete
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
       }
       else if (indirect != -1)
       {
            byte[] indirectData = readIndirectData();
            int offset = (tempID - directSize) * 2;
            blockID = SysLib.bytes2short(indirectData, offset); // *short to int
       }
       return blockID;
   }

    // Add a free block to the inode
    // Returns true if the block was successfully added, false otherwise
    boolean addBlock (short freeBlock)
    {
        int tempID = (length / Disk.blockSize) + 1 ;
        if(tempID < directSize) // add block to direct
        {
            direct[tempID] = freeBlock;
        }
        else if (indirect == -1) // add block as indirect block
        {
            indirect = freeBlock;
        }
        else // add block to indirect
        {
            byte[] indirectData = readIndirectData();
            int offset = 0;
            short blockID = SysLib.bytes2short(indirectData, offset);
            while(blockID != -1)
            {
                if (offset >= Disk.blockSize)
                {
                  return false; // Indirect block is full
                }

                offset += 2;
                blockID = SysLib.bytes2short(indirectData, offset);
            }

            SysLib.short2bytes(freeBlock, indirectData, offset);
            SysLib.rawwrite(indirect, indirectData);
        }
        return true;
    }

    byte[] readIndirectData ()
    {
        if (indirect != -1)
        {
            byte[] indirectData = new byte[Disk.blockSize];
            SysLib.rawread(indirect, indirectData);
            indirect = -1;
            return indirectData;
        }
        return null;
    }
}
