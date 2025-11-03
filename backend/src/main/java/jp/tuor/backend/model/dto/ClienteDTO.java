package jp.tuor.backend.model.dto;

import jp.tuor.backend.model.enums.TipoPessoa;
import jp.tuor.backend.utils.annotations.Obrigatorio;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class ClienteDTO {
    private Long id;
    @Obrigatorio
    private TipoPessoa tipoPessoa;
    @Obrigatorio(dependeDeCampo = "tipoPessoa")
    private String cpf;
    @Obrigatorio(dependeDeCampo = "tipoPessoa")
    private String cnpj;
    @Obrigatorio(dependeDeCampo = "tipoPessoa")
    private String nome;
    @Obrigatorio(dependeDeCampo = "tipoPessoa")
    private String rg;
    @Obrigatorio(dependeDeCampo = "tipoPessoa")
    private Date dataNascimento;
    @Obrigatorio(dependeDeCampo = "tipoPessoa")
    private Date dataCriacao;
    @Obrigatorio(dependeDeCampo = "tipoPessoa")
    private String razaoSocial;
    @Obrigatorio(dependeDeCampo = "tipoPessoa")
    private String inscricaoEstadual;
    private String email;
    private boolean ativo;
    private List<EnderecoDTO> enderecos;
}
