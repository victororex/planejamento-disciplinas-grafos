# Publicação e manutenção

Repositório público: [victororex/planejamento-disciplinas-grafos](https://github.com/victororex/planejamento-disciplinas-grafos).

Descrição: “Aplicação Java Swing para planejamento acadêmico utilizando grafos direcionados, ordenação topológica por Kahn e DFS e detecção de ciclos.”

A criação foi feita na sessão autenticada do navegador, pois a integração disponível do GitHub não oferece criação de repositórios. O conteúdo e os commits são enviados pela integração autenticada. A visibilidade pública foi confirmada pela API do GitHub. Nenhum token ou senha foi colocado no projeto.

São publicados fontes, scripts, quatro exemplos, testes, documentação, capturas reais e licença MIT. A autoria da licença usa o nome e o login confirmados pelo perfil autenticado. PDFs da professora, JDK portátil, arquivos `.class`, `out/`, `dist/`, temporários e configurações pessoais da IDE são excluídos.

## Obter uma cópia

```text
git clone https://github.com/victororex/planejamento-disciplinas-grafos.git
cd planejamento-disciplinas-grafos
```

No Windows, execute `executar.bat` após instalar o JDK 17 ou superior. Para validar: `testar.bat`.

## Enviar alterações futuras

Configure a identidade do Git com seus dados reais. Para evitar divulgar um endereço de email pessoal, use o endereço `noreply` que aparece nas suas configurações de email do GitHub. Não coloque credenciais nos comandos ou na URL do remoto.

Exemplo de fluxo com o GitHub CLI já instalado:

```text
gh auth login
git switch -c codex/melhoria-planejamento
git add src docs README.md
git diff --cached --check
git commit -m "feat: descreve a melhoria implementada"
git push -u origin codex/melhoria-planejamento
gh pr create --base main
```

Use `git status` antes do commit. Para confirmar o remoto e sua branch principal:

```text
git remote -v
git ls-remote --heads origin main
```

A pasta local da entrega acompanha a branch pública `main` em uma branch de trabalho com prefixo `codex/`. A publicação inicial não depende de criar um pull request: o repositório foi criado especificamente para este projeto.
