// Edited By Chris Knakal and Hyungjin Lee
// CSS 430 Program 5
// 5/30/2016


import java.util.Vector;

public class FileTable
{
   private final static int UNUSED = 0;
   private final static int USED = 1;
   private final static int WRITE = 2;
   private final static int PENDING_DELETE = 3;

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
      short inumber = -1;
      Inode node = null;
      while(true)
      {
          // allocate/retrieve and register the corresponding inode using dir
          short inumber = dir.namei(filename);
          // if the file doesn't exist in the directory and mode isn't read
          if (inumber == -1 && !mode.equals("r"))
          {
              // allocate an inumber for the file
              inumber = dir.ialloc(filename);
              // create an inode for the file
              node = new Inode();
          }
          // if the file does exist in the directory
          else if (0 < inumber)
          {
              // get the file from disk
              node = new Inode(inumber);
              // if the request mode is read
              if (mode.equals("r"))
              {
                  // if no other threads are writing, or the inode is pendingDelete
                  if (node.flag != WRITE && node.flag != PENDING_DELETE)
                  {
                      node.flag = READ;
                      break;
                  }
                  // else other threads are writing to the inode
                  else
                  {
                      try
                      {
                          // wait for writing thread to get done
                          wait();
                      }
                      catch (Exception e){}
                  }

              }
          }
          else
          {
              return null;
          }
      }


      FileTableEntry ftEnt = new FileTableEntry(node, inumber, mode);
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
      // if you can remove the file table entry from the file table
      if (table.removeElement(e))
      {
          // decrement inode count
          table.get(i).inode.count--;
          // set the inode flag
          table.get(i).inode.flag = UNUSED
          // save the corresponding inode to the disk
          table.get(i).inode.toDisk();
          // free this file table entry.
          table.remove(i);
          // return true if this file table entry found in my table
          return true;
      }
      // file table entry not removed
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
