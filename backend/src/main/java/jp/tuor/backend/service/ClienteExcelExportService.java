package jp.tuor.backend.service;

import jp.tuor.backend.model.Cliente;
import jp.tuor.backend.model.Endereco;
import jp.tuor.backend.model.enums.TipoPessoa;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClienteExcelExportService {

  public ByteArrayInputStream gerarPlanilhaClientes(List<Cliente> clientes) throws IOException {
    //novo workbook
    try (Workbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("Clientes");

      //linha de Cabeçalho
      Row headerRow = sheet.createRow(0);
      String[] colunas = {"ID", "Tipo Pessoa", "Nome/Razão Social", "CPF/CNPJ", "Email", "Inscrição Estadual", "RG", "Ativo", "Endereços"};

      for (int i = 0; i < colunas.length; i++) {
        Cell cell = headerRow.createCell(i);
        cell.setCellValue(colunas[i]);
      }

      int rowIdx = 1;
      for (Cliente cliente : clientes) {
        Row dataRow = sheet.createRow(rowIdx++);

        dataRow.createCell(0).setCellValue(cliente.getId());
        dataRow.createCell(1).setCellValue(cliente.getTipoPessoa().name());

        if (cliente.getTipoPessoa().equals(TipoPessoa.FISICA)) {
          dataRow.createCell(2).setCellValue(cliente.getNome());
          dataRow.createCell(3).setCellValue(cliente.getCpf());
          dataRow.createCell(6).setCellValue(cliente.getRg());
        } else {
          dataRow.createCell(2).setCellValue(cliente.getRazaoSocial());
          dataRow.createCell(3).setCellValue(cliente.getCnpj());
          dataRow.createCell(5).setCellValue(cliente.getInscricaoEstadual());
        }

        dataRow.createCell(4).setCellValue(cliente.getEmail());
        dataRow.createCell(7).setCellValue(cliente.isAtivo() ? "Sim" : "Não");

        //transforma a lista de endereços em uma única string
        dataRow.createCell(8).setCellValue(flattenEnderecos(cliente.getEnderecos()));
      }

      //workbook em ByteArrayOutputStream
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      workbook.write(out);

      //inputStream para o controller
      return new ByteArrayInputStream(out.toByteArray());
    }
  }

  private String flattenEnderecos(List<Endereco> enderecos) {
    if (enderecos == null || enderecos.isEmpty()) {
      return "N/A";
    }

    return enderecos
            .stream()
            .map(e ->
                    String.format("%s, %s, %s - %s (%s)", e.getLogradouro(), e.getNumero(), e.getBairro(), e.getCidade(), e.getEstado()))
            .collect(Collectors.joining(" | "));
  }
}