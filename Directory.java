// Edited By Chris Knakal and Hyungjin Lee
// CSS 430 Program 5
// 5/30/2016

public class Directory
{
   private static int maxChars = 30; // max characters of each file name

   // Directory entries
   private short fsize[];        // each element stores a different file size.
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

      int offest = 0;

      // fill in the fsize array
      for (int i = 0; i < fsize.length; i++)
      {
          fsize[i] = SysLib.bytes2short(data, (i * 2));
          offest += 2;
      }

      // fill in the fnames array
      for (int j = 0; j < fnames.length; j++)
      {
          for (int k = 0; k < fnames[j].length; k++)
          {
              // take 2 bytes from data[] to make char
              fnames[j][k] = (char) (data[offset] + data[offset + 1]);
              // increase the offset by 2 bytes
              offset += 2;
          }
      }

      return 1;
   }

   public byte[] directory2bytes()
   {
      // converts and return Directory information into a plain byte array
      // this byte array will be written back to disk
      // note: only meaningfull directory information should be converted
      // into bytes.

      // directory gets its own disk block
      byte[] data = new byte[Disk.blockSize];
      int offset = 0;

      // convert the fsize array into bytes
      for (int i = 0; i < fsize.length; i++)
      {
          SysLib.short2bytes(fsize[i], data, (i * 2));
          offest += 2;
      }

      // convert the fnames array into bytes
      for (int j = 0; j < fnames.length; j++)
      {
          for (int k = 0; k < fnames[j].length; k++)
          {
              // put each character into the buffer
              data[offset] = fnames[j][k];
              // offset by char byte size (2 bytes)
              offset += 2;
          }
      }

      return data;
   }

   public short ialloc(String filename)
   {
      // filename is the one of a file to be created.
      // allocates a new inode number for this filename
      for (int i = 0; i < fsize.length; i++)
      {
          // find the next free iNumber (aka fsize index)
          if (fsize[i] == 0)
          {
              // record the filename length
              fsize[i] = filename.length();
              // put the file name in the fnames array
              filename.getChars(0, fsize[i], fnames[i], 0);
              // return the index as a short to be this file's iNumber
              return i;
          }
      }
      // No free iNumbers left in the directory
      return (short) -1;
   }

   /*
   * Deletes the Inode in memory by dereferencing the Inumber.
   * The Inode data stays in memeory, but will be overwritten by
   *  the next Inode to occupy the same disk space.
   */
   public boolean ifree(short iNumber)
   {
      // check if the iNumber is currently being used
      if (fsize[iNumber] == 0)
      {
          return false;
      }

      // deallocate this inumber (inode number)
      fsize[iNumber] = 0;
      // clear the fnames array for this inumber
      for (int i = 0; i < fnames[iNumber].length; i++)
      {
          fnames[iNumber][i] = '';
      }

      // the corresponding file will be deleted.
      return true;

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
              // return the index
              return i;
          }
      }
      // file not found
      return (short) -1;
   }
}


/*
* Questions:
*  How do I convert the character array to bytes?
*  How do I delete the file from the directory?
*
*/
