// Edited By Chris Knakal and Hyungjin Lee
// CSS 430 Program 5
// 5/30/2016

public class Directory
{
   private static int maxChars = 30; // max characters of each file name

   // Directory entries
   private int fsize[];        // each element stores a different file size.
   private char fnames[][];    // each element stores a different file name.

   public Directory(int maxInumber) // directory constructor
   {
      fsize = new int[maxInumber];     // maxInumber = max files
      for (int i = 0; i < maxInumber; i++)
         fsize[i] = 0;                 // all file size initialized to 0
      fnames = new char[maxInumber][maxChars];
      String root = "/";                // entry(inode) 0 is "/"
      fsize[0] = root.length();        // fsize[0] is the size of "/".
      root.getChars(0, fsize[0], fnames[0], 0); // fnames[0] includes "/"
   }

   public int bytes2directory(byte data[])
   {
      // assumes data[] received directory information from disk
      // initializes the Directory instance with this data[]

      // fill in the fsize array
      for (int i = 0; i < fsize.length; i++)
      {
          fsize[i] = SysLib.bytes2int(data, (i * 4));
      }

      // fill in the fnames array


   }

   public byte[] directory2bytes()
   {
      // converts and return Directory information into a plain byte array
      // this byte array will be written back to disk
      // note: only meaningfull directory information should be converted
      // into bytes.

      // directory gets its own disk block
      byte[] data = new byte[512];

      // convert the fsize array
      for (int i = 0; i < fsize.length; i++)
      {
          SysLib.int2bytes(fsize[i], data, (i * 4));
      }

      // convert the fnames array

   }

   public short ialloc(String filename)
   {
      // filename is the one of a file to be created.
      // allocates a new inode number for this filename
      for (int i = 0; i < fsize.length; i++)
      {
          // find the next free iNumber (aka fsize index)
          if (fsize[i] != 0)
          {
              // record the filename length
              fsize[i] = filename.length();
              // put the file name in the fnames array
              filename.getChars(0, fsize[i], fnames[i], 0);
              // return the index as a short to be this file's iNumber
              return (short) i;
          }
      }
      // No free iNumbers left in the directory
      return (short) -1;
   }

   public boolean ifree(short iNumber)
   {
      // deallocates this inumber (inode number)
      // the corresponding file will be deleted.
   }

   public short namei(String filename)
   {
      // returns the inumber corresponding to this filename
      for (int i = 0; i < fsize.length; i++)
      {
          // if the length of the file names are the same and
          //  the strings are the same
          if (fsize[i] == filename.length() && filename.equals(String.valueOf(fnames[i])));
          {
              // return the index as a short
              return (short) i;
          }
      }
      // file not found
      return (short) -1;
   }
}


/*
* Questions:
*  How do I conver the character array to bytes?
*
*  
*/
