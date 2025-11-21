# Projeto desafio Spring + Angular + Wicket

O projeto deste repositório foi desenvolvido atendendo aos requisitos do documento "Desafio estágio" da Unika Sistemas.

## Recursos

As planilhas usadas na função importar clientes estão disponíveis na raiz deste repositório.

### Planilhas para testes 

`import-2.xlsx` é o modelo sem erros que será importado corretamente, `teste-importacao-erro.xlsx` é o modelo que, de propósito, contém erros, para mostrar como será feito e exibido o tratamento de erros.

## Como rodar o projeto

Para usuários do `Intellij IDEA` basta navegar até `src/main/java/jp/tuor/backend/BackendApplication.java` e clicar no botão executar para rodar o servico back-end e no caminho `src/main/java/jp/tuor/frontend/package.json` clicar no botão executar script `"start"`.

Para executar via terminal:
* Front-ent: navegar até `src/main/java/jp/tuor/frontend` pelo terminal e rodar o comando `npm run start`.
* Back-end: navegar até `src/main/java/jp/tuor/backend` pelo terminal e rodar o comando `mvn spring-boot:run`.

Verifique se as dependências estão instaladas e se o banco de dados está configurado conforme o arquivo `application.properties`.