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
        SERVER_LOGIN_INCORRECT,
        SERVER_BLOCKED_USER,
        SERVER_ALLOW_USER_ENTER,
        SERVER_SEND_FILE_LIST,
        SERVER_SEND_FILE_CLUSTER_INFO,
        SERVER_SEND_FILE_PIECE,
        SERVER_SEND_FILE_NOT_AVAILABLE
    }
    private final static String JSON_MSG_BODY = "body";
    private final static String JSON_MSG_TYPE = "type";
    private final static String JSON_AUTH = "auth";
    private final static String JSON_PIECE = "piece";
    private final static String JSON_DETAILS = "details";
    private final static String JSON_AUTHED = "authed";

    public static MsgType getMessage(String message) {
        System.out.println(message);
        ObjectMapper om = new ObjectMapper();
        Map msg = null;
        String key;
        try {
            msg = om.readValue(message, Map.class);
        } catch (java.io.IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
        key = (String) msg.get(JSON_MSG_TYPE);
        System.out.println(key);
        if (key.equals(JSON_AUTH)) {
            return MsgType.CLIENT_AUTHORIZE;
        } else if (key.equals(JSON_PIECE)) {
        }
        return null;
    }

    public UserCreditentials convert(String message) {
        UserCreditentials uc = new UserCreditentials();
        ObjectMapper om = new ObjectMapper();
        Map msg = null;
        Map content;
        try {
            msg = om.readValue(message, Map.class);
        } catch (java.io.IOException ioe) {
            return null;
        }
        content = (Map) msg.get(JSON_MSG_BODY);
        uc.user = (String) content.get("user");
        uc.pass = (String) content.get("pass");
        return uc;
    }

    public String createMessage(MsgType msg, Object content) {
        String msgToSend = "";
        switch (msg) {
            case SERVER_ALLOW_USER_ENTER:
                msgToSend = "OKYOUAREIN";
                break;
        }
        return msgToSend;
    }
}
