package command;

import interfaces.Executor;
import interfaces.Storage;
import interfaces.UserInterface;
import messages.Request;
import messages.Response;
import messages.ResponseStatus;
import utils.UserUtils;

import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ServerCommandExecutor implements Executor {
    private static final String INVALID_ARGS_COUNT_MESSAGE_FORMAT =
            "Invalid count of arguments: \"%s\" expects at least %d arguments.";

    private static final String REGISTER = "register";
    private static final String UNREGISTER = "unregister";
    private static final String LIST_FILES = "list-files";
    private static final String LIST_USERS = "list-users";
    private static final String DISCONNECT = "disconnect";

    private final Storage<UserInterface, Set<String>> fileStorage;
    private final Storage<SocketChannel, UserInterface> sessionStorage;

    public ServerCommandExecutor(Storage<UserInterface, Set<String>> fileStorage, Storage<SocketChannel, UserInterface> sessionStorage) {
        this.fileStorage = fileStorage;
        this.sessionStorage = sessionStorage;
    }

    public Response execute(Request request) {
        Command cmd = request.command();
        return switch (cmd.command()) {
            case REGISTER -> register(request);
            case UNREGISTER -> unregister(request);
            case LIST_FILES -> listFiles(request);
            case LIST_USERS -> listUsers(request);
            case DISCONNECT -> disconnect(request);
            default -> new Response(ResponseStatus.ERROR, "Unknown command");
        };
    }

    public Response register(Request request) {
        try {
            String[] arguments = request.command().arguments();
            if (arguments.length < 2) {
                throw new IllegalArgumentException(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, REGISTER, 2));
            }
            String[] userData = arguments[0].split("-");
            UserInterface user = UserUtils.processUser(userData);
            Set<String> files = new HashSet<>(Arrays.asList(arguments).subList(1, arguments.length));
            fileStorage.addValues(user, files);
            sessionStorage.addValues(request.session(), user);
            return new Response(ResponseStatus.OK, "Files are successfully registered for downloading for user: " + user);
        } catch (IllegalArgumentException exception) {
            return new Response(ResponseStatus.ERROR, exception.getMessage());
        }
    }

    public Response unregister(Request request) {
        try {
            String[] arguments = request.command().arguments();
            if (arguments.length < 2) {
                throw new IllegalArgumentException(String.format(INVALID_ARGS_COUNT_MESSAGE_FORMAT, UNREGISTER, 2));
            }
            String[] userData = arguments[0].split("-");
            UserInterface user = UserUtils.processUser(userData);
            if (!fileStorage.getKeys().contains(user)) {
                throw new IllegalArgumentException("User " + user + " does not exist");
            }
            Set<String> files = new HashSet<>(Arrays.asList(arguments).subList(1, arguments.length));
            fileStorage.removeValues(user, files);
            return new Response(ResponseStatus.OK, "Files are successfully unregistered for downloading from user: " + user);
        } catch (NumberFormatException exception) {
            return new Response(ResponseStatus.ERROR, "User data format is invalid. It should be following format: <username> - <host:port>");
        } catch (IllegalArgumentException exception) {
            return new Response(ResponseStatus.ERROR, exception.getMessage());
        }
    }

    public Response listFiles(Request request) {
        try {
            String[] arguments = request.command().arguments();
            if (arguments.length != 0) {
                throw new IllegalArgumentException(LIST_FILES + " command does not require any arguments.");
            }
            Set<UserInterface> users = fileStorage.getKeys();
            StringBuilder filesLog = new StringBuilder();
            long i = 0;
            for (UserInterface user : users) {
                filesLog.append(user);
                filesLog.append(" : ");
                Set<String> userFiles = fileStorage.listValues(user);
                filesLog.append(userFiles);
                if (i != users.size() - 1) {
                    filesLog.append(System.lineSeparator());
                }
                i++;
            }
            if (users.isEmpty()) {
                filesLog.append("[]");
            }
            return new Response(ResponseStatus.OK, filesLog.toString());
        } catch (IllegalArgumentException exception) {
            return new Response(ResponseStatus.ERROR, exception.getMessage());
        }
    }

    public Response listUsers(Request request) {
        try {
            String[] arguments = request.command().arguments();
            if (arguments.length != 0) {
                throw new IllegalArgumentException(LIST_USERS + " command does not require any arguments.");
            }
            Set<UserInterface> users = fileStorage.getKeys();
            StringBuilder usersLog = new StringBuilder();
            long i = 0L;
            for (UserInterface user : users) {
                usersLog.append(user);
                if (i != users.size() - 1) {
                    usersLog.append(System.lineSeparator());
                }
                i++;
            }
            return new Response(ResponseStatus.OK, usersLog.toString());
        } catch (IllegalArgumentException exception) {
            return new Response(ResponseStatus.ERROR, exception.getMessage());
        }
    }

    public Response disconnect(Request request) {
        try {
            String[] arguments = request.command().arguments();
            if (arguments.length != 0) {
                throw new IllegalArgumentException(DISCONNECT + " command does not require any arguments.");
            }
            UserInterface user = sessionStorage.remove(request.session());
            fileStorage.remove(user);
            return new Response(ResponseStatus.OK, user + "'s session has been terminated!");
        } catch (IllegalArgumentException exception) {
            return new Response(ResponseStatus.ERROR, exception.getMessage());
        }
    }
}
