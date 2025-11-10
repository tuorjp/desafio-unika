package jp.tuor.backend.utils;

import jp.tuor.backend.model.Cliente;
import jp.tuor.backend.model.Endereco;
import jp.tuor.backend.model.enums.TipoPessoa;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class StringUtils {
  public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

  public static String removerMascaraDigito(String valor) {
    if (valor == null || valor.isEmpty()) {
      return valor;
    }

    return valor.replaceAll("\\D", "");
  }

  public static String getNomeCliente(Cliente cliente) {
    String nome = cliente.getTipoPessoa() == TipoPessoa.FISICA ? cliente.getNome() : cliente.getRazaoSocial();
    return nome != null ? nome : "";
  }

  public static String getDataCliente(Cliente cliente) {
    Date data = cliente.getTipoPessoa() == TipoPessoa.FISICA ? cliente.getDataNascimento() : cliente.getDataCriacao();
    return data != null ? DATE_FORMAT.format(data) : "N/A";
  }

  public static String getEnderecoPrincipal(List<Endereco> enderecos) {
    if (enderecos == null || enderecos.isEmpty()) {
      return "N/A";
    }

    Endereco principal = enderecos.stream()
            .filter(Endereco::isEnderecoPrincipal)
            .findFirst()
            .orElse(null);

    if (principal == null) {
      principal = enderecos.get(0);
    }

    return String.format("%s, %s, %s-%s",
            principal.getLogradouro(),
            principal.getNumero(),
            principal.getCidade(),
            principal.getEstado()
    );
  }

  public static String getDocumentoCliente(Cliente cliente) {
    String doc = cliente.getTipoPessoa() == TipoPessoa.FISICA ? cliente.getCpf() : cliente.getCnpj();
    if (doc == null) {
      return "N/A";
    }
    return doc;
  }
}
