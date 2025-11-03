package jp.tuor.backend.controller.exceptions;

import jp.tuor.backend.service.exceptions.CPFCNPJDuplicadoException;
import jp.tuor.backend.service.exceptions.CampoInvalidoException;
import jp.tuor.backend.service.exceptions.ClienteNaoEncontradoException;
import jp.tuor.backend.service.exceptions.EmailDuplicadoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ControllerExceptionHandler {
    @ExceptionHandler(ClienteNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleClienteNaoEncontradoException(ClienteNaoEncontradoException e) {
        var code = HttpStatus.NOT_FOUND.value();
        var message = e.getMessage();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(montarBody(code, message));
    }

    @ExceptionHandler(CPFCNPJDuplicadoException.class)
    public ResponseEntity<Map<String, Object>> handleCPFCNPJDuplicadoException(CPFCNPJDuplicadoException e) {
        var code = HttpStatus.CONFLICT.value();
        var message = e.getMessage();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(montarBody(code, message));
    }

    @ExceptionHandler(EmailDuplicadoException.class)
    public ResponseEntity<Map<String, Object>> handleEmailDuplicadoException(EmailDuplicadoException e) {
        var code = HttpStatus.CONFLICT.value();
        var message = e.getMessage();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(montarBody(code, message));
    }

    @ExceptionHandler(CampoInvalidoException.class)
    public ResponseEntity<Map<String, Object>> handleCampoInvalidoException(CampoInvalidoException e) {
        var code = HttpStatus.BAD_REQUEST.value();
        var message = e.getMessage();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(montarBody(code, message));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, Object>> handleIOException(IOException e) {
        var code = HttpStatus.INTERNAL_SERVER_ERROR.value();
        var message = e.getMessage();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(montarBody(code, message));
    }

    private Map<String, Object> montarBody(Object code, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", true);
        body.put("status", code);
        body.put("message", message);

        return body;
    }
}
