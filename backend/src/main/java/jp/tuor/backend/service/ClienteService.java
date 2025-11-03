package jp.tuor.backend.service;

import jp.tuor.backend.model.Cliente;
import jp.tuor.backend.model.Endereco;
import jp.tuor.backend.model.dto.ClienteDTO;
import jp.tuor.backend.model.dto.EnderecoDTO;
import jp.tuor.backend.model.enums.TipoOperacao;
import jp.tuor.backend.model.enums.TipoPessoa;
import jp.tuor.backend.repository.ClienteRepository;
import jp.tuor.backend.service.exceptions.CPFCNPJDuplicadoException;
import jp.tuor.backend.service.exceptions.EnderecoNaoEncontradoException;
import jp.tuor.backend.utils.ValidadorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClienteService {
    private final ClienteRepository clienteRepository;
    private final ValidadorUtil validadorUtil;

    public void novoCliente(ClienteDTO clienteDTO) {
        validarClienteDTO(clienteDTO, TipoOperacao.CRIACAO);

        Cliente cliente = new Cliente();
        preencheClienteObjComClienteDTO(cliente, clienteDTO, TipoOperacao.CRIACAO);
        this.clienteRepository.save(cliente);
    }

    public void editarCliente(ClienteDTO clienteDTO) {
        validarClienteDTO(clienteDTO, TipoOperacao.EDICAO);

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

    private void montarEnderecosDoCliente(Cliente cliente, List<EnderecoDTO> enderecosDTO) {
        //cliente criado sem nenhum endereço
        if (enderecosDTO == null) {
            return;
        }

        //criando lista vazia caso não haja endereço cadastrado
        if(cliente.getEnderecos() == null) {
            cliente.setEnderecos(new ArrayList<>());
        }

        //pega os endereços existentes do cliente caso eles existam, caso seja uma requisição PUT
        List<Endereco> enderecosAtuais = cliente.getEnderecos();

        //atualizando ou cadastrando novos endereços na lista de endereços do cliente
        for(EnderecoDTO dto: enderecosDTO) {
            if(dto.getId() != null) {
                Endereco existente = enderecosAtuais
                  .stream()
                  .filter(e -> e.getId().equals(dto.getId()))
                  .findFirst()
                  .orElseThrow(() -> new EnderecoNaoEncontradoException("Endereço não encontrado: " + dto.getId()));

                preencherEnderecoComDTO(existente, dto);
            } else {
                //adiciona novo endereço
                Endereco novoEndereco = new Endereco();
                preencherEnderecoComDTO(novoEndereco, dto);
                novoEndereco.setCliente(cliente);
                enderecosAtuais.add(novoEndereco);
            }
        }

        //removendo endereços que não vieram do front
        enderecosAtuais.removeIf(
          e -> e.getId() != null &&
            enderecosDTO.stream()
              .map(EnderecoDTO::getId)
              .filter(Objects::nonNull)
              .noneMatch(id -> id.equals(e.getId()))
        );
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

    private void validarClienteDTO(ClienteDTO clienteDTO, TipoOperacao tipoOperacao) {
        validadorUtil.validarCamposObrigatorios(clienteDTO);

        Optional<Cliente> clienteBusca;

        if (tipoOperacao.equals(TipoOperacao.CRIACAO)) {
            if (clienteDTO.getId() != null) clienteDTO.setId(null);

            if (clienteDTO.getTipoPessoa().equals(TipoPessoa.FISICA)) {
                clienteBusca = clienteRepository.findByCpf(clienteDTO.getCpf());
                if (clienteBusca.isPresent())
                    throw new CPFCNPJDuplicadoException("Cliente com CPF " + clienteDTO.getCpf() + " já cadastrado.");
            } else {
                clienteBusca = clienteRepository.findByCnpj(clienteDTO.getCnpj());
                if (clienteBusca.isPresent())
                    throw new CPFCNPJDuplicadoException("Cliente com CNPJ " + clienteDTO.getCnpj() + " já cadastrado.");
            }
        } else if (tipoOperacao.equals(TipoOperacao.EDICAO)) {
            if (clienteDTO.getTipoPessoa().equals(TipoPessoa.FISICA)) {
                clienteBusca = clienteRepository.findByCpf(clienteDTO.getCpf());
                if (clienteBusca.isPresent() && !clienteBusca.get().getId().equals(clienteDTO.getId())) {
                    throw new CPFCNPJDuplicadoException("CPF " + clienteDTO.getCpf() + " já pertence a outro cliente.");
                }
            } else {
                clienteBusca = clienteRepository.findByCnpj(clienteDTO.getCnpj());
                if (clienteBusca.isPresent() && !clienteBusca.get().getId().equals(clienteDTO.getId())) {
                    throw new CPFCNPJDuplicadoException("CNPJ " + clienteDTO.getCnpj() + " já pertence a outro cliente.");
                }
            }
        }
    }
}
