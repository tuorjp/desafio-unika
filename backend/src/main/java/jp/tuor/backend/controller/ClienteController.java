package jp.tuor.backend.controller;

import jp.tuor.backend.model.Cliente;
import jp.tuor.backend.model.dto.ClienteDTO;
import jp.tuor.backend.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cliente")
@RequiredArgsConstructor
public class ClienteController {
    private final ClienteService clienteService;

    @PostMapping("/create")
    public ResponseEntity<String> createCliente(@RequestBody ClienteDTO clienteDTO) {
        this.clienteService.novoCliente(clienteDTO);
        return ResponseEntity.ok("Cliente criado com sucesso");
    }

    @GetMapping("/cliente-page")
    public Page<Cliente> listPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return clienteService.listarClientesPaginado(pageable);
    }
}
