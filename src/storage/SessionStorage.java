package storage;

import interfaces.Storage;
import interfaces.UserInterface;

import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SessionStorage implements Storage<SocketChannel, UserInterface> {
    private final Map<SocketChannel, UserInterface> sessions = new HashMap<>();
    @Override
    public Set<SocketChannel> getKeys() {
        return Set.copyOf(sessions.keySet());
    }

    @Override
    public UserInterface remove(SocketChannel key) {
        if (key != null) {
            return sessions.remove(key);
        }
        return null;
    }

    @Override
    public UserInterface listValues(SocketChannel session) {
        if (session == null) {
            return null;
        }
        return sessions.get(session);
    }

    @Override
    public void addValues(SocketChannel session, UserInterface user) {
        if (session != null) {
            sessions.put(session, user);
        }
    }

    @Override
    public void removeValues(SocketChannel session, UserInterface user) {
    }
}
