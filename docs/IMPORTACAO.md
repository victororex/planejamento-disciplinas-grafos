# Importação e exportação offline

Use **Matriz e arquivos → Importar CSV / JSON / PGD**. A prévia mostra contagens, erros e avisos (incluindo ciclos e campos desconhecidos). Escolha substituir o projeto, abrir em outra janela ou cancelar. Projetos com alterações pedem confirmação antes de descarte. Nenhuma importação mescla versões silenciosamente. Uma falha preserva o projeto aberto.

Cada arquivo/janela representa **uma versão de matriz**. Use nomes de arquivos distintos, informe instituição, curso e versão e salve os projetos separadamente. A aplicação inicia livre, sem instituição padrão. Os arquivos em `exemplos/curriculares/` são fictícios e opcionais.

## Planilha CSV

Modelo: [disciplinas.csv](../exemplos/modelos/disciplinas.csv).

Arquivo UTF-8 (BOM opcional), separador vírgula ou ponto e vírgula detectado pelo cabeçalho. Nomes de colunas são exatos, sem acentos. Campos entre aspas duplas aceitam delimitadores e quebras de linha; escape aspas como `""`. Para números, prefira ponto decimal; CSV com ponto e vírgula também aceita vírgula decimal.

| Coluna | Conteúdo |
| --- | --- |
| `codigo` | Obrigatório, único, 1–24 letras/números, `_` ou `-` |
| `nome` | Obrigatório, 1–120 caracteres, sem controles |
| `periodo` | Inteiro 1–100; vazio = não informado |
| `horas`, `creditos` | Número de 0 a 1.000.000; vazio = desconhecido |
| `categoria` | Texto livre, inclusive categoria personalizada |
| `status` | `NAO_CURSADA`, `EM_ANDAMENTO`, `APROVADA`, `REPROVADA` ou `DISPENSADA` |
| `descricao`, `observacoes`, `oferta` | Textos opcionais |

Somente código e nome são obrigatórios no cabeçalho. Status vazio assume **Não cursada**, que é um estado inicial do acompanhamento, não um dado oficial da instituição. Demais campos ausentes não recebem dados acadêmicos inventados. Colunas extras aparecem na prévia e não são incorporadas. Erros por registro (duplicados, nomes, valores) são acumulados.

CSV contém somente disciplinas: não transporta identificação da matriz, relações, posições ou cenários. A exportação mostra esse aviso. Use JSON ou PGD 2 para cópias completas. Não use o CSV como backup completo. Ao abrir dados de fontes não confiáveis em editores de planilhas, use a importação de texto do editor para evitar que células sejam interpretadas como fórmulas.

## Catálogo JSON versão 2

Modelo completo e legível: [catalogo.json](../exemplos/modelos/catalogo.json).

Raiz: `versao: 2`, `disciplinas: [...]` e, opcionalmente, `matriz`, `prerequisitos`, `requisitos`, `planos`. Não há downloads, resolução de URLs ou código executado durante a leitura. `fonte` é texto de referência, nunca buscado automaticamente.

`matriz` possui: `instituicao`, `curso`, `codigoCurso`, `versao`, `ano`, `campus`, `turno`, `periodos`, `observacoes`, `fonte`. Strings ausentes ficam vazias; ano e quantidade de períodos ausentes ficam `null`. Ano, quando informado, é 1–9999. Quantidade de períodos, 1–100.

Cada disciplina possui os mesmos campos do CSV e pode incluir `x` e `y`, ambos obrigatórios quando um deles for informado. Sem coordenadas, é gerada apenas uma posição visual; não são inferidos períodos, horas ou créditos. `periodo`, `horas` e `creditos` desconhecidos são `null` na exportação. `descricao`, `observacoes` e `oferta` são opcionais.

`prerequisitos` contém pares `[origem, destino]`: `[["P1", "P2"]]` significa P1 concluída antes de P2. Esses pares são a adjacência usada por Kahn e DFS. Todas as referências devem existir. Relações repetidas e autorreferências são rejeitadas; ciclos entre códigos distintos são aceitos e avisados como dados editáveis.

`requisitos` é um objeto com código de destino e lista de regras. Todas as regras de topo são combinadas com E:

```json
{
  "PROJ": [
    {"tipo": "OR", "itens": [
      {"tipo": "CONCLUIDA", "codigo": "P2"},
      {"tipo": "CONCLUIDA", "codigo": "LAB"}
    ]},
    {"tipo": "HORAS", "minimo": 60},
    {"tipo": "CREDITOS", "minimo": 4},
    {"tipo": "ATIVIDADE", "codigo": "ATIV"}
  ],
  "P2": [{"tipo": "CORREQUISITO", "codigo": "LAB"}]
}
```

| Tipo | Regra |
| --- | --- |
| `CONCLUIDA` | Exige código aprovado ou dispensado anteriormente |
| `CORREQUISITO` | Aceita código concluído **ou** incluído no mesmo período planejado |
| `ATIVIDADE` | Exige conclusão da atividade cadastrada como disciplina, indicada por código |
| `HORAS`, `CREDITOS` | Exige `minimo` conhecido já concluído |
| `AND`, `OR` | Exige lista não vazia em `itens`, com regras aninhadas |

Não misture campos: regras de código não aceitam mínimo/filhos; regras de mínimo não aceitam código/filhos; grupos não aceitam código/mínimo. Ausência de mínimo não significa zero. Profundidade de regras limitada a 16 níveis. As árvores são extensíveis por novos tipos no modelo e no serviço; regras textuais desconhecidas não são interpretadas como aprovação.

Um correquisito que exige **conclusão anterior** deve ser cadastrado como `CONCLUIDA`; caso seja uma precedência obrigatória simples, prefira cadastrá-lo como pré-requisito no grafo. Regras alternativas não são transformadas em arestas comuns, pois isso alteraria sua lógica.

`planos` contém cenários com `nome` único, `periodo` (texto livre, sem datas inventadas), `maxHoras`, `maxDisciplinas`, `selecionadas` (códigos) e `aprovacoesSimuladas` (códigos). Limites `null` significam sem limite. Cenários podem ser salvos com conflitos para comparação; a tela sempre revalida seu conteúdo.

Campos desconhecidos no JSON são avisados na prévia e omitidos após a importação confirmada. JSON malformado, chaves repetidas, números não finitos, Unicode inválido e referências inexistentes rejeitam o arquivo. O primeiro erro estrutural interrompe a análise JSON; não se trata de um reparador de arquivos corrompidos.

## Limites e preservação

Leitura até 8 MiB, 2.000 disciplinas e 20.000 pré-requisitos. Listas JSON são limitadas a 20.000 itens; profundidade JSON a 32 níveis. PGD 1 mantém o limite legado de 4.096 caracteres por linha. Textos adicionais aceitam até 10.000 caracteres. Coordenadas ficam entre 90–10.000 em x e 45–10.000 em y.

A importação constrói outro projeto em memória. Não escreve no projeto aberto nem na origem. Exportações pedem confirmação de sobrescrita e gravam via arquivo temporário na pasta de destino. Não há desserialização Java. Os arquivos não incluem dados pessoais de terceiros ou documentação de universidades.
