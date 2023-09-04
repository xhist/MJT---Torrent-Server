package messages;

import command.Command;

import java.nio.channels.SocketChannel;

public record Request(SocketChannel session, Command command) {

}
