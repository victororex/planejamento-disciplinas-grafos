import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Cenários pessoais editáveis e comparáveis; não altera o histórico do aluno. */
// Componentes Swing não são persistidos por serialização Java; o projeto usa PGD/JSON.
@SuppressWarnings("serial")
public final class PainelPlanejamento extends JPanel {
  private static final long serialVersionUID = 1L;
  private final Supplier<ProjetoAcademico> projeto;
  private final Consumer<List<PlanoAcademico>> salvar;
  private final JTextField nome = new JTextField("Cenário 1", 12),
      periodo = new JTextField("", 10),
      horas = new JTextField("", 5),
      quantidade = new JTextField("", 4);
  private final JComboBox<String> cenarios = new JComboBox<>();
  private final JTextArea resultado = new JTextArea(7, 40);
  private final DefaultTableModel modelo =
      new DefaultTableModel(
          new String[] {
            "Cursar", "Simular aprovação anterior", "Código", "Nome", "Status", "Horas"
          },
          0) {
        @Override
        public Class<?> getColumnClass(int c) {
          return c < 2 ? Boolean.class : String.class;
        }

        @Override
        public boolean isCellEditable(int r, int c) {
          return c < 2;
        }
      };

  public PainelPlanejamento(
      Supplier<ProjetoAcademico> projeto, Consumer<List<PlanoAcademico>> salvar) {
    this.projeto = projeto;
    this.salvar = salvar;
    setLayout(new BorderLayout());
    JPanel topo = new JPanel(new GridLayout(0, 1));
    JPanel campos = new JPanel(new FlowLayout(FlowLayout.LEFT));
    campos.add(new JLabel("Cenário"));
    campos.add(nome);
    campos.add(new JLabel("Período futuro"));
    campos.add(periodo);
    campos.add(new JLabel("Máx. horas"));
    campos.add(horas);
    campos.add(new JLabel("Máx. disciplinas"));
    campos.add(quantidade);
    topo.add(campos);
    JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
    botoes.add(Tema.botao("Validar seleção", () -> tentar(() -> mostrar(plano()))));
    botoes.add(
        Tema.botao(
            "Sugerir combinação",
            () ->
                tentar(
                    () -> {
                      PlanoAcademico p =
                          new ServicoAcademico().sugerir(projeto.get().grafo(), plano());
                      preencher(p);
                      mostrar(p);
                    })));
    botoes.add(Tema.botao("Salvar cenário", () -> tentar(this::guardar)));
    botoes.add(cenarios);
    botoes.add(Tema.botao("Carregar", () -> carregar()));
    botoes.add(Tema.botao("Excluir", () -> excluir()));
    botoes.add(Tema.botao("Comparar cenários", this::comparar));
    topo.add(botoes);
    topo.add(
        new JLabel(
            "  Simulação curricular: não verifica oferta, vagas, horários nem normas não"
                + " cadastradas. Campos de limite vazios = sem limite."));
    JScrollPane controles =
        new JScrollPane(
            topo,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    controles.setPreferredSize(new Dimension(600, 150));
    add(controles, BorderLayout.NORTH);
    JTable tabela = new JTable(modelo);
    tabela.setAutoCreateRowSorter(true);
    tabela.setRowHeight(28);
    resultado.setEditable(false);
    resultado.setLineWrap(true);
    resultado.setWrapStyleWord(true);
    JSplitPane split =
        new JSplitPane(
            JSplitPane.VERTICAL_SPLIT, new JScrollPane(tabela), new JScrollPane(resultado));
    split.setResizeWeight(.65);
    add(split, BorderLayout.CENTER);
  }

  /** Bloqueia edição de cenários enquanto a janela salva ou abre um projeto. */
  public void habilitar(boolean habilitado) {
    habilitarComponentes(this, habilitado);
  }

  private void habilitarComponentes(Component c, boolean habilitado) {
    c.setEnabled(habilitado);
    if (c instanceof Container pai)
      for (Component filho : pai.getComponents()) habilitarComponentes(filho, habilitado);
  }

  public void atualizar() {
    modelo.setRowCount(0);
    for (Disciplina d : projeto.get().grafo().getDisciplinas())
      modelo.addRow(
          new Object[] {
            false,
            false,
            d.getCodigo(),
            d.getNome(),
            d.getStatus().toString(),
            d.getCargaHoraria() == null ? "?" : d.getCargaHoraria().toString()
          });
    cenarios.removeAllItems();
    projeto.get().planos().forEach(p -> cenarios.addItem(p.nome()));
    resultado.setText(
        "Selecione disciplinas e/ou aprovações hipotéticas. As simulações não alteram os status"
            + " oficiais. Salve cenários antes de editar o projeto.");
  }

  private PlanoAcademico plano() {
    Set<String> selecao = new LinkedHashSet<>(), simuladas = new LinkedHashSet<>();
    for (int i = 0; i < modelo.getRowCount(); i++) {
      if (Boolean.TRUE.equals(modelo.getValueAt(i, 0)))
        selecao.add((String) modelo.getValueAt(i, 2));
      if (Boolean.TRUE.equals(modelo.getValueAt(i, 1)))
        simuladas.add((String) modelo.getValueAt(i, 2));
    }
    return new PlanoAcademico(
        nome.getText(),
        periodo.getText(),
        horas.getText().isBlank() ? null : Double.valueOf(horas.getText().replace(',', '.')),
        quantidade.getText().isBlank() ? null : Integer.valueOf(quantidade.getText()),
        selecao,
        simuladas);
  }

  private void preencher(PlanoAcademico p) {
    nome.setText(p.nome());
    periodo.setText(p.periodo());
    horas.setText(p.maxHoras() == null ? "" : p.maxHoras().toString());
    quantidade.setText(p.maxDisciplinas() == null ? "" : p.maxDisciplinas().toString());
    for (int i = 0; i < modelo.getRowCount(); i++) {
      String c = (String) modelo.getValueAt(i, 2);
      modelo.setValueAt(p.selecionadas().contains(c), i, 0);
      modelo.setValueAt(p.aprovacoesSimuladas().contains(c), i, 1);
    }
  }

  private String resumo(PlanoAcademico p) {
    var v = new ServicoAcademico().validar(projeto.get().grafo(), p);
    return p.nome()
        + " — "
        + p.periodo()
        + ": "
        + p.selecionadas()
        + "\n"
        + (v.valido()
            ? "Seleção válida segundo os dados cadastrados."
            : String.join("\n", v.conflitos()))
        + "\nCarga: "
        + (v.horas() == null ? "não totalmente informada" : v.horas() + " h")
        + "\nAprovações hipotéticas: "
        + p.aprovacoesSimuladas()
        + "\n";
  }

  private void mostrar(PlanoAcademico p) {
    resultado.setText(resumo(p));
  }

  private void guardar() {
    PlanoAcademico p = plano();
    List<PlanoAcademico> ps = new ArrayList<>(projeto.get().planos());
    if (ps.stream().anyMatch(x -> x.nome().equals(p.nome()))
        && JOptionPane.showConfirmDialog(
                this,
                "Substituir o cenário " + p.nome() + "?",
                "Cenário",
                JOptionPane.OK_CANCEL_OPTION)
            != JOptionPane.OK_OPTION) return;
    ps.removeIf(x -> x.nome().equals(p.nome()));
    ps.add(p);
    salvar.accept(ps);
    preencher(p);
    mostrar(p);
  }

  private void carregar() {
    projeto.get().planos().stream()
        .filter(p -> p.nome().equals(cenarios.getSelectedItem()))
        .findFirst()
        .ifPresent(
            p -> {
              preencher(p);
              mostrar(p);
            });
  }

  private void excluir() {
    Object n = cenarios.getSelectedItem();
    if (n != null
        && JOptionPane.showConfirmDialog(
                this, "Excluir cenário " + n + "?", "Cenário", JOptionPane.OK_CANCEL_OPTION)
            == JOptionPane.OK_OPTION)
      salvar.accept(projeto.get().planos().stream().filter(p -> !p.nome().equals(n)).toList());
  }

  private void comparar() {
    resultado.setText(
        String.join("\n", projeto.get().planos().stream().map(this::resumo).toList()));
  }

  private void tentar(Runnable r) {
    try {
      r.run();
    } catch (IllegalArgumentException e) {
      JOptionPane.showMessageDialog(this, "Confira os dados: " + e.getMessage());
    }
  }
}
