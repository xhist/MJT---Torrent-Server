package storage;

import interfaces.UserInterface;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageTest {

    private final FileStorage storage = new FileStorage();

    @BeforeEach
    void setUp() {
        storage.addValues(new User("Pesho123", "127.0.0.1", 2713), new LinkedHashSet<>(Arrays.asList("/home/hmmm/hi.txt")));
    }

    @AfterEach
    void tearDown() {
        Set<UserInterface> users = storage.getKeys();
        for (UserInterface user : users) {
            storage.remove(user);
        }
    }

    @Test
    void getAllKeysTest() {
        Set<UserInterface> users = storage.getKeys();
        assertEquals(1, users.size());
        assertTrue(users.contains(new User("Pesho123", "127.0.0.1", 2713)));
    }

    @Test
    void removeExistingUserTest() {
        storage.remove(new User("Pesho123", "127.0.0.1", 2713));
        assertEquals(0, storage.getKeys().size());
    }

    @Test
    void removeNonExistingUserTest() {
        storage.remove(new User("Pesho12", "127.0.0.1", 2713));
        assertEquals(1, storage.getKeys().size());
    }

    @Test
    void listValuesOfExistingUserTest() {
        Set<String> valuesOfExistingUser = storage.listValues(new User("Pesho123", "127.0.0.1", 2713));
        assertEquals(1,valuesOfExistingUser.size());
        assertTrue(valuesOfExistingUser.contains("/home/hmmm/hi.txt"));
    }
    @Test
    void listValuesOfNonExistingUserTest() {
        Set<String> valuesOfExistingUser = storage.listValues(new User("Pesho12", "127.0.0.1", 2713));
        assertEquals(0,valuesOfExistingUser.size());
    }

    @Test
    void addValuesToExistingUserTest() {
        storage.addValues(new User("Pesho123", "127.0.0.1", 2713),Set.of("/home/hmmm/hello.txt"));
        assertEquals(2, storage.listValues(new User("Pesho123", "127.0.0.1", 2713)).size());
        assertTrue(storage.listValues(new User("Pesho123", "127.0.0.1", 2713)).contains("/home/hmmm/hello.txt"));
    }
    @Test
    void addValuesToNonExistingUserTest() {
        storage.addValues(new User("Pesho12", "127.0.0.1", 2713),new LinkedHashSet<>(Arrays.asList("/home/hmmm/hello.txt")));
        assertEquals(1, storage.listValues(new User("Pesho12", "127.0.0.1", 2713)).size());
        assertTrue(storage.listValues(new User("Pesho12", "127.0.0.1", 2713)).contains("/home/hmmm/hello.txt"));
    }
    @Test
    void removeExistingValuesFromUserTest() {
        storage.removeValues(new User("Pesho123", "127.0.0.1", 2713),new LinkedHashSet<>(Arrays.asList("/home/hmmm/hi.txt")));
        assertEquals(0, storage.listValues(new User("Pesho123", "127.0.0.1", 2713)).size());
    }
    @Test
    void removeNonExistingValuesFromUserTest() {
        storage.removeValues(new User("Pesho123", "127.0.0.1", 2713),new LinkedHashSet<>(Arrays.asList("/home/hmmm/hello.txt")));
        assertEquals(1, storage.listValues(new User("Pesho123", "127.0.0.1", 2713)).size());
        assertFalse(storage.listValues(new User("Pesho123", "127.0.0.1", 2713)).contains("/home/hmmm/hello.txt"));
    }
}