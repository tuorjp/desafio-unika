package jp.tuor.backend.web.components;

import jp.tuor.backend.model.Cliente;
import jp.tuor.backend.model.dto.ClienteDTO;
import jp.tuor.backend.model.enums.TipoPessoa;
import jp.tuor.backend.model.mapper.ClienteMapper;
import jp.tuor.backend.service.ClienteService;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;

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

    //PF
    containerPF = new WebMarkupContainer("containerPF");
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
    TextField<Date> dataNascimentoField = new TextField<>("dataNascimento");
    dataNascimentoField.add(new AttributeAppender("type", "date"));
    containerPF.add(dataNascimentoField);
    form.add(containerPF);

    //PJ
    containerPJ = new WebMarkupContainer("containerPJ");
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
    TextField<Date> dataCriacaoField = new TextField<>("dataCriacao");
    dataCriacaoField.add(new AttributeAppender("type", "date"));
    containerPJ.add(dataCriacaoField);
    form.add(containerPJ);

    //botão salvar
    form.add(new AjaxButton("saveButton", form) {
      @Override
      protected void onSubmit(AjaxRequestTarget target) {
        ClienteDTO clienteDTO = form.getModelObject();
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
    form.setModelObject(dto);
    target.add(form);
    abrirModal(target);
  }

  public void openForEdit(IModel<Cliente> clienteModel, AjaxRequestTarget target) {
    this.isEditMode = true;
    titleModel.setObject("Editar Cliente");

    ClienteDTO dto = clienteMapper.clienteParaDTO(clienteModel.getObject());
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
    // Cria o objeto JS do Bootstrap no cliente
    response.render(OnDomReadyHeaderItem.forScript(
            "new bootstrap.Modal(document.getElementById('" + modal.getMarkupId() + "'));"
    ));
  }

  private void abrirModal(AjaxRequestTarget target) {
    target.appendJavaScript("bootstrap.Modal.getInstance(document.getElementById('" + modal.getMarkupId() + "')).show();");
  }

  private void fecharModal(AjaxRequestTarget target) {
    target.appendJavaScript("bootstrap.Modal.getInstance(document.getElementById('" + modal.getMarkupId() + "')).hide();");
  }
}
