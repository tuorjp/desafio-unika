package jp.tuor.backend.repository;

import jp.tuor.backend.model.Cliente;
import jp.tuor.backend.model.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnderecoRepository extends JpaRepository<Endereco, Long> {
    List<Endereco> getByCliente(Cliente cliente);
}
