package jp.tuor.backend.web.pages;

import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.UrlResourceReference;

public class BasePage extends WebPage {
    public BasePage() {
        super();
        add(new Label("pageTitle", Model.of("Clientes")));
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        response.render(CssHeaderItem.forReference(
                new UrlResourceReference(Url.parse("/css/bootstrap.min.css")))
        );

        response.render(CssHeaderItem.forReference(
                new UrlResourceReference(Url.parse("/css/bootstrap-icons.min.css")))
        );

        response.render(JavaScriptHeaderItem.forReference(
                new UrlResourceReference(Url.parse("/js/bootstrap.bundle.min.js")))
        );
    }
}
