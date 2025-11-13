package jp.tuor.backend.web;

import jp.tuor.backend.web.pages.home.HomePage;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.csp.CSPDirective;
import org.apache.wicket.csp.CSPDirectiveSrcValue;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

@Component
public class WicketApp extends WebApplication {
  public static final MetaDataKey<ZoneId> ZONE_ID_KEY = new MetaDataKey<>() {};

  @Override
  public Class<? extends Page> getHomePage() {
    return HomePage.class;
  }

  @Override
  protected void init() {
    super.init();
    getMarkupSettings().setStripWicketTags(true);
    getDebugSettings().setDevelopmentUtilitiesEnabled(true);

    var csp = getCspSettings().blocking();

    //define a política base para 'self' (próprios arquivos)
    csp.add(CSPDirective.DEFAULT_SRC, CSPDirectiveSrcValue.SELF);

    //adiciona 'nonce' para scripts e estilos (que o Wicket gerencia)
    csp.add(CSPDirective.SCRIPT_SRC, CSPDirectiveSrcValue.NONCE);
    csp.add(CSPDirective.STYLE_SRC, CSPDirectiveSrcValue.NONCE);

    //permite 'unsafe-eval' (necessário para o Wicket AJAX)
    csp.add(CSPDirective.SCRIPT_SRC, CSPDirectiveSrcValue.UNSAFE_EVAL);

    //permite 'data:' para imagens
    /*csp.add(CSPDirective.IMG_SRC, CSPDirectiveSrcValue.SELF);
    csp.add(CSPDirective.IMG_SRC, "data:");
    csp.add(CSPDirective.IMG_SRC, "http://www.w3.org/2000/svg");
    csp.add(CSPDirective.IMG_SRC, "http://www.w3.org/1999/xlink");*/
    csp.add(CSPDirective.IMG_SRC,
      CSPDirectiveSrcValue.SELF.toString(),
      "data:",
      "http://www.w3.org/2000/svg",
      "http://www.w3.org/1999/xlink"
    );

    // Permite 'data:' para fontes (se necessário)
    csp.add(CSPDirective.FONT_SRC, CSPDirectiveSrcValue.SELF);
    csp.add(CSPDirective.FONT_SRC, "data:");
  }

  @Override
  public Session newSession(Request request, Response response) {
    Session session = super.newSession(request, response);

    session.setMetaData(ZONE_ID_KEY, ZoneId.of("America/Sao_Paulo"));

    return session;
  }
}
