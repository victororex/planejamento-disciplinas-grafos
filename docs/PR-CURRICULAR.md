A aplicação Java Swing passa a trabalhar com matrizes curriculares de qualquer instituição ou curso, mantendo projetos livres e o estudo manual de grafos. O aluno pode cadastrar metadados, horas/créditos e status, visualizar períodos oficiais e simular cenários sem reescrever sua matriz ou histórico.

Correquisitos e árvores explícitas AND/OR são avaliados fora da adjacência de pré-requisitos: Teoria e Laboratório podem ser cursados juntos sem produzir um falso ciclo em Kahn/DFS. A disponibilidade lista pendências e distingue conclusões de reprovações e disciplinas em andamento.

A mudança inclui:

- Formulários dedicados, visões de grafo/matriz/planejamento/acompanhamento, busca e filtros visuais, foco em dependências e legenda de correquisitos.
- Cenários persistidos, aprovações hipotéticas, limites de carga/quantidade, validação e sugestões gulosas de combinações.
- PGD 2 completo com leitura dos quatro PGD 1 antigos, importação CSV/JSON com prévia independente do projeto e exportação com avisos de perda nos formatos parciais.
- Cinco catálogos fictícios, modelos de importação, guia de estudo, scripts Linux/macOS e atualização do executor Windows.

Validação realizada em OpenJDK 21.0.13 com `--release 17`: 52 testes originais e 41 curriculares aprovados, zero falhas. O fluxo gráfico com Xvfb passou, exercitando formulários, status, correquisito, busca, cenários, persistência, Kahn/DFS e importação inválida preservando o projeto. Saída real em `docs/resultado-testes.txt` e capturas em `docs/capturas/`.

A sugestão é heurística, não ótima; na alternativa OR, tenta o fecho da primeira alternativa. Oferta, vagas e horários não são inferidos. O cálculo de níveis continua matemático e usa apenas pré-requisitos simples. O aplicativo e os formatos não dependem de rede, servidor ou banco de dados.
