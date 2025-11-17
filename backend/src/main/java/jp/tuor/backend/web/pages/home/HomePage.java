package jp.tuor.backend.web.pages.home;

import com.giffing.wicket.spring.boot.context.scan.WicketHomePage;
import jp.tuor.backend.model.Cliente;
import jp.tuor.backend.service.ClienteExcelImportService;
import jp.tuor.backend.service.ClienteService;
import jp.tuor.backend.service.ReportService;
import jp.tuor.backend.utils.StringUtils;
import jp.tuor.backend.utils.WicketMultipartFile;
import jp.tuor.backend.web.ClienteDataProvider;
import jp.tuor.backend.web.components.ClienteFormPanel;
import jp.tuor.backend.web.pages.BasePage;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WicketHomePage
public class HomePage extends BasePage {

  //spring
  @SpringBean
  private ClienteService clienteService;
  @SpringBean
  private ClienteExcelImportService importService;
  @SpringBean
  private ReportService reportService;

  //componentes principais da página
  private FeedbackPanel feedbackPanel;
  private WebMarkupContainer tableContainer;
  private WebMarkupContainer emptyMessage;
  private SortableDataProvider<Cliente, String> dataProvider;

  //filtros da tabela
  private IModel<String> nomeFilterModel;
  private IModel<String> cidadeFilterModel;
  private IModel<String> cpfCnpjFilterModel;
  private TextField<String> cpfCnpjFilterField;

  //componentes dos modais
  private ClienteFormPanel clienteFormModal;
  private WebMarkupContainer confirmDeleteModal;
  private IModel<String> confirmMessageModel;
  private IModel<Long> clienteIdParaExcluirModel;
  private Label confirmMessage;

  //recursos para download
  private AbstractResource excelResource;
  private AbstractResource pdfResource;

  public HomePage() {
    super();
    add(new Label("titulo", "Clientes"));

    //chamando inicialização dos componentes
    addFeedbackPanel();
    addFilterFormAndDataProvider();
    addDataTableAndPagination();
    addPageActions();
    addModals();

    atualizarVisibilidadeTabela();
  }

  //--------------------------------------------------------------------------------------------------------
  //métodos de inicialização dos componentes
  //--------------------------------------------------------------------------------------------------------

  //feedBackPanel
  private void addFeedbackPanel() {
    feedbackPanel = new FeedbackPanel("feedbackPanel");
    feedbackPanel.setOutputMarkupId(true);
    add(feedbackPanel);
  }

  //inicializa os IModels dos filtros, o formulário de filtro e o DataProvider
  private void addFilterFormAndDataProvider() {
    nomeFilterModel = Model.of((String) null);
    cidadeFilterModel = Model.of((String) null);
    cpfCnpjFilterModel = Model.of((String) null);

    Form<?> filterForm = new Form<>("filterForm");
    add(filterForm);

    filterForm.add(new TextField<>("nomeFilter", nomeFilterModel));
    filterForm.add(new TextField<>("cidadeFilter", cidadeFilterModel));
    cpfCnpjFilterField = new TextField<>("cpfCnpjFilter", cpfCnpjFilterModel);
    cpfCnpjFilterField.setOutputMarkupId(true);
    filterForm.add(cpfCnpjFilterField);

    //botão de busca
    filterForm.add(new AjaxButton("searchButton") {
      @Override
      protected void onSubmit(AjaxRequestTarget target) {
        dataProvider.detach(); //limpa o cache do provider
        atualizarVisibilidadeTabela();
        target.add(tableContainer, emptyMessage); //redesenha a tabela
      }
    });

    //dataProvider (depende dos modelos de filtro)
    dataProvider = new ClienteDataProvider(
      clienteService,
      nomeFilterModel,
      cpfCnpjFilterModel,
      cidadeFilterModel
    );
  }

  //inicializa o container da tabela, o DataView, a paginação e a mensagem de vazio
  private void addDataTableAndPagination() {
    //container da tabela
    tableContainer = new WebMarkupContainer("tableContainer");
    tableContainer.setOutputMarkupId(true);
    tableContainer.setOutputMarkupPlaceholderTag(true);
    add(tableContainer);

    //dataView
    DataView<Cliente> dataView = getClienteDataView(dataProvider);
    tableContainer.add(dataView);

    //paginação
    //tableContainer.add(new PagingNavigator("pagingNavigator", dataView));
    var ajaxPN = new AjaxPagingNavigator("pagingNavigator", dataView);
    ajaxPN.setOutputMarkupId(true);
    tableContainer.add(ajaxPN);

    //label de total
    IModel<String> totalMsgModel = new LoadableDetachableModel<String>() {
      @Override
      protected String load() {
        long total = dataProvider.size();
        return "Total de registros: " + total;
      }
    };
    tableContainer.add(new Label("totalRegistros", totalMsgModel));

    //mensagem de "Nenhum cliente encontrado"
    emptyMessage = new WebMarkupContainer("emptyMessage");
    emptyMessage.setOutputMarkupId(true);
    emptyMessage.setOutputMarkupPlaceholderTag(true);
    add(emptyMessage);
  }

  //ações exportar, importar, novo
  private void addPageActions() {
    addImportForm();
    addExportButtons();
    addNovoClienteButton();
  }

  //formulário de importação xlsx
  private void addImportForm() {
    final FileUploadField fileUploadField = new FileUploadField("fileUpload");
    Form<?> importForm = new Form<>("importForm");
    importForm.setMultiPart(true);
    importForm.add(fileUploadField);
    criarBotaoImportSubmitAjax(importForm, fileUploadField);
    add(importForm);
  }

  //botões de exportar xlsx e PDF
  private void addExportButtons() {
    //botão Exportar Excel
    excelResource = new AbstractResource() {
      @Override
      protected ResourceResponse newResourceResponse(Attributes attributes) {
        ResourceResponse response = new ResourceResponse();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentDisposition(ContentDisposition.ATTACHMENT);
        response.setFileName("clientes.xlsx");

        response.setWriteCallback(new WriteCallback() {
          @Override
          public void writeData(Attributes attributes) throws IOException {
            try (ByteArrayInputStream bis = clienteService.gerarRelatorioExcel()) {
              bis.transferTo(attributes.getResponse().getOutputStream());
            } catch (Exception e) {
              throw new IOException("Erro ao gerar relatório Excel", e);
            }
          }
        });
        return response;
      }
    };
    add(new ResourceLink<Void>("exportXlsxButton", excelResource));

    //botão Exportar PDF
    pdfResource = new AbstractResource() {
      @Override
      protected ResourceResponse newResourceResponse(Attributes attributes) {
        ResourceResponse response = new ResourceResponse();
        response.setContentType("application/pdf");
        response.setContentDisposition(ContentDisposition.ATTACHMENT);
        response.setFileName("clientes_geral.pdf");

        response.setWriteCallback(new WriteCallback() {
          @Override
          public void writeData(Attributes attributes) throws IOException {
            try {
              byte[] pdfBytes = reportService.gerarRelatorioGeralClientes();
              attributes.getResponse().getOutputStream().write(pdfBytes);
            } catch (Exception e) {
              throw new IOException("Erro ao gerar relatório PDF", e);
            }
          }
        });
        return response;
      }
    };
    add(new ResourceLink<Void>("exportPdfButton", pdfResource));
  }

  //botão novo cliente
  private void addNovoClienteButton() {
    add(new AjaxLink<Void>("novoButton") {
      @Override
      public void onClick(AjaxRequestTarget target) {
        clienteFormModal.openForCreate(target);
      }
    });
  }

  //modais
  private void addModals() {
    addDeleteConfirmModal();
    addClienteFormModal();
  }

  //modal delete
  private void addDeleteConfirmModal() {
    confirmMessageModel = Model.of("");
    clienteIdParaExcluirModel = Model.of((Long) null);
    confirmDeleteModal = new WebMarkupContainer("confirmDeleteModal");
    confirmDeleteModal.setOutputMarkupId(true);

    confirmMessage = new Label("confirmMessage", confirmMessageModel);
    confirmMessage.setOutputMarkupId(true);
    confirmDeleteModal.add(confirmMessage);

    //botão cancelar do modal
    confirmDeleteModal.add(new AjaxLink<Void>("cancelButton") {
      @Override
      public void onClick(AjaxRequestTarget target) {
        target.appendJavaScript("bootstrap.Modal.getInstance(document.getElementById('" + confirmDeleteModal.getMarkupId() + "')).hide();");
      }
    });

    //botão confirmar Exclusão
    AjaxLink<Void> confirmButton = new AjaxLink<Void>("confirmButton") {
      @Override
      public void onClick(AjaxRequestTarget target) {
        Long idParaExcluir = clienteIdParaExcluirModel.getObject();
        if (idParaExcluir != null) {
          try {
            clienteService.deletarCliente(idParaExcluir);
            success("Cliente excluído com sucesso.");
            dataProvider.detach();
            atualizarVisibilidadeTabela();
            target.add(tableContainer, emptyMessage, feedbackPanel);
          } catch (Exception e) {
            error("Erro ao excluir cliente: " + e.getMessage());
            target.add(feedbackPanel);
          }
        }
        target.appendJavaScript("bootstrap.Modal.getInstance(document.getElementById('" + confirmDeleteModal.getMarkupId() + "')).hide();");
        clienteIdParaExcluirModel.setObject(null);
      }
    };
    confirmDeleteModal.add(confirmButton);
    add(confirmDeleteModal);
  }

  //modal formulário do cliente
  private void addClienteFormModal() {
    clienteFormModal = new ClienteFormPanel("clienteFormModal") {
      @Override
      public void onSave(AjaxRequestTarget target) {
        dataProvider.detach();
        atualizarVisibilidadeTabela();
        target.add(tableContainer, emptyMessage, feedbackPanel);
        success("Cliente salvo com sucesso.");
        target.add(feedbackPanel);
      }

      @Override
      public void onCancel(AjaxRequestTarget target) {
      }
    };
    add(clienteFormModal);
  }

  //--------------------------------------------------------------------------------------------------------
  //renderhead configurações css e js
  //--------------------------------------------------------------------------------------------------------

  @Override
  public void renderHead(IHeaderResponse response) {
    super.renderHead(response);

    response.render(CssHeaderItem.forReference(new PackageResourceReference(HomePage.class, "HomePage.css")));

    String modalId = confirmDeleteModal.getMarkupId();
    response.render(OnDomReadyHeaderItem.forScript(
      "new bootstrap.Modal(document.getElementById('" + modalId + "'));"
    ));

    String filterFieldId = cpfCnpjFilterField.getMarkupId();
    String maskScript = String.format(
      "new IMask(document.getElementById('%s'), {" +
        "  mask: [" +
        "    { mask: '000.000.000-00' }," +  //CPF
        "    { mask: '00.000.000/0000-00' }" + //CNPJ
        "  ]" +
        "});",
      filterFieldId
    );

    response.render(OnDomReadyHeaderItem.forScript(maskScript));
  }

  //--------------------------------------------------------------------------------------------------------
  //métodos auxiliares preenchimento de dados e UI
  //--------------------------------------------------------------------------------------------------------

  //dataview
  private DataView<Cliente> getClienteDataView(SortableDataProvider<Cliente, String> dataProvider) {
    DataView<Cliente> dataView = new DataView<Cliente>("clienteList", dataProvider) {
      @Override
      protected void populateItem(Item<Cliente> item) {
        Cliente cliente = item.getModelObject();
        IModel<Cliente> clienteIModel = item.getModel();

        //dados
        item.add(new Label("id", cliente.getId()));
        item.add(new Label("nome", StringUtils.getNomeCliente(cliente)));
        item.add(new Label("email", cliente.getEmail() != null ? cliente.getEmail() : "n/a"));
        item.add(new Label("documento", StringUtils.getDocumentoCliente(cliente)));
        item.add(new Label("data", StringUtils.getDataCliente(cliente)));
        item.add(new Label("endereco", StringUtils.getEnderecoPrincipal(cliente.getEnderecos())));

        //status
        Label statusLabel = new Label("status", cliente.isAtivo() ? "Ativo" : "Inativo");
        statusLabel.add(new AttributeAppender("class", cliente.isAtivo() ? "text-bg-success" : "text-bg-danger"));
        item.add(statusLabel);

        //ações
        item.add(criaBotaoDelete(clienteIModel)); //delete
        item.add(new AjaxLink<Cliente>("editLink", clienteIModel) {//edit
          @Override
          public void onClick(AjaxRequestTarget target) {
            clienteFormModal.openForEdit(getModel(), target);
          }
        });
        item.add(criaBotaoDownloadPdfIndividual(clienteIModel)); //download
      }
    };

    dataView.setItemsPerPage(5);
    return dataView;
  }

  //botão para download de PDF individual
  private ResourceLink<Void> criaBotaoDownloadPdfIndividual(IModel<Cliente> clienteIModel) {
    AbstractResource individualPdfResource = new AbstractResource() {
      @Override
      protected ResourceResponse newResourceResponse(Attributes attributes) {
        Long clienteId = clienteIModel.getObject().getId();
        ResourceResponse response = new ResourceResponse();
        response.setContentType("application/pdf");
        response.setContentDisposition(ContentDisposition.ATTACHMENT);
        response.setFileName("cliente_" + clienteId + ".pdf");

        response.setWriteCallback(new WriteCallback() {
          @Override
          public void writeData(Attributes attributes) throws IOException {
            try {
              byte[] pdfBytes = reportService.gerarRelatorioClienteIndividual(clienteId);
              attributes.getResponse().getOutputStream().write(pdfBytes);
            } catch (Exception e) {
              throw new IOException("Erro ao gerar PDF individual para ID: " + clienteId, e);
            }
          }
        });
        return response;
      }
    };
    return new ResourceLink<>("downloadLink", individualPdfResource);
  }

  //atualiza tabela e mensagem caso n haja clientes
  private void atualizarVisibilidadeTabela() {
    long totalItems = dataProvider.size();
    tableContainer.setVisible(totalItems > 0);
    emptyMessage.setVisible(totalItems == 0);
  }

  //adiciona lógica de ajax ao botão de submit do formulário de importação
  private void criarBotaoImportSubmitAjax(Form<?> importForm, FileUploadField fileUploadField) {
    importForm.add(new AjaxButton("importSubmitButton", importForm) {
      @Override
      protected void onSubmit(AjaxRequestTarget target) {
        final FileUpload fileUpload = fileUploadField.getFileUpload();
        if (fileUpload == null) {
          error("Por favor, selecione um arquivo .xlsx para importar.");
          target.add(feedbackPanel);
          return;
        }

        MultipartFile multipartFile = new WicketMultipartFile(fileUpload);
        try {
          Map<String, Object> resultado = importService.importarClientesExcel(multipartFile);
          Boolean sucesso = (Boolean) resultado.getOrDefault("sucesso", false);
          if (sucesso) {
            Integer criados = (Integer) resultado.get("clientesCriados");
            success("Importação concluída. " + criados + " clientes salvos.");
            dataProvider.detach();
            atualizarVisibilidadeTabela();
            target.add(tableContainer, emptyMessage);
          } else {
            List<String> erros = (List<String>) resultado.get("erros");
            if (erros != null && !erros.isEmpty()) {
              erros.forEach(this::error);
            } else {
              error("Importação falhou.");
            }
          }
        } catch (Exception e) {
          error("Erro inesperado: " + e.getMessage());
        }
        target.add(feedbackPanel);
      }

      @Override
      protected void onError(AjaxRequestTarget target) {
        target.add(feedbackPanel);
      }
    });
  }

  //cria o link de excluir para cada linha da tabela
  private AjaxLink<Cliente> criaBotaoDelete(IModel<Cliente> clienteIModel) {
    return new AjaxLink<Cliente>("deleteLink", clienteIModel) {
      @Override
      public void onClick(AjaxRequestTarget ajaxRequestTarget) {
        Cliente cliente = getModelObject();
        confirmMessageModel.setObject("Tem certeza que deseja excluir o cliente " + StringUtils.getNomeCliente(cliente) + "?");
        clienteIdParaExcluirModel.setObject(cliente.getId());
        ajaxRequestTarget.add(confirmMessage);
        ajaxRequestTarget.appendJavaScript("bootstrap.Modal.getInstance(document.getElementById('" + confirmDeleteModal.getMarkupId() + "')).show();");
      }
    };
  }
}