package jp.tuor.backend.controller.exceptions;

import jp.tuor.backend.service.exceptions.CPFCNPJDuplicadoException;
import jp.tuor.backend.service.exceptions.ClienteNaoEncontradoException;
import jp.tuor.backend.service.exceptions.EmailDuplicadoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ControllerExceptionHandler {
    @ExceptionHandler(ClienteNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleClienteNaoEncontradoException(ClienteNaoEncontradoException e) {
        var code = HttpStatus.NOT_FOUND.value();
        var message = e.getMessage();

        Map<String, Object> body = new HashMap<>();
        body.put("code", code);
        body.put("message", message);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(CPFCNPJDuplicadoException.class)
    public ResponseEntity<Map<String, Object>> handleCPFCNPJDuplicadoException(CPFCNPJDuplicadoException e) {
        var code = HttpStatus.CONFLICT.value();
        var message = e.getMessage();

        Map<String, Object> body = new HashMap<>();
        body.put("code", code);
        body.put("message", message);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(EmailDuplicadoException.class)
    public ResponseEntity<Map<String, Object>> handleEmailDuplicadoException(EmailDuplicadoException e) {
        var code = HttpStatus.CONFLICT.value();
        var message = e.getMessage();

        Map<String, Object> body = new HashMap<>();
        body.put("code", code);
        body.put("message", message);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
}
