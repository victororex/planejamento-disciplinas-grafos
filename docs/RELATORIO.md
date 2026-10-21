# Planejamento Inteligente para o Gerenciamento da Oferta de Disciplinas

## 1. Introdução e descrição do problema

O planejamento acadêmico precisa respeitar as relações de pré-requisito entre disciplinas. Uma sequência aparentemente adequada pode ser inviável quando coloca uma disciplina antes da conclusão de suas dependências. Uma dependência circular, por sua vez, impede que qualquer sequência atenda a todas as restrições do grupo envolvido.

Este projeto implementa uma aplicação Java 17 com interface Swing para cadastrar disciplinas, construir suas relações, obter ordenações topológicas e identificar etapas de estudo simultâneo. O usuário pode editar livremente os exemplos e seus próprios grafos. A visualização de estados intermediários permite acompanhar as decisões dos algoritmos de Kahn e de busca em profundidade (DFS).

## 2. Modelagem como grafo direcionado

O modelo é um grafo G = (V, E), no qual cada vértice representa uma disciplina e cada aresta A → B indica que A precisa ser concluída antes de B. As arestas são direcionadas: inserir A → B não cria B → A.

A representação utiliza um mapa de listas de adjacência, implementadas com conjuntos para impedir arestas repetidas. A ordem de inserção é preservada para tornar os resultados reprodutíveis. Código, nome e período de referência pertencem ao cadastro. Os pré-requisitos são derivados das arestas de entrada, evitando armazenar a mesma informação em duas estruturas mutáveis. As posições dos cartões pertencem a uma estrutura visual separada e não influenciam os algoritmos.

O período cadastrado descreve uma referência acadêmica. A etapa recomendada é calculada pelas dependências e pode ser diferente. Para contar componentes fracas, uma DFS percorre uma projeção não direcionada, iniciando uma nova busca em cada vértice ainda não visitado, conforme a lógica da Aula 7 (pp. 3–6). Essa projeção não altera as arestas do modelo original. Componentes separadas podem ser ordenadas normalmente quando não há ciclos direcionados.

## 3. Algoritmo de Kahn

Kahn calcula os graus de entrada e coloca os vértices de grau zero em uma fila FIFO. A cada retirada, acrescenta o vértice à ordem parcial e reduz o grau residual de cada sucessor. Um sucessor entra na fila quando seu grau chega a zero. A regra garante que uma disciplina só seja processada depois de todos os seus pré-requisitos.

O programa preserva o grafo e altera apenas uma cópia dos graus. Isso representa a remoção lógica das arestas descrita na Aula 8 (p. 8), mantendo as relações disponíveis para novas análises. No exemplo acadêmico com A → B, A → C, B → D e C → E, a fila começa com A. Ao processá-la, B e C tornam-se elegíveis. O resultado produzido foi **A → B → C → D → E**, como no enunciado do projeto (p. 2).

Se a fila esvazia antes de processar |V| vértices, o grafo possui um ciclo. A ordem parcial permanece apenas como estado de estudo e não é aceita como solução completa.

## 4. Algoritmo DFS

A DFS usa três estados: branco para não visitado, cinza para ativo e preto para finalizado, conforme a Aula 9 (p. 10). Ao entrar em um vértice, marca-o como cinza e explora seus sucessores. Depois de examinar todos eles, marca o vértice como preto e registra sua finalização. Em um grafo acíclico, inverter a finalização produz uma ordenação topológica, como no código apresentado na Aula 9 (p. 15).

A implementação usa uma pilha explícita de quadros com vértice e iterador de sucessores. Ela reproduz a organização das chamadas recursivas e permite percorrer cadeias profundas sem depender do limite da pilha de chamadas da JVM. O caminho ativo é mostrado na interface.

No mesmo exemplo, a finalização foi **D → B → E → C → A**, e a ordem invertida foi **A → C → E → B → D**. D aparece antes de B na finalização porque a exploração de B aguarda seus sucessores. Após a inversão, B aparece antes de D, respeitando o pré-requisito.

## 5. Detecção de ciclos

Uma aresta da DFS para um vértice cinza alcança um ancestral ainda ativo. O trecho correspondente na pilha, seguido da aresta de retorno, constitui um ciclo real. Para A → B, B → C e C → A, a execução reconstruiu **A → B → C → A**. Nesse caso, não existe ordenação topológica e o planejamento é recusado.

O resíduo de Kahn pode incluir disciplinas que não pertencem a um ciclo. Por exemplo, ao acrescentar C → D ao ciclo anterior, D fica bloqueada sem participar da circularidade. Para classificar todos os participantes, o detector utiliza a ordem de finalização da DFS e uma segunda passagem no grafo transposto, seguindo Kosaraju. Como autodependências são rejeitadas, componentes fortemente conexas com mais de um vértice identificam os participantes de ciclos.

A interface destaca as arestas do primeiro ciclo testemunha e lista separadamente os participantes de qualquer ciclo e os vértices apenas bloqueados. O usuário pode corrigir uma relação e repetir as análises. Os testes confirmaram que remover D → A após sua inserção no exemplo acíclico restaura as duas ordenações válidas.

## 6. Planejamento por períodos

O planejamento associa a cada vértice a primeira etapa possível segundo as dependências. Fontes recebem etapa 1. Para qualquer outro vértice v, aplica-se etapa(v) = 1 + máximo das etapas de seus pré-requisitos.

O cálculo propaga essas restrições em ordem topológica. Assim, vários pré-requisitos são combinados pelo máximo, sem depender da posição arbitrária da disciplina na sequência. No exemplo 1, os resultados foram:

| Etapa recomendada | Disciplinas |
|---|---|
| 1 | A — Algoritmos |
| 2 | B — Estruturas de Dados; C — Banco de Dados |
| 3 | D — Engenharia de Software; E — Desenvolvimento Web |

Essa solução representa o menor número de etapas quando cada disciplina dura uma etapa e não há limite de simultaneidade. Ela não considera automaticamente vagas, carga horária, disponibilidade de professores ou conflitos de horários. O período cadastrado não é alterado pelo cálculo.

## 7. Análise de complexidade

Se V é a quantidade de vértices e E a quantidade de arestas, as duas ordenações têm tempo O(V + E), considerando acesso médio O(1) nos mapas e conjuntos.

Kahn inicializa V graus e percorre E arestas para contá-los. Cada vértice entra e sai da fila no máximo uma vez. As arestas são percorridas novamente ao processar suas origens. A DFS visita cada vértice uma vez e examina cada aresta uma vez, mantendo cores e informações da pilha.

O estado auxiliar essencial de Kahn e DFS ocupa O(V). Nesta implementação, a obtenção de uma visão independente da adjacência acrescenta uma cópia O(V + E), portanto a memória adicional total da execução inclui esse custo. A detecção completa de participantes por Kosaraju, o planejamento e a verificação de uma ordem também têm tempo O(V + E). O grafo transposto usado na classificação ocupa O(V + E).

Guardar T fotografias para navegação didática tem custo adicional O(T·V), além dos textos. Por isso o histórico é limitado por estimativa de estados e células armazenadas; entradas maiores continuam permitindo análise final. Esses custos e a renderização Graphics2D são separados da análise teórica dos algoritmos.

## 8. Comparação dos algoritmos

| Critério | Kahn | DFS |
|---|---|---|
| Escolha | Fonte com grau residual zero | Vértice branco alcançado na exploração |
| Estruturas | Graus e fila | Cores, pilha e finalização |
| Construção da ordem | Na retirada da fila | Inversão da finalização |
| Evidência de ciclo | Menos de V processados | Aresta para ancestral cinza |
| Facilidade de implementação | Iteração direta com fila e graus residuais | Exige coordenar cores, pilha ativa e inversão da finalização |
| Vantagem | Evidencia disciplinas elegíveis | Reconstrói um caminho circular |
| Limitação | Resíduo não identifica sozinho os participantes | Finalização é inválida como solução quando há ciclo |
| Aplicações | Escalonamento e dependências de compilação | Análise de fluxos e dependências |

As duas ordens obtidas no exemplo 1 são diferentes e corretas. Um verificador independente confirma que são permutações completas dos vértices e que, para toda aresta A → B, a posição de A é menor que a posição de B. A liberdade entre disciplinas sem dependência recíproca explica a existência de múltiplas respostas.

## 9. Resultados dos testes

A suíte foi executada em 20 de outubro de 2026 com JDK Eclipse Temurin 17.0.20.1, compilação UTF-8 e alvo Java 17. Foram executados **52 casos nomeados, com 52 aprovações e nenhuma falha**. A saída integral está em `resultado-testes.txt`.

Os casos abrangem cadastro, validações, edição, remoção, grafos vazios, isoladas, DAGs, ciclos de dois e três vértices, múltiplas componentes, verificador independente, planejamento, preservação do grafo, consistência de históricos, geometria e persistência válida e inválida. Os exercícios da Aula 7 (p. 7) foram usados como testes adicionais: a projeção K4 produziu uma componente, e a projeção K3 com um vértice isolado produziu duas. Dentro de três casos de maior abrangência foram examinados 100 DAGs aleatórios, 40 grafos cuja classificação de ciclos foi comparada com alcançabilidade independente e uma cadeia com 10000 vértices. Essas amostras não são contadas como testes nomeados adicionais.

O gerador aleatório usa sementes fixas. O cenário com ciclo e uma disciplina bloqueada confirmou a diferença entre participar da circularidade e sofrer suas consequências. O salvamento e a leitura preservaram caracteres acentuados, períodos, posições e relações. Arquivos malformados foram rejeitados.

A primeira execução encontrou uma restrição de movimentação de arquivos no diretório temporário isolado do ambiente. A suíte foi repetida usando a pasta temporária `.tmp` do workspace; todos os casos passaram. Os limites e resultados da validação gráfica são registrados separadamente em `VALIDACAO.md`, sem confundir testes lógicos automatizados com cliques manuais.

## 10. Aplicações profissionais

A mesma modelagem pode apoiar sequenciamento de tarefas, pipelines de processamento, ordem de compilação de módulos e análise de dependências entre entregas. Fontes indicam tarefas inicialmente disponíveis, enquanto ciclos revelam restrições incompatíveis. As camadas podem indicar paralelismo quando não há conflitos adicionais de recursos.

Uma aplicação profissional precisa combinar esse planejamento lógico com durações, capacidade, calendários e prioridades. As ordenações topológicas constituem uma base verificável, mas não resolvem isoladamente todas as restrições de um sistema real de oferta acadêmica.

## 11. Conclusão

O sistema implementa duas estratégias manuais de ordenação topológica, verificação independente, reconstrução de ciclos e planejamento por camadas. A separação entre cadastro, grafo, posições e estados de execução facilita a compreensão do código e evita que a simulação modifique as dependências originais. Os resultados dos testes sustentam a correção lógica nos cenários examinados, e o modo de estudo permite observar as regras aplicadas a cada estado.

O trabalho foi conferido diretamente com o PDF do Projeto Prático 1 (pp. 1–4) e com as Aulas 7, 8 e 9 da professora Silvia Brandão. A comparação contempla os critérios de estratégia, estruturas auxiliares, ciclos, facilidade de implementação e aplicações exigidos no enunciado (p. 3). A interface usa tema escuro, preservando a distinção visual e textual dos estados. Os materiais originais não integram a distribuição.

Materiais consultados: BRANDÃO, Silvia. *Projeto Prático 1 — Algoritmos em Grafos*, 2026.2, 4 p.; *Aula 7 — Contagem de Componentes Conexos*, 7 p.; *Aula 8 — Ordenação Topológica*, 11 p.; *Aula 9 — Detecção de Ciclos*, 17 p. PDFs fornecidos pelo estudante; consulta em 20 out. 2026.
