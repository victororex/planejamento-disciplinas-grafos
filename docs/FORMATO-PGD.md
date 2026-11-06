# Formatos de projeto PGD 2 e PGD 1


## PGD 2 — formato atual

Novos salvamentos usam `PGD<TAB>2` na primeira linha, seguido de um objeto JSON versão 2 em UTF-8. Quebras LF e CRLF são aceitas após o cabeçalho. O esquema é exatamente o [catálogo documentado em IMPORTACAO.md](IMPORTACAO.md): matriz, disciplinas, posições, pré-requisitos, regras adicionais e cenários pessoais.

A escrita sempre inclui todas as posições e todos os dados cadastrados. Os resultados transitórios dos algoritmos continuam sendo recalculados. Não há objetos serializados ou conteúdo executável. Limite total de 8 MiB. Diferentemente da importação de catálogo JSON com prévia, PGD 2 rejeita campos desconhecidos para evitar que a abertura e o salvamento silenciosamente descartem dados de uma versão futura.

A leitura de PGD 1 permanece disponível. Abra qualquer um dos quatro exemplos antigos em `exemplos/exemplo-N.pgd`; nomes, códigos, relações e posições são recuperados. Metadados ficam vazios, horas/créditos não informados e status inicia como Não cursada. Salvar passa a produzir PGD 2; para conservar o arquivo legado, escolha outro nome. A interface confirma antes de sobrescrever.

**Exportar PGD 1 (legado)** é uma operação separada. A tela avisa que metadados, horas, créditos, categorias, status, descrições, correquisitos, regras e cenários serão perdidos na cópia exportada. Se houver disciplina sem período informado, a exportação legada é recusada: nenhum período é inventado. A API também exige consentimento explícito (`aceitarPerda=true`). Prefira JSON ou PGD 2 para preservar tudo.

## PGD 1 — leitura compatível e exportação explícita

Arquivo textual UTF-8, extensão `.pgd`, com campos separados por TAB (`\t`) e uma linha por registro. A leitura aceita quebras LF e CRLF. A escrita usa as quebras da plataforma. A primeira linha é obrigatoriamente:

```text
PGD<TAB>1
```

`<TAB>` representa um caractere de tabulação real, não esse texto literal.

## Disciplina

```text
D<TAB>codigoBase64<TAB>nomeBase64<TAB>periodo<TAB>x<TAB>y
```

- Código e nome são codificados em UTF-8 e depois Base64 padrão. Base64 não é criptografia.
- O código preserva maiúsculas e minúsculas; `A` e `a` são distintos. Aceita 1 a 24 letras, números, `_` e `-`.
- O nome tem 1 a 120 caracteres, sem caracteres de controle.
- O período cadastrado é inteiro, entre 1 e 100.
- `x` e `y` são as coordenadas do centro do cartão em unidades do grafo, sem aplicar o zoom. Números decimais usam ponto, independentemente do idioma do sistema.
- Valores finitos: `90 ≤ x ≤ 10000` e `45 ≤ y ≤ 10000`.

Exemplo real, com as tabulações representadas por espaços apenas para leitura:

```text
D  QQ==  QWxnb3JpdG1vcw==  1  140.0  235.0
```

Esse registro representa `A — Algoritmos`, período cadastrado 1, centro (140, 235).

## Relação de pré-requisito

```text
R<TAB>origemBase64<TAB>destinoBase64
```

O registro `R QQ== Qg==` representa A → B. Os dois códigos devem existir. Arestas duplicadas e autodependências são inválidas. Dependências circulares entre disciplinas distintas são válidas como **dados editáveis**, mas impedem ordenação e planejamento.

## Ordem e validação

A escrita legada preserva ordem de cadastro, depois ordem de origens e seus sucessores. A leitura aceita relações antes de cadastros, validando todas as referências após ler os registros. O leitor legado não aceita linhas vazias, comentários, registros desconhecidos, campos extras ou versões diferentes.

Limites de leitura: 8 MiB por arquivo, 2000 disciplinas, 20000 relações e 4096 caracteres por linha. Textos Base64 e UTF-8 são validados. Erros de cadastro ou relações informam o número da linha; erros do sistema de arquivos ou codificação são comunicados pela interface. O projeto é construído em memória separada e só substitui o atual se toda a validação passar.

O salvamento escreve primeiro um arquivo temporário completo na mesma pasta. Tenta substituir o destino por movimentação atômica; se o provedor não a suporta ou a recusa, usa movimentação com substituição. Erros reais são propagados e a interface mantém o projeto disponível. Em sistemas sem substituição atômica, não há garantia contra interrupção de energia durante a substituição.

Não são persistidos cores, fila, pilha, graus residuais, ordenações, resultados de ciclos nem etapas recomendadas. Eles são recalculados a partir do grafo. O arquivo não contém código executável ou objetos serializados.
