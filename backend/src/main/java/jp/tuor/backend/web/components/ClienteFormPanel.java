package jp.tuor.backend.web.components;

import jp.tuor.backend.model.Cliente;
import jp.tuor.backend.model.dto.ClienteDTO;
import jp.tuor.backend.model.dto.EnderecoDTO;
import jp.tuor.backend.model.dto.ViaCepResponse;
import jp.tuor.backend.model.enums.TipoPessoa;
import jp.tuor.backend.model.mapper.ClienteMapper;
import jp.tuor.backend.service.ClienteService;
import jp.tuor.backend.utils.StringUtils;
import jp.tuor.backend.web.WicketApp;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.DateValidator;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public abstract class ClienteFormPanel extends Panel {

  //soring
  @SpringBean
  private ClienteService clienteService;
  @SpringBean
  private ClienteMapper clienteMapper;

  //componentes wicket
  private Form<ClienteDTO> form;
  private WebMarkupContainer modal;
  private Label modalTitle;
  private WebMarkupContainer containerPF;
  private WebMarkupContainer containerPJ;
  private WebMarkupContainer enderecosContainer;
  private FeedbackPanel feedbackPanel;

  //estado do componente
  private IModel<String> titleModel;
  private boolean isEditMode;

  public ClienteFormPanel(String id) {
    super(id);

    addModalContainer();
    addFormAndFeedback();
    addClienteFields();
    addPessoaFisicaJuridicaFields();
    addEnderecosSection();
    addFormActions();
  }

  //--------------------------------------------------------------------------------------------------------
  //inicialização componentes
  //--------------------------------------------------------------------------------------------------------

  //container do modal
  private void addModalContainer() {
    modal = new WebMarkupContainer("clienteModal");
    modal.setOutputMarkupId(true);
    add(modal);
  }

  //formulário principal e feedBackPanel
  private void addFormAndFeedback() {
    titleModel = Model.of("Novo Cliente");
    modalTitle = new Label("modalTitle", titleModel);

    CompoundPropertyModel<ClienteDTO> formModel = new CompoundPropertyModel<>(new ClienteDTO());
    form = new Form<>("clienteForm", formModel);
    form.setOutputMarkupId(true);
    form.add(modalTitle);

    feedbackPanel = new FeedbackPanel("formFeedback");
    feedbackPanel.setOutputMarkupId(true);
    feedbackPanel.add(new AttributeAppender("class", new Model<String>() {
      @Override
      public String getObject() {
        return feedbackPanel.anyMessage() ? "" : "d-none";
      }
    }, " "));
    form.add(feedbackPanel);
    modal.add(form);
  }

  //campos comuns do cliente tipoPessoa, email, ativo
  private void addClienteFields() {
    DropDownChoice<TipoPessoa> tipoPessoaField = new DropDownChoice<>("tipoPessoa", Arrays.asList(TipoPessoa.values()));
    tipoPessoaField.setRequired(true);
    tipoPessoaField.add(new AjaxFormComponentUpdatingBehavior("change") {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        target.add(containerPF, containerPJ);
        //replica as máscaras nos campos que ficaram visíveis
        String initMasksScript = String.format(
          "document.querySelectorAll('#%s [data-mask]').forEach(el => {" +
            "  if (!el.imask) { new IMask(el, { mask: el.dataset.mask, lazy: false }); }" +
            "});",
          modal.getMarkupId()
        );
        target.appendJavaScript(initMasksScript);
      }
    });
    form.add(tipoPessoaField);
    form.add(new EmailTextField("email"));
    form.add(new CheckBox("ativo"));
  }

  //validadores de data, containers de PJ e PF
  private void addPessoaFisicaJuridicaFields() {
    //configuração de datas
    ZoneId fusoBr = Session.get().getMetaData(WicketApp.ZONE_ID_KEY);
    if (fusoBr == null) {
      fusoBr = ZoneId.of("America/Sao_Paulo");
    }
    LocalDate dataMinima = LocalDate.of(1900, 1, 1);
    LocalDate dataMaxima = LocalDate.now(fusoBr);
    Date minDate = Date.from(dataMinima.atStartOfDay(fusoBr).toInstant());
    Date maxDate = Date.from(dataMaxima.atStartOfDay(fusoBr).toInstant());

    //container PF
    containerPF = new WebMarkupContainer("containerPF") {
      @Override
      protected void onConfigure() {
        super.onConfigure();
        setEnabled(TipoPessoa.FISICA.equals(form.getModelObject().getTipoPessoa()));
      }
    };
    containerPF.setOutputMarkupId(true);
    containerPF.add(new AttributeAppender("class", new Model<String>() {
      @Override
      public String getObject() {
        return TipoPessoa.FISICA.equals(form.getModelObject().getTipoPessoa()) ? "" : "d-none";
      }
    }, " "));

    containerPF.add(new TextField<>("cpf"));
    containerPF.add(new TextField<>("nome"));
    containerPF.add(new TextField<>("rg"));
    DateTextField dataNascimentoField = new DateTextField("dataNascimento", "yyyy-MM-dd");
    //dataNascimentoField.add(DateValidator.range(minDate, maxDate));
    dataNascimentoField.add(new AttributeAppender("min", dataMinima.toString()));
    dataNascimentoField.add(new AttributeAppender("max", dataMaxima.toString()));
    containerPF.add(dataNascimentoField);
    form.add(containerPF);

    //container PJ
    containerPJ = new WebMarkupContainer("containerPJ") {
      @Override
      protected void onConfigure() {
        super.onConfigure();
        setEnabled(TipoPessoa.JURIDICA.equals(form.getModelObject().getTipoPessoa()));
      }
    };
    containerPJ.setOutputMarkupId(true);
    containerPJ.add(new AttributeAppender("class", new Model<String>() {
      @Override
      public String getObject() {
        return TipoPessoa.JURIDICA.equals(form.getModelObject().getTipoPessoa()) ? "" : "d-none";
      }
    }, " "));

    containerPJ.add(new TextField<>("cnpj"));
    containerPJ.add(new TextField<>("razaoSocial"));
    containerPJ.add(new TextField<>("inscricaoEstadual"));
    DateTextField dataCriacaoField = new DateTextField("dataCriacao", "yyyy-MM-dd");
    //dataCriacaoField.add(DateValidator.range(minDate, maxDate));
    dataCriacaoField.add(new AttributeAppender("min", dataMinima.toString()));
    dataCriacaoField.add(new AttributeAppender("max", dataMaxima.toString()));
    containerPJ.add(dataCriacaoField);
    form.add(containerPJ);
  }

  //endereços, ListView, botão adicionar endereço
  private void addEnderecosSection() {
    enderecosContainer = new WebMarkupContainer("enderecosContainer");
    enderecosContainer.setOutputMarkupId(true);
    form.add(enderecosContainer);

    //ListView para os endereços
    ListView<EnderecoDTO> enderecosView = new ListView<EnderecoDTO>("enderecos") {
      @Override
      protected void populateItem(ListItem<EnderecoDTO> item) {
        //popula os campos e botões de cada endereço
        populateEnderecoItem(item);
      }
    };
    enderecosView.setReuseItems(false);
    enderecosContainer.add(enderecosView);

    //botão adicionar endereço
    AjaxLink<Void> addEnderecoLink = new AjaxLink<Void>("addEndereco") {
      @Override
      public void onClick(AjaxRequestTarget target) {
        form.getModelObject().getEnderecos().add(new EnderecoDTO());
        target.add(enderecosContainer);

        //reaplica máscaras no novo item
        String initMasksScript = String.format(
          "document.querySelectorAll('#%s [data-mask]').forEach(el => {" +
            "  if (!el.imask) { new IMask(el, { mask: el.dataset.mask, lazy: false }); }" +
            "});",
          enderecosContainer.getMarkupId()
        );
        target.appendJavaScript(initMasksScript);
      }
    };
    form.add(addEnderecoLink);
  }

  //método auxiliar que popula os items para cada endereço da ListView
  private void populateEnderecoItem(ListItem<EnderecoDTO> item) {
    item.setDefaultModel(new CompoundPropertyModel<>(item.getModel()));
    item.setOutputMarkupId(true);

    //campos
    final TextField<String> cepField = new TextField<>("cep");
    item.add(cepField);

    final TextField<String> logradouroField = new TextField<>("logradouro");
    logradouroField.setOutputMarkupId(true);
    logradouroField.add(new AttributeAppender("readonly", new LoadableDetachableModel<Object>() {
      @Override
      protected String load() {
        String val = logradouroField.getModelObject();
        return (val != null && !val.isEmpty()) ? "readonly" : null;
      }
    }));
    item.add(logradouroField);

    final TextField<String> numeroField = new TextField<>("numero");
    numeroField.setOutputMarkupId(true);
    item.add(numeroField);

    final TextField<String> complementoField = new TextField<>("complemento");
    complementoField.setOutputMarkupId(true);
    item.add(complementoField);

    final TextField<String> bairroField = new TextField<>("bairro");
    bairroField.setOutputMarkupId(true);
    bairroField.add(new AttributeAppender("readonly", new LoadableDetachableModel<String>() {
      @Override
      protected String load() {
        String val = bairroField.getModelObject();
        return (val != null && !val.isEmpty()) ? "readonly" : null;
      }
    }));
    item.add(bairroField);

    final TextField<String> cidadeField = new TextField<>("cidade");
    cidadeField.setOutputMarkupId(true);
    cidadeField.add(new AttributeAppender("readonly", new LoadableDetachableModel<String>() {
      @Override
      protected String load() {
        String val = cidadeField.getModelObject();
        return (val != null && !val.isEmpty()) ? "readonly" : null;
      }
    }));
    item.add(cidadeField);

    final TextField<String> estadoField = new TextField<>("estado");
    estadoField.setOutputMarkupId(true);
    estadoField.add(new AttributeAppender("readonly", new LoadableDetachableModel<String>() {
      @Override
      protected String load() {
        String val = estadoField.getModelObject();
        return (val != null && !val.isEmpty()) ? "readonly" : null;
      }
    }));
    item.add(estadoField);

    item.add(new CheckBox("enderecoPrincipal"));

    //botão buscar cep
    AjaxButton buscarCepButton = criarBuscarCepButton(item, cepField);
    item.add(buscarCepButton);

    //botão remover
    AjaxLink<Void> removeLink = new AjaxLink<Void>("removeEndereco") {
      @Override
      public void onClick(AjaxRequestTarget target) {
        EnderecoDTO dtoToRemove = item.getModelObject();
        form.getModelObject().getEnderecos().remove(dtoToRemove);
        target.add(enderecosContainer);
      }
    };
    item.add(removeLink);
  }

  //método auxiliar que cria um botão de busca de cep para cada endereço da ListView
  private AjaxButton criarBuscarCepButton(ListItem<EnderecoDTO> item, TextField<String> cepField) {
    AjaxButton buscarCepButton = new AjaxButton("buscarCepButton") {
      @Override
      protected void onSubmit(AjaxRequestTarget target) {
        String cep = cepField.getInput();
        cep = StringUtils.removerMascaraDigito(cep);

        if (cep == null || cep.length() != 8) {
          error("CEP inválido. Digite 8 números.");
          target.add(feedbackPanel);
          return;
        }

        RestTemplate restTemplate = new RestTemplate();
        String url = "https://viacep.com.br/ws/" + cep + "/json";

        try {
          ViaCepResponse response = restTemplate.getForObject(url, ViaCepResponse.class);
          if (response == null || response.isErro()) {
            error("CEP não encontrado.");
            target.add(feedbackPanel);
            return;
          }

          //atualiza o modelo
          item.getModelObject().setCep(cepField.getInput());//salva cep com máscara
          item.getModelObject().setLogradouro(response.getLogradouro());
          item.getModelObject().setBairro(response.getBairro());
          item.getModelObject().setCidade(response.getLocalidade());
          item.getModelObject().setEstado(response.getUf());
          item.getModelObject().setComplemento(response.getComplemento());

          //redesenha o container e reaplica as máscaras
          target.add(enderecosContainer);
          String initMasksScript = String.format(
            "document.querySelectorAll('#%s [data-mask]').forEach(el => {" +
              "  if (!el.imask) { new IMask(el, { mask: el.dataset.mask, lazy: false }); }" +
              "});",
            modal.getMarkupId()
          );
          target.appendJavaScript(initMasksScript);

        } catch (Exception e) {
          error("Erro ao consultar o CEP. Tente novamente.");
          target.add(feedbackPanel);
          e.printStackTrace();
        }
      }

      @Override
      protected void onError(AjaxRequestTarget target) {
        target.add(feedbackPanel);
      }
    };
    buscarCepButton.setDefaultFormProcessing(false);
    return buscarCepButton;
  }

  //botões de salvar e cancelar
  private void addFormActions() {
    //botão salvar
    form.add(new AjaxButton("saveButton", form) {
      @Override
      protected void onSubmit(AjaxRequestTarget target) {
        ClienteDTO clienteDTO = form.getModelObject();

        //limpa máscaras antes de salvar
        if (TipoPessoa.FISICA.equals(clienteDTO.getTipoPessoa())) {
          clienteDTO.setCpf(StringUtils.removerMascaraDigito(clienteDTO.getCpf()));
        } else if (TipoPessoa.JURIDICA.equals(clienteDTO.getTipoPessoa())) {
          clienteDTO.setCnpj(StringUtils.removerMascaraDigito(clienteDTO.getCnpj()));
        }
        if (clienteDTO.getEnderecos() != null) {
          for (EnderecoDTO end : clienteDTO.getEnderecos()) {
            end.setCep(StringUtils.removerMascaraDigito(end.getCep()));
          }
        }

        //tenta salvar
        try {
          if (isEditMode) {
            clienteService.editarCliente(clienteDTO);
          } else {
            clienteService.novoCliente(clienteDTO);
          }
          fecharModal(target);
          onSave(target);
        } catch (Exception e) {
          //exibição de múltiplos erros
          String rawMessages = e.getMessage();
          if (rawMessages != null && rawMessages.contains(";")) {
            String[] erros = rawMessages.split(";");
            error("Erro ao salvar: ");
            for (String msg : erros) {
              if (!msg.trim().isEmpty()) {
                error(msg.trim());
              }
            }
          } else {
            String message = (rawMessages != null) ? rawMessages : "Erro ao salvar.";
            error(message);
          }
          target.add(feedbackPanel);
        }
      }

      @Override
      protected void onError(AjaxRequestTarget target) {
        //erro de validação do Wicket (DateTextField por exemplo)
        target.add(feedbackPanel);
      }
    });

    //botão cancelar
    form.add(new AjaxLink<Void>("cancelButton") {
      @Override
      public void onClick(AjaxRequestTarget target) {
        fecharModal(target);
        onCancel(target);
      }
    });
  }

  //--------------------------------------------------------------------------------------------------------
  //métodos públicos (API do painel)
  //--------------------------------------------------------------------------------------------------------

  //abre modal para criação de cliente
  public void openForCreate(AjaxRequestTarget target) {
    this.isEditMode = false;
    titleModel.setObject("Novo Cliente");

    ClienteDTO dto = new ClienteDTO();
    dto.setTipoPessoa(TipoPessoa.FISICA);
    dto.setAtivo(true);
    dto.setEnderecos(new ArrayList<>());
    dto.getEnderecos().add(new EnderecoDTO());

    form.setModelObject(dto);
    target.add(form);
    abrirModal(target);
  }

  //abre modal para edição de cliente
  public void openForEdit(IModel<Cliente> clienteModel, AjaxRequestTarget target) {
    this.isEditMode = true;
    titleModel.setObject("Editar Cliente");

    ClienteDTO dto = clienteMapper.clienteParaDTO(clienteModel.getObject());
    if (dto.getEnderecos() == null || dto.getEnderecos().isEmpty()) {
      dto.setEnderecos(new ArrayList<>());
      dto.getEnderecos().add(new EnderecoDTO());
    }

    form.setModelObject(dto);
    target.add(form);
    abrirModal(target);
  }

  //--------------------------------------------------------------------------------------------------------
  //métodos abstratos
  //--------------------------------------------------------------------------------------------------------

  public abstract void onSave(AjaxRequestTarget target);
  public abstract void onCancel(AjaxRequestTarget target);

  //--------------------------------------------------------------------------------------------------------
  //renderHead para css e js
  //--------------------------------------------------------------------------------------------------------

  @Override
  public void renderHead(IHeaderResponse response) {
    response.render(JavaScriptHeaderItem.forUrl("https://unpkg.com/imask"));

    response.render(OnDomReadyHeaderItem.forScript(
      "new bootstrap.Modal(document.getElementById('" + modal.getMarkupId() + "'));"
    ));

    String initMasksScript = String.format(
      "document.querySelectorAll('#%s [data-mask]').forEach(el => {" +
        "  if (!el.imask) { new IMask(el, { mask: el.dataset.mask, lazy: false }); }" +
        "});",
      modal.getMarkupId()
    );

    String bootstrapListenerScript = String.format(
      "const modalEl = document.getElementById('%s');" +
        "modalEl.addEventListener('shown.bs.modal', () => { %s });",
      modal.getMarkupId(),
      initMasksScript
    );
    response.render(OnDomReadyHeaderItem.forScript(bootstrapListenerScript));
  }

  //--------------------------------------------------------------------------------------------------------
  //métodos auxiliares UI
  //--------------------------------------------------------------------------------------------------------

  private void abrirModal(AjaxRequestTarget target) {
    target.appendJavaScript("bootstrap.Modal.getInstance(document.getElementById('" + modal.getMarkupId() + "')).show();");
  }

  private void fecharModal(AjaxRequestTarget target) {
    target.appendJavaScript("bootstrap.Modal.getInstance(document.getElementById('" + modal.getMarkupId() + "')).hide();");
  }
}