package jp.tuor.backend.web.pages.home;

import com.giffing.wicket.spring.boot.context.scan.WicketHomePage;
import jp.tuor.backend.model.Cliente;
import jp.tuor.backend.service.ClienteExcelImportService;
import jp.tuor.backend.service.ClienteService;
import jp.tuor.backend.utils.StringUtils;
import jp.tuor.backend.utils.WicketMultipartFile;
import jp.tuor.backend.web.ClienteDataProvider;
import jp.tuor.backend.web.pages.BasePage;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WicketHomePage
public class HomePage extends BasePage {
    @SpringBean
    private ClienteService clienteService;
    @SpringBean
    private ClienteExcelImportService importService;

    private final WebMarkupContainer tableContainer;
    private final WebMarkupContainer emptyMessage;
    private final FeedbackPanel feedbackPanel;
    private final SortableDataProvider<Cliente, String> dataProvider;
    private WebMarkupContainer confirmDeleteModal;
    private final IModel<String> confirmMessageModel;
    private final IModel<Long> clienteIdParaExcluirModel;
    private Label confirmMessage;
    private AjaxLink<Void> confirmButton;

    //--------------------------------------------------------------------------------------------------------
    //métodos de renderização e configuração
    public HomePage() {
        //título simples
        super();
        String str = "Clientes";
        add(new Label("titulo", str));

        feedbackPanel = new FeedbackPanel("feedbackPanel");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);

        //modal delete
        confirmMessageModel = Model.of("");
        clienteIdParaExcluirModel = Model.of((Long) null);
        confirmDeleteModal = new WebMarkupContainer("confirmDeleteModal");
        confirmDeleteModal.setOutputMarkupId(true);
        confirmMessage = new Label("confirmMessage", confirmMessageModel);
        confirmMessage.setOutputMarkupId(true);
        confirmDeleteModal.add(confirmMessage);

        confirmDeleteModal.add(new AjaxLink<Void>("cancelButton") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                target.appendJavaScript("bootstrap.Modal.getInstance(document.getElementById('" + confirmDeleteModal.getMarkupId() + "')).hide();");
            }
        });

        confirmButton = new AjaxLink<Void>("confirmButton") {
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
                //fecha o modal
                target.appendJavaScript("bootstrap.Modal.getInstance(document.getElementById('" + confirmDeleteModal.getMarkupId() + "')).hide();");
                clienteIdParaExcluirModel.setObject(null); //limpa o ID
            }
        };

        confirmDeleteModal.add(confirmButton);
        add(confirmDeleteModal);

        //date provider da tabela
        dataProvider = new ClienteDataProvider(clienteService);

        //container da tabela e paginação
        tableContainer = new WebMarkupContainer("tableContainer");
        tableContainer.setOutputMarkupId(true);
        tableContainer.setOutputMarkupPlaceholderTag(true);
        add(tableContainer);

        //data view para a tabela
        DataView<Cliente> dataView = getClienteDataView(dataProvider);
        tableContainer.add(dataView);

        //paging navigator
        tableContainer.add(new PagingNavigator("pagingNavigator", dataView));

        //label total de registros
        IModel<String> totalMsgModel = new LoadableDetachableModel<String>() {
            @Override
            protected String load() {
                long total = dataProvider.size();
                return "Total de registros: " + total;
            }
        };
        tableContainer.add(new Label("totalRegistros", totalMsgModel));

        //configurações de visibilidade
        long totalItems = dataProvider.size();
        tableContainer.setVisible(totalItems > 0);

        emptyMessage = new WebMarkupContainer("emptyMessage");
        emptyMessage.setOutputMarkupId(true);
        emptyMessage.setOutputMarkupPlaceholderTag(true);
        emptyMessage.setVisible(totalItems == 0);
        add(emptyMessage);
        atualizarVisibilidadeTabela();

        //importação do arquivo .xlsx
        //cria campo de upload
        final FileUploadField fileUploadField = new FileUploadField("fileUpload");
        //cria form
        Form<?> importForm = new Form<>("importForm");
        importForm.setMultiPart(true);
        importForm.add(fileUploadField);

        //botão submit com ajax
        criarBotaoImportSubmitAjax(importForm, fileUploadField);

        add(importForm);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        response.render(CssHeaderItem.forReference(new PackageResourceReference(HomePage.class, "HomePage.css")));
        String modalId = confirmDeleteModal.getMarkupId();
        response.render(OnDomReadyHeaderItem.forScript(
                "new bootstrap.Modal(document.getElementById('" + modalId + "'));"
        ));
    }

    //--------------------------------------------------------------------------------------------------------
    //métodos auxiliares resgatar/tratar dados
    private DataView<Cliente> getClienteDataView(SortableDataProvider<Cliente, String> dataProvider) {
        DataView<Cliente> dataView = new DataView<Cliente>("clienteList", dataProvider) {
            @Override
            protected void populateItem(Item<Cliente> item) {
                Cliente cliente = item.getModelObject();

                //dados do cliente
                item.add(new Label("id", cliente.getId()));
                item.add(new Label("nome", StringUtils.getNomeCliente(cliente)));
                item.add(new Label("email", cliente.getEmail() != null? cliente.getEmail() : "n/a" ));
                item.add(new Label("documento", StringUtils.getDocumentoCliente(cliente)));
                item.add(new Label("data", StringUtils.getDataCliente(cliente)));
                item.add(new Label("endereco", StringUtils.getEnderecoPrincipal(cliente.getEnderecos())));

                //badge do status
                Label statusLabel = new Label("status", cliente.isAtivo() ? "Ativo" : "Inativo");
                statusLabel.add(new AttributeAppender("class", cliente.isAtivo() ? "text-bg-success" : "text-bg-danger"));
                item.add(statusLabel);

                //ações da tabela
                //delete
                IModel<Cliente> clienteIModel = item.getModel();
                AjaxLink<Cliente> deleteLink = criaBotaoDelete(clienteIModel);
                item.add(deleteLink);
            }
        };

        dataView.setItemsPerPage(10);
        return dataView;
    }

    //--------------------------------------------------------------------------------------------------------
    //métodos auxiliares UI
    private void atualizarVisibilidadeTabela() {
        long totalItems = dataProvider.size();
        tableContainer.setVisible(totalItems > 0);
        emptyMessage.setVisible(totalItems == 0);
    }

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
                    if(sucesso) {
                        Integer criados = (Integer) resultado.get("clientesCriados");
                        success("Importação concluída. " + criados + " clientes salvos.");

                        dataProvider.detach();
                        atualizarVisibilidadeTabela();
                        target.add(tableContainer, emptyMessage);
                    } else {
                        List<String> erros = (List<String>) resultado.get("erros");
                        if(erros != null && !erros.isEmpty()) {
                            erros.forEach(this::error);
                        } else {
                            error("Importação falhou.");
                        }
                    }
                } catch (IOException e) {
                    error("Erro ao ler arquivo " + e.getMessage());
                } catch (Exception e) {
                    error("Erro inesperado " + e.getMessage());
                }
                target.add(feedbackPanel);
            }

            protected void onError(AjaxRequestTarget target) {
                //caso o wicket der um erro (ex arquivo muito grande)
                target.add(feedbackPanel);
            }
        });
    }

    private AjaxLink<Cliente> criaBotaoDelete(IModel<Cliente> clienteIModel) {
        AjaxLink<Cliente> deleteLink = new AjaxLink<Cliente>("deleteLink") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                Cliente cliente = clienteIModel.getObject();

                String nome = StringUtils.getNomeCliente(cliente);
                confirmMessageModel.setObject("Tem certeza que deseja excluir o cliente " + nome + "?");
                clienteIdParaExcluirModel.setObject(cliente.getId());

                ajaxRequestTarget.add(confirmMessage);

                ajaxRequestTarget
                        .appendJavaScript("bootstrap.Modal.getInstance(document.getElementById('" + confirmDeleteModal.getMarkupId() + "')).show();");
            }
        };
        return deleteLink;
    }
}
