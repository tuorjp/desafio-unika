package jp.tuor.backend.web;

import jp.tuor.backend.web.pages.home.HomePage;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.springframework.stereotype.Component;

@Component
public class WicketApp extends WebApplication {
    @Override
    public Class<? extends Page> getHomePage() {
        return HomePage.class;
    }

    @Override
    protected void init() {
        super.init();
        getMarkupSettings().setStripWicketTags(true);
        getDebugSettings().setDevelopmentUtilitiesEnabled(true);
    }
}
