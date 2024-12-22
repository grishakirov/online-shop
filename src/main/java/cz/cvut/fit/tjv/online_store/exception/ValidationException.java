package cz.cvut.fit.tjv.online_store.exception;

import java.util.List;

public class ValidationException extends RuntimeException {
    private final List<String> errors;

    public ValidationException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}