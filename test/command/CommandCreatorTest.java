package command;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommandCreatorTest {
    @Test
    void newCommand() {
        String commandText = "download user source destination";
        Command command = CommandCreator.newCommand(commandText);
        assertEquals("download", command.command());
        assertEquals(3, command.arguments().length);
        assertEquals("user",command.arguments()[0]);
        assertEquals("source", command.arguments()[1]);
        assertEquals("destination", command.arguments()[2]);
    }
}