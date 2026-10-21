import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;

/** Formato PGD 1 em UTF-8. Nunca usa desserialização Java nem executa conteúdo de arquivos. */
public final class GerenciadorArquivos {
    public static final int MAX_VERTICES = 2000;
    public static final int MAX_ARESTAS = 20000;
    private static final long MAX_BYTES = 8 * 1024 * 1024;

    public void salvar(Path caminho, ProjetoAcademico projeto) throws IOException {
        if (projeto.grafo().quantidadeVertices() > MAX_VERTICES || projeto.grafo().quantidadeArestas() > MAX_ARESTAS) {
            throw new IOException("O formato aceita até 2000 disciplinas e 20000 dependências.");
        }
        Path destino = caminho.toAbsolutePath();
        Path temporario = Files.createTempFile(destino.getParent(), ".pgd-", ".tmp");
        try {
            try (BufferedWriter w = Files.newBufferedWriter(temporario, StandardCharsets.UTF_8)) {
                w.write("PGD\t1"); w.newLine();
                for (Disciplina d : projeto.grafo().getDisciplinas()) {
                    VerticeVisual p = projeto.posicoes().get(d.getCodigo());
                    w.write(String.join("\t", "D", codificar(d.getCodigo()), codificar(d.getNome()),
                            Integer.toString(d.getPeriodoReferencia()), Double.toString(p.getX()), Double.toString(p.getY())));
                    w.newLine();
                }
                for (var e : projeto.grafo().listaAdjacencia().entrySet()) for (String v : e.getValue()) {
                    w.write("R\t" + codificar(e.getKey()) + "\t" + codificar(v)); w.newLine();
                }
            }
            try { Files.move(temporario, destino, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException | AccessDeniedException e) {
                // Alguns provedores Windows recusam renomeação atômica sobre um arquivo existente.
                // A alternativa ainda usa o temporário completo; erros reais de permissão são propagados.
                Files.move(temporario, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally { Files.deleteIfExists(temporario); }
    }

    public ProjetoAcademico abrir(Path caminho) throws IOException {
        if (Files.size(caminho) > MAX_BYTES) throw new IOException("Arquivo maior que o limite de 8 MiB.");
        // Tudo é validado em um novo projeto; um erro jamais altera o projeto aberto na tela.
        GrafoDisciplinas g = new GrafoDisciplinas();
        Map<String, VerticeVisual> posicoes = new LinkedHashMap<>();
        List<String[]> arestas = new ArrayList<>();
        int linha = 1;
        try (BufferedReader r = Files.newBufferedReader(caminho, StandardCharsets.UTF_8)) {
            if (!"PGD\t1".equals(r.readLine())) throw new IOException("Cabeçalho inválido. Selecione um projeto .pgd na versão 1.");
            String texto;
            while ((texto = r.readLine()) != null) {
                linha++;
                if (texto.length() > 4096) throw new IllegalArgumentException("Linha muito longa.");
                String[] partes = texto.split("\t", -1);
                if (partes.length == 6 && partes[0].equals("D")) {
                    if (g.quantidadeVertices() >= MAX_VERTICES) throw new IllegalArgumentException("Limite de disciplinas excedido.");
                    Disciplina d = new Disciplina(decodificar(partes[1]), decodificar(partes[2]), Integer.parseInt(partes[3]));
                    VerticeVisual p = new VerticeVisual(Double.parseDouble(partes[4]), Double.parseDouble(partes[5]));
                    g.adicionarDisciplina(d); posicoes.put(d.getCodigo(), p);
                } else if (partes.length == 3 && partes[0].equals("R")) {
                    if (arestas.size() >= MAX_ARESTAS) throw new IllegalArgumentException("Limite de dependências excedido.");
                    arestas.add(new String[]{decodificar(partes[1]), decodificar(partes[2]), Integer.toString(linha)});
                } else throw new IllegalArgumentException("Registro desconhecido ou número de campos incorreto.");
            }
            for (String[] e : arestas) {
                linha = Integer.parseInt(e[2]); g.adicionarDependencia(e[0], e[1]);
            }
        } catch (IllegalArgumentException e) {
            throw new IOException("Projeto inválido na linha " + linha + ": " + e.getMessage(), e);
        }
        return new ProjetoAcademico(g, posicoes);
    }
    private String codificar(String s) { return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8)); }
    private String decodificar(String s) {
        try {
            return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(Base64.getDecoder().decode(s))).toString();
        } catch (CharacterCodingException e) { throw new IllegalArgumentException("Texto UTF-8 inválido."); }
    }
}
