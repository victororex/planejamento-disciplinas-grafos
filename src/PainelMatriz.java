import java.awt.*;
import java.util.*;
import java.util.function.*;
import javax.swing.*;

/** Colunas pelos períodos OFICIAIS; não desenha conexões sobrepostas. */
// Componentes Swing não são persistidos por serialização Java; o projeto usa PGD/JSON.
@SuppressWarnings("serial")
public final class PainelMatriz extends JPanel {
  private static final long serialVersionUID = 1L;
  private final Consumer<String> selecionar, editar;

  public PainelMatriz(Consumer<String> selecionar, Consumer<String> editar) {
    this.selecionar = selecionar;
    this.editar = editar;
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
  }

  public void atualizar(ProjetoAcademico p, Predicate<Disciplina> filtro, String selecionado) {
    removeAll();
    GrafoDisciplinas g = p.grafo();
    Map<Integer, java.util.List<Disciplina>> grupos = new TreeMap<>();
    if (p.matriz().periodos() != null)
      for (int n = 1; n <= p.matriz().periodos(); n++) grupos.put(n, new ArrayList<>());
    for (Disciplina d : g.getDisciplinas())
      if (filtro.test(d))
        grupos.computeIfAbsent(d.getPeriodoReferencia(), n -> new ArrayList<>()).add(d);
    Set<String> anteriores =
        selecionado != null && g.contem(selecionado) ? g.alcance(selecionado, true) : Set.of();
    Set<String> posteriores =
        selecionado != null && g.contem(selecionado) ? g.alcance(selecionado, false) : Set.of();
    Set<String> diretos =
        selecionado != null && g.contem(selecionado) ? g.obterPrerequisitos(selecionado) : Set.of();
    Set<String> dependentesDiretas =
        selecionado != null && g.contem(selecionado) ? g.obterVizinhos(selecionado) : Set.of();
    Set<String> correquisitos = new LinkedHashSet<>();
    if (selecionado != null && g.contem(selecionado)) {
      for (Requisito r : g.requisitos(selecionado)) coletarCorrequisitos(r, correquisitos);
      for (String c : g.getCodigos())
        for (Requisito r : g.requisitos(c)) {
          Set<String> refs = new HashSet<>();
          coletarCorrequisitos(r, refs);
          if (refs.contains(selecionado)) correquisitos.add(c);
        }
    }
    for (var grupo : grupos.entrySet()) {
      JPanel coluna = new JPanel();
      coluna.setLayout(new BoxLayout(coluna, BoxLayout.Y_AXIS));
      coluna.setBorder(Tema.margem(8));
      JLabel titulo =
          new JLabel(grupo.getKey() == 0 ? "Período não informado" : grupo.getKey() + "º período");
      titulo.setBorder(Tema.margem(8));
      coluna.add(titulo);
      for (Disciplina d : grupo.getValue()) {
        String c = d.getCodigo();
        String destaque =
            c.equals(selecionado)
                ? "SELECIONADA"
                : diretos.contains(c)
                    ? "PRÉ-REQUISITO DIRETO"
                    : anteriores.contains(c)
                        ? "PRÉ-REQUISITO INDIRETO"
                        : posteriores.contains(c)
                            ? (dependentesDiretas.contains(c)
                                ? "DEPENDENTE DIRETA"
                                : "DEPENDENTE INDIRETA")
                            : "";
        if (correquisitos.contains(c))
          destaque += (destaque.isEmpty() ? "" : " · ") + "CORREQUISITO (ver regra)";
        String nome = d.getNome().length() > 30 ? d.getNome().substring(0, 28) + "…" : d.getNome();
        String texto =
            "<html><b>"
                + h(c)
                + " · "
                + h(nome)
                + "</b><br>"
                + (d.getCargaHoraria() == null ? "Carga não informada" : d.getCargaHoraria() + " h")
                + " · "
                + h(d.getStatus().toString())
                + "<br>Requisitos: "
                + g.obterPrerequisitos(c).size()
                + " prévios + "
                + g.requisitos(c).size()
                + " regras<br>"
                + destaque
                + "</html>";
        JButton card = Tema.botao(texto, () -> selecionar.accept(c));
        card.setHorizontalAlignment(SwingConstants.LEFT);
        card.setToolTipText(d.toString() + " — duplo clique para editar");
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(300, 115));
        card.setPreferredSize(new Dimension(300, 105));
        card.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                    destaque.isEmpty() ? Tema.LINHA : Tema.VERDE, destaque.isEmpty() ? 1 : 2),
                Tema.margem(8)));
        card.addMouseListener(
            new java.awt.event.MouseAdapter() {
              public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) editar.accept(c);
              }
            });
        coluna.add(card);
        coluna.add(Box.createVerticalStrut(8));
      }
      coluna.add(Box.createVerticalGlue());
      coluna.setAlignmentY(TOP_ALIGNMENT);
      coluna.setPreferredSize(
          new Dimension(320, Math.max(250, 60 + grupo.getValue().size() * 120)));
      coluna.setMaximumSize(new Dimension(320, Integer.MAX_VALUE));
      add(coluna);
    }
    if (grupos.isEmpty()) add(new JLabel("Crie disciplinas ou importe uma matriz para começar."));
    revalidate();
    repaint();
  }

  private void coletarCorrequisitos(Requisito r, Set<String> codigos) {
    if (r.tipo() == Requisito.Tipo.CORREQUISITO) codigos.add(r.codigo());
    for (Requisito item : r.itens()) coletarCorrequisitos(item, codigos);
  }

  private String h(String s) {
    return ExplicadorDidatico.html(s);
  }
}
