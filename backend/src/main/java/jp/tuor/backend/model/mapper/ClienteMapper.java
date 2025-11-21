package jp.tuor.backend.model.mapper;

import jp.tuor.backend.model.Cliente;
import jp.tuor.backend.model.Endereco;
import jp.tuor.backend.model.dto.ClienteDTO;
import jp.tuor.backend.model.dto.EnderecoDTO;
import jp.tuor.backend.model.enums.TipoOperacao;
import jp.tuor.backend.model.enums.TipoPessoa;
import jp.tuor.backend.service.exceptions.EnderecoNaoEncontradoException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class ClienteMapper {
  public void preencheClienteObjComClienteDTO(Cliente cliente, ClienteDTO clienteDTO, TipoOperacao tipoOperacao) {
    cliente.setTipoPessoa(clienteDTO.getTipoPessoa());
    if (clienteDTO.getTipoPessoa().equals(TipoPessoa.FISICA)) {
      cliente.setCpf(clienteDTO.getCpf().trim());
      cliente.setNome(clienteDTO.getNome().trim());
      cliente.setRg(clienteDTO.getRg().trim());
      cliente.setDataNascimento(clienteDTO.getDataNascimento());
    } else {
      cliente.setCnpj(clienteDTO.getCnpj().trim());
      cliente.setRazaoSocial(clienteDTO.getRazaoSocial().trim());
      cliente.setInscricaoEstadual(clienteDTO.getInscricaoEstadual().trim());
      cliente.setDataCriacao(clienteDTO.getDataCriacao());
    }

    if (tipoOperacao.equals(TipoOperacao.CRIACAO)) {
      cliente.setAtivo(true);
    } else {
      cliente.setAtivo(clienteDTO.isAtivo());
    }

    cliente.setEmail(clienteDTO.getEmail().trim());
    montarEnderecosDoCliente(cliente, clienteDTO.getEnderecos());
  }

  public void limpaCamposNaoUsados(Cliente cliente, ClienteDTO clienteDTO) {
    if (clienteDTO.getTipoPessoa().equals(TipoPessoa.FISICA)) {
      cliente.setCnpj(null);
      cliente.setRazaoSocial(null);
      cliente.setInscricaoEstadual(null);
      cliente.setDataCriacao(null);
    } else {
      cliente.setCpf(null);
      cliente.setNome(null);
      cliente.setRg(null);
      cliente.setDataNascimento(null);
    }
  }

  public void montarEnderecosDoCliente(Cliente cliente, List<EnderecoDTO> enderecosDTO) {
    if (enderecosDTO == null) {
      return;
    }

    if (cliente.getEnderecos() == null) {
      cliente.setEnderecos(new ArrayList<>());
    }

    List<Endereco> enderecosAtuais = cliente.getEnderecos();

    for (EnderecoDTO dto : enderecosDTO) {
      if (dto.getId() != null) {
        Endereco existente = enderecosAtuais
          .stream()
          .filter(e -> e.getId().equals(dto.getId()))
          .findFirst()
          .orElseThrow(() -> new EnderecoNaoEncontradoException("Endereço não encontrado: " + dto.getId()));

        preencherEnderecoComDTO(existente, dto);
      } else {
        Endereco novoEndereco = new Endereco();
        preencherEnderecoComDTO(novoEndereco, dto);
        novoEndereco.setCliente(cliente);
        enderecosAtuais.add(novoEndereco);
      }
    }

    enderecosAtuais.removeIf(
      e -> e.getId() != null &&
        enderecosDTO.stream()
          .map(EnderecoDTO::getId)
          .filter(Objects::nonNull)
          .noneMatch(id -> id.equals(e.getId()))
    );
  }

  public void preencherEnderecoComDTO(Endereco endereco, EnderecoDTO dto) {
    endereco.setLogradouro(dto.getLogradouro().trim());
    endereco.setNumero(dto.getNumero().trim());
    endereco.setCep(dto.getCep().trim());
    endereco.setBairro(dto.getBairro().trim());
    endereco.setCidade(dto.getCidade().trim());
    endereco.setEstado(dto.getEstado().trim());
    endereco.setEnderecoPrincipal(dto.isEnderecoPrincipal());
    endereco.setComplemento(dto.getComplemento());
  }

  public ClienteDTO clienteParaDTO(Cliente cliente) {
    if (cliente == null) {
      return null;
    }

    ClienteDTO dto = new ClienteDTO();

    dto.setId(cliente.getId());
    dto.setTipoPessoa(cliente.getTipoPessoa());
    dto.setEmail(cliente.getEmail());
    dto.setAtivo(cliente.isAtivo());

    dto.setCpf(cliente.getCpf());
    dto.setNome(cliente.getNome());
    dto.setRg(cliente.getRg());
    dto.setDataNascimento(cliente.getDataNascimento());

    dto.setCnpj(cliente.getCnpj());
    dto.setRazaoSocial(cliente.getRazaoSocial());
    dto.setInscricaoEstadual(cliente.getInscricaoEstadual());
    dto.setDataCriacao(cliente.getDataCriacao());

    if (cliente.getEnderecos() != null && !cliente.getEnderecos().isEmpty()) {
      dto.setEnderecos(
        cliente.getEnderecos().stream()
          .map(this::enderecoParaDTO)
          .collect(Collectors.toList())
      );
    } else {
      dto.setEnderecos(new ArrayList<>());
    }

    return dto;
  }

  public EnderecoDTO enderecoParaDTO(Endereco endereco) {
    if (endereco == null) {
      return null;
    }

    EnderecoDTO dto = new EnderecoDTO();
    dto.setId(endereco.getId());
    dto.setLogradouro(endereco.getLogradouro());
    dto.setNumero(endereco.getNumero());
    dto.setCep(endereco.getCep());
    dto.setBairro(endereco.getBairro());
    dto.setCidade(endereco.getCidade());
    dto.setEstado(endereco.getEstado());
    dto.setEnderecoPrincipal(endereco.isEnderecoPrincipal());
    dto.setComplemento(endereco.getComplemento());
    return dto;
  }
}
