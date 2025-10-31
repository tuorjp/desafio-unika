package jp.tuor.backend.service;

import jp.tuor.backend.model.Cliente;
import jp.tuor.backend.model.Endereco;
import jp.tuor.backend.repository.EnderecoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnderecoService {
    private final EnderecoRepository enderecoRepository;

    public void editEndereco(Endereco endereco) {
        this.enderecoRepository.save(endereco);
    }

    public Endereco findById(Long id) {
        return this.enderecoRepository.findById(id).orElse(null);
    }

    public List<Endereco> getEnderecosByCliente(Cliente cliente) {
        return this.enderecoRepository.getByCliente(cliente);
    }
}
