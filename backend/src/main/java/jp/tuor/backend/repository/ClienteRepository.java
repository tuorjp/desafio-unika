package jp.tuor.backend.repository;

import jp.tuor.backend.model.Cliente;
import jp.tuor.backend.model.enums.TipoPessoa;
import jp.tuor.backend.repository.dto.ResumoClientesPorCidade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long>, JpaSpecificationExecutor<Cliente> {
  Optional<Cliente> findByCnpj(String cnpj);

  Optional<Cliente> findByCpf(String cpf);

  //forçando a busca dos endereços mesmo eles sendo FtchType.LAZY, um LEFT JOIN FETCH c.enderecos automático
  @Override
  @EntityGraph(attributePaths = {"enderecos"})
  Optional<Cliente> findById(Long id);

  @Override
  @EntityGraph(attributePaths = {"enderecos"})
  Page<Cliente> findAll(Specification<Cliente> spec, Pageable pageable);

  //JPQL
  @Query(
          "SELECT DISTINCT c FROM Cliente c " +
                  "JOIN c.enderecos e " +
                  "WHERE c.tipoPessoa = :tipoPessoa AND e.estado = :estado"
  )
  List<Cliente> findByTipoPessoaAndEstado(
          @Param("tipoPessoa") TipoPessoa tipoPessoa,
          @Param("estado") String estado
  );

  //SQL puro
  @Query(
          value = "SELECT e.cidade AS cidade, COUNT(DISTINCT c.id) AS totalClientes " +
                  "FROM clientes c " +
                  "JOIN enderecos e ON c.id = e.cliente_id " +
                  "GROUP BY e.cidade " +
                  "ORDER BY totalClientes DESC",
          nativeQuery = true
  )
  List<ResumoClientesPorCidade> getResumoClientesPorCidade();

  Long id(Long id);
}
