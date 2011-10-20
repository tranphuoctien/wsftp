package in.mustafaak.wsftp;

import java.util.Properties;
import java.io.*;
import in.mustafaak.wsftp.util.*;

/**
 *
 * @author Mustafa
 */
public class ConnectionManager {
    private static final String PROPERTIES_FILE = "properties.file";
    Properties props;
    
    public enum Authentication {

        LOGIN_OK, LOGIN_INCORRECT, LOGIN_BLOCKED
    }

    public ConnectionManager() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(PROPERTIES_FILE));
        } catch (IOException e) {
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            this.props.store(new FileOutputStream(PROPERTIES_FILE), null);
        } catch (IOException e) {
        }
        super.finalize();
    }

    public Authentication authenticate(UserCreditentials uc) {
        String user = uc.user;
        String password = uc.pass;
        if (user.equals("mustafa") && password.equals("buket")) {
            return Authentication.LOGIN_OK;
        } else {
            return Authentication.LOGIN_INCORRECT;
        }
    }
}
