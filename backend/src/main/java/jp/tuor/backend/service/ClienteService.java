package jp.tuor.backend.service;

import jp.tuor.backend.model.Cliente;
import jp.tuor.backend.model.Endereco;
import jp.tuor.backend.model.dto.ClienteDTO;
import jp.tuor.backend.model.dto.EditClienteDTO;
import jp.tuor.backend.model.enums.TipoPessoa;
import jp.tuor.backend.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClienteService {
    private final ClienteRepository clienteRepository;

    public void novoCliente(ClienteDTO clienteDTO) {
        Cliente cliente = new Cliente();
        preencheClienteObjComClienteDTO(cliente, clienteDTO);
        this.clienteRepository.save(cliente);
    }

    public void editarCliente(EditClienteDTO clienteDTO) {
        Optional<Cliente> clienteSalvo = this.clienteRepository.findById(clienteDTO.getId());
        if(clienteSalvo.isPresent()) {
            Cliente cliente = clienteSalvo.get();
            limpaCamposDTOPorTipoPessoa(cliente, clienteDTO);
            preencheClienteObjComClienteDTO(cliente, clienteDTO);
            this.clienteRepository.save(cliente);
        }
    }

    public void deletarCliente(Long id) {
        Cliente cliente = this.buscaClientePorId(id);

        if(cliente != null) {
            this.clienteRepository.delete(cliente);
        }
    }

    public void invalidarCliente(Long id) {
        Cliente cliente = this.buscaClientePorId(id);
        if(cliente != null) {
            cliente.setAtivo(false);
            this.clienteRepository.save(cliente);
        }
    }

    public List<Cliente> listarClientes() {
        return this.clienteRepository.findAll();
    }

    public Page<Cliente> listarClientesPaginado(Pageable pageable) {
        return this.clienteRepository.findAll(pageable);
    }

    public Cliente buscaClientePorId(Long id) {
        Optional<Cliente> cliente = this.clienteRepository.findById(id);
        return cliente.orElse(null);
    }

    private void preencheClienteObjComClienteDTO(Cliente cliente, ClienteDTO clienteDTO) {
        cliente.setTipoPessoa(clienteDTO.getTipoPessoa());
        if (clienteDTO.getTipoPessoa().equals(TipoPessoa.FISICA)) {
            cliente.setCpf(clienteDTO.getCpf());
            cliente.setNome(clienteDTO.getNome());
            cliente.setRg(clienteDTO.getRg());
            cliente.setDataNascimento(clienteDTO.getDataNascimento());
        } else {
            cliente.setCnpj(clienteDTO.getCnpj());
            cliente.setRazaoSocial(clienteDTO.getRazaoSocial());
            cliente.setInscricaoEstadual(clienteDTO.getInscricaoEstadual());
            cliente.setDataCriacao(clienteDTO.getDataCriacao());
        }

        cliente.setEmail(clienteDTO.getEmail());
        cliente.setAtivo(true);
        cliente.setEnderecos(montarEnderecosDoCliente(cliente, clienteDTO.getEnderecos()));
    }

    private void limpaCamposDTOPorTipoPessoa(Cliente cliente, ClienteDTO clienteDTO) {
        if (clienteDTO.getTipoPessoa().equals(TipoPessoa.FISICA)) {
            cliente.setCnpj("");
            cliente.setRazaoSocial("");
            cliente.setInscricaoEstadual("");
            cliente.setDataCriacao(null);
        } else {
            cliente.setCpf("");
            cliente.setNome("");
            cliente.setRg("");
            cliente.setDataNascimento(null);
        }
    }

    private List<Endereco> montarEnderecosDoCliente(Cliente cliente, List<Endereco> enderecos) {
        cliente.setEnderecos(
                enderecos
                        .stream()
                        .map(dto -> {
                            Endereco endereco = new Endereco();
                            endereco.setLogradouro(dto.getLogradouro());
                            endereco.setNumero(dto.getNumero());
                            endereco.setCep(dto.getCep());
                            endereco.setBairro(dto.getBairro());
                            endereco.setCidade(dto.getCidade());
                            endereco.setEstado(dto.getEstado());
                            endereco.setEnderecoPrincipal(dto.isEnderecoPrincipal());
                            endereco.setComplemento(dto.getComplemento());
                            endereco.setCliente(cliente);

                            return endereco;
                        })
                        .toList()
        );

        return cliente.getEnderecos();
    }
}
