package jp.tuor.backend.controller;

import jp.tuor.backend.model.Cliente;
import jp.tuor.backend.model.dto.ClienteDTO;
import jp.tuor.backend.model.dto.EditClienteDTO;
import jp.tuor.backend.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cliente")
@RequiredArgsConstructor
public class ClienteController {
    private final ClienteService clienteService;

    @GetMapping("/list-page")
    public Page<Cliente> listPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return clienteService.listarClientesPaginado(pageable);
    }

    @PostMapping("/create")
    public ResponseEntity<Object> createCliente(@RequestBody ClienteDTO clienteDTO) {
        this.clienteService.novoCliente(clienteDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(montarBody(HttpStatus.CREATED.value(), "Usuário criado com sucesso."));
    }

    @PutMapping("/edit")
    public ResponseEntity<Object> updateCliente(@RequestBody EditClienteDTO editClienteDTO) {
        this.clienteService.editarCliente(editClienteDTO);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(montarBody(HttpStatus.OK.value(), "Usuário editado com sucesso."));
    }

    private Map<String, Object> montarBody(Object code, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", false);
        body.put("status", code);
        body.put("message", message);

        return body;
    }
}
