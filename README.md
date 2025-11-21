# Projeto desafio Spring + Angular + Wicket

O projeto deste repositório foi desenvolvido atendendo aos requisitos do documento "Desafio estágio" da Unika Sistemas.

## Recursos

As planilhas usadas na função importar clientes estão disponíveis na raiz deste repositório.

### Planilhas para testes 

`import-2.xlsx` é o modelo sem erros que será importado corretamente, `teste-importacao-erro.xlsx` é o modelo que, de propósito, contém erros, para mostrar como será feito e exibido o tratamento de erros.

## Como rodar o projeto Web

Para usuários do `Intellij IDEA` basta navegar até `src/main/java/jp/tuor/backend/BackendApplication.java` e clicar no botão executar para rodar o servico back-end e no caminho `src/main/java/jp/tuor/frontend/package.json` clicar no botão executar script `"start"`.

Para executar via terminal:
* Front-ent: navegar até `src/main/java/jp/tuor/frontend` pelo terminal e rodar o comando `npm run start`.
* Back-end: navegar até `src/main/java/jp/tuor/backend` pelo terminal e rodar o comando `mvn spring-boot:run`.

Verifique se as dependências estão instaladas e se o banco de dados está configurado conforme o arquivo `application.properties`.

## Como rodar o projeto no Android

Para executar o frontend Angular como um aplicativo Android usando o **Capacitor**, é necessário configurar a comunicação de rede para contornar as restrições de segurança do emulador (CORS e HTTP Cleartext).

### Configuração Inicial do Capacitor

Se for a primeira vez, instale e inicialize o Capacitor no projeto Angular (`src/main/java/jp/tuor/frontend/`):

1.  **Instalar Capacitor:**
    ```bash
    npm install @capacitor/core @capacitor/cli
    ```
2.  **Inicializar e Adicionar Plataforma:**
    ```bash
    npx cap init
    npx cap add android
    ```

### Configurações de Rede (Essenciais para Debug)

Estas configurações garantem que o emulador (que usa o IP `10.0.2.2`) consiga se comunicar com o computador.

#### Configuração do Capacitor (Frontend)

**`capacitor.config.ts`** na raiz do projeto Angular. Necessário para forçar o carregamento via HTTP e apontar para o `ng serve`:

```typescript
// capacitor.config.ts
import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'jp.tuor.frontend',
  appName: 'clientes',
  webDir: 'dist/frontend/browser',
    server: {
        //aponta o emulador para o ng serve do host via HTTP
        url: "http://10.0.2.2:4200",
        cleartext: true,
        //define o hostname para forçar a WebView a carregar via HTTP, resolvendo o Mixed Content error.
        "hostname": "localhost"
    }
};

export default config;
```

### Configurações nativas do Android

#### HTTP

O tráfego Http deve ser permitido. Para isso, é preciso criar o arquivo: `android/app/src/main/res/xml/network_security_config.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
  <base-config cleartextTrafficPermitted="true">
    <trust-anchors>
      <certificates src="system" />
    </trust-anchors>
  </base-config>
</network-security-config>
```

É preciso referenciar esse arquivo no `AndroidManifest.xml`:

```xml
    <activity
        android:name="jp.tuor.frontend.MainActivity"
        android:networkSecurityConfig="@xml/network_security_config"
        android:usesCleartextTraffic="true"
    ></activity>
```

### Rodando o projeto Android

Com as configurações prontas, basta abrir um terminal em `src/main/java/jp/tuor/frontend/` e os comandos abaixo:

```shell
npm run start-host
npx cap sync android
npx cap run android
```

