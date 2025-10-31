package jp.tuor.backend.model.dto;

import jp.tuor.backend.model.Endereco;
import jp.tuor.backend.model.enums.TipoPessoa;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class ClienteDTO {
    private TipoPessoa tipoPessoa;
    private String cpf;
    private String cnpj;
    private String nome;
    private String rg;
    private Date dataNascimento;
    private Date dataCriacao;
    private String razaoSocial;
    private String inscricaoEstadual;
    private String email;
    private boolean ativo;
    private List<Endereco> enderecos;
}
