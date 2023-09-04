package interfaces;

import messages.Request;
import messages.Response;

import java.io.FileNotFoundException;

public interface Executor {
    Response execute(Request request) throws FileNotFoundException;
}
