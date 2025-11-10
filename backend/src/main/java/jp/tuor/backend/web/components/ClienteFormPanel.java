package jp.tuor.backend.web.components;

import jp.tuor.backend.model.Cliente;
import jp.tuor.backend.model.dto.ClienteDTO;
import jp.tuor.backend.model.dto.EnderecoDTO;
import jp.tuor.backend.model.enums.TipoPessoa;
import jp.tuor.backend.model.mapper.ClienteMapper;
import jp.tuor.backend.service.ClienteService;
import jp.tuor.backend.utils.StringUtils;
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
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public abstract class ClienteFormPanel extends Panel {
  @SpringBean
  private ClienteService clienteService;
  @SpringBean
  private ClienteMapper clienteMapper;

  private Form<ClienteDTO> form;
  private WebMarkupContainer modal;
  private Label modalTitle;
  private IModel<String> titleModel;
  private boolean isEditMode;
  private WebMarkupContainer containerPF;
  private WebMarkupContainer containerPJ;
  private WebMarkupContainer enderecosContainer;

  public ClienteFormPanel(String id) {
    super(id);

    //div container do modal <div wicket:id="clienteModal">
    modal = new WebMarkupContainer("clienteModal");
    modal.setOutputMarkupId(true);
    add(modal);

    //titulo
    titleModel = Model.of("Novo Cliente");
    modalTitle = new Label("modalTitle", titleModel);

    //como se fosse o FormGroup do wicket
    CompoundPropertyModel<ClienteDTO> formModel = new CompoundPropertyModel<>(new ClienteDTO());
    form = new Form<>("clienteForm", formModel);
    form.setOutputMarkupId(true);
    modal.add(form);
    form.add(modalTitle);

    //painel de feedback
    FeedbackPanel feedbackPanel = new FeedbackPanel("formFeedback");
    feedbackPanel.setOutputMarkupId(true);
    feedbackPanel.add(new AttributeAppender("class", new Model<String>() {
      @Override
      public String getObject() {
        return feedbackPanel.anyMessage() ? "" : "d-none";
      }
    }, " "));
    form.add(feedbackPanel);

    //campos do formulário
    DropDownChoice<TipoPessoa> tipoPessoaField = new DropDownChoice<>("tipoPessoa", Arrays.asList(TipoPessoa.values()));
    tipoPessoaField.setRequired(true);
    tipoPessoaField.add(new AjaxFormComponentUpdatingBehavior("change") {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        target.add(containerPF, containerPJ);
      }
    });
    form.add(tipoPessoaField);
    form.add(new EmailTextField("email"));
    form.add(new CheckBox("ativo"));

    //PF
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
    DateTextField dataNascimentoField = new DateTextField("dataNascimento", "dd/MM/yyyy");
    containerPF.add(dataNascimentoField);
    form.add(containerPF);

    //PJ
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
    DateTextField dataCriacaoField = new DateTextField("dataCriacao", "dd/MM/yyyy");
    containerPJ.add(dataCriacaoField);
    form.add(containerPJ);

    //endereços
    enderecosContainer = new WebMarkupContainer("enderecosContainer");
    enderecosContainer.setOutputMarkupId(true);
    form.add(enderecosContainer);

    //ListView equivalente ao *ngFor
    ListView<EnderecoDTO> enderecosView = new ListView<EnderecoDTO>("enderecos") {
      @Override
      protected void populateItem(ListItem<EnderecoDTO> item) {
        item.setDefaultModel(new CompoundPropertyModel<>(item.getModel()));

        item.add(new TextField<>("cep"));
        item.add(new TextField<>("logradouro"));
        item.add(new TextField<>("numero"));
        item.add(new TextField<>("complemento"));
        item.add(new TextField<>("bairro"));
        item.add(new TextField<>("cidade"));
        item.add(new TextField<>("estado"));
        item.add(new CheckBox("enderecoPrincipal"));

        AjaxLink<Void> removeLink = new AjaxLink<Void>("removeEndereco") {
          @Override
          public void onClick(AjaxRequestTarget target) {
            EnderecoDTO dtoToRemove = item.getModelObject();
            form.getModelObject().getEnderecos().remove(dtoToRemove);
            target.add(enderecosContainer);
          }
        };

        item.add(removeLink);
      };
    };

    enderecosView.setReuseItems(true);
    enderecosContainer.add(enderecosView);

    AjaxLink<Void> addEnderecoLink = new AjaxLink<Void>("addEndereco") {
      @Override
      public void onClick(AjaxRequestTarget target) {
        form.getModelObject().getEnderecos().add(new EnderecoDTO());
        target.add(enderecosContainer);

        String initMasksScript = String.format(
                "document.querySelectorAll('#%s [data-mask]').forEach(el => {" +
                        "  if (!el.imask) { new IMask(el, { mask: el.dataset.mask, lazy: false }); }" +
                        "});",
                enderecosContainer.getMarkupId() //pega o ID do container de endereços
        );
        target.appendJavaScript(initMasksScript);
      }
    };

    form.add(addEnderecoLink);

    //botão salvar
    form.add(new AjaxButton("saveButton", form) {
      @Override
      protected void onSubmit(AjaxRequestTarget target) {
        ClienteDTO clienteDTO = form.getModelObject();

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

        try {
          if (isEditMode) {
            clienteService.editarCliente(clienteDTO);
          } else {
            clienteService.novoCliente(clienteDTO);
          }
          fecharModal(target);
          onSave(target);
        } catch (Exception e) {
          error("Erro ao salvar: " + e.getMessage());
          target.add(feedbackPanel);
        }
      }

      @Override
      protected void onError(AjaxRequestTarget target) {
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

  //métodos para o componente pai chamar
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

  public void openForEdit(IModel<Cliente> clienteModel, AjaxRequestTarget target) {
    this.isEditMode = true;
    titleModel.setObject("Editar Cliente");

    ClienteDTO dto = clienteMapper.clienteParaDTO(clienteModel.getObject());

    if(dto.getEnderecos() == null || dto.getEnderecos().isEmpty()) {
      dto.setEnderecos(new ArrayList<>());
      dto.getEnderecos().add(new EnderecoDTO());
    }

    form.setModelObject(dto); //preenche o formulário
    target.add(form); //atualiza o formulário (preenchido)
    abrirModal(target); //chama o JS para abrir
  }

  //métodos abstratos, equivalente ao @Output do Angular
  public abstract void onSave(AjaxRequestTarget target);

  public abstract void onCancel(AjaxRequestTarget target);

  //manipulação do modal UI
  @Override
  public void renderHead(IHeaderResponse response) {
    response.render(JavaScriptHeaderItem.forUrl("https://unpkg.com/imask"));

    // Cria o objeto JS do Bootstrap no cliente
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

  private void abrirModal(AjaxRequestTarget target) {
    target.appendJavaScript("bootstrap.Modal.getInstance(document.getElementById('" + modal.getMarkupId() + "')).show();");
  }

  private void fecharModal(AjaxRequestTarget target) {
    target.appendJavaScript("bootstrap.Modal.getInstance(document.getElementById('" + modal.getMarkupId() + "')).hide();");
  }
}
