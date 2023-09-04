package messages;

import java.io.Serializable;

public record Response(ResponseStatus status, String message) implements Serializable {
}
