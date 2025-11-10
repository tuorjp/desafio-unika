package jp.tuor.backend.service.exceptions;

public class ClienteNaoEncontradoException extends RuntimeException {
  public ClienteNaoEncontradoException(String message) {
    super(message);
  }
}
