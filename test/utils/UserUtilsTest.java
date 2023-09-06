package utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import storage.User;

import static org.junit.jupiter.api.Assertions.*;

class UserUtilsTest {

    @Test
    void processUserInvalidCountOfArgumentsTest() {
        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                UserUtils.processUser(new String[]{"Pesho123"});
            }
        });
    }

    @Test
    void processUserInvalidHostAndPortTest() {
        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                UserUtils.processUser(new String[]{"Pesho123", "127.0.0.1"});
            }
        });
    }
    @Test
    void processUserInvalidPortTest() {
        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                UserUtils.processUser(new String[]{"Pesho123", "127.0.0.1:hdsd"});
            }
        });
    }

    @Test
    void processUserValidTest() {
        assertEquals(new User("Pesho123", "127.0.0.1", 2713), UserUtils.processUser(new String[]{"Pesho123","127.0.0.1:2713"}));
    }
}