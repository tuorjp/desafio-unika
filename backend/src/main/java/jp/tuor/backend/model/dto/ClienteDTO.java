package jp.tuor.backend.model.dto;

import jp.tuor.backend.model.enums.TipoPessoa;
import jp.tuor.backend.utils.StringUtils;
import jp.tuor.backend.utils.annotations.Obrigatorio;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
public class ClienteDTO implements Serializable {
  private static final long serialVersionUID = 1L;

  @Setter
  private Long id;

  @Setter
  @Obrigatorio
  private TipoPessoa tipoPessoa;

  @Obrigatorio(dependeDeCampo = "tipoPessoa")
  private String cpf;

  @Obrigatorio(dependeDeCampo = "tipoPessoa")
  private String cnpj;

  @Setter
  @Obrigatorio(dependeDeCampo = "tipoPessoa")
  private String nome;

  @Obrigatorio(dependeDeCampo = "tipoPessoa")
  private String rg;

  @Setter
  @Obrigatorio(dependeDeCampo = "tipoPessoa")
  private Date dataNascimento;

  @Setter
  @Obrigatorio(dependeDeCampo = "tipoPessoa")
  private Date dataCriacao;

  @Setter
  @Obrigatorio(dependeDeCampo = "tipoPessoa")
  private String razaoSocial;

  @Setter
  @Obrigatorio(dependeDeCampo = "tipoPessoa")
  private String inscricaoEstadual;

  @Setter
  private String email;

  @Setter
  private boolean ativo;

  @Setter
  private List<EnderecoDTO> enderecos;

  //setters customizados
  public void setCpf(String cpf) {
    this.cpf = StringUtils.removerMascaraDigito(cpf);
  }

  public void setCnpj(String cnpj) {
    this.cnpj = StringUtils.removerMascaraDigito(cnpj);
  }

  public void setRg(String rg) {
    this.rg = StringUtils.removerMascaraDigito(rg);
  }
}
