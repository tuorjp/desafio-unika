package jp.tuor.backend.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnderecoDTO {
    private String logradouro;
    private String numero;
    private String cep;
    private String bairro;
    private String telefone;
    private String cidade;
    private String estado;
    private boolean enderecoPrincipal;
    private String complemento;
    private Long clienteId;
}
