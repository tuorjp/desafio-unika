package jp.tuor.backend.service.exceptions;

public class CampoInvalidoException extends RuntimeException {
    public CampoInvalidoException(String message) {
        super(message);
    }
}
