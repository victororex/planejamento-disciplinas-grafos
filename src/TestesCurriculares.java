import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Regressões do modelo universal, importação e planejamento. Sem dependências externas. */
public final class TestesCurriculares {
  @FunctionalInterface
  interface Caso {
    void executar() throws Exception;
  }

  private static int passou, falhou;
  private static final ServicoAcademico SERVICO = new ServicoAcademico();

  private static void teste(String nome, Caso c) {
    try {
      c.executar();
      passou++;
      System.out.println("[OK] " + nome);
    } catch (Throwable e) {
      falhou++;
      System.err.println("[FALHA] " + nome);
      e.printStackTrace();
    }
  }

  private static void verifica(boolean b) {
    if (!b) throw new AssertionError("Condição não satisfeita");
  }

  private static void igual(Object a, Object b) {
    if (!Objects.equals(a, b)) throw new AssertionError(a + " != " + b);
  }

  private static void rejeita(Caso c) throws Exception {
    try {
      c.executar();
    } catch (IllegalArgumentException | java.io.IOException e) {
      return;
    }
    throw new AssertionError("Entrada inválida foi aceita");
  }

  private static GrafoDisciplinas grafo() {
    GrafoDisciplinas g = new GrafoDisciplinas();
    for (String c : List.of("A", "B", "C", "D"))
      g.adicionarDisciplina(disc(c, 60.0, 4.0, StatusAcademico.NAO_CURSADA));
    return g;
  }

  private static Disciplina disc(String c, Double h, Double cr, StatusAcademico st) {
    return new Disciplina(
        c,
        "Disciplina " + c,
        1,
        h,
        cr,
        "Categoria personalizada",
        "Descrição",
        "Obs.",
        "Não informada",
        st);
  }

  private static ProjetoAcademico projeto(GrafoDisciplinas g) {
    Map<String, VerticeVisual> p = new LinkedHashMap<>();
    int i = 0;
    for (String c : g.getCodigos()) p.put(c, new VerticeVisual(140 + 100 * (i++ % 30), 120));
    return new ProjetoAcademico(
        g,
        p,
        new MatrizCurricular(
            "Universidade Exemplo",
            "Curso livre",
            "C1",
            "2026-1",
            2026,
            "Campus X",
            "",
            8,
            "Fictícia",
            "https://example.org"),
        List.of());
  }

  private static PlanoAcademico plano(
      Set<String> selecionadas, Set<String> feitas, Double horas, Integer max) {
    return new PlanoAcademico("Cenário", "Futuro", horas, max, selecionadas, feitas);
  }

  private static Path arquivo(String extensao, String texto) throws Exception {
    Path p = Files.createTempFile("curriculo-", extensao);
    Files.writeString(p, texto, StandardCharsets.UTF_8);
    return p;
  }

  public static void main(String[] args) throws Exception {
    teste(
        "Instituição, curso, versão, campus e projeto livre",
        () -> {
          var m = projeto(grafo()).matriz();
          igual("Universidade Exemplo", m.instituicao());
          igual("2026-1", m.versao());
          verifica(MatrizCurricular.livre().instituicao().isEmpty());
          rejeita(() -> new MatrizCurricular("", "", "", "", 0, "", "", 101, "", ""));
        });
    teste(
        "Múltiplas versões não compartilham modelo",
        () -> {
          var a = projeto(grafo());
          var b = projeto(grafo());
          a.grafo().removerDisciplina("A");
          verifica(b.grafo().contem("A"));
        });
    teste(
        "Horas, créditos, categorias personalizadas e campos desconhecidos",
        () -> {
          Disciplina d = disc("A", null, null, StatusAcademico.NAO_CURSADA);
          igual(null, d.getCargaHoraria());
          igual(null, d.getCreditos());
          igual("Categoria personalizada", d.getCategoria());
          rejeita(() -> disc("A", Double.NaN, 4.0, StatusAcademico.NAO_CURSADA));
          rejeita(() -> disc("A", 60.0, -1.0, StatusAcademico.NAO_CURSADA));
        });
    teste(
        "Período desconhecido não é inventado",
        () -> {
          Disciplina d =
              new Disciplina("X", "X", 0, null, null, "", "", "", "", StatusAcademico.NAO_CURSADA);
          igual(0, d.getPeriodoReferencia());
          rejeita(() -> new Disciplina("X", "X", 0));
        });
    teste(
        "Pré-requisito libera apenas sucessora imediata",
        () -> {
          var g = grafo();
          g.adicionarDependencia("A", "B");
          g.adicionarDependencia("B", "C");
          g.editarDisciplina("A", g.obterDisciplina("A").comStatus(StatusAcademico.APROVADA));
          verifica(SERVICO.avaliar(g, "B", Set.of(), Set.of()).disponivel());
          verifica(!SERVICO.avaliar(g, "C", Set.of(), Set.of()).disponivel());
        });
    teste(
        "Reprovação não satisfaz pré-requisito",
        () -> {
          var g = grafo();
          g.adicionarDependencia("A", "B");
          g.editarDisciplina("A", g.obterDisciplina("A").comStatus(StatusAcademico.REPROVADA));
          verifica(!SERVICO.avaliar(g, "B", Set.of(), Set.of()).disponivel());
        });
    teste(
        "Em andamento não é aprovada nem nova matrícula",
        () -> {
          var g = grafo();
          g.adicionarDependencia("A", "B");
          g.editarDisciplina("A", g.obterDisciplina("A").comStatus(StatusAcademico.EM_ANDAMENTO));
          verifica(!SERVICO.avaliar(g, "B", Set.of(), Set.of()).disponivel());
          verifica(!SERVICO.avaliar(g, "A", Set.of(), Set.of()).disponivel());
        });
    teste(
        "Dispensa satisfaz requisito de conclusão",
        () -> {
          var g = grafo();
          g.adicionarDependencia("A", "B");
          g.editarDisciplina("A", g.obterDisciplina("A").comStatus(StatusAcademico.DISPENSADA));
          verifica(SERVICO.avaliar(g, "B", Set.of(), Set.of()).disponivel());
        });
    teste(
        "Todos os motivos de bloqueio são listados",
        () -> {
          var g = grafo();
          g.adicionarDependencia("A", "C");
          g.adicionarDependencia("B", "C");
          var r = SERVICO.avaliar(g, "C", Set.of(), Set.of());
          igual(2, r.motivos().size());
          verifica(r.motivos().toString().contains("A") && r.motivos().toString().contains("B"));
        });
    teste(
        "Correquisito precisa ser concluído ou simultâneo",
        () -> {
          var g = grafo();
          g.definirRequisitos("B", List.of(Requisito.disciplina(Requisito.Tipo.CORREQUISITO, "A")));
          verifica(!SERVICO.avaliar(g, "B", Set.of(), Set.of()).disponivel());
          verifica(SERVICO.avaliar(g, "B", Set.of(), Set.of("A", "B")).disponivel());
          verifica(SERVICO.avaliar(g, "B", Set.of("A"), Set.of()).disponivel());
        });
    teste(
        "Correquisitos mútuos não criam ciclo topológico",
        () -> {
          var g = grafo();
          g.definirRequisitos("A", List.of(Requisito.disciplina(Requisito.Tipo.CORREQUISITO, "B")));
          g.definirRequisitos("B", List.of(Requisito.disciplina(Requisito.Tipo.CORREQUISITO, "A")));
          verifica(!new AlgoritmoKahn().executar(g).possuiCiclo());
          verifica(!new AlgoritmoDFS().executar(g).possuiCiclo());
          verifica(!new DetectorCiclos().analisar(g).possuiCiclo());
          verifica(SERVICO.validar(g, plano(Set.of("A", "B"), Set.of(), 120.0, 2)).valido());
        });
    teste(
        "Exigência anterior não aceita simultaneidade",
        () -> {
          var g = grafo();
          g.definirRequisitos("B", List.of(Requisito.disciplina(Requisito.Tipo.CONCLUIDA, "A")));
          verifica(!SERVICO.validar(g, plano(Set.of("A", "B"), Set.of(), null, null)).valido());
        });
    teste(
        "Mínimos de horas e créditos consideram só conclusões",
        () -> {
          var g = grafo();
          g.definirRequisitos(
              "C",
              List.of(
                  Requisito.minimo(Requisito.Tipo.HORAS, 60),
                  Requisito.minimo(Requisito.Tipo.CREDITOS, 4)));
          igual(2, SERVICO.avaliar(g, "C", Set.of(), Set.of()).motivos().size());
          verifica(SERVICO.avaliar(g, "C", Set.of("A"), Set.of()).disponivel());
        });
    teste(
        "Valores desconhecidos não satisfazem mínimos",
        () -> {
          var g = grafo();
          g.editarDisciplina("A", disc("A", null, null, StatusAcademico.APROVADA));
          g.definirRequisitos("B", List.of(Requisito.minimo(Requisito.Tipo.HORAS, 1)));
          verifica(!SERVICO.avaliar(g, "B", Set.of(), Set.of()).disponivel());
        });
    teste(
        "Alternativas OR e conjunção AND explícitas",
        () -> {
          var g = grafo();
          g.definirRequisitos(
              "D",
              List.of(
                  Requisito.grupo(
                      Requisito.Tipo.AND,
                      Requisito.grupo(
                          Requisito.Tipo.OR,
                          Requisito.disciplina(Requisito.Tipo.CONCLUIDA, "A"),
                          Requisito.disciplina(Requisito.Tipo.CONCLUIDA, "B")),
                      Requisito.disciplina(Requisito.Tipo.ATIVIDADE, "C"))));
          verifica(!SERVICO.avaliar(g, "D", Set.of("A"), Set.of()).disponivel());
          verifica(SERVICO.avaliar(g, "D", Set.of("B", "C"), Set.of()).disponivel());
        });
    teste(
        "Árvore inválida e autorreferência são rejeitadas",
        () -> {
          var g = grafo();
          rejeita(() -> Requisito.grupo(Requisito.Tipo.OR));
          rejeita(
              () ->
                  g.definirRequisitos(
                      "A", List.of(Requisito.disciplina(Requisito.Tipo.CORREQUISITO, "A"))));
          rejeita(
              () ->
                  g.definirRequisitos(
                      "A", List.of(Requisito.disciplina(Requisito.Tipo.CONCLUIDA, "INEXISTENTE"))));
        });
    teste(
        "Dependências diretas, indiretas e ramificadas",
        () -> {
          var g = grafo();
          g.adicionarDependencia("A", "B");
          g.adicionarDependencia("A", "C");
          g.adicionarDependencia("C", "D");
          igual(Set.of("B", "C", "D"), g.alcance("A", false));
          igual(Set.of("A", "C"), g.alcance("D", true));
          igual(Set.of("B", "C"), g.obterVizinhos("A"));
        });
    teste(
        "Alcance termina mesmo com ciclo",
        () -> {
          var g = grafo();
          g.adicionarDependencia("A", "B");
          g.adicionarDependencia("B", "A");
          igual(Set.of("B"), g.alcance("A", false));
        });
    teste(
        "Progresso inclui aproveitadas e não inclui andamento",
        () -> {
          var g = grafo();
          g.editarDisciplina("A", disc("A", 60.0, 4.0, StatusAcademico.APROVADA));
          g.editarDisciplina("B", disc("B", null, null, StatusAcademico.DISPENSADA));
          g.editarDisciplina("C", disc("C", 60.0, 4.0, StatusAcademico.EM_ANDAMENTO));
          var p = SERVICO.progresso(g);
          igual(1, p.aprovadas());
          igual(1, p.dispensadas());
          igual(2, p.pendentes());
          igual(1, p.andamento());
          igual(50.0, p.percentual());
          igual(60.0, p.horas());
          igual(4.0, p.creditos());
          igual(1, p.cargasDesconhecidas());
        });
    teste(
        "Simular aprovação não altera histórico nem período",
        () -> {
          var g = grafo();
          g.adicionarDependencia("A", "B");
          var antes = g.obterDisciplina("A");
          verifica(SERVICO.validar(g, plano(Set.of("B"), Set.of("A"), 60.0, 1)).valido());
          igual(antes, g.obterDisciplina("A"));
        });
    teste(
        "Limites de carga e quantidade",
        () -> {
          var g = grafo();
          verifica(!SERVICO.validar(g, plano(Set.of("A", "B"), Set.of(), 100.0, 2)).valido());
          verifica(!SERVICO.validar(g, plano(Set.of("A", "B"), Set.of(), 120.0, 1)).valido());
          verifica(SERVICO.validar(g, plano(Set.of("A", "B"), Set.of(), 120.0, 2)).valido());
        });
    teste(
        "Carga desconhecida impede afirmar limite satisfeito",
        () -> {
          var g = grafo();
          g.editarDisciplina("A", disc("A", null, null, StatusAcademico.NAO_CURSADA));
          var r = SERVICO.validar(g, plano(Set.of("A"), Set.of(), 60.0, 1));
          verifica(!r.valido());
          igual(null, r.horas());
        });
    teste(
        "Sugestão respeita requisitos, limites e pares simultâneos",
        () -> {
          var g = ExemplosCurriculares.criar(3).grafo();
          var p = SERVICO.sugerir(g, plano(Set.of(), Set.of(), 120.0, 2));
          igual(Set.of("D1", "D2"), p.selecionadas());
          verifica(SERVICO.validar(g, p).valido());
        });
    teste(
        "Simultâneo bloqueado invalida o conjunto",
        () -> {
          var g = grafo();
          g.definirRequisitos("B", List.of(Requisito.disciplina(Requisito.Tipo.CORREQUISITO, "A")));
          g.adicionarDependencia("C", "A");
          verifica(!SERVICO.validar(g, plano(Set.of("A", "B"), Set.of(), null, null)).valido());
        });
    teste(
        "Renomear preserva regras e copiar mantém isolamento",
        () -> {
          var g = grafo();
          g.definirRequisitos("B", List.of(Requisito.disciplina(Requisito.Tipo.CORREQUISITO, "A")));
          var copia = g.copiar();
          g.editarDisciplina("A", disc("X", 60.0, 4.0, StatusAcademico.NAO_CURSADA));
          igual(Set.of("X"), g.requisitos("B").get(0).referencias());
          igual(Set.of("A"), copia.requisitos("B").get(0).referencias());
          g.removerDisciplina("X");
          verifica(g.requisitos("B").isEmpty());
        });
    teste(
        "CSV com BOM, vírgula, ponto e vírgula, aspas e multiline",
        () -> {
          Path p =
              arquivo(
                  ".csv",
                  "\ufeffcodigo;nome;periodo;horas;creditos;categoria\r\n"
                      + "A;\"Disciplina; A\";;60;4;Personalizada\r\n");
          try {
            var v = new ImportadorMatrizes().prever(p);
            verifica(v.valida());
            igual(0, v.projeto().grafo().obterDisciplina("A").getPeriodoReferencia());
            igual("Disciplina; A", v.projeto().grafo().obterDisciplina("A").getNome());
          } finally {
            Files.delete(p);
          }
          igual(
              "linha\ncom \"aspas\"",
              ImportadorMatrizes.lerCsv("a,\"linha\ncom \"\"aspas\"\"\"\n", ',').get(0).get(1));
        });
    teste(
        "CSV inválido relata duplicados e valores inválidos",
        () -> {
          Path p = arquivo(".csv", "codigo,nome,horas\nA,A,60\nA,Outro,40\nB,B,-1\n");
          try {
            var v = new ImportadorMatrizes().prever(p);
            verifica(!v.valida());
            igual(2, v.erros().size());
          } finally {
            Files.delete(p);
          }
        });
    teste(
        "CSV campo desconhecido aparece na prévia",
        () -> {
          Path p = arquivo(".csv", "codigo,nome,colunaNova\nA,A,x\n");
          try {
            var v = new ImportadorMatrizes().prever(p);
            verifica(v.valida());
            verifica(v.avisos().toString().contains("colunaNova"));
          } finally {
            Files.delete(p);
          }
        });
    teste(
        "JSON completo preserva disciplina e metadados",
        () -> {
          var p = projeto(grafo());
          var avisos = new ArrayList<String>();
          var q = new CatalogoJson(avisos).ler(Json.ler(Json.escrever(CatalogoJson.escrever(p))));
          igual(p.matriz(), q.matriz());
          igual(p.grafo().getDisciplinas(), q.grafo().getDisciplinas());
          verifica(avisos.isEmpty());
        });
    teste(
        "JSON desconhecido e ciclos são mostrados antes de importar",
        () -> {
          Path p =
              arquivo(
                  ".json",
                  Json.escrever(CatalogoJson.escrever(ExemplosCurriculares.criar(4)))
                      .replaceFirst("\\{", "{\"desconhecido\":true,"));
          try {
            var v = new ImportadorMatrizes().prever(p);
            verifica(v.valida());
            igual(2, v.avisos().size());
          } finally {
            Files.delete(p);
          }
        });
    teste(
        "JSON referências inexistentes e duplicados são inválidos",
        () -> {
          var m = CatalogoJson.objeto(CatalogoJson.escrever(projeto(grafo())));
          m.put("prerequisitos", List.of(List.of("A", "X")));
          rejeita(() -> new CatalogoJson(new ArrayList<>()).ler(m));
          rejeita(() -> Json.ler("{\"x\":1,\"x\":2}"));
          rejeita(() -> Json.ler("[NaN]"));
          rejeita(() -> Json.ler("[1,]"));
          rejeita(() -> Json.ler("\"\\uD800\""));
        });
    teste(
        "PGD 1 abre todos os exemplos reais e recupera posições",
        () -> {
          for (int i = 1; i <= 4; i++) {
            var p = new GerenciadorArquivos().abrir(Path.of("exemplos/exemplo-" + i + ".pgd"));
            verifica(p.grafo().quantidadeVertices() > 0);
            igual(p.grafo().getCodigos(), p.posicoes().keySet());
            verifica(
                p.grafo().getDisciplinas().stream()
                    .allMatch(
                        d ->
                            d.getCargaHoraria() == null
                                && d.getStatus() == StatusAcademico.NAO_CURSADA));
          }
        });
    teste(
        "PGD 2 persiste regras, status, posições, versão e cenários",
        () -> {
          var g = grafo();
          g.editarDisciplina("A", disc("A", 60.0, 4.0, StatusAcademico.APROVADA));
          g.definirRequisitos(
              "B",
              List.of(
                  Requisito.grupo(
                      Requisito.Tipo.OR,
                      Requisito.disciplina(Requisito.Tipo.CORREQUISITO, "A"),
                      Requisito.minimo(Requisito.Tipo.HORAS, 120))));
          var base = projeto(g);
          var p =
              new ProjetoAcademico(
                  g,
                  base.posicoes(),
                  base.matriz(),
                  List.of(plano(Set.of("B"), Set.of("C"), 60.0, 1)));
          Path path = Files.createTempFile("pgd2-", ".pgd");
          try {
            new GerenciadorArquivos().salvar(path, p);
            var q = new GerenciadorArquivos().abrir(path);
            igual(Json.escrever(CatalogoJson.escrever(p)), Json.escrever(CatalogoJson.escrever(q)));
            verifica(Files.readString(path).startsWith("PGD\t2"));
          } finally {
            Files.delete(path);
          }
        });
    teste(
        "Exportação legada exige consentimento e não muda original",
        () -> {
          var p = projeto(grafo());
          Path path = Files.createTempFile("legado-", ".pgd");
          try {
            rejeita(() -> new GerenciadorArquivos().exportarLegado(path, p, false));
            new GerenciadorArquivos().exportarLegado(path, p, true);
            igual(4, new GerenciadorArquivos().abrir(path).grafo().quantidadeVertices());
            igual("2026-1", p.matriz().versao());
          } finally {
            Files.delete(path);
          }
        });
    teste(
        "Importação inválida não altera projeto já aberto",
        () -> {
          var p = projeto(grafo());
          String antes = Json.escrever(CatalogoJson.escrever(p));
          Path path = arquivo(".json", "{\"versao\":2,\"disciplinas\":[{\"codigo\":\"A\"}]}");
          try {
            var v = new ImportadorMatrizes().prever(path);
            verifica(!v.valida());
            igual(antes, Json.escrever(CatalogoJson.escrever(p)));
          } finally {
            Files.delete(path);
          }
        });
    teste(
        "Exportar e reimportar CSV e JSON",
        () -> {
          var p = projeto(grafo());
          for (String ext : List.of(".csv", ".json")) {
            Path path = Files.createTempFile("roundtrip-", ext);
            try {
              var i = new ImportadorMatrizes();
              if (ext.equals(".csv")) i.exportarCsv(path, p);
              else i.exportarJson(path, p);
              var q = i.prever(path);
              verifica(q.valida());
              igual(p.grafo().getDisciplinas(), q.projeto().grafo().getDisciplinas());
            } finally {
              Files.delete(path);
            }
          }
        });
    teste(
        "PGD 2 corrompido é rejeitado",
        () -> {
          for (String s :
              List.of(
                  "PGD\t2\n{}",
                  "PGD\t2\n{\"versao\":2,\"disciplinas\":[] ,\"surpresa\":1}",
                  "PGD\t3\n")) {
            Path p = arquivo(".pgd", s);
            try {
              rejeita(() -> new GerenciadorArquivos().abrir(p));
            } finally {
              Files.delete(p);
            }
          }
        });
    teste(
        "Matriz com 2000 disciplinas e ramificação",
        () -> {
          var g = new GrafoDisciplinas();
          for (int i = 0; i < 2000; i++)
            g.adicionarDisciplina(new Disciplina("D" + i, "Disciplina " + i, 1));
          for (int i = 1; i < 2000; i++) g.adicionarDependencia("D0", "D" + i);
          igual(1999, g.alcance("D0", false).size());
          verifica(new ComparadorAlgoritmos().comparar(g).kahnValida());
          igual(2, new PlanejadorPeriodos().planejar(g).grupos().size());
          igual(2000, SERVICO.disponibilidade(g, Set.of(), Set.of()).size());
        });
    teste(
        "Exemplos fictícios preservam Kahn, DFS e ciclo real",
        () -> {
          for (int i = 0; i < 5; i++) {
            var p = ExemplosCurriculares.criar(i);
            igual(i == 4, new DetectorCiclos().analisar(p.grafo()).possuiCiclo());
            igual(i == 4, new AlgoritmoKahn().executar(p.grafo()).possuiCiclo());
            igual(i == 4, new AlgoritmoDFS().executar(p.grafo()).possuiCiclo());
            verifica(p.matriz().instituicao().contains("fictícia"));
          }
        });
    teste(
        "Modelos de importação e cinco catálogos fictícios são válidos",
        () -> {
          List<Path> paths = new ArrayList<>();
          try (var stream = Files.list(Path.of("exemplos/curriculares"))) {
            paths.addAll(stream.toList());
          }
          paths.add(Path.of("exemplos/modelos/disciplinas.csv"));
          paths.add(Path.of("exemplos/modelos/catalogo.json"));
          igual(7, paths.size());
          for (Path path : paths) {
            var previa = new ImportadorMatrizes().prever(path);
            verifica(previa.valida());
            verifica(previa.erros().isEmpty());
          }
        });
    teste(
        "Posições e dados reais de cada registro PGD 1 são exatos",
        () -> {
          for (int i = 1; i <= 4; i++) {
            Path path = Path.of("exemplos/exemplo-" + i + ".pgd");
            var projeto = new GerenciadorArquivos().abrir(path);
            for (String linha : Files.readAllLines(path))
              if (linha.startsWith("D\t")) {
                String[] cs = linha.split("\t");
                String codigo =
                    new String(Base64.getDecoder().decode(cs[1]), StandardCharsets.UTF_8);
                String nome = new String(Base64.getDecoder().decode(cs[2]), StandardCharsets.UTF_8);
                igual(nome, projeto.grafo().obterDisciplina(codigo).getNome());
                igual(
                    Integer.parseInt(cs[3]),
                    projeto.grafo().obterDisciplina(codigo).getPeriodoReferencia());
                igual(Double.parseDouble(cs[4]), projeto.posicoes().get(codigo).getX());
                igual(Double.parseDouble(cs[5]), projeto.posicoes().get(codigo).getY());
              }
          }
        });
    System.out.println(
        "RESULTADO CURRICULAR: " + passou + " testes aprovados; " + falhou + " falhas.");
    if (falhou > 0) System.exit(1);
  }
}
