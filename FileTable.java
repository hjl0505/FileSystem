// Edited By Chris Knakal and Hyungjin Lee
// CSS 430 Program 5
// 5/30/2016


import java.util.Vector;

public class FileTable
{

   private Vector table;         // the actual entity of this file table
   private Directory dir;        // the root directory

   private Vector<Inode> inodeList; // maintains all inode on MEMORY

   public FileTable(Directory directory) // constructor
   {
      table = new Vector();     // instantiate a file (structure) table
      dir = directory;           // receive a reference to the Director
   }                             // from the file system

   // major public methods
   public synchronized FileTableEntry falloc(String filename, String mode)
   {
      // allocate a new file (structure) table entry for this file name
      // allocate/retrieve and register the corresponding inode using dir
      // increment this inode's count
      // immediately write back this inode to the disk
      // return a reference to this file (structure) table entry
      return null;
   }

   public synchronized boolean ffree(FileTableEntry e)
   {
      // receive a file table entry reference
      // save the corresponding inode to the disk
      // free this file table entry.
      // return true if this file table entry found in my table
      return false;
   }

   // return if table is empty
   // should be called before starting a format
   public synchronized boolean fempty()
   {
      return table.isEmpty( );
   }

   // Gets the specified Inode via iNumber lookup through FileTableEntries
   public synchronized Inode getInode(int iNumber)
   {
       for (int i = 0; i < table.size(); i++)
       {
           if (table.get(i).iNumber == (short) i)
           {
               return table.get(i).inode;
           }
       }
       // error
       return null;
   }
}
