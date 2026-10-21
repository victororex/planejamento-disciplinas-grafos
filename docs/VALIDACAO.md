# Validação e roteiro de apresentação

## Ambiente e evidências

Validação realizada em **20/10/2026**, no Windows, com Eclipse Temurin JDK **17.0.20.1+1**, alvo Java 17 e codificação UTF-8. Não foram encontradas dependências externas no projeto.

- `compilar.bat`: compilação concluída com `--release 17 -Xlint:all`, sem avisos.
- `testar.bat`: **50 testes aprovados; 0 falhas**. Saída integral em [resultado-testes.txt](resultado-testes.txt).
- `gerar-jar.bat`: JAR executável produzido em `dist/planejamento-disciplinas.jar` (artefato local de build, ignorado pelo Git).
- Quatro exemplos `.pgd` gerados pelo próprio programa.

Os 50 testes nomeados incluem verificações internas de 100 DAGs aleatórios, 40 grafos com classificação de ciclos conferida por alcançabilidade independente e uma cadeia com 10000 vértices. As sementes são fixas e a suíte retorna código de saída diferente de zero se houver falha.

## Conferência da interface real

A aplicação foi aberta e inspecionada no desktop Windows. Foram observados cadastros manuais de disciplinas e uma conexão feita pelo usuário durante a sessão. Para a conferência final, foi usado o exemplo editável com cinco disciplinas.

| Ação conferida na interface | Resultado observado |
|---|---|
| Abrir o exemplo 1 por arquivo | Cinco disciplinas e quatro dependências recuperadas |
| Executar Kahn | 11 estados; ordem A → B → C → D → E |
| Avançar, voltar e ir ao fim | Estados, fila, cores e ordem parcial atualizados sem mudar as dependências |
| Executar DFS | 16 estados; finalização D → B → E → C → A; ordem A → C → E → B → D |
| Consultar comparação | Ambas as ordens verificadas como válidas |
| Planejar períodos | Etapas A / B,C / D,E e rótulos calculados no grafo |
| Inserir D → A pelo modo Conectar | Grafo passa a ter cinco dependências e as análises anteriores são invalidadas |
| Detectar ciclos | Ambos os métodos detectam o problema; A → B → D → A destacado em vermelho |
| Distinguir participantes e bloqueadas | A, B, D em ciclo; C e E apenas bloqueadas |
| Remover D → A pelo diálogo | Grafo retorna a quatro dependências |
| Comparar novamente | As duas ordens voltam a ser válidas |
| Salvar o exemplo restaurado | Confirmação de substituição e mensagem de salvamento concluído |
| Reabrir o arquivo salvo | Grafo recuperado com cinco vértices e quatro arestas |

As [capturas em `capturas/`](capturas/) foram obtidas da janela real em execução. Não são imagens geradas, desenhos de referência ou simulações da aparência. O documento não apresenta a inspeção visual como um teste automatizado completo de todos os cliques e combinações possíveis.

Durante a conferência, foi aumentado o espaço reservado à execução e separada a tabela de estados da área de fila/pilha com divisor ajustável. A barra lateral também recebeu rolagem. Os campos acadêmicos permanecem distintos das etapas calculadas. A inspeção foi feita principalmente em uma janela de aproximadamente 1436 × 933 pixels; outras resoluções e escalas de DPI devem seguir o roteiro abaixo.

## Roteiro reproduzível para apresentação

1. Abra `executar.bat`. Mostre que a aplicação começa vazia.
2. Em **Criar**, clique na área de desenho e cadastre A — Algoritmos, período 1. Cadastre B — Estruturas de Dados e C — Banco de Dados, período 2; D — Engenharia de Software e E — Desenvolvimento Web, período 3. Alternativamente, carregue o exemplo 1 para começar a demonstração pelos algoritmos.
3. Em **Conectar**, clique A, depois B; repita para A → C, B → D e C → E. Leia a mensagem da barra inferior para confirmar a direção.
4. Em **Selecionar**, arraste B. Observe as arestas acompanhando o cartão e suas pontas na borda do destino. Use duplo clique para editar o nome e conferir os dados.
5. Execute Kahn. Mostre A na fila, seu grau residual zero e a redução dos graus de B e C. Avance, volte e finalize.
6. Execute DFS. Mostre a pilha A, B, D e explique que D termina antes de B na finalização. Finalize e explique a inversão.
7. Abra a comparação. Confirme as duas ordenações válidas e explique a liberdade entre disciplinas independentes.
8. Planeje os períodos. Explique que o período cadastrado não é o resultado do algoritmo e que a primeira etapa possível depende do pré-requisito mais tardio.
9. Conecte D → A. Execute **Detectar ciclos**. Identifique A → B → D → A; mostre C e E apenas bloqueadas. Confirme que não há planejamento válido.
10. Remova D → A pela barra lateral e compare de novo.
11. Salve um arquivo `.pgd`, abra-o e confira as posições e relações. Tente abrir um arquivo de texto inválido e verifique que o grafo atual permanece disponível.
12. Carregue os exemplos 2, 3 e 4 para mostrar múltiplas ordens, o ciclo A → B → C → A e componentes separadas com uma isolada.
13. Redimensione a janela, ajuste os divisores, teste a rolagem lateral, os tooltips, o zoom e **Ajustar**. Desative e reative o modo de estudo.
14. Termine na aba **Complexidade**: explique V, E, O(V + E) e por que o histórico e o desenho possuem custos adicionais separados.

## Limites da validação

Os testes lógicos não constituem uma prova formal para todos os grafos. A interface não foi examinada em todos os sistemas operacionais, escalas de DPI ou resoluções. O planejamento não resolve vagas, horários e limites de carga. A navegação guarda estados apenas dentro do limite documentado no README; grafos maiores conservam a análise final.
