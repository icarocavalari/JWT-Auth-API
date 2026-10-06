package io.github.icarocavalari.authapi.exceptions;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String errorMessage) {
        super(errorMessage);
    }
}
