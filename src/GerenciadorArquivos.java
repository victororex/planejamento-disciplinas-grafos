import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

/** Formatos PGD 1 e 2 em UTF-8. Nunca usa desserialização Java nem executa conteúdo de arquivos. */
public final class GerenciadorArquivos {
  public static final int MAX_VERTICES = 2000;
  public static final int MAX_ARESTAS = 20000;
  private static final long MAX_BYTES = 8 * 1024 * 1024;

  public void salvar(Path caminho, ProjetoAcademico projeto) throws IOException {
    if (projeto.grafo().quantidadeVertices() > MAX_VERTICES
        || projeto.grafo().quantidadeArestas() > MAX_ARESTAS) {
      throw new IOException("O formato aceita até 2000 disciplinas e 20000 dependências.");
    }
    Path destino = caminho.toAbsolutePath();
    Path temporario = Files.createTempFile(destino.getParent(), ".pgd-", ".tmp");
    try {
      String conteudo = "PGD\t2\n" + Json.escrever(CatalogoJson.escrever(projeto)) + "\n";
      if (conteudo.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES)
        throw new IOException("Arquivo maior que 8 MiB.");
      Files.writeString(temporario, conteudo, StandardCharsets.UTF_8);
      try {
        Files.move(
            temporario,
            destino,
            StandardCopyOption.ATOMIC_MOVE,
            StandardCopyOption.REPLACE_EXISTING);
      } catch (AtomicMoveNotSupportedException | AccessDeniedException e) {
        // Alguns provedores Windows recusam renomeação atômica sobre um arquivo existente.
        // A alternativa ainda usa o temporário completo; erros reais de permissão são propagados.
        Files.move(temporario, destino, StandardCopyOption.REPLACE_EXISTING);
      }
    } finally {
      Files.deleteIfExists(temporario);
    }
  }

  public ProjetoAcademico abrir(Path caminho) throws IOException {
    if (Files.size(caminho) > MAX_BYTES)
      throw new IOException("Arquivo maior que o limite de 8 MiB.");
    String conteudo = Files.readString(caminho, StandardCharsets.UTF_8);
    if (conteudo.startsWith("PGD\t2\n") || conteudo.startsWith("PGD\t2\r\n")) {
      try {
        List<String> avisos = new ArrayList<>();
        ProjetoAcademico p =
            new CatalogoJson(avisos).ler(Json.ler(conteudo.substring(conteudo.indexOf('\n') + 1)));
        if (!avisos.isEmpty()) throw new IllegalArgumentException(String.join("; ", avisos));
        return p;
      } catch (IllegalArgumentException | NullPointerException e) {
        throw new IOException("PGD 2 inválido: " + e.getMessage(), e);
      }
    }
    // Tudo é validado em um novo projeto; um erro jamais altera o projeto aberto na tela.
    GrafoDisciplinas g = new GrafoDisciplinas();
    Map<String, VerticeVisual> posicoes = new LinkedHashMap<>();
    List<String[]> arestas = new ArrayList<>();
    int linha = 1;
    try (BufferedReader r = Files.newBufferedReader(caminho, StandardCharsets.UTF_8)) {
      if (!"PGD\t1".equals(r.readLine()))
        throw new IOException("Cabeçalho inválido. Selecione um projeto .pgd na versão 1.");
      String texto;
      while ((texto = r.readLine()) != null) {
        linha++;
        if (texto.length() > 4096) throw new IllegalArgumentException("Linha muito longa.");
        String[] partes = texto.split("\t", -1);
        if (partes.length == 6 && partes[0].equals("D")) {
          if (g.quantidadeVertices() >= MAX_VERTICES)
            throw new IllegalArgumentException("Limite de disciplinas excedido.");
          Disciplina d =
              new Disciplina(
                  decodificar(partes[1]), decodificar(partes[2]), Integer.parseInt(partes[3]));
          VerticeVisual p =
              new VerticeVisual(Double.parseDouble(partes[4]), Double.parseDouble(partes[5]));
          g.adicionarDisciplina(d);
          posicoes.put(d.getCodigo(), p);
        } else if (partes.length == 3 && partes[0].equals("R")) {
          if (arestas.size() >= MAX_ARESTAS)
            throw new IllegalArgumentException("Limite de dependências excedido.");
          arestas.add(
              new String[] {
                decodificar(partes[1]), decodificar(partes[2]), Integer.toString(linha)
              });
        } else
          throw new IllegalArgumentException(
              "Registro desconhecido ou número de campos incorreto.");
      }
      for (String[] e : arestas) {
        linha = Integer.parseInt(e[2]);
        g.adicionarDependencia(e[0], e[1]);
      }
    } catch (IllegalArgumentException e) {
      throw new IOException("Projeto inválido na linha " + linha + ": " + e.getMessage(), e);
    }
    return new ProjetoAcademico(g, posicoes);
  }

  /** Exportação legada explícita; dados extras exigem consentimento para perda. */
  public void exportarLegado(Path caminho, ProjetoAcademico p, boolean aceitarPerda)
      throws IOException {
    if (!aceitarPerda)
      throw new IOException(
          "PGD 1 perde metadados, status, horas, créditos, requisitos adicionais e cenários."
              + " Prefira PGD 2 ou JSON.");
    StringBuilder s = new StringBuilder("PGD\t1\n");
    for (Disciplina d : p.grafo().getDisciplinas()) {
      if (d.getPeriodoReferencia() == 0)
        throw new IOException("PGD 1 exige período informado em todas as disciplinas.");
      VerticeVisual v = p.posicoes().get(d.getCodigo());
      s.append(
              String.join(
                  "\t",
                  "D",
                  codificar(d.getCodigo()),
                  codificar(d.getNome()),
                  Integer.toString(d.getPeriodoReferencia()),
                  Double.toString(v.getX()),
                  Double.toString(v.getY())))
          .append('\n');
    }
    p.grafo()
        .listaAdjacencia()
        .forEach(
            (a, bs) ->
                bs.forEach(
                    b ->
                        s.append("R\t")
                            .append(codificar(a))
                            .append('\t')
                            .append(codificar(b))
                            .append('\n')));
    ImportadorMatrizes.gravar(caminho, s.toString());
  }

  private String codificar(String s) {
    return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
  }

  private String decodificar(String s) {
    try {
      return StandardCharsets.UTF_8
          .newDecoder()
          .onMalformedInput(CodingErrorAction.REPORT)
          .decode(ByteBuffer.wrap(Base64.getDecoder().decode(s)))
          .toString();
    } catch (CharacterCodingException e) {
      throw new IllegalArgumentException("Texto UTF-8 inválido.");
    }
  }
}
