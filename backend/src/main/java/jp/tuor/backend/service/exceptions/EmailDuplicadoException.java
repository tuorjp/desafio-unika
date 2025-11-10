package jp.tuor.backend.service.exceptions;

public class EmailDuplicadoException extends RuntimeException {
  public EmailDuplicadoException(String message) {
    super(message);
  }
}
