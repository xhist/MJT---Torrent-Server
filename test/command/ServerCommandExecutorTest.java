package command;

import messages.Request;
import messages.Response;
import messages.ResponseStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import storage.FileStorage;
import storage.SessionStorage;
import storage.User;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ServerCommandExecutorTest {
    private FileStorage fileStorage;
    private ServerCommandExecutor commandExecutor;

    @BeforeEach
    void setUp() {
        fileStorage = new FileStorage();
        commandExecutor = new ServerCommandExecutor(fileStorage, new SessionStorage());
    }

    @AfterEach
    void tearDown() {
        fileStorage = new FileStorage();
        commandExecutor = new ServerCommandExecutor(fileStorage, new SessionStorage());
    }

    @Test
    void registerExistingUserTest() {
        commandExecutor.execute(new Request(null, CommandCreator.newCommand("register Pesho123-127.0.0.1:2713 /home/hmmm/hi.txt")));
        assertTrue(fileStorage.getKeys().contains(new User("Pesho123", "127.0.0.1", 2713)));
        assertEquals(1, fileStorage.listValues(new User("Pesho123","127.0.0.1", 2713)).size());
        assertTrue(fileStorage.listValues(new User("Pesho123", "127.0.0.1", 2713)).containsAll(Set.of("/home/hmmm/hi.txt")));
    }
    @Test
    void registerNonExistingUserTest() {
        commandExecutor.execute(new Request(null, CommandCreator.newCommand("register Pesho12-127.0.0.1:2713 /home/hmmm/hi.txt")));
        assertTrue(fileStorage.getKeys().contains(new User("Pesho12", "127.0.0.1", 2713)));
        assertEquals(1, fileStorage.listValues(new User("Pesho12","127.0.0.1", 2713)).size());
        assertTrue(fileStorage.listValues(new User("Pesho12", "127.0.0.1", 2713)).containsAll(Set.of("/home/hmmm/hi.txt")));
    }

    @Test
    void registerInvalidArgumentsCountTest() {
        assertEquals(ResponseStatus.ERROR,commandExecutor.execute(new Request(null, CommandCreator.newCommand("register Pesho12-127.0.0.1:2713"))).status());
    }

    @Test
    void unregisterExistingUserTest() {
        commandExecutor.execute(new Request(null, CommandCreator.newCommand("register Pesho123-127.0.0.1:2713 /home/hmmm/hi.txt")));
        commandExecutor.execute(new Request(null, CommandCreator.newCommand("unregister Pesho123-127.0.0.1:2713 /home/hmmm/hi.txt")));
        assertTrue(fileStorage.getKeys().contains(new User("Pesho123", "127.0.0.1", 2713)));
        assertEquals(0, fileStorage.listValues(new User("Pesho123","127.0.0.1", 2713)).size());
        assertFalse(fileStorage.listValues(new User("Pesho123", "127.0.0.1", 2713)).containsAll(Set.of("/home/hmmm/hi.txt")));
    }
    @Test
    void unregisterNonExistingUserTest() {
        assertEquals(ResponseStatus.ERROR,commandExecutor.execute(new Request(null, CommandCreator.newCommand("unregister Pesho12-127.0.0.1:2713 /home/hmmm/hi.txt"))).status());
    }
    @Test
    void unregisterInvalidArgumentsCountTest() {
        assertEquals(ResponseStatus.ERROR,commandExecutor.execute(new Request(null, CommandCreator.newCommand("unregister Pesho123-127.0.0.1:2713"))).status());
    }

    @Test
    void listFiles() {
        Response response = commandExecutor.execute(new Request(null, CommandCreator.newCommand("list-files")));
        assertEquals(new Response(ResponseStatus.OK, "[]"), response);
    }

    @Test
    void listUsers() {
        Response response = commandExecutor.execute(new Request(null, CommandCreator.newCommand("list-users")));
        assertEquals(new Response(ResponseStatus.OK, ""), response);
    }
}