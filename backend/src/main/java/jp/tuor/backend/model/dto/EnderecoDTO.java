package jp.tuor.backend.model.dto;

import jp.tuor.backend.utils.StringUtils;
import jp.tuor.backend.utils.annotations.Obrigatorio;
import lombok.Getter;
import lombok.Setter;

@Getter
public class EnderecoDTO {
    @Setter
    private Long id;

    @Setter
    @Obrigatorio
    private String logradouro;

    @Setter
    @Obrigatorio
    private String numero;

    @Obrigatorio
    private String cep;

    @Setter
    @Obrigatorio
    private String bairro;

    @Setter
    @Obrigatorio
    private String cidade;

    @Setter
    @Obrigatorio
    private String estado;

    @Setter
    @Obrigatorio
    private boolean enderecoPrincipal;

    @Setter
    private String complemento;

    //setter customizado
    public void setCep(String cep) {
        this.cep = StringUtils.removerMascaraDigito(cep);
    }
}
