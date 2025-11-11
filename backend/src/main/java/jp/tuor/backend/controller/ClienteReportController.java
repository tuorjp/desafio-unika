package jp.tuor.backend.controller;

import jp.tuor.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JRException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/api/cliente/report")
@RequiredArgsConstructor
public class ClienteReportController {
  private final ReportService reportService;

  @GetMapping("/pdf/geral")
  public ResponseEntity<InputStreamResource> exportReportGeral() throws JRException, FileNotFoundException {
    byte[] pdfBytes = reportService.gerarRelatorioGeralClientes();
    return criarRespostaPdf(pdfBytes, "relatorio_geral_clientes");
  }

  @GetMapping("/pdf/{id}")
  public ResponseEntity<InputStreamResource> exportReportIndividual(@PathVariable Long id) throws JRException, FileNotFoundException {
    byte[] pdfBytes = reportService.gerarRelatorioClienteIndividual(id);
    return criarRespostaPdf(pdfBytes, "relatorio_cliente_" + id);
  }

  private ResponseEntity<InputStreamResource> criarRespostaPdf(byte[] pdfBytes, String baseFileName) {
    ByteArrayInputStream bais = new ByteArrayInputStream(pdfBytes);

    String data = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    String filename = String.format("%s_%s.pdf", baseFileName, data);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + filename);

    return ResponseEntity
      .ok()
      .headers(headers)
      .contentType(MediaType.APPLICATION_PDF)
      .body(new InputStreamResource(bais));
  }
}
