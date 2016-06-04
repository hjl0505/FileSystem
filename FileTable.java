// Edited By Chris Knakal and Hyungjin Lee
// CSS 430 Program 5
// 5/30/2016

import java.util.Vector;

public class FileTable
{
   private final static int UNUSED = 0;
   private final static int USED = 1;
   private final static int READ = 2;
   private final static int WRITE = 3;
   private final static int PENDING_DELETE = 4;

   private Vector table;         // the actual entity of this file table
   private Directory dir;        // the root directory

   private Vector<Inode> inodeList; // maintains all inode on MEMORY

   public FileTable(Directory directory) // constructor
   {
      table = new Vector();     // instantiate a file (structure) table
      dir = directory;           // receive a reference to the Director
   }                             // from the file system

   // major public methods

   // Allocates new file table entry for the filename
   // Allocate and retrieve the corresponding inode
   // Returns reference to the file table entry
   public synchronized FileTableEntry falloc(String filename, String mode)
   {
      short inumber = -1;
      Inode node = null;
      // loop for synchronized threads
      while(true)
      {
          // allocate/retrieve and register the corresponding inode using dir
          inumber = dir.namei(filename);
          // if the file doesn't exist in the directory and mode isn't read
          if (inumber == -1 && !mode.equals("r"))
          {
              // allocate an inumber for the file
              inumber = dir.ialloc(filename);
              // create an inode for the file
              node = new Inode();
          }
          // if the file does exist in the directory
          else if (inumber >= 0)
          {
              // get the file from disk
              node = new Inode(inumber);
              SysLib.cout("Inumber: " + inumber + "\n");
              SysLib.cout("Inode count: " + node.count + "\n");

              // if the inode is set to be deleted
              if (node.flag == PENDING_DELETE)
              {
                  // no other threads may access the inode
                  return null;
              }

              // if the request mode is read
              if (mode.equals("r"))
              {
                  // if no other threads are writing, or the inode is pendingDelete
                  if (node.flag != WRITE)
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
              // if the request mode is write, write/read, or append
              else
              {
                  if (node.flag == USED || node.flag == UNUSED)
                  {
                      node.flag = WRITE;
                      break;
                  }
                  else
                  {
                      try
                      {
                          wait();
                      }
                      catch (Exception e) {}
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
      // immediately write back this inode to the disk
      ftEnt.inode.toDisk(inumber);
      // add inode to the inodeList
      //inodeList.addElement(ftEnt.inode);
      // return a reference to this file (structure) table entry
      return ftEnt;
   }

   public synchronized boolean ffree(FileTableEntry e)
   {
      // if you can remove the file table entry from the file table
      if (table.removeElement(e))
      {
          // decrement inode count
          e.inode.count--;
          // set the inode flag
          e.inode.flag = UNUSED;
          // save the corresponding inode to the disk
          e.inode.toDisk(e.iNumber);
          // free this file table entry.
          table.remove(e);
          // notify all threads waiting for access to this inode
          notifyAll();
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
           FileTableEntry temp = (FileTableEntry) table.elementAt(i);
           if (temp.iNumber == (short) i)
           {
               return temp.inode;
           }
       }
       // error
       return null;
   }
}
