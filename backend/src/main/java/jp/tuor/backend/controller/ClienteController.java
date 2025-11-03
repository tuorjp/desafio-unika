package jp.tuor.backend.controller;

import jp.tuor.backend.model.Cliente;
import jp.tuor.backend.model.dto.ClienteDTO;
import jp.tuor.backend.service.ClienteService;
import jp.tuor.backend.service.exceptions.CampoInvalidoException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cliente")
@RequiredArgsConstructor
public class ClienteController {
    private final ClienteService clienteService;

    @GetMapping("/export-excel")
    public ResponseEntity<InputStreamResource> exportClientsExcel() throws IOException {
        ByteArrayInputStream bais = clienteService.gerarRelatorioExcel();

        String data = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        String filename = "relatorio_clientes_" + data + ".xslx";

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(bais));
    }

    @GetMapping("list-all")
    public ResponseEntity<List<Cliente>> listAll() {
        List<Cliente> clientes = clienteService.listarClientes();

        return ResponseEntity.ok(clientes);
    }

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

    @PostMapping("/import/excel")
    public ResponseEntity<?> importarClientesExcel(@RequestParam("file")MultipartFile file) {
       if(file.isEmpty()) {
           throw new CampoInvalidoException("Por favor envie um arquivo .xlsx");
       }

       try {
           Map<String, Object> resultado = clienteService.importarClientesExcel(file);

           if(resultado.containsKey("erros") && !((List<?>) resultado.get("erros")).isEmpty()){
                return ResponseEntity.badRequest().body(resultado);
           }

            return ResponseEntity.ok(resultado);
       } catch (Exception e) {
           System.out.println(e.getMessage());
           return ResponseEntity.status(500).body("Erro inesperado ao gerar o arquivo");
       }
    }

    @PutMapping("/edit")
    public ResponseEntity<Object> updateCliente(@RequestBody ClienteDTO editClienteDTO) {
        this.clienteService.editarCliente(editClienteDTO);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(montarBody(HttpStatus.OK.value(), "Usuário editado com sucesso."));
    }

    //método auxiliar que monta bodies genéricos
    private Map<String, Object> montarBody(Object code, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", false);
        body.put("status", code);
        body.put("message", message);

        return body;
    }
}
