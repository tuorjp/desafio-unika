package jp.tuor.backend.service;

import jp.tuor.backend.model.Cliente;
import jp.tuor.backend.model.dto.ClienteDTO;
import jp.tuor.backend.model.enums.TipoOperacao;
import jp.tuor.backend.model.enums.TipoPessoa;
import jp.tuor.backend.model.mapper.ClienteMapper;
import jp.tuor.backend.repository.ClienteRepository;
import jp.tuor.backend.repository.dto.ResumoClientesPorCidade;
import jp.tuor.backend.repository.specification.ClienteSpecification;
import jp.tuor.backend.service.exceptions.CPFCNPJDuplicadoException;
import jp.tuor.backend.service.exceptions.CampoInvalidoException;
import jp.tuor.backend.service.exceptions.ClienteNaoEncontradoException;
import jp.tuor.backend.utils.ValidadorUtil;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClienteService {
    private final ClienteRepository clienteRepository;
    private final ValidadorUtil validadorUtil;
    private final ClienteMapper clienteMapper;
    private final ClienteExcelExportService clienteExcelExportService;
    private final ClienteExcelImportService excelImportService;

    public ByteArrayInputStream gerarRelatorioExcel() throws IOException {
        List<Cliente> clientes = this.clienteRepository.findAll();
        return clienteExcelExportService.gerarPlanilhaClientes(clientes);
    }

    public void novoCliente(ClienteDTO clienteDTO) {
        validarClienteDTO(clienteDTO, TipoOperacao.CRIACAO);

        Cliente cliente = new Cliente();
        clienteMapper.preencheClienteObjComClienteDTO(cliente, clienteDTO, TipoOperacao.CRIACAO);
        this.clienteRepository.save(cliente);
    }

    public void editarCliente(ClienteDTO clienteDTO) {
        validarClienteDTO(clienteDTO, TipoOperacao.EDICAO);

        Optional<Cliente> clienteSalvo = this.clienteRepository.findById(clienteDTO.getId());
        if (clienteSalvo.isPresent()) {
            Cliente cliente = clienteSalvo.get();
            clienteMapper.limpaCamposNaoUsados(cliente, clienteDTO);
            clienteMapper.preencheClienteObjComClienteDTO(cliente, clienteDTO, TipoOperacao.EDICAO);
            this.clienteRepository.save(cliente);
        } else {
            throw new ClienteNaoEncontradoException("Cliente não encontrado: " + clienteDTO.getId());
        }
    }

    public Map<String, Object> importarClientesExcel(MultipartFile file) throws IOException {
        return excelImportService.importarClientesExcel(file);
    }

    public void deletarCliente(Long id) {
        Cliente cliente = this.buscaClientePorId(id);

        if (cliente != null) {
            this.clienteRepository.delete(cliente);
        } else {
            throw new ClienteNaoEncontradoException("Cliente com id: " + id + " não encontrado");
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

    public List<Cliente> buscarPorTipoEEstado(TipoPessoa tipoPessoa, String estado) {
        return clienteRepository.findByTipoPessoaAndEstado(tipoPessoa, estado);
    }

    public List<ResumoClientesPorCidade> buscarResumoPorCidade() {
        return clienteRepository.getResumoClientesPorCidade();
    }

    public Page<Cliente> buscarClientesFiltrados(String nome, String cpfCnpj, String cidade, Pageable pageable) {
        Specification<Cliente> spec = ClienteSpecification.comFiltros(nome, cpfCnpj, cidade);
        return clienteRepository.findAll(spec, pageable);
    }

    public Cliente buscaClientePorId(Long id) {
        Optional<Cliente> cliente = this.clienteRepository.findById(id);
        return cliente.orElse(null);
    }

    private void validarClienteDTO(ClienteDTO clienteDTO, TipoOperacao tipoOperacao) {
        if (tipoOperacao.equals(TipoOperacao.EDICAO) && clienteDTO.getId() == null) {
            throw new CampoInvalidoException("O ID do cliente é obrigatório para edição.");
        }

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
