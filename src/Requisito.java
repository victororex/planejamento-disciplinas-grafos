import java.util.*;

/** Árvore explícita de regras. CORREQUISITO aceita conclusão anterior OU matrícula simultânea. */
public record Requisito(Tipo tipo, String codigo, Double minimo, List<Requisito> itens) {
  public enum Tipo {
    CONCLUIDA,
    CORREQUISITO,
    HORAS,
    CREDITOS,
    ATIVIDADE,
    AND,
    OR
  }

  public Requisito {
    Objects.requireNonNull(tipo);
    codigo = codigo == null ? "" : codigo;
    itens = itens == null ? List.of() : List.copyOf(itens);
    boolean grupo = tipo == Tipo.AND || tipo == Tipo.OR;
    boolean quantidade = tipo == Tipo.HORAS || tipo == Tipo.CREDITOS;
    if (grupo) {
      if (itens.isEmpty() || !codigo.isEmpty() || minimo != null)
        throw new IllegalArgumentException("AND/OR exigem itens, sem código ou mínimo.");
    } else if (quantidade) {
      Disciplina.validarQuantidade(minimo);
      if (minimo == null || !codigo.isEmpty() || !itens.isEmpty())
        throw new IllegalArgumentException("Informe somente o mínimo de horas/créditos.");
    } else if (!codigo.matches("[\\p{L}\\p{N}_-]{1,24}") || minimo != null || !itens.isEmpty())
      throw new IllegalArgumentException("Regra exige somente um código válido.");
    if (profundidade(itens) > 16)
      throw new IllegalArgumentException("Regras profundas demais (máximo 16 níveis).");
  }

  private static int profundidade(List<Requisito> rs) {
    return rs.stream().mapToInt(r -> 1 + profundidade(r.itens)).max().orElse(0);
  }

  public static Requisito disciplina(Tipo tipo, String codigo) {
    return new Requisito(tipo, codigo, null, List.of());
  }

  public static Requisito minimo(Tipo tipo, double n) {
    return new Requisito(tipo, "", n, List.of());
  }

  public static Requisito grupo(Tipo tipo, Requisito... itens) {
    return new Requisito(tipo, "", null, List.of(itens));
  }

  public Set<String> referencias() {
    Set<String> r = new LinkedHashSet<>();
    if (!codigo.isEmpty()) r.add(codigo);
    itens.forEach(i -> r.addAll(i.referencias()));
    return Collections.unmodifiableSet(r);
  }

  public Requisito renomear(String anterior, String novo) {
    return new Requisito(
        tipo,
        codigo.equals(anterior) ? novo : codigo,
        minimo,
        itens.stream().map(r -> r.renomear(anterior, novo)).toList());
  }

  @Override
  public String toString() {
    return switch (tipo) {
      case AND, OR ->
          "("
              + String.join(
                  tipo == Tipo.AND ? " E " : " OU ", itens.stream().map(Object::toString).toList())
              + ")";
      case HORAS -> minimo + " horas concluídas";
      case CREDITOS -> minimo + " créditos concluídos";
      case CORREQUISITO -> codigo + " concluída ou no mesmo período";
      case CONCLUIDA, ATIVIDADE -> codigo + " concluída";
    };
  }
}
