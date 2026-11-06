import java.awt.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Smoke gráfico real, executado separadamente com DISPLAY ou xvfb-run. Não conta como teste
 * headless.
 */
public final class TestesInterface {
  private static TelaPrincipal tela;
  private static Throwable erroDialogo;
  private static final List<javax.swing.Timer> timers = new ArrayList<>();

  private static Object campo(Object alvo, String nome) throws Exception {
    Field f = alvo.getClass().getDeclaredField(nome);
    f.setAccessible(true);
    return f.get(alvo);
  }

  private static Object chamar(String nome, Class<?>[] tipos, Object... argumentos)
      throws Exception {
    Method m = TelaPrincipal.class.getDeclaredMethod(nome, tipos);
    m.setAccessible(true);
    return m.invoke(tela, argumentos);
  }

  private static void verifica(boolean b) {
    if (!b) throw new AssertionError("Falha na interface.");
  }

  private static List<Component> componentes(Container raiz) {
    List<Component> cs = new ArrayList<>();
    for (Component c : raiz.getComponents()) {
      cs.add(c);
      if (c instanceof Container sub) cs.addAll(componentes(sub));
    }
    return cs;
  }

  private static void clique(Container raiz, String texto) {
    for (Component c : componentes(raiz))
      if (c instanceof AbstractButton b && texto.equals(b.getText())) {
        b.doClick();
        return;
      }
    throw new AssertionError("Botão ausente: " + texto);
  }

  private static void texto(Container raiz, String nome, String valor) {
    for (Component c : componentes(raiz))
      if (nome.equals(c.getName()) && c instanceof JTextField f) {
        f.setText(valor);
        return;
      }
    throw new AssertionError("Campo ausente: " + nome);
  }

  private static void dialogo(Consumer<JDialog> preencher) {
    javax.swing.Timer t = new javax.swing.Timer(150, null);
    t.addActionListener(
        e -> {
          for (Window w : Window.getWindows())
            if (w instanceof JDialog d && d.isShowing()) {
              t.stop();
              try {
                preencher.accept(d);
                for (Component c : componentes(d))
                  if (c instanceof JOptionPane pane) {
                    pane.setValue(JOptionPane.OK_OPTION);
                    return;
                  }
              } catch (Throwable falha) {
                erroDialogo = falha;
                d.dispose();
              }
              return;
            }
        });
    timers.add(t);
    t.start();
  }

  private static void ocioso() throws Exception {
    long fim = System.currentTimeMillis() + 15000;
    while (System.currentTimeMillis() < fim) {
      boolean[] b = {true};
      SwingUtilities.invokeAndWait(
          () -> {
            try {
              b[0] = (boolean) campo(tela, "ocupado");
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          });
      if (!b[0]) return;
      Thread.sleep(50);
    }
    throw new AssertionError("Tarefa não terminou.");
  }

  private static void edt(Runnable r) throws Exception {
    SwingUtilities.invokeAndWait(r);
    if (erroDialogo != null) throw new AssertionError("Formulário falhou", erroDialogo);
  }

  private static void invocar(String nome, Class<?>[] tipos, Object... args) {
    try {
      chamar(nome, tipos, args);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(String[] args) throws Exception {
    if (GraphicsEnvironment.isHeadless())
      throw new IllegalStateException("Teste exige display gráfico.");
    Path evidencia = Path.of(args.length == 0 ? ".tmp/interface" : args[0]);
    Files.createDirectories(evidencia);
    try {
      edt(
          () -> {
            Tema.instalar();
            tela = new TelaPrincipal();
            tela.setVisible(true);
          });
      edt(
          () -> {
            dialogo(
                d -> {
                  texto(d, "Instituição", "Instituição fictícia");
                  texto(d, "Curso", "Curso de teste");
                  texto(d, "Versão da matriz", "2026-A");
                  texto(d, "Quantidade de períodos", "4");
                });
            invocar("editarMatriz", new Class<?>[0]);
          });
      for (String codigo : List.of("A", "B"))
        edt(
            () -> {
              dialogo(
                  d -> {
                    texto(d, "Código *", codigo);
                    texto(d, "Nome *", "Disciplina " + codigo);
                    texto(
                        d,
                        "Período oficial (vazio = desconhecido)",
                        codigo.equals("A") ? "1" : "2");
                    texto(d, "Carga horária total", "60");
                    texto(d, "Créditos", "4");
                  });
              invocar("formulario", new Class<?>[] {String.class, VerticeVisual.class}, null, null);
            });
      edt(
          () -> {
            tela.conectar("A", "B");
            dialogo(
                d -> {
                  for (Component c : componentes(d))
                    if (c instanceof JComboBox<?> combo
                        && combo.getItemAt(0) instanceof StatusAcademico)
                      combo.setSelectedIndex(StatusAcademico.APROVADA.ordinal());
                });
            tela.editar("A");
          });
      edt(
          () -> {
            try {
              var g = (GrafoDisciplinas) campo(tela, "grafo");
              verifica(g.obterDisciplina("A").getStatus() == StatusAcademico.APROVADA);
              verifica(new ServicoAcademico().avaliar(g, "B", Set.of(), Set.of()).disponivel());
              var tabs = (JTabbedPane) campo(tela, "visoes");
              verifica(tabs.getTabCount() == 4);
              tabs.setSelectedIndex(1);
              ((JTextField) campo(tela, "busca")).setText("B");
              verifica(g.quantidadeVertices() == 2 && g.quantidadeArestas() == 1);
              ((JTextField) campo(tela, "busca")).setText("");
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          });
      edt(
          () -> {
            dialogo(
                d -> {
                  for (Component c : componentes(d))
                    if (c instanceof JComboBox<?> combo
                        && combo.getItemCount() == 5
                        && Objects.toString(combo.getItemAt(0)).contains("Concluída"))
                      combo.setSelectedIndex(1);
                  clique(d, "Adicionar regra");
                });
            invocar("editarRequisitos", new Class<?>[] {String.class}, "B");
          });
      edt(
          () -> {
            try {
              var g = (GrafoDisciplinas) campo(tela, "grafo");
              verifica(g.requisitos("B").get(0).tipo() == Requisito.Tipo.CORREQUISITO);
              var pessoal = (PainelPlanejamento) campo(tela, "painelPessoal");
              var tabela = (DefaultTableModel) campo(pessoal, "modelo");
              tabela.setValueAt(true, 1, 0);
              clique(pessoal, "Validar seleção");
              verifica(
                  ((JTextArea) campo(pessoal, "resultado")).getText().contains("Seleção válida"));
              clique(pessoal, "Salvar cenário");
              verifica(((List<?>) campo(tela, "planos")).size() == 1);
              var tabs = (JTabbedPane) campo(tela, "visoes");
              tabs.setSelectedIndex(1);
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          });
      edt(
          () -> {
            try {
              ImageIO.write(
                  new Robot().createScreenCapture(tela.getBounds()),
                  "png",
                  evidencia.resolve("matriz.png").toFile());
              var tabs = (JTabbedPane) campo(tela, "visoes");
              tabs.setSelectedIndex(2);
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          });
      edt(
          () -> {
            try {
              ImageIO.write(
                  new Robot().createScreenCapture(tela.getBounds()),
                  "png",
                  evidencia.resolve("planejamento.png").toFile());
              ((JTabbedPane) campo(tela, "visoes")).setSelectedIndex(0);
              clique(tela, "Executar Kahn");
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          });
      ocioso();
      edt(
          () -> {
            try {
              verifica(!((List<?>) campo(tela, "passos")).isEmpty());
              verifica(
                  ((ComparadorAlgoritmos.Comparacao) campo(tela, "ultimaAnalise")).kahnValida());
              clique(tela, "Executar DFS");
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          });
      ocioso();
      edt(
          () -> {
            try {
              verifica(
                  ((ComparadorAlgoritmos.Comparacao) campo(tela, "ultimaAnalise")).dfsValida());
              ProjetoAcademico p = (ProjetoAcademico) chamar("projetoAtual", new Class<?>[0]);
              Path arquivo = evidencia.resolve("projeto.pgd");
              new GerenciadorArquivos().salvar(arquivo, p);
              ProjetoAcademico q = new GerenciadorArquivos().abrir(arquivo);
              verifica(
                  q.matriz().versao().equals("2026-A")
                      && q.planos().size() == 1
                      && q.grafo().requisitos("B").size() == 1);
              ImageIO.write(
                  new Robot().createScreenCapture(tela.getBounds()),
                  "png",
                  evidencia.resolve("grafo.png").toFile());
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          });
      Path invalido = evidencia.resolve("invalido.json");
      Files.writeString(invalido, "{invalido");
      String[] antes = {null};
      edt(
          () -> {
            try {
              antes[0] =
                  Json.escrever(
                      CatalogoJson.escrever(
                          (ProjetoAcademico) chamar("projetoAtual", new Class<?>[0])));
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
            dialogo(
                d -> {
                  for (Component c : componentes(d))
                    if (c instanceof JFileChooser chooser) {
                      chooser.setSelectedFile(invalido.toFile());
                      chooser.approveSelection();
                      dialogo(erro -> {});
                      return;
                    }
                  throw new AssertionError("Seletor não encontrado");
                });
            invocar("importar", new Class<?>[0]);
          });
      ocioso();
      edt(
          () -> {
            try {
              verifica(
                  antes[0].equals(
                      Json.escrever(
                          CatalogoJson.escrever(
                              (ProjetoAcademico) chamar("projetoAtual", new Class<?>[0])))));
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          });
      System.out.println(
          "INTERFACE OK: formulários de matriz e disciplinas, status, correquisito, filtros, quatro"
              + " abas, cenário, PGD 2, importação inválida preservando projeto, Kahn/DFS e"
              + " capturas reais.");
    } finally {
      edt(
          () -> {
            for (javax.swing.Timer timer : timers) timer.stop();
            for (Window w : Window.getWindows()) w.dispose();
          });
    }
  }
}
