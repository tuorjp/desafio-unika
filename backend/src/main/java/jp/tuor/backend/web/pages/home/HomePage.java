package jp.tuor.backend.web.pages.home;

import com.giffing.wicket.spring.boot.context.scan.WicketHomePage;
import jp.tuor.backend.model.Cliente;
import jp.tuor.backend.model.Endereco;
import jp.tuor.backend.model.enums.TipoPessoa;
import jp.tuor.backend.service.ClienteService;
import jp.tuor.backend.web.ClienteDataProvider;
import jp.tuor.backend.web.pages.BasePage;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@WicketHomePage
public class HomePage extends BasePage {
    @SpringBean
    private ClienteService clienteService;
    private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    //--------------------------------------------------------------------------------------------------------
    //métodos de renderização e configuração
    public HomePage() {
        //título simples
        super();
        String str = "Clientes";
        add(new Label("titulo", str));

        //date provider da tabela
        SortableDataProvider<Cliente, String> dataProvider = new ClienteDataProvider(clienteService);

        //container da tabela e paginação
        WebMarkupContainer tableContainer = new WebMarkupContainer("tableContainer");
        add(tableContainer);

        //data view para a tabela
        DataView<Cliente> dataView = new DataView<Cliente>("clienteList", dataProvider) {
            @Override
            protected void populateItem(Item<Cliente> item) {
                Cliente cliente = item.getModelObject();

                item.add(new Label("id", cliente.getId()));
                item.add(new Label("nome", getNomeCliente(cliente)));
                item.add(new Label("email", cliente.getEmail() != null? cliente.getEmail() : "n/a" ));
                item.add(new Label("documento", getDocumentoCliente(cliente)));
                item.add(new Label("data", getDataCliente(cliente)));
                item.add(new Label("endereco", getEnderecoPrincipal(cliente.getEnderecos())));

                //badge do status
                Label statusLabel = new Label("status", cliente.isAtivo() ? "Ativo" : "Inativo");
                statusLabel.add(new AttributeAppender("class", cliente.isAtivo() ? "text-bg-success" : "text-bg-danger"));
                item.add(statusLabel);
            }
        };

        dataView.setItemsPerPage(10);
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

        WebMarkupContainer emptyMessage = new WebMarkupContainer("emptyMessage");
        emptyMessage.setVisible(totalItems == 0);
        add(emptyMessage);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        response.render(CssHeaderItem.forReference(new PackageResourceReference(HomePage.class, "HomePage.css")));
    }

    //--------------------------------------------------------------------------------------------------------
    //métodos auxiliares

    private String getNomeCliente(Cliente cliente) {
        String nome = cliente.getTipoPessoa() == TipoPessoa.FISICA ? cliente.getNome() : cliente.getRazaoSocial();
        return nome != null ? nome : "";
    }

    private String getDataCliente(Cliente cliente) {
        Date data = cliente.getTipoPessoa() == TipoPessoa.FISICA ? cliente.getDataNascimento() : cliente.getDataCriacao();
        return data != null ? DATE_FORMAT.format(data) : "N/A";
    }

    private String getEnderecoPrincipal(List<Endereco> enderecos) {
        if (enderecos == null || enderecos.isEmpty()) {
            return "N/A";
        }

        Endereco principal = enderecos.stream()
                .filter(Endereco::isEnderecoPrincipal)
                .findFirst()
                .orElse(null);

        if (principal == null) {
            principal = enderecos.get(0);
        }

        return String.format("%s, %s, %s-%s",
                principal.getLogradouro(),
                principal.getNumero(),
                principal.getCidade(),
                principal.getEstado()
        );
    }

    private String getDocumentoCliente(Cliente cliente) {
        String doc = cliente.getTipoPessoa() == TipoPessoa.FISICA ? cliente.getCpf() : cliente.getCnpj();
        if (doc == null) {
            return "N/A";
        }
        return doc;
    }
}
