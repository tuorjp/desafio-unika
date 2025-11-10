package jp.tuor.backend.service.exceptions;

public class EnderecoNaoEncontradoException extends RuntimeException {
  public EnderecoNaoEncontradoException(String message) {
    super(message);
  }
}
