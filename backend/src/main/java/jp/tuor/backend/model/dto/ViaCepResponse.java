package jp.tuor.backend.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ViaCepResponse {
  private String logradouro;
  private String complemento;
  private String bairro;
  private String localidade;
  private String uf;
  private boolean erro = false;

  @Override
  public String toString() {
    return "ViaCepResponse{" +
      "logradouro='" + logradouro + '\'' +
      ", complemento='" + complemento + '\'' +
      ", bairro='" + bairro + '\'' +
      ", localidade='" + localidade + '\'' +
      ", uf='" + uf + '\'' +
      ", erro=" + erro +
      '}';
  }
}
