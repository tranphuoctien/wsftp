/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package in.mustafaak.wsftp;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;

/**
 *
 * @author Mustafa
 */
public class FileServer {

    static final int PACKAGE_SIZE = 32 * 1024; // Due to WebSockets limitations.
    static final int CLUSTER_SIZE = 4 * 1024 * 1024;

    public FileServer() {
        
    }
    
    public byte[] getFile(String fileHash){
        // HASH -> FILE Resolution
        return null;
    }
    private static byte[] readFile(File f, int offset) {
        try {
            RandomAccessFile raf = new RandomAccessFile(f, "r");
            long startByte = offset * CLUSTER_SIZE;
            long fileLength = f.length();
            long readOffset = CLUSTER_SIZE;
            if (startByte > fileLength - readOffset) {
                readOffset = fileLength - startByte;
            }
            MappedByteBuffer b = raf.getChannel().map(FileChannel.MapMode.READ_ONLY, startByte, readOffset);
            byte[] readData = new byte[(int) readOffset];
            int counter = 0;
            while (b.hasRemaining()) {
                readData[counter++] = b.get();
            }
            raf.close();
            return readData;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
