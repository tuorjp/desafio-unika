package jp.tuor.backend.repository.specification;

import jakarta.persistence.criteria.*;
import jp.tuor.backend.model.Cliente;
import jp.tuor.backend.model.Endereco;
import jp.tuor.backend.utils.StringUtils;
import org.springframework.data.jpa.domain.Specification;

public class ClienteSpecification {
  public static Specification<Cliente> comFiltros(String nome, String cpfCnpj, String cidade) {
    return (Root<Cliente> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
      Predicate predicate = cb.conjunction(); //1=1

      if (nome != null && !nome.isEmpty()) {
        Predicate filtroNome = cb.like(cb.lower(root.get("nome")), "%" + nome.toLowerCase() + "%");
        Predicate filtroRazao = cb.like(cb.lower(root.get("razaoSocial")), "%" + nome.toLowerCase() + "%");
        predicate = cb.and(predicate, cb.or(filtroNome, filtroRazao));
      }

      if (cpfCnpj != null && !cpfCnpj.isEmpty()) {
        String cleanCpfCnpj = StringUtils.removerMascaraDigito(cpfCnpj);

        Predicate filtroCpf = cb.like(root.get("cpf"), "%" + cleanCpfCnpj + "%");
        Predicate filtroCnpj = cb.like(root.get("cnpj"), "%" + cleanCpfCnpj + "%");
        predicate = cb.and(predicate, cb.or(filtroCpf, filtroCnpj));
      }

      if (cidade != null && !cidade.isEmpty()) {
        Join<Cliente, Endereco> joinEnderecos = root.join("enderecos", JoinType.LEFT);
        Predicate filtroCidade = cb.like(joinEnderecos.get("cidade"), "%" + cidade + "%");
        predicate = cb.and(predicate, filtroCidade);
        if (query != null) {
          query.distinct(true);
        }
      }

      return predicate;
    };
  }
}
