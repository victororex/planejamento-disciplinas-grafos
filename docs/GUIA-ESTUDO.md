# Guia de estudo do planejador curricular

## Do curso ao grafo

Uma matriz é a versão de um currículo: instituição, curso, período de vigência e disciplinas. Cada arquivo representa uma matriz. Você pode abrir várias janelas, mas cada uma mantém seu próprio modelo e arquivo. Um projeto também pode ser livre, sem instituição.

Uma disciplina vira um vértice. Uma dependência obrigatória simples vira uma seta: **A → B** significa que A deve ser concluída antes de B. Exemplo: Programação I → Programação II → Estruturas de Dados. Códigos são a identidade; nomes podem se repetir. Renomear um código atualiza relações, regras e cenários. Remover uma disciplina remove suas arestas, regras inteiras que a mencionem e referências em cenários, após confirmação na tela.

O período oficial é dado cadastrado, não resultado de algoritmo. Se não for conhecido, fica vazio (internamente 0, exportado como null). Reorganizar ou filtrar o desenho não modifica esse período nem as relações matemáticas.

## Responsabilidades no código

| Classes | Responsabilidade |
| --- | --- |
| `MatrizCurricular`, `Disciplina`, `StatusAcademico` | Identificação e dados imutáveis; validação de cadastro |
| `GrafoDisciplinas` | CRUD, adjacência de precedência, requisitos extras, cópias e alcance iterativo |
| `Requisito` | Árvore explícita de condições AND/OR, conclusão, correquisito, horas/créditos e atividades |
| `ProjetoAcademico` | Agrega grafo, posições, identificação da matriz e cenários |
| `AlgoritmoKahn`, `AlgoritmoDFS`, `DetectorCiclos` | Algoritmos manuais, independentes de Swing |
| `PassoAlgoritmo`, `ResultadoOrdenacao`, `ExplicadorDidatico` | Fotografias imutáveis, resultados e explicações de estudo |
| `PlanejadorPeriodos` | Níveis mínimos do grafo de precedência simples |
| `ServicoAcademico`, `PlanoAcademico` | Histórico, progresso, elegibilidade e cenários pessoais |
| `GerenciadorArquivos` | Leitura PGD 1/2 e salvamento PGD 2 |
| `Json`, `CatalogoJson`, `ImportadorMatrizes` | Parser limitado, esquema e prévia transacional CSV/JSON |
| `TelaPrincipal` | Coordena ações e compartilha o mesmo projeto entre visões |
| `FormulariosCurriculares`, `PainelMatriz`, `PainelPlanejamento` | Formulários, cartões por período e simulações dedicadas |
| `PainelGrafo`, `VerticeVisual`, `Tema` | Desenho, posições separadas da matemática e identidade visual |
| `ExemplosAcademicos`, `ExemplosCurriculares` | Grafos antigos e cinco matrizes fictícias |
| `TestesAlgoritmos`, `TestesCurriculares`, `TestesInterface` | Regressão matemática, regras/formatos e fluxos gráficos reais |

## Pré-requisito e correquisito não são a mesma coisa

Se Teoria é pré-requisito de Laboratório, ela precisa estar concluída antes. Se Teoria pode ser concluída **ou cursada junto**, cadastre um correquisito. A linha é tracejada e tem o rótulo CO com indicação do destino. A legenda também usa estilo e texto, não apenas cor.

Teoria e Laboratório podem ser correquisitos mútuos. O conjunto simultâneo é permitido se não houver outras pendências; não é um ciclo de pré-requisitos. Por isso correquisitos não entram na adjacência de Kahn/DFS.

Exigências mais complexas são árvores. Por exemplo, `(A OU B) E 120 horas E atividade X` significa que basta concluir A ou B, mas é necessário satisfazer as outras duas condições. No formulário de requisitos, selecione regras com Ctrl e use **Agrupar selecionadas: E/OU**. Regras de topo são sempre E. Atividades são disciplinas cadastradas com categoria adequada e status de conclusão; não se presume que uma atividade desconhecida foi feita.

## Como Kahn organiza disciplinas

1. Conta quantos pré-requisitos entram em cada vértice.
2. Coloca em uma fila as disciplinas com grau zero.
3. Retira uma disciplina, acrescenta à ordem e reduz os graus das dependentes.
4. Se todos foram processados, encontrou uma ordenação válida. Caso contrário, existe ciclo no grafo de precedência.

Uma ordem não é necessariamente única. A → C e B → C permite A,B,C ou B,A,C. O verificador confere que cada disciplina aparece uma vez e que cada origem vem antes de seu destino. A implementação percorre V vértices e E arestas: O(V + E), além das cópias e do histórico explicados no README original.

## Como DFS encontra um ciclo

A DFS marca branco (ainda não visitada), cinza (ativa no caminho) e preto (finalizada). Uma aresta para um vértice cinza revela uma volta ao caminho ativo: é um ciclo. A pilha explícita evita estourar a pilha de chamadas numa cadeia longa. Inverta a ordem de finalização para obter uma ordenação quando não houver ciclos. Kosaraju identifica todos os participantes de ciclos, sem confundi-los com dependentes apenas bloqueadas.

Use **Executar Kahn/DFS → Próximo/Anterior** na visão Grafo para estudar os estados e explicações existentes. Nas outras visões, o painel de algoritmos fica recolhido para dar espaço ao currículo; retorne à aba Grafo para vê-lo.

## Histórico, elegibilidade e progresso

Aprovada e Dispensada satisfazem conclusão. Não cursada, Reprovada e Em andamento não satisfazem. Em andamento também não é oferecida como nova matrícula. Edite o status no formulário da disciplina.

Depois de aprovar Programação I, **Ver disciplinas disponíveis** mostra Programação II liberada pelas regras cadastradas. Estruturas de Dados segue bloqueada até concluir Programação II. Todas as pendências conjuntivas são listadas. Em OR, a tela explica que é preciso satisfazer uma das alternativas.

O progresso percentual é `(aprovadas + dispensadas) / total de disciplinas`. Não representa a integralização oficial de um curso com grupos optativos complexos. Pendentes inclui em andamento. Horas e créditos somam somente conclusões com valores conhecidos; a tela informa quantas cargas/créditos não são conhecidos. Valores ausentes nunca são fabricados, e não ajudam a atingir mínimos.

## Três formas de visualizar o mesmo modelo

- **Grafo:** arraste, aplique zoom, oculte relações ou reorganize posições. Menu de contexto mostra todos os pré-requisitos ou todos os dependentes. Rótulos distinguem diretos e indiretos. Limpar filtros ou Voltar à visão geral remove o foco.
- **Matriz curricular:** colunas de períodos oficiais, incluindo Não informado. Cartões exibem código, nome, carga, status e contagem de requisitos. Clique destaca dependências; duplo clique edita.
- **Planejamento pessoal:** seleção de disciplinas, aprovações hipotéticas e cenários independentes do cadastro oficial.

Busca por nome/código (Ctrl+F), período, categoria e status filtram grafo e matriz sem excluir vértices. O acompanhamento e os algoritmos continuam usando o modelo inteiro. O painel de inspeção explica os conjuntos completos. Requisitos AND/OR são inspecionados em texto, sem criar um emaranhado de arestas que sugira uma semântica incorreta.

## Simular próximos períodos

Informe um nome e um rótulo de período futuro, como "Próximo semestre". Marque **Cursar** e, se quiser, **Simular aprovação anterior**. Essa segunda coluna supõe a disciplina concluída antes do período simulado; ela nunca muda o histórico real.

**Validar seleção** verifica todas as disciplinas do conjunto, incluindo correquisitos simultâneos, regras extras, horas máximas e quantidade máxima. Um pré-requisito comum não é satisfeito por simplesmente marcá-lo no mesmo período. Uma carga desconhecida impede afirmar que um teto de horas foi respeitado. Deixar o teto vazio significa não impor esse limite.

**Sugerir combinação** percorre a ordem de cadastro e tenta incluir disciplinas com seus correquisitos, aceitando somente conjuntos válidos. É uma heurística gulosa, não um otimizador: pode não encontrar o maior conjunto. Para OR, tenta o fecho da primeira alternativa; valide manualmente outras combinações. A sugestão recalcula a seleção; não mantém obrigatoriamente caixas marcadas anteriormente.

Salve cenários com nomes distintos e use **Comparar cenários** para ver seleções, cargas, aprovações hipotéticas e conflitos. Também é permitido salvar um cenário ainda inválido para estudo. Editar a matriz recarrega o painel de cenários: salve o cenário antes de mudar o cadastro. Os cenários salvos ficam no projeto PGD 2/JSON.

O modo **Níveis matemáticos** permanece separado: calcula `1 + máximo(nível dos pré-requisitos)` e ignora status, cargas, correquisitos e árvores AND/OR. Não o interprete como sugestão completa de matrícula.

## Importar, salvar e testar

Use os [modelos e instruções de importação](IMPORTACAO.md). Examine a prévia, especialmente avisos de campos desconhecidos e ciclos. Escolha outra janela para manter matrizes simultaneamente. Salve como PGD 2 para conservar todos os dados; o programa ainda abre os quatro PGD 1 antigos. Veja a [migração](FORMATO-PGD.md).

Com JDK 17 ou superior, execute `bash testar.sh` no Linux/macOS ou `testar.bat` no Windows. A primeira suíte mantém os 52 casos antigos; a segunda testa o modelo curricular, o acompanhamento, o planejamento, os formatos e os exemplos reais antigos.

O teste gráfico é separado: depois de compilar, execute `java -cp out TestesInterface` num desktop, ou `xvfb-run -a java -cp out TestesInterface` no Linux com Xvfb. Ele cria uma matriz pelos formulários reais, cadastra e aprova disciplinas, adiciona um correquisito, testa filtros, cenário, PGD 2 e Kahn/DFS. As capturas e o projeto temporário ficam em `.tmp/interface/` (ignorados pelo Git).

O aplicativo é local e offline. Não consulta normas, turmas ou dados oficiais na internet; não prevê oferta, vagas, datas, horários, duração variável ou conflitos semanais. Campos de fonte são referências cadastradas. Uma seleção válida significa apenas que as regras disponíveis foram satisfeitas.
