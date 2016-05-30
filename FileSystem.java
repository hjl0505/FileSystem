// Edited By Chris Knakal and Hyungjin Lee
// CSS 430 Program 5
// 5/30/2016


public class FileSystem
{
    private  final int SEEK_SET = 0;
    private final int SEEK_CUR = 1;
    private final int SEEK_END = 2;

    private SuperBlock superBlock;
    private Directory directory;
    private FileTable filetable;

    public FileSystem(int diskBlocks)
    {
        // create superblock and format disk with diskBlocks # of Inodes
        superblock = new SuperBlock(diskBlocks);

        // create directory and register "/" in directory 0
        directory = new Directory(superBlock.inodeBlocks);

        // creat file table and store directory in file table
        filetable = new FileTable(directory);

        // directory reconstruction
        FileTableEntry dirEnt = open("/", "r");
        int dirSize = fsize(dirEnt);
        if (dirSize > 0)
        {
            byte[] dirData = new byte[dirSize];
            read(dirEnt, dirData);
            directory.bytes2directory(dirData);
        }
        close(dirEnt);
    }

    void sync()
    {

    }

    // Formats the disk with maximum number of files/Inodes to be created
    boolean format(int files)
    {

    }

    // Opens the specified file in the given mode
    int open(String filename, String mode)
    {

    }

    //
    boolean close(FileTableEntry ftEnt)
    {

    }

    int fsize(FileTableEntry ftEnt)
    {

    }

    // Reads up to buffer.length bytes from the file indicated by fd,
    //  starting at the position currently pointed to by the seek pointer
    // Returns the number of bytes read or error code
    int read(FileTableEntry ftEnt, byte[] buffer)
    {

    }

    int write(FileTableEntry ftEnt, byte[] buffer)
    {

    }

    private boolean deallocateAllBlocks(FileTableEntry ftEnd)
    {

    }

    boolean delete(String filename)
    {

    }

    int seek(FileTableEntry ftEnt, int offest, int whence)
    {

    }
}
