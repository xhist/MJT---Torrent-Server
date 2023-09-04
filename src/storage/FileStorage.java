package storage;

import interfaces.Storage;
import interfaces.UserInterface;

import java.util.*;

public class FileStorage implements Storage<UserInterface, Set<String>> {
    private final Map<UserInterface, Set<String>> files = new HashMap<>();

    @Override
    public Set<UserInterface> getKeys() {
        return Set.copyOf(files.keySet());
    }

    @Override
    public Set<String> remove(UserInterface key) {
        if (key != null) {
            return files.remove(key);
        }
        return null;
    }

    @Override
    public Set<String> listValues(UserInterface user) {
        if (files.get(user) == null) {
            return Collections.emptySet();
        }
        return files.get(user);
    }

    @Override
    public void addValues(UserInterface user, Set<String> files) {
        if (files == null) {
            files = Collections.emptySet();
        }
        if (user != null) {
            this.files.putIfAbsent(user, files);
        }
        this.files.get(user).addAll(files);
    }

    @Override
    public void removeValues(UserInterface user, Set<String> files) {
        if (user != null && this.files.get(user) != null) {
            this.files.get(user).removeAll(files);
        }
    }
}
