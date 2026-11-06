/** Identifica uma versão de matriz. Campos vazios representam informação desconhecida. */
public record MatrizCurricular(
    String instituicao,
    String curso,
    String codigoCurso,
    String versao,
    Integer ano,
    String campus,
    String turno,
    Integer periodos,
    String observacoes,
    String fonte) {
  public MatrizCurricular {
    instituicao = texto(instituicao);
    curso = texto(curso);
    codigoCurso = texto(codigoCurso);
    versao = texto(versao);
    campus = texto(campus);
    turno = texto(turno);
    observacoes = texto(observacoes);
    fonte = texto(fonte);
    if (ano != null && (ano < 1 || ano > 9999))
      throw new IllegalArgumentException("Ano entre 1 e 9999.");
    if (periodos != null && (periodos < 1 || periodos > 100))
      throw new IllegalArgumentException("Quantidade de períodos entre 1 e 100.");
  }

  static String texto(String s) {
    if (s == null) return "";
    if (s.length() > 10000
        || s.chars()
            .anyMatch(c -> Character.isISOControl(c) && c != '\n' && c != '\r' && c != '\t'))
      throw new IllegalArgumentException("Texto inválido ou maior que 10000 caracteres.");
    return s.strip();
  }

  public static MatrizCurricular livre() {
    return new MatrizCurricular("", "", "", "", null, "", "", null, "", "");
  }

  public String identificacao() {
    return (curso.isBlank() ? "Projeto livre" : curso)
        + (versao.isBlank() ? "" : " · " + versao)
        + (instituicao.isBlank() ? "" : " · " + instituicao);
  }
}
