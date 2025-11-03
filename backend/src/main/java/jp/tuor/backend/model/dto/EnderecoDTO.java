package jp.tuor.backend.model.dto;

import jp.tuor.backend.utils.annotations.Obrigatorio;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnderecoDTO {
    private Long id;
    @Obrigatorio
    private String logradouro;
    @Obrigatorio
    private String numero;
    @Obrigatorio
    private String cep;
    @Obrigatorio
    private String bairro;
    @Obrigatorio
    private String cidade;
    @Obrigatorio
    private String estado;
    @Obrigatorio
    private boolean enderecoPrincipal;
    private String complemento;
}
