# Conferência com os materiais da professora

Os quatro PDFs fornecidos foram lidos em 20/10/2026. A extração de texto abrangeu as 39 páginas, com inspeção visual das páginas relevantes de requisitos, exemplos e algoritmos. Os arquivos originais permanecem fora do repositório; esta conferência contém referências e conclusões sobre o programa.

## Projeto Prático 1 — Algoritmos em Grafos (4 páginas)

| Requisito e página | Implementação e evidência |
|---|---|
| Cadastro: código, nome, período e pré-requisitos — p. 1 | `Disciplina` valida o cadastro; `GrafoDisciplinas.obterPrerequisitos` deriva a lista das arestas de entrada. A interface permite editar as relações. |
| A → B significa A antes de B — p. 2 | Adjacência guarda os dependentes de cada origem, sem criar automaticamente a relação inversa. |
| Kahn: fontes, processamento e atualização dos graus — p. 2 | `AlgoritmoKahn`, com fila FIFO e cópia dos graus; 11 estados no exemplo do enunciado. |
| DFS: não visitado, em processamento e finalizado — p. 2 | `AlgoritmoDFS`, com três cores e pilha explícita equivalente à organização das chamadas recursivas. |
| Ciclo A → B → C → A impede ordenação — p. 2 | Exemplo 3, reconstrução pela pilha da DFS e rejeição de ordem e planejamento inválidos. |
| Exemplo A → B, A → C, B → D, C → E — p. 2 | Exemplo 1 reproduz as duas saídas do PDF: Kahn A B C D E e DFS A C E B D. Ambas passam pelo verificador independente. |
| Períodos simultâneos — p. 1 | `PlanejadorPeriodos` calcula a etapa de cada disciplina com o máximo das etapas de seus pré-requisitos. Não utiliza apenas a posição na ordem. |
| O(V + E), com V e E definidos — p. 3 | Aba Complexidade e seção 7 do relatório detalham os percursos de vértices e arestas e distinguem os custos da visualização. |
| Estratégia, estruturas, ciclos, facilidade e aplicações — p. 3 | Aba Comparação e seção 8 do relatório contemplam os cinco aspectos. A facilidade de implementação passou a constar como linha explícita. |
| Código, relatório e demonstração de três situações — pp. 3–4 | Fontes em `src/`, relatório em `docs/RELATORIO.md` e exemplos 1, 2 e 3, todos editáveis. |

## Aula 7 — Contagem de Componentes Conexos (7 páginas)

As páginas 3–5 apresentam DFS em grafos **não direcionados**, iniciando uma busca em cada vértice ainda não visitado. Na aplicação, o grafo de pré-requisitos continua direcionado; apenas uma projeção temporária não direcionada é usada para contar componentes **fracas** por DFS iterativa.

A página 6 distingue esse problema das componentes fortemente conexas de dígrafos e menciona Kosaraju. O detector usa essa segunda análise para identificar todos os participantes de ciclos; o caminho destacado continua vindo da pilha da primeira DFS. A contagem de componentes fracas não é usada como critério de detecção de ciclos.

Os exercícios da página 7 foram transformados em dois testes: K4 com uma componente; K3 com um vértice isolado com duas componentes. As arestas reversas da projeção não são acrescentadas ao modelo acadêmico.

## Aula 8 — Ordenação Topológica (11 páginas)

A definição de precedência da página 4 e o processamento de fontes da página 8 correspondem às implementações. Na aula, as arestas consideradas são removidas; no programa, a remoção é **lógica**, representada pela redução de graus residuais. O grafo original é preservado para edição, repetição das análises e navegação do histórico.

A página 9 apresenta tempo O(V + E) e discute o caráter iterativo de Kahn. Essa justificativa consta na explicação de complexidade e na comparação de facilidade de implementação.

## Aula 9 — Detecção de Ciclos (17 páginas)

As páginas 10–12 descrevem branco, cinza, preto e a pilha ativa; uma aresta para cinza evidencia um ciclo. O programa usa a mesma regra e reconstrói um caminho real a partir do trecho ativo da pilha. A finalização e sua inversão seguem o princípio apresentado na página 15. O laço externo cobre componentes desconectadas.

As aplicações em dependências de tarefas e fluxo de dados das páginas 13–16 estão relacionadas na seção 10 do relatório. Os tópicos de Euler e Hamilton presentes nas primeiras páginas da aula não constituem entregáveis do planejador e não foram usados para alterar a definição de ordenação topológica.

## Ajustes após a conferência

- Comparação com uma linha explícita sobre facilidade de implementação.
- DFS iterativa na contagem de componentes fracas, acompanhada de dois testes da Aula 7.
- Referências do README e relatório atualizadas para registrar a leitura direta dos PDFs.
- Tema escuro compartilhado por Swing, HTML e Graphics2D. Indicadores branco, cinza e preto da DFS e alertas de ciclos continuam distinguíveis.

O atendimento descrito é uma conferência técnica dos requisitos, sem atribuir nota ou substituir a avaliação da professora.
