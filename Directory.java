// Edited By Chris Knakal and Hyungjin Lee
// CSS 430 Program 5
// 5/30/2016

public class Directory
{
   private static int maxChars = 30; // max characters of each file name
   private static int NAME_BYTES = 60; // 30 character * 2 bytes = 60 bytes

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

      int offset = 0;

      // fill in the fsize array
      for (int i = 0; i < fsize.length; offset += 4)
      {
          fsize[i] = SysLib.bytes2short(data, offset);
          i++;
      }

      // fill in the fnames array
      for (int j = 0; j < fnames.length; offset += maxChars * 2)
      {
          String name = new String(data, offset, maxChars * 2);
          name.getChars(0, fsize[j], fnames[j], 0);
		  j++;

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
      byte[] data = new byte[(4 * fsize.length) + (fsize.length * maxChars * 2)];
      int offset = 0;

      // convert the fsize array into bytes
        for (int i = 0; i < fsize.length; offset += 4) 
		{
            SysLib.int2bytes(fsize[i], data, offset);
			i++;
        }

      // convert the fnames array into bytes
      for (int j = 0; j < fnames.length; offset += maxChars * 2)
      {
          String name = new String(fnames[j], 0, fsize[j]);
          byte[] temp = name.getBytes();
          System.arraycopy(temp, 0, data, offset, temp.length);
		  j++;
      }
      return data;
   }

   public short ialloc(String filename)
   {   
      // filename is the one of a file to be created.
      // allocates a new inode number for this filename
      for (int i = 1; i < fsize.length; i++)
      {
          // find the next free iNumber (aka fsize index)
          if (fsize[i] == 0)
          {
              // record the filename length
              fsize[i] = Math.min(filename.length(), maxChars);
              // put the file name in the fnames array
              filename.getChars(0, fsize[i], fnames[i], 0);
              // return the index as a short to be this file's iNumber
              return (short)i;
          }
      }	  
      // No free iNumbers left in the directory
      return (short) -1;
   }

   // Deletes the Inode in memory by dereferencing the Inumber.
   // The Inode data stays in memeory, but will be overwritten by
   // the next Inode to occupy the same disk space.
   public boolean ifree(short iNumber)
   {
		if (fsize[iNumber] > 0)
		{
			fsize[iNumber] = 0;
			return true;
		}
		return false;
   }

   public short namei(String filename)
   {
	    // returns the inumber corresponding to this filename
        for (int i = 0; i < fsize.length; i++) 
		{
			// if the length of the file names are the same and
			//  the strings are the same
            String otherFile = new String(fnames[i], 0, fsize[i]);
            // make sure the size matches and strings match
            if (fsize[i] == filename.length() && filename.equals(otherFile)) 
			{
                // return the index
                return (short)i;
            }
        }
        return -1;
   }
}
