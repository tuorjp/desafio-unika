package jp.tuor.backend.web.pages.home;

import com.giffing.wicket.spring.boot.context.scan.WicketHomePage;
import jp.tuor.backend.web.pages.BasePage;
import org.apache.wicket.markup.html.basic.Label;

@WicketHomePage
public class HomePage extends BasePage {
    public HomePage() {
        super();
        String str = "Aplicação Web Wicket + BootStrap";
        add(new Label("titulo", str));
    }
}
