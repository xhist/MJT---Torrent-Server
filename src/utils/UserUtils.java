package utils;

import interfaces.UserInterface;
import storage.User;

public class UserUtils {
    private UserUtils() {}
    private static boolean isInteger(String str) {
        try {
            Integer d = Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static UserInterface processUser(String[] userData) {
        if (userData.length != 2) {
            throw new IllegalArgumentException("User data format is invalid. It should be following format: <username> - <host:port>");
        }
        String username = userData[0].strip();
        String[] userIp = userData[1].strip().split(":");
        if (userIp.length != 2) {
            throw new IllegalArgumentException("User data format is invalid. It should be following format: <username> - <host:port>");
        }
        String host = userIp[0];
        if (!isInteger(userIp[1])) {
            throw new IllegalArgumentException("Port is not integer");
        }
        Integer port = Integer.parseInt(userIp[1]);
        UserInterface user = new User(username, host, port);
        return user;
    }
}
