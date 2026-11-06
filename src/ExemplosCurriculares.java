import java.util.*;

/** Dados fictícios: nunca representam catálogos oficiais de uma instituição. */
public final class ExemplosCurriculares {
  public static final String[] NOMES = {
    "Ciência da Computação (fictícia)",
    "Engenharia (fictícia)",
    "Curso técnico (fictício)",
    "Correquisitos mútuos (fictícia)",
    "Dependência circular (fictícia)"
  };

  public static ProjetoAcademico criar(int indice) {
    GrafoDisciplinas g = new GrafoDisciplinas();
    Map<String, VerticeVisual> ps = new LinkedHashMap<>();
    String[] nomes =
        switch (indice) {
          case 0 ->
              new String[] {
                "Programação I", "Programação II", "Estruturas de Dados", "Projeto Integrador"
              };
          case 1 -> new String[] {"Fundamentos Matemáticos", "Cálculo", "Física", "Laboratório"};
          case 2 ->
              new String[] {"Fundamentos Técnicos", "Prática I", "Prática II", "Projeto Final"};
          default -> new String[] {"Teoria", "Laboratório", "Projeto", "Seminário"};
        };
    for (int i = 0; i < 4; i++) {
      String c = "D" + (i + 1);
      g.adicionarDisciplina(
          new Disciplina(
              c,
              nomes[i],
              i + 1,
              60.0,
              4.0,
              "Obrigatória",
              "Exemplo fictício.",
              "",
              "",
              StatusAcademico.NAO_CURSADA));
      ps.put(c, new VerticeVisual(150 + i * 235, 140));
    }
    if (indice == 3) {
      g.definirRequisitos("D1", List.of(Requisito.disciplina(Requisito.Tipo.CORREQUISITO, "D2")));
      g.definirRequisitos("D2", List.of(Requisito.disciplina(Requisito.Tipo.CORREQUISITO, "D1")));
    } else {
      g.adicionarDependencia("D1", "D2");
      g.adicionarDependencia("D2", "D3");
      if (indice == 4) g.adicionarDependencia("D3", "D1");
      else g.adicionarDependencia("D3", "D4");
    }
    return new ProjetoAcademico(
        g,
        ps,
        new MatrizCurricular(
            "Instituição fictícia",
            NOMES[indice],
            "",
            "Exemplo didático",
            null,
            "",
            "",
            4,
            "Dados fictícios e editáveis; não usar como matriz oficial.",
            ""),
        List.of());
  }
}
