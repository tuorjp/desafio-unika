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
    if (cliente == null) {
      return "n/a";
    }

    if (TipoPessoa.FISICA.equals(cliente.getTipoPessoa()) && cliente.getCpf() != null) {
      return formatarCPF(cliente.getCpf());

    } else if (TipoPessoa.JURIDICA.equals(cliente.getTipoPessoa()) && cliente.getCnpj() != null) {
      return formatarCNPJ(cliente.getCnpj());

    } else {
      return "n/a";
    }
  }

  public static String formatarCPF(String cpf) {
    cpf = removerMascaraDigito(cpf);
    if (cpf == null || cpf.length() != 11) {
      return cpf;
    }
    return cpf.replaceFirst("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
  }

  public static String formatarCNPJ(String cnpj) {
    cnpj = removerMascaraDigito(cnpj);
    if (cnpj == null || cnpj.length() != 14) {
      return cnpj;
    }
    return cnpj.replaceFirst("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
  }

  public static String formatarCEP(String cep) {
    cep = removerMascaraDigito(cep);
    if (cep == null || cep.length() != 8) {
      return cep;
    }
    return cep.replaceFirst("(\\d{5})(\\d{3})", "$1-$2");
  }
}
