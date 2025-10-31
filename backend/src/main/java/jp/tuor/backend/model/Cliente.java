package jp.tuor.backend.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jp.tuor.backend.model.enums.TipoPessoa;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private TipoPessoa tipoPessoa;

    @Column
    private String cpf;

    @Column
    private String cnpj;

    @Column
    private String nome;

    @Column
    private String rg;

    @Column
    private Date dataNascimento;

    @Column
    private Date dataCriacao;

    @Column
    private String razaoSocial;

    @Column
    private String inscricaoEstadual;

    @Column
    private String email;

    @Column(nullable = false)
    private boolean ativo;

    @JsonManagedReference
    @OneToMany(
            mappedBy = "cliente",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Endereco> enderecos;
}
