// Edited By Chris Knakal and Hyungjin Lee
// CSS 430 Program 5
// 5/30/2016


public class Inode
{
   private final static int iNodeSize = 32;       // fix to 32 bytes
   private final static int directSize = 11;      // # direct pointers

   public int length;                             // file size in bytes
   public short count;                            // # file-table entries pointing to this
   public short flag;                             // 0 = unused, 1 = used, ...
   public short direct[] = new short[directSize]; // direct pointers
   public short indirect;                         // a indirect pointer

   Inode() // a default constructor
   {
      length = 0;
      count = 0;
      flag = 1;
      for ( int i = 0; i < directSize; i++ )
      {
          direct[i] = -1;
      }
      indirect = -1;
   }

   Inode(short iNumber) // retrieving inode from disk
   {
      // read in disk block where Inode lives
      // disk block = iNumber / 16
      byte[] data = new byte[512];
      SysLib.rawread(iNumber / 16, data);

      // read Inodes 32 bytes within the disk block
      // Inode bytes within disk block start at (iNumber % 16) * 32
      int startIndex = (iNumber % 16) * 32;
      length = SysLib.bytes2int(data, startIndex);
      count = SysLib.bytes2short(data, startIndex + 4);
      flag = SysLib.bytes2short(data, startIndex + 6);
      for (int i = 0; i < directSize; i++)
      {
          direct[i] + SysLib.bytes2short(data, (startIndex + 8) + (2 * i));
      }
      indirect = SysLib.bytes2short(data, startIndex + 30);
   }

   int toDisk(short iNumber) // save to disk as the i-th inode
   {
      // Get the disk where this Inode belongs
      // disk block = iNumber / 16
      byte[]data = new byte[512];
      SysLib.rawread(iNumber / 16, data);

      // Inode bytes within disk block start at (iNumber % 16) * 32
      int startIndex = (iNumber % 16) * 32;
      SysLib.int2bytes(length, data, startIndex);
      SysLib.short2bytes(count, data, startIndex + 4);
      SysLib.short2bytes(flag, data, startIndex + 6);
      for (int i = 0; i < directSize; i++)
      {
          SysLib.short2bytes(direct[i], data, (startIndex + 8) + (2 * i));
      }
      SysLib.short2bytes(indirect, data, startIndex + 30);
   }
}
