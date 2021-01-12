package es.um.asio.service.exceptions;

public class CustomFederationException extends RuntimeException {

    private static final long serialVersionUID = 7718828512143293558L;

    public CustomFederationException(String message, Throwable cause) {
        super(message,cause);
    }

    public CustomFederationException(String message) {
        super(message);
    }

    public CustomFederationException() {
    }

}
