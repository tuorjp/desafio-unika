package jp.tuor.backend.service;

import jp.tuor.backend.model.Cliente;
import jp.tuor.backend.model.Endereco;
import jp.tuor.backend.model.dto.ClienteDTO;
import jp.tuor.backend.model.dto.EnderecoDTO;
import jp.tuor.backend.model.enums.TipoOperacao;
import jp.tuor.backend.model.enums.TipoPessoa;
import jp.tuor.backend.model.mapper.ClienteMapper;
import jp.tuor.backend.repository.ClienteRepository;
import jp.tuor.backend.service.exceptions.CPFCNPJDuplicadoException;
import jp.tuor.backend.service.exceptions.CampoInvalidoException;
import jp.tuor.backend.service.exceptions.ClienteNaoEncontradoException;
import jp.tuor.backend.utils.ValidadorUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ClienteServiceTest {
  //@Mock cria simulações dos repositories, services, etc

  @Mock
  private ClienteRepository clienteRepository;

  @Mock
  private ValidadorUtil validadorUtil;

  @Mock
  private ClienteMapper clienteMapper;

  @Mock
  private ClienteExcelExportService clienteExcelExportService;

  @Mock
  private ClienteExcelImportService excelImportService;

  @InjectMocks
  private ClienteService clienteService;

  private ClienteDTO clienteDTO_PF;

  private Cliente cliente_PF;

  @BeforeEach
    //antes de cada @Test
  void setUp() {
    //endereço DTO
    EnderecoDTO enderecoDTO = new EnderecoDTO();
    enderecoDTO.setLogradouro("Rua Teste");
    enderecoDTO.setNumero("123");
    enderecoDTO.setCep("12345-678");
    enderecoDTO.setBairro("Bairro");
    enderecoDTO.setCidade("Cidade");
    enderecoDTO.setEstado("TS");
    enderecoDTO.setEnderecoPrincipal(true);

    //pessoa física DTO
    clienteDTO_PF = new ClienteDTO();
    clienteDTO_PF.setTipoPessoa(TipoPessoa.FISICA);
    clienteDTO_PF.setNome("Cliente Teste PF");
    clienteDTO_PF.setCpf("123.456.789-00");
    clienteDTO_PF.setEmail("teste@email.com");
    clienteDTO_PF.setEnderecos(List.of(enderecoDTO));

    //pessoa física entidade
    cliente_PF = new Cliente();
    cliente_PF.setId(1L);
    cliente_PF.setTipoPessoa(TipoPessoa.FISICA);
    cliente_PF.setNome("Cliente Teste PF");
    cliente_PF.setCpf("123.456.789-00");
    cliente_PF.setAtivo(true);
    cliente_PF.setEnderecos(new ArrayList<>());
  }

  @Test //teste de novo cliente
  @DisplayName("Deve criar novo cliente com sucesso")
  void novoCliente_DeveCriarComSucesso() {
    //simulação de validação
    Mockito.doNothing().when(validadorUtil).validarCamposObrigatorios(Mockito.any(ClienteDTO.class));

    //simula o repositório, não deve achar nada ao pesquisar por CPF
    Mockito.when(clienteRepository.findByCpf(clienteDTO_PF.getCpf())).thenReturn(Optional.empty());

    // Simula o mapper, já que ele é chamado antes do save
    Mockito.doNothing().when(clienteMapper)
      .preencheClienteObjComClienteDTO(Mockito.any(Cliente.class), Mockito.eq(clienteDTO_PF), Mockito.eq(TipoOperacao.CRIACAO));

    //simula o repositório, quando salvar retorna o objeto Cliente
    Mockito.when(clienteRepository.save(Mockito.any(Cliente.class))).thenReturn(new Cliente());

    clienteService.novoCliente(clienteDTO_PF);

    //verifica se o validador foi chamado 1 vez
    Mockito.verify(validadorUtil, Mockito.times(1)).validarCamposObrigatorios(clienteDTO_PF);

    //verifica se o repositório foi chamado para checar duplicidade
    Mockito.verify(clienteRepository, Mockito.times(1)).findByCpf(clienteDTO_PF.getCpf());

    //verifica se o mapper foi chamado
    Mockito.verify(clienteMapper, Mockito.times(1))
      .preencheClienteObjComClienteDTO(Mockito.any(Cliente.class), Mockito.eq(clienteDTO_PF), Mockito.eq(TipoOperacao.CRIACAO));

    //verifica se o repositório foi chamado para salvar
    Mockito.verify(clienteRepository, Mockito.times(1)).save(Mockito.any(Cliente.class));
  }

  @Test
  @DisplayName("Deve falhar ao criar cliente se validador falhar")
  void novoCliente_DeveLancarExcecaoSeValidadorFalhar() {
    //simula o validador: quando chamado, lança uma exceção
    Mockito.doThrow(new CampoInvalidoException("Erro de validação"))
      .when(validadorUtil).validarCamposObrigatorios(clienteDTO_PF);

    //verifica se a exceção correta é lançada
    Assertions.assertThrows(CampoInvalidoException.class, () -> {
      clienteService.novoCliente(clienteDTO_PF);
    });

    //garante que o método save não é chamado se a validação falhou
    Mockito.verify(clienteRepository, Mockito.never()).save(Mockito.any());
    Mockito.verify(clienteMapper, Mockito.never()).preencheClienteObjComClienteDTO(Mockito.any(), Mockito.any(), Mockito.any());
  }

  @Test
  @DisplayName("Deve falhar ao criar cliente se CPF já existir")
  void novoCliente_DeveLancarExcecaoSeCPFDuplicado() {
    //validador passa
    Mockito.doNothing().when(validadorUtil).validarCamposObrigatorios(clienteDTO_PF);

    //repositório ACHA um cliente com o mesmo CPF
    Mockito.when(clienteRepository.findByCpf(clienteDTO_PF.getCpf()))
      .thenReturn(Optional.of(new Cliente()));

    Assertions.assertThrows(CPFCNPJDuplicadoException.class, () -> {
      clienteService.novoCliente(clienteDTO_PF);
    });

    Mockito.verify(clienteRepository, Mockito.never()).save(Mockito.any());
    Mockito.verify(clienteMapper, Mockito.never()).preencheClienteObjComClienteDTO(Mockito.any(), Mockito.any(), Mockito.any());
  }

  //testes para editarCliente
  @Test
  @DisplayName("Deve editar um cliente com sucesso")
  void editarCliente_DeveEditarComSucesso() {
    clienteDTO_PF.setId(1L);
    clienteDTO_PF.setNome("Nome Alterado");

    //validador passa
    Mockito.doNothing().when(validadorUtil).validarCamposObrigatorios(clienteDTO_PF);

    //checagem de duplicidade na edição (não acha outro)
    Mockito.when(clienteRepository.findByCpf(clienteDTO_PF.getCpf())).thenReturn(Optional.empty());

    //repositório acha o cliente original para editar
    Mockito.when(clienteRepository.findById(clienteDTO_PF.getId())).thenReturn(Optional.of(cliente_PF));

    // Simula os mappers que são chamados na edição
    Mockito.doNothing().when(clienteMapper).limpaCamposNaoUsados(Mockito.any(Cliente.class), Mockito.eq(clienteDTO_PF));
    Mockito.doNothing().when(clienteMapper)
      .preencheClienteObjComClienteDTO(Mockito.any(Cliente.class), Mockito.eq(clienteDTO_PF), Mockito.eq(TipoOperacao.EDICAO));


    //capturador para verificar o objeto que foi salvo
    ArgumentCaptor<Cliente> clienteCaptor = ArgumentCaptor.forClass(Cliente.class);

    //Act
    clienteService.editarCliente(clienteDTO_PF);

    //Assert
    //verifica se o save foi chamado
    Mockito.verify(clienteRepository, Mockito.times(1)).save(clienteCaptor.capture());

    //pega o cliente que foi passado para o método save
    Cliente clienteSalvo = clienteCaptor.getValue();

    Mockito.verify(clienteMapper, Mockito.times(1)).limpaCamposNaoUsados(cliente_PF, clienteDTO_PF);
    Mockito.verify(clienteMapper, Mockito.times(1))
      .preencheClienteObjComClienteDTO(cliente_PF, clienteDTO_PF, TipoOperacao.EDICAO);
  }

  @Test
  @DisplayName("Deve falhar ao editar se cliente não for encontrado")
  void editarCliente_DeveLancarExcecaoSeClienteNaoEncontrado() {
    clienteDTO_PF.setId(99L); //id que não existe

    //validador passa
    Mockito.doNothing().when(validadorUtil).validarCamposObrigatorios(clienteDTO_PF);

    //checagem de duplicidade (não acha outro)
    Mockito.when(clienteRepository.findByCpf(clienteDTO_PF.getCpf())).thenReturn(Optional.empty());

    //repositório não acha o cliente
    Mockito.when(clienteRepository.findById(clienteDTO_PF.getId())).thenReturn(Optional.empty());

    //Act e assert
    Assertions.assertThrows(ClienteNaoEncontradoException.class, () -> {
      clienteService.editarCliente(clienteDTO_PF);
    });

    Mockito.verify(clienteRepository, Mockito.never()).save(Mockito.any());
    Mockito.verify(clienteMapper, Mockito.never()).limpaCamposNaoUsados(Mockito.any(), Mockito.any());
  }

  @Test
  @DisplayName("Deve remover endereço que não veio no DTO durante a edição")
  void editarCliente_DeveRemoverEnderecoAusente() {
    clienteDTO_PF.setId(1L);

    //adiciona um endereço antigo no banco
    Endereco enderecoAntigo = new Endereco();
    enderecoAntigo.setId(10L); // ID importante
    enderecoAntigo.setLogradouro("Endereco Antigo");
    enderecoAntigo.setCliente(cliente_PF);
    cliente_PF.getEnderecos().add(enderecoAntigo);

    //DTO vem sem nenhum endereço
    clienteDTO_PF.setEnderecos(new ArrayList<>());

    //validador passa
    Mockito.doNothing().when(validadorUtil).validarCamposObrigatorios(clienteDTO_PF);
    Mockito.when(clienteRepository.findByCpf(Mockito.any())).thenReturn(Optional.empty());
    Mockito.when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente_PF));

    // Simula os mappers
    Mockito.doNothing().when(clienteMapper).limpaCamposNaoUsados(Mockito.any(Cliente.class), Mockito.any(ClienteDTO.class));
    Mockito.doNothing().when(clienteMapper)
      .preencheClienteObjComClienteDTO(Mockito.any(Cliente.class), Mockito.any(ClienteDTO.class), Mockito.eq(TipoOperacao.EDICAO));

    ArgumentCaptor<Cliente> clienteCaptor = ArgumentCaptor.forClass(Cliente.class);

    //Act
    clienteService.editarCliente(clienteDTO_PF);

    //Assert
    Mockito.verify(clienteRepository).save(clienteCaptor.capture());
    Mockito.verify(clienteMapper, Mockito.times(1)).limpaCamposNaoUsados(cliente_PF, clienteDTO_PF);
  }

  //testes para buscaClientePorId
  @Test
  @DisplayName("Deve buscar cliente por ID e retornar nulo se não encontrar")
  void buscaClientePorId_DeveRetornarNuloSeNaoEncontrar() {
    Mockito.when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

    Cliente resultado = clienteService.buscaClientePorId(99L);

    Assertions.assertNull(resultado);
  }

  @Test
  @DisplayName("Deve buscar cliente por ID e retornar o cliente se encontrar")
  void buscaClientePorId_DeveRetornarClienteSeEncontrar() {
    Mockito.when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente_PF));

    Cliente resultado = clienteService.buscaClientePorId(1L);

    Assertions.assertNotNull(resultado);
    Assertions.assertEquals(cliente_PF.getId(), resultado.getId());
  }

  //testes para invalidarCliente
  @Test
  @DisplayName("Deve invalidar um cliente (setar ativo=false)")
  void invalidarCliente_DeveSetarAtivoFalse() {
    Assertions.assertTrue(cliente_PF.isAtivo());

    Mockito.when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente_PF));

    ArgumentCaptor<Cliente> clienteCaptor = ArgumentCaptor.forClass(Cliente.class);

    clienteService.invalidarCliente(1L);

    Mockito.verify(clienteRepository, Mockito.times(1)).save(clienteCaptor.capture());

    Cliente clienteSalvo = clienteCaptor.getValue();
    Assertions.assertFalse(clienteSalvo.isAtivo());
  }

  @Test
  @DisplayName("Não deve fazer nada ao invalidar cliente que não existe")
  void invalidarCliente_NaoDeveFazerNadaSeNaoEncontrar() {
    Mockito.when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

    clienteService.invalidarCliente(99L);

    Mockito.verify(clienteRepository, Mockito.never()).save(Mockito.any());
  }
}