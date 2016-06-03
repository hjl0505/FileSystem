// Edited By Chris Knakal and Hyungjin Lee
// CSS 430 Program 5
// 5/30/2016

public class FileSystem
{
    private final int SEEK_SET = 0;
    private final int SEEK_CUR = 1;
    private final int SEEK_END = 2;
    private final static int MAX_FILESIZE = 136704; // bytes

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
        while(!fileTable.fempty())
        {
            // check that no FileTableEntries are in the FileTable
            // wait for the file table to empty
        }
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
        // decrement Inode count
        // if count is 0 after decrementing, write Inode to disk
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
        // The mode for entry should be r or w+
        if (entry == null || entry.mode.equals("w") || entry.mode.equals("a"))
        {
            return -1;
        }

        synchronized (entry)
        {
            int filesize = fsize(entry);
            int bytesRead = 0;
            int bufferRemaining = buffer.length;

            while (bufferRemaining > 0 && entry.seekPtr < filesize)
            {
                int blockID = entry.inode.getBlockID(entry.seekPtr); // block to read
                if (blockID != -1)
                {
                    // read the whole block
                    byte[] blockData = new byte[Disk.blockSize];
                    SysLib.rawread(blockID, blockData);
                    int startPoint = entry.seekPtr % Disk.blockSize;

                    // Calculate how much to read from the data
                    // must be less than filesize, block size, and remaining buffer size
                    int blockDataToRead = Disk.blockSize - startPoint;
                    int fileDataLeft = filesize - entry.seekPtr;
                    int sizeToRead = Math.min(Math.min(blockDataToRead, fileDataLeft), bufferRemaining);

                    // copy block data over to buffer
                    System.arraycopy(blockData, startPoint, buffer, bytesRead, sizeToRead);

                    // adjust seek pointer, total bytes read, and size of remaining buffer
                    entry.seekPtr += sizeToRead;
                    bytesRead += sizeToRead;
                    bufferRemaining -= sizeToRead;
                }
            }
            return bytesRead;
        }
    }

    int write(FileTableEntry entry, byte[] buffer)
    {
        // The mode for entry should be w, a, or w+
        if (entry == null || entry.mode.equals("r"))
        {
            return -1;
        }

        synchronized (entry)
        {
            int filesize = fsize(entry);
            int bytesWritten = 0;
            int bufferRemaining = buffer.length;

            while (bufferRemaining > 0 && filesize < MAX_FILESIZE)
            {
                int blockID = entry.inode.getBlockID(entry.seekPtr);
                // Need to find free block and give it to inode
                while (blockID == -1)
                {
                    int freeBlock = superBlock.getBlock();
                    blockID = entry.inode.addBlock(freeBlock, entry.seekPtr);
                }

                // read the whole block
                byte[] blockData = new byte[Disk.blockSize];
                SysLib.rawread(blockID, blockData);
                int startPoint = entry.seekPtr % Disk.blockSize;

                // calculate how much to write to block
                // must be less than block size and buffer remaining size
                int blockSizeLeft = Disk.blockSize - startPoint;
                int sizeToWrite = Math.min(blockSizeLeft, bufferRemaining);

                // copy buffer data over to the blockData
                System.arraycopy(buffer, bytesWritten, blockData, startPoint, sizeToWrite);

                // adjust seek pointer, total bytes written, and size of remaining buffer
                entry.seekPtr += sizeToWrite;
                bytesWritten += sizeToWrite;
                bufferRemaining -= sizeToWrite;

                // adjust the file size
                if (entry.seekPtr > filesize)
                {
                    entry.inode.length = entry.seekPtr;
                }
            }
            // update inode on disk
            entry.inode.toDisk(entry.iNumber);
            return bytesWritten;
        }
    }

    // delete file
    // if the file is currently open by another thread, it will wait
    // new attempts to open the file will fail
    boolean delete(String filename)
    {
        // search the directory for the filename
        short iNumber = directory.namei(filename);
        // if the filename was found, get the Inode from the FileTableEntry
        if (iNumber != -1)
        {
        }
        // set the Inode's flag to 2 (delete pending)

        // if the Inode count is = 0

        // call the directory's ifree method

        // return true

        return false;
    }

    private boolean deallocateAllBlocks(FileTableEntry entry)
    {
        return true;
    }

}
