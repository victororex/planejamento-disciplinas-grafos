# Publicação e manutenção

Repositório: [victororex/planejamento-disciplinas-grafos](https://github.com/victororex/planejamento-disciplinas-grafos).

A evolução curricular é desenvolvida na branch `feat/planejador-curricular-universal`, criada após conferir que o HEAD inicial correspondia a `origin/main` (`12c8ed79b41060ebb459f9f7fcba08c9ab4e9681`). A licença MIT e os algoritmos manuais foram preservados. Não há force push ou descarte de contribuições.

A validação reproduzível é `bash testar.sh` (Linux/macOS) ou `testar.bat` (Windows), usando JDK 17 ou superior. O fluxo gráfico roda separadamente com `java -cp out TestesInterface` num desktop ou `xvfb-run -a java -cp out TestesInterface` no Linux.

São publicados fontes, testes, scripts, documentação, capturas reais, modelos e exemplos fictícios. JDK, pacotes de ferramentas, `.class`, `out/`, `dist/`, `.tmp/` e arquivos pessoais ficam fora do Git. O JAR é gerado localmente; não é necessário baixar dependências para executar o aplicativo.

## Revisão da evolução curricular

O acesso à API `api.github.com` retornou **Forbidden** na política de rede desta sessão. Isso impede criar o Pull Request e consultar regras de integração pela API. A autenticação HTTPS fornecida ao Git foi testada separadamente; uma falha da API não é tratada como ausência de token de Git. A branch deve ser revisada antes da integração à main.

Após permitir acesso à API nas configurações do ambiente e verificar a autenticação da operação, o fluxo de PR pode ser concluído com:

```bash
gh pr create --base main --head feat/planejador-curricular-universal \
  --title 'Evolui o aplicativo para planejador curricular universal' \
  --body-file docs/PR-CURRICULAR.md
```

Também é possível iniciar a revisão pela [comparação de branches](https://github.com/victororex/planejamento-disciplinas-grafos/compare/main...feat/planejador-curricular-universal). A página de comparação não é um Pull Request criado. Confira o histórico e o resultado da publicação descritos na entrega da sessão.

Não são necessários tokens em arquivos de configuração do projeto. Nunca grave credenciais no código ou nas URLs dos remotos. Para futuras mudanças, preserve o histórico, use uma branch e execute as verificações antes de integrar.
