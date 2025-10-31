package jp.tuor.backend.service;

import jp.tuor.backend.model.Cliente;
import jp.tuor.backend.model.Endereco;
import jp.tuor.backend.model.dto.ClienteDTO;
import jp.tuor.backend.model.dto.EditClienteDTO;
import jp.tuor.backend.model.dto.EditEnderecoDTO;
import jp.tuor.backend.model.dto.EnderecoDTO;
import jp.tuor.backend.model.enums.TipoOperacao;
import jp.tuor.backend.model.enums.TipoPessoa;
import jp.tuor.backend.repository.ClienteRepository;
import jp.tuor.backend.service.exceptions.EnderecoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClienteService {
    private final ClienteRepository clienteRepository;
    private final EnderecoService enderecoService;

    public void novoCliente(ClienteDTO clienteDTO) {
        Cliente cliente = new Cliente();
        preencheClienteObjComClienteDTO(cliente, clienteDTO, TipoOperacao.CRIACAO);
        this.clienteRepository.save(cliente);
    }

    public void editarCliente(EditClienteDTO clienteDTO) {
        Optional<Cliente> clienteSalvo = this.clienteRepository.findById(clienteDTO.getId());
        if (clienteSalvo.isPresent()) {
            Cliente cliente = clienteSalvo.get();
            limpaCamposNaoUsados(cliente, clienteDTO);
            preencheClienteObjComClienteDTO(cliente, clienteDTO, TipoOperacao.EDICAO);
            this.clienteRepository.save(cliente);
        }
    }

    public void deletarCliente(Long id) {
        Cliente cliente = this.buscaClientePorId(id);

        if (cliente != null) {
            this.clienteRepository.delete(cliente);
        }
    }

    public void invalidarCliente(Long id) {
        Cliente cliente = this.buscaClientePorId(id);
        if (cliente != null) {
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

    private void preencheClienteObjComClienteDTO(Cliente cliente, ClienteDTO clienteDTO, TipoOperacao tipoOperacao) {
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

        if (tipoOperacao.equals(TipoOperacao.CRIACAO)) {
            cliente.setAtivo(true);
        } else {
            cliente.setAtivo(clienteDTO.isAtivo());
        }

        cliente.setEmail(clienteDTO.getEmail());
        montarEnderecosDoCliente(cliente, clienteDTO.getEnderecos());
    }

    private void limpaCamposNaoUsados(Cliente cliente, ClienteDTO clienteDTO) {
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

    private void montarEnderecosDoCliente(Cliente cliente, List<? extends EnderecoDTO> enderecosDTO) {
        if (enderecosDTO == null) {
            if(cliente.getEnderecos() != null) {
                cliente.getEnderecos().clear();
            } else {
                cliente.setEnderecos(new ArrayList<>());
            }
            return;
        }

        //pega os endereços existentes do cliente caso eles existam, caso seja uma requisição PUT
        List<Endereco> enderecosAtuais = new ArrayList<>();
        List<Endereco> enderecosAtualizados = new ArrayList<>();

        if(cliente.getId() != null) {
            enderecosAtuais = this.enderecoService.getEnderecosByCliente(cliente);
        }

        for(EnderecoDTO dto : enderecosDTO) {
            if(dto instanceof EditEnderecoDTO && ((EditEnderecoDTO) dto).getId() != null) {
                Long id = ((EditEnderecoDTO) dto).getId();
                Endereco endereco = enderecosAtuais
                        .stream()
                        .filter(e -> e.getId().equals(id))
                        .findFirst()
                        .orElse(null);

                if(endereco != null) {
                    preencherEnderecoComDTO(endereco, dto);
                    enderecosAtualizados.add(endereco);
                    enderecosAtuais.remove(endereco);
                } else {
                    throw new EnderecoNaoEncontradoException("Endereço não encontrado.");
                }

            } else {
                Endereco novoEndereco = new Endereco();
                preencherEnderecoComDTO(novoEndereco, dto);
                novoEndereco.setCliente(cliente);

                enderecosAtualizados.add(novoEndereco);
            }
        }

        List<Endereco> listaGerenciada = cliente.getEnderecos();

        if(listaGerenciada == null) {
            listaGerenciada = new ArrayList<>();
        }
        //avisa o hibernate para remover os órfãos
        listaGerenciada.clear();
        listaGerenciada.addAll(enderecosAtualizados);

        cliente.setEnderecos(listaGerenciada);
    }

    private void preencherEnderecoComDTO(Endereco endereco, EnderecoDTO dto) {
        endereco.setLogradouro(dto.getLogradouro());
        endereco.setNumero(dto.getNumero());
        endereco.setCep(dto.getCep());
        endereco.setBairro(dto.getBairro());
        endereco.setCidade(dto.getCidade());
        endereco.setEstado(dto.getEstado());
        endereco.setEnderecoPrincipal(dto.isEnderecoPrincipal());
        endereco.setComplemento(dto.getComplemento());
    }
}
