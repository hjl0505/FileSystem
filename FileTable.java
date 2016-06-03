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
      FileTableEntry ftEnt;
      // allocate/retrieve and register the corresponding inode using dir
      short inumber = dir.namei(filename);
      // if the file doesn't exist in the directory
      if (inumber == -1)
      {
          // allocate an inumber for the file
          inumber = dir.ialloc(filename);
          // create an inode for the file
          Inode node = new Inode();
          ftEnt = new FileTableEntry(node, inumber, mode);
      }
      else
      {
          Inode node = new Inode(inumber);
          ftEnt = new FileTableEntry(node, inumber, mode);
      }

      // allocate a new file (structure) table entry for this file name
      table.addElement(ftEnt);
      // increment this inode's count
      ftEnt.inode.count++;
      // add inode to the inodeList
      inodeList.addElement(ftEnt.inode);
      // immediately write back this inode to the disk
      ftEnt.inode.toDisk();
      // return a reference to this file (structure) table entry
      return ftEnt;
   }

   public synchronized boolean ffree(FileTableEntry e)
   {
      // receive a file table entry reference
      for (int i = 0; i < table.size(); i++)
      {
          if (table.get(i).equals(e))
          {
              // save the corresponding inode to the disk
              table.get(i).inode.toDisk();
              // free this file table entry.
              table.remove(i);
              // return true if this file table entry found in my table
              return true;
          }
      }
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
