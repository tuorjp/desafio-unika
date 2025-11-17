package jp.tuor.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@Entity
public class Endereco implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String logradouro;

  @Column
  private String numero;

  @Column(nullable = false)
  private String cep;

  @Column(nullable = false)
  private String bairro;

  @Column(nullable = false)
  private String cidade;

  @Column(nullable = false)
  private String estado;

  @Column(nullable = false)
  private boolean enderecoPrincipal;

  @Column
  private String complemento;

  @JsonBackReference
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cliente_id")
  private Cliente cliente;
}
