package jp.tuor.backend.web;

import jp.tuor.backend.model.Cliente;
import jp.tuor.backend.service.ClienteService;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

public class ClienteDataProvider extends SortableDataProvider<Cliente, String> {
  private final ClienteService clienteService;
  private final IModel<String> nomeFilterModel;
  private final IModel<String> cpfCnpjFilterModel;
  private final IModel<String> cidadeFilterModel;

  private Page<Cliente> lastPageCache;
  private String cachedNome;
  private String cachedCpfCnpj;
  private String cachedCidade;

  public ClienteDataProvider(ClienteService clienteService, IModel<String> nomeFilterModel, IModel<String> cpfCnpjFilterModel, IModel<String> cidadeFilterModel) {
    this.clienteService = clienteService;
    this.nomeFilterModel = nomeFilterModel;
    this.cpfCnpjFilterModel = cpfCnpjFilterModel;
    this.cidadeFilterModel = cidadeFilterModel;
  }

  private Page<Cliente> getPage(Pageable pageable) {
    String nome = nomeFilterModel.getObject();
    String cpfCnpj = cpfCnpjFilterModel.getObject();
    String cidade = cidadeFilterModel.getObject();

    if (
      lastPageCache != null &&
        Objects.equals(nome, cachedNome) &&
        Objects.equals(cpfCnpj, cachedCpfCnpj) &&
        Objects.equals(cidade, cachedCidade) &&
        lastPageCache.getPageable().equals(pageable)
    ) {
      return lastPageCache;
    }

    lastPageCache = clienteService.buscarClientesFiltrados(nome, cpfCnpj, cidade, pageable);
    cachedNome = nome;
    cachedCpfCnpj = cpfCnpj;
    cachedCidade = cidade;

    return lastPageCache;
  }

  @Override
  public Iterator<? extends Cliente> iterator(long first, long count) {
    int page = (int) (first / count);
    int size = (int) count;

    Pageable pageable = PageRequest.of(page, size);

    Page<Cliente> resultPage = getPage(pageable);

    return resultPage != null ? resultPage.iterator() : Collections.emptyIterator();
  }

  @Override
  public long size() {
    Pageable pageable = PageRequest.of(0, 10);

    String nome = nomeFilterModel.getObject();
    String cpfCnpj = cpfCnpjFilterModel.getObject();
    String cidade = cidadeFilterModel.getObject();

    if (lastPageCache != null &&
      Objects.equals(nome, cachedNome) &&
      Objects.equals(cpfCnpj, cachedCpfCnpj) &&
      Objects.equals(cidade, cachedCidade)) {
      return lastPageCache.getTotalElements();
    }

    Page<Cliente> resultPage = getPage(pageable);

    return resultPage != null ? resultPage.getTotalElements() : 0;
  }

  @Override
  public IModel<Cliente> model(Cliente cliente) {
    final Long clienteId = cliente.getId();

    return new LoadableDetachableModel<Cliente>() {
      @Override
      protected Cliente load() {
        return clienteService.buscaClientePorId(clienteId);
      }
    };
  }

  @Override
  public void detach() {
    super.detach();
    this.lastPageCache = null;
    this.cachedNome = null;
    this.cachedCpfCnpj = null;
    this.cachedCidade = null;

    nomeFilterModel.detach();
    cpfCnpjFilterModel.detach();
    cidadeFilterModel.detach();
  }
}
