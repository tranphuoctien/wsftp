/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package in.mustafaak.wsftp;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import org.webbitserver.*;
import java.io.*;
import java.nio.channels.*;
import java.nio.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.*;

/**
 *
 * @author Mustafa
 */
public class BasicServer implements WebSocketHandler {

    List<WebSocketConnection> connections = new ArrayList<WebSocketConnection>();
    List<String> files = new ArrayList<String>();
    int connectionCount;
    static final int PACKAGE_SIZE = 32 * 1024; // Due to WebSockets limitations.
    static final int CLUSTER_SIZE = 4 * 1024 * 1024;

    @Override
    public void onOpen(WebSocketConnection connection) {
        System.out.println("A dude connected: " + connection.httpRequest().remoteAddress());
        connectionCount++;
    }

    @Override
    public void onClose(WebSocketConnection connection) {
        connectionCount--;
    }

    @Override
    public void onMessage(WebSocketConnection connection, String message) {
        // Determine the command.
        System.out.println(message);
        String[] k = message.split(";");
        String command = k[0];
        if (command.equals("LOGIN")) {
            String user = k[1];
            String pwd = k[2];
            if (user.equals("mustafa") && pwd.equals("buket")) {
                connections.add(connection);
                listFiles("M://");
                String flst = "FILELIST;";
                for (String s : files) {
                    flst += s + ";";
                }
                connection.send(flst);
            } else {
                connection.close();
            }
        } else if (command.equals("FILEINFO")) {
            String filePath = k[1];
            File f = new File(filePath);
            long pieceCount = f.length() / CLUSTER_SIZE + 1;
            String msg = "FILEINFO;" + f.getAbsolutePath() + ";" + pieceCount;
            connection.send(msg);
        } else if (command.equals("FILEGET")) {
            int offset = Integer.parseInt(k[1]);
            String filePath = k[2];
            File f = new File(filePath);
            sendBinaryData(connection, f, offset);
        }
    }

    private void sendBinaryData(WebSocketConnection connection, File f, int offset) {
        byte b[] = readFile(f, offset);
        int pieceCount = (int) (Math.ceil(b.length / PACKAGE_SIZE));
        for (int i = 0; i < pieceCount - 1; i++) {
            byte toSend[] = new byte[PACKAGE_SIZE];
            System.arraycopy(b, i * PACKAGE_SIZE, toSend, 0, toSend.length);
            connection.send(toSend);
        }
        int remainingSize = b.length - (pieceCount - 1) * PACKAGE_SIZE;
        byte toSend[] = new byte[remainingSize];
        System.arraycopy(b, (pieceCount - 1) * PACKAGE_SIZE, toSend, 0, remainingSize - 1);
        connection.send(toSend);
        connection.send("FILESENT;" + offset);
    }

    @Override
    public void onMessage(WebSocketConnection connection, byte[] message) {
    }

    @Override
    public void onPong(WebSocketConnection connection, String message) {
    }

    public static void main(String[] args) throws Exception {
        WebServer webServer = WebServers.createWebServer(8080).add("/wsftp", new BasicServer()).start();
        System.out.println("Server running at " + webServer.getUri());
    }

    public void listFiles(String path) {
        files = new ArrayList<String>();
        walk(path);
    }

    private void walk(String path) {
        try {
            File root = new File(path);
            File[] list = root.listFiles();
            for (File f : list) {
                if (f.isDirectory()) {
                    walk(f.getAbsolutePath());
                } else if (f.length() < 1024 * 1024 * 30 && f.length() > 1024 * 1024 * 15 ) {
                    files.add(f.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            
        }
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
