package storage;

import interfaces.UserInterface;

public class User implements UserInterface {
    private final String username;
    private final String host;
    private final Integer port;

    public User(String username, String host, Integer port) {
        this.username = username;
        this.host = host;
        this.port = port;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public Integer getPort() {
        return port;
    }

    @Override
    public int hashCode() {
        return username.hashCode() + host.hashCode() + port.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UserInterface)) {
            return false;
        }
        UserInterface other = (UserInterface) obj;
        return (this.username.equals(other.getUsername()) &&
            this.host.equals(other.getHost()) &&
            this.port.equals(other.getPort()));
    }

    @Override
    public String toString() {
        return username + "-" + host + ":" + port;
    }
}
