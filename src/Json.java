import java.util.*;

/** JSON estrito e limitado, sem reflexão, execução de código ou dependências externas. */
public final class Json {
  private final String texto;
  private int pos;

  private Json(String texto) {
    this.texto = texto;
  }

  public static Object ler(String texto) {
    Json p = new Json(texto);
    Object v = p.valor(0);
    p.espaco();
    if (p.pos != texto.length()) throw p.erro("Conteúdo após JSON");
    return v;
  }

  private IllegalArgumentException erro(String m) {
    return new IllegalArgumentException(m + " na posição " + pos + ".");
  }

  private void espaco() {
    while (pos < texto.length() && " \t\r\n".indexOf(texto.charAt(pos)) >= 0) pos++;
  }

  private boolean consumir(char c) {
    espaco();
    if (pos < texto.length() && texto.charAt(pos) == c) {
      pos++;
      return true;
    }
    return false;
  }

  private void exigir(char c) {
    if (!consumir(c)) throw erro("Esperado '" + c + "'");
  }

  private Object valor(int nivel) {
    if (nivel > 32) throw erro("JSON profundo demais");
    espaco();
    if (pos >= texto.length()) throw erro("Valor ausente");
    char c = texto.charAt(pos);
    if (c == '"') return string();
    if (consumir('{')) {
      Map<String, Object> m = new LinkedHashMap<>();
      if (consumir('}')) return m;
      do {
        espaco();
        String k = string();
        exigir(':');
        Object v = valor(nivel + 1);
        if (m.containsKey(k)) throw erro("Campo duplicado: " + k);
        m.put(k, v);
      } while (consumir(','));
      exigir('}');
      return m;
    }
    if (consumir('[')) {
      List<Object> l = new ArrayList<>();
      if (consumir(']')) return l;
      do {
        l.add(valor(nivel + 1));
        if (l.size() > 20000) throw erro("Lista grande demais");
      } while (consumir(','));
      exigir(']');
      return l;
    }
    for (String literal : List.of("true", "false", "null"))
      if (texto.startsWith(literal, pos)) {
        pos += literal.length();
        return literal.equals("null") ? null : Boolean.valueOf(literal);
      }
    int inicio = pos;
    while (pos < texto.length() && "-+0123456789.eE".indexOf(texto.charAt(pos)) >= 0) pos++;
    String n = texto.substring(inicio, pos);
    if (!n.matches("-?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+-]?[0-9]+)?"))
      throw erro("Número inválido");
    double d = Double.parseDouble(n);
    if (!Double.isFinite(d)) throw erro("Número não finito");
    return d;
  }

  private String string() {
    exigir('"');
    StringBuilder s = new StringBuilder();
    while (pos < texto.length()) {
      char c = texto.charAt(pos++);
      if (c == '"') return validarUnicode(s.toString());
      if (c < 32) throw erro("Controle não escapado");
      if (c == '\\') {
        if (pos >= texto.length()) throw erro("Escape incompleto");
        c = texto.charAt(pos++);
        switch (c) {
          case '"', '\\', '/' -> s.append(c);
          case 'b' -> s.append('\b');
          case 'f' -> s.append('\f');
          case 'n' -> s.append('\n');
          case 'r' -> s.append('\r');
          case 't' -> s.append('\t');
          case 'u' -> {
            if (pos + 4 > texto.length()) throw erro("Unicode incompleto");
            try {
              s.append((char) Integer.parseInt(texto.substring(pos, pos + 4), 16));
            } catch (NumberFormatException e) {
              throw erro("Unicode inválido");
            }
            pos += 4;
          }
          default -> throw erro("Escape inválido");
        }
      } else s.append(c);
    }
    throw erro("Texto sem fechamento");
  }

  private String validarUnicode(String s) {
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (Character.isHighSurrogate(c)) {
        if (++i >= s.length() || !Character.isLowSurrogate(s.charAt(i)))
          throw erro("Unicode inválido");
      } else if (Character.isLowSurrogate(c)) throw erro("Unicode inválido");
    }
    return s;
  }

  public static String escrever(Object v) {
    if (v == null) return "null";
    if (v instanceof String s) {
      StringBuilder b = new StringBuilder("\"");
      for (char c : s.toCharArray())
        switch (c) {
          case '"' -> b.append("\\\"");
          case '\\' -> b.append("\\\\");
          case '\n' -> b.append("\\n");
          case '\r' -> b.append("\\r");
          case '\t' -> b.append("\\t");
          default -> {
            if (c < 32) b.append(String.format("\\u%04x", (int) c));
            else b.append(c);
          }
        }
      return b.append('"').toString();
    }
    if (v instanceof Number n) {
      if (!Double.isFinite(n.doubleValue()))
        throw new IllegalArgumentException("Número não finito.");
      return n.toString();
    }
    if (v instanceof Boolean) return v.toString();
    if (v instanceof Map<?, ?> m) {
      List<String> partes = new ArrayList<>();
      m.forEach((k, x) -> partes.add(escrever(k.toString()) + ":" + escrever(x)));
      return "{" + String.join(",", partes) + "}";
    }
    if (v instanceof Collection<?> l)
      return "[" + String.join(",", l.stream().map(Json::escrever).toList()) + "]";
    throw new IllegalArgumentException("Tipo JSON não suportado.");
  }
}
