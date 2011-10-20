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
import in.mustafaak.wsftp.util.*;
import java.io.*;
import org.codehaus.jackson.map.ObjectMapper;
import java.nio.channels.*;
import java.nio.*;
import java.util.*;

/**
 *
 * @author Mustafa
 */
public class BasicServer implements WebSocketHandler {

    MessageParser msgParser = new MessageParser();
    ConnectionManager connMgr = new ConnectionManager();
    private HashSet<WebSocketConnection> connections = new HashSet<WebSocketConnection>();
    int connectionCount;

    @Override
    public void onOpen(WebSocketConnection connection) {
        System.out.println("A dude connected: " + connection.httpRequest().remoteAddress());
        connectionCount++;
    }

    @Override
    public void onClose(WebSocketConnection connection) {
        System.out.println("Dude disconnected.. :(");
        connectionCount--;
    }

    @Override
    public void onMessage(WebSocketConnection connection, String message) {
        String msg = "UNKNOWN";
        switch (MessageParser.getMessage(message)) {
            case CLIENT_AUTHORIZE:
                UserCreditentials uc = msgParser.convert(message);
                switch (connMgr.authenticate(uc)) {
                    case LOGIN_OK:
                        msg = msgParser.sendMessage(MessageParser.MsgType.SERVER_ALLOW_USER_ENTER);                        
                        break;
                }
                break;

        }
        this.sendMessage(connection, msg);
    }

    private void sendBinaryData(WebSocketConnection connection, File f, int offset) {
    }

    private void sendMessage(WebSocketConnection connection, String message) {
        connection.send(message);
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
        ObjectMapper om = new ObjectMapper();
        String message = "{\"type\": \"auth\", \"content\":5}";
        System.out.println(message);
        Map p;
        System.out.println(p = om.readValue(message, Map.class));
       
    }
}
