package in.mustafaak.wsftp;

import in.mustafaak.wsftp.util.*;
import java.util.*;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author Mustafa
 */
public class MessageParser {

    public enum MsgType {
        WTF,
        CLIENT_AUTHORIZE,
        CLIENT_WANT_PIECE,
        CLIENT_WANT_FILE_DETAILS,
        CLIENT_WANT_MESSAGE,
        SERVER_ALLOW_USER_ENTER,
        SERVER_SEND_FILE_LIST,
        SERVER_SEND_FILE_CLUSTER_INFO,
        SERVER_SEND_FILE_PIECE,
        SERVER_SEND_FILE_NOT_AVAILABLE
    }
    private final static String JSON_AUTH = "auth";
    private final static String JSON_PIECE = "piece";
    private final static String JSON_DETAILS = "details";
    
    public static MsgType getMessage(String message) {
        ObjectMapper om = new ObjectMapper();
        Map msg = null;
        String key;
        try {
            msg = om.readValue(message, Map.class);
        } catch (java.io.IOException ioe) {
            return null;
        }
        key = (String)msg.get("message");
      
        if ( key.equals(JSON_AUTH)){
            return MsgType.CLIENT_AUTHORIZE;
        } else if ( key.equals(JSON_PIECE)){
            
        }
        return null;
    }

    public UserCreditentials convert(String message) {
        UserCreditentials uc = new UserCreditentials();
        String[] tmp = message.split(";");
        uc.user = tmp[1];
        uc.pass = tmp[2];
        return uc;
    }

    public String sendMessage(MsgType msg) {
        String msgToSend = "";
        switch (msg) {
            case SERVER_ALLOW_USER_ENTER:
                msgToSend = "OKYOUAREIN";
                break;
        }
        return msgToSend;
    }
}
