// Edited By Chris Knakal and Hyungjin Lee
// CSS 430 Program 5
// 5/30/2016

public class FileSystem
{
    private final int SEEK_SET = 0;
    private final int SEEK_CUR = 1;
    private final int SEEK_END = 2;

    private SuperBlock superBlock;
    private Directory directory;
    private FileTable fileTable;

    public FileSystem(int diskBlocks)
    {
        // create superblock and format disk with diskBlocks # of Inodes
        superBlock = new SuperBlock(diskBlocks);

        // create directory and register "/" in directory 0
        directory = new Directory(superBlock.totalInodes);

        // create file table and store directory in file table
        fileTable = new FileTable(directory);

        // directory reconstruction -- read directory from Disk
        FileTableEntry dirEntry = open("/", "r");
        int dirSize = fsize(dirEntry);
        if (dirSize > 0)
        {
            byte[] dirData = new byte[dirSize];
            read(dirEntry, dirData);
            directory.bytes2directory(dirData);
        }
        close(dirEntry);
    }

    // sync file system metadata to the disk
    // includes superblock and directory
    void sync()
    {
        // write directory back to disk
        FileTableEntry dirEntry = open ("/", "w");
        byte[] dirData = directory.directory2bytes();
        write(dirEntry, dirData);
        close(dirEntry);

        // write superblock back to disk
        superBlock.sync();
    }

    // Formats the disk with maximum number of files/Inodes to be created
    boolean format(int fileCount)
    {
        superBlock.format(fileCount);
        directory = new Directory(superBlock.totalInodes);
        fileTable = new FileTable(directory);
        return true;
    }

    // Opens the specified file in the given mode
    FileTableEntry open(String filename, String mode)
    {
        FileTableEntry entry = fileTable.falloc(filename, mode);
        if (entry != null && mode.equals("w")) // entry was allocated properly and mode is write
        {
            if (!deallocateAllBlocks(entry)) // couldn't deallocate all blocks
            {
                entry = null;
            }
        }
        return entry;
    }

    // Closes the file
    boolean close(FileTableEntry entry)
    {
        synchronized (entry)
        {
            entry.count--; // decrement the entry count
            if (entry.count > 0) // other threads still hold access to this entry
            {
                return true;
            }
        }
        return fileTable.ffree(entry); // free this entry from the table
    }

    // return size of the file in bytes
    int fsize(FileTableEntry entry)
    {
        synchronized (entry)
        {
            return entry.inode.length;
        }
    }

    // updates the seek pointer in the file table entry
    int seek(FileTableEntry entry, int offset, int whence)
    {
        if (entry == null)
        {
            SysLib.cerr("File Table Entry is invalid");
            return -1;
        }
        synchronized (entry)
        {
            switch (whence) {
                case SEEK_SET: // set to offset bytes from beginning of file
                    entry.seekPtr = offset;
                    break;
                case SEEK_CUR: // set to offset bytes from current position
                    entry.seekPtr += offset;
                    break;
                case SEEK_END: // set to offset bytes from end of file
                    entry.seekPtr = fsize(entry) + offset;
                    break;
            }
            entry.seekPtr = Math.min(entry.seekPtr, fsize(entry)); // clamp to size
            entry.seekPtr = Math.max (0, entry.seekPtr); // or clamp to 0
        }
        return entry.seekPtr;
    }

    // Reads up to buffer.length bytes from the file indicated by fd,
    // starting at the position currently pointed to by the seek pointer
    // Returns the number of bytes read or -1 for error
    int read(FileTableEntry entry, byte[] buffer)
    {
        if (entry == null || entry.mode.equals("w") || entry.mode.equals("a"))
        {
            return -1;
        }



        synchronized (entry)
        {


        }
    }

    int write(FileTableEntry entry, byte[] buffer)
    {
        return -1;
    }

    // delete file
    // if the file is currently open by another thread, it will wait
    // new attempts to open the file will fail
    boolean delete(String filename)
    {

        return false;
    }

    private boolean deallocateAllBlocks(FileTableEntry entry)
    {
        return true;
    }

}
