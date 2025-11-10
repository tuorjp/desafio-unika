package jp.tuor.backend.service;

import jp.tuor.backend.model.Cliente;
import jp.tuor.backend.model.dto.ClienteDTO;
import jp.tuor.backend.model.dto.EnderecoDTO;
import jp.tuor.backend.model.enums.TipoOperacao;
import jp.tuor.backend.model.enums.TipoPessoa;
import jp.tuor.backend.model.mapper.ClienteMapper;
import jp.tuor.backend.repository.ClienteRepository;
import jp.tuor.backend.service.exceptions.CPFCNPJDuplicadoException;
import jp.tuor.backend.service.exceptions.CampoInvalidoException;
import jp.tuor.backend.utils.ValidadorUtil;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ClienteExcelImportService {
  private final ClienteRepository clienteRepository;
  private final ValidadorUtil validadorUtil;
  private final ClienteMapper clienteMapper;

  private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy");

  public Map<String, Object> importarClientesExcel(MultipartFile file) throws IOException {
    List<String> erros = new ArrayList<>();
    List<Cliente> clientesParaSalvar = new ArrayList<>();
    DataFormatter dataFormatter = new DataFormatter();

    //LAYOUT:
    //Col 0: TIPO_PESSOA (FISICA/JURIDICA)
    //Col 1: CPF
    //Col 2: CNPJ
    //Col 3: NOME
    //Col 4: RAZAO_SOCIAL
    //Col 5: RG
    //Col 6: INSCRICAO_ESTADUAL
    //Col 7: DATA_NASCIMENTO (dd/MM/yyyy)
    //Col 8: DATA_CRIACAO (dd/MM/yyyy)
    //Col 9: EMAIL
    //Col 10: ENDERECOS (formato: "log;num;cep;bairro;cidade;estado | log2;num2...")

    try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
      Sheet sheet = workbook.getSheetAt(0);

      for (int i = 1; i <= sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i);
        if (row == null) continue;

        int linhaAtual = i + 1;

        try {
          ClienteDTO dto = new ClienteDTO();
          String tipoPessoaStr = getValorCelula(row.getCell(0), dataFormatter).toUpperCase();

          if (tipoPessoaStr.equals("FISICA")) {
            dto.setTipoPessoa(TipoPessoa.FISICA);
            dto.setCpf(getValorCelula(row.getCell(1), dataFormatter));
            dto.setNome(getValorCelula(row.getCell(3), dataFormatter));
            dto.setRg(getValorCelula(row.getCell(5), dataFormatter));
            dto.setDataNascimento(parseData(getValorCelula(row.getCell(7), dataFormatter), linhaAtual));
          } else if (tipoPessoaStr.equals("JURIDICA")) {
            dto.setTipoPessoa(TipoPessoa.JURIDICA);
            dto.setCnpj(getValorCelula(row.getCell(2), dataFormatter));
            dto.setRazaoSocial(getValorCelula(row.getCell(4), dataFormatter));
            dto.setInscricaoEstadual(getValorCelula(row.getCell(6), dataFormatter));
            dto.setDataCriacao(parseData(getValorCelula(row.getCell(8), dataFormatter), linhaAtual));
          } else {
            throw new CampoInvalidoException("Tipo de Pessoa '" + tipoPessoaStr + "' inválido.");
          }

          dto.setEmail(getValorCelula(row.getCell(9), dataFormatter));

          //endereços do cliente
          String enderecosStr = getValorCelula(row.getCell(10), dataFormatter);
          dto.setEnderecos(parseMultiplosEnderecos(enderecosStr, linhaAtual));

          validadorUtil.validarCamposObrigatorios(dto);

          if (dto.getTipoPessoa().equals(TipoPessoa.FISICA)) {
            if (clienteRepository.findByCpf(dto.getCpf()).isPresent()) {
              throw new CPFCNPJDuplicadoException("CPF " + dto.getCpf() + " já cadastrado.");
            }
          } else {
            if (clienteRepository.findByCnpj(dto.getCnpj()).isPresent()) {
              throw new CPFCNPJDuplicadoException("CNPJ " + dto.getCnpj() + " já cadastrado.");
            }
          }

          Cliente cliente = new Cliente();
          clienteMapper.preencheClienteObjComClienteDTO(cliente, dto, TipoOperacao.CRIACAO);
          clientesParaSalvar.add(cliente);

        } catch (Exception e) {
          String errorMessage = "Linha " + linhaAtual + ": " + e.getMessage();
          if (e.getMessage() != null && e.getMessage().contains("; ")) {
            errorMessage = "Linha " + linhaAtual + ": " + e.getMessage().replace(";", " | ");
          }
          erros.add(errorMessage);
        }
      }
    }

    Map<String, Object> resultado = new HashMap<>();

    //transação: salvar todos ou lançar erros
    if (erros.isEmpty()) {
      clienteRepository.saveAll(clientesParaSalvar);
      resultado.put("sucesso", true);
      resultado.put("clientesCriados", clientesParaSalvar.size());
    } else {
      resultado.put("sucesso", false);
      resultado.put("clientesCriados", 0);
      resultado.put("erros", erros);
    }

    return resultado;
  }

  private String getValorCelula(Cell cell, DataFormatter formatter) {
    if (cell == null) {
      return "";
    }
    return formatter.formatCellValue(cell).trim();
  }

  private Date parseData(String dataStr, int linha) throws CampoInvalidoException {
    if (dataStr == null || dataStr.trim().isEmpty()) {
      return null;
    }
    try {
      return DATE_FORMATTER.parse(dataStr);
    } catch (ParseException e) {
      throw new CampoInvalidoException("Formato de data inválido (esperado dd/MM/yyyy): " + dataStr);
    }
  }

  private List<EnderecoDTO> parseMultiplosEnderecos(String enderecosStr, int linha) throws CampoInvalidoException {
    List<EnderecoDTO> listaEnderecos = new ArrayList<>();
    if (enderecosStr == null || enderecosStr.trim().isEmpty()) {
      return listaEnderecos;
    }

    String[] enderecosArray = enderecosStr.split("\\|"); //pipe separador

    for (String endStr : enderecosArray) {
      String[] campos = endStr.split(";");

      //6 campos: log, num, cep, bairro, cidade, estado
      if (campos.length < 6) {
        throw new CampoInvalidoException("Formato de endereço inválido. Esperava 6 campos separados por ';'. Recebido: " + endStr);
      }

      EnderecoDTO dto = new EnderecoDTO();
      dto.setLogradouro(campos[0].trim());
      dto.setNumero(campos[1].trim());
      dto.setCep(campos[2].trim());
      dto.setBairro(campos[3].trim());
      dto.setCidade(campos[4].trim());
      dto.setEstado(campos[5].trim());

      //complemento
      if (campos.length > 6) {
        dto.setComplemento(campos[6].trim());
      }

      //primeiro endereço da lista como o principal
      if (listaEnderecos.isEmpty()) {
        dto.setEnderecoPrincipal(true);
      } else {
        dto.setEnderecoPrincipal(false);
      }

      listaEnderecos.add(dto);
    }

    return listaEnderecos;
  }
}
