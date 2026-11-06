import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

/** Formulários dedicados; validação de domínio permanece nos modelos. */
public final class FormulariosCurriculares {
  private FormulariosCurriculares() {}

  private static JTextField campo(Object valor) {
    return new JTextField(valor == null ? "" : valor.toString(), 25);
  }

  private static JPanel grade(String[] nomes, JComponent... campos) {
    JPanel p = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(5, 6, 5, 6);
    c.fill = GridBagConstraints.HORIZONTAL;
    for (int i = 0; i < campos.length; i++) {
      c.gridy = i;
      c.gridx = 0;
      c.weightx = 0;
      JLabel label = new JLabel(nomes[i]);
      label.setLabelFor(campos[i]);
      campos[i].setName(nomes[i]);
      p.add(label, c);
      c.gridx = 1;
      c.weightx = 1;
      p.add(campos[i], c);
    }
    return p;
  }

  private static boolean confirmar(Component pai, Component p, String titulo) {
    JScrollPane scroll = new JScrollPane(p);
    scroll.setPreferredSize(new Dimension(620, Math.min(620, p.getPreferredSize().height + 20)));
    return JOptionPane.showConfirmDialog(
            pai, scroll, titulo, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
        == JOptionPane.OK_OPTION;
  }

  private static Double decimal(JTextField f) {
    return f.getText().isBlank() ? null : Double.valueOf(f.getText().strip().replace(',', '.'));
  }

  private static Integer inteiro(JTextField f) {
    return f.getText().isBlank() ? null : Integer.valueOf(f.getText().strip());
  }

  public static Disciplina disciplina(Component pai, Disciplina d) {
    JTextField codigo = campo(d == null ? null : d.getCodigo()),
        nome = campo(d == null ? null : d.getNome()),
        periodo =
            campo(d == null || d.getPeriodoReferencia() == 0 ? null : d.getPeriodoReferencia());
    JTextField horas = campo(d == null ? null : d.getCargaHoraria()),
        creditos = campo(d == null ? null : d.getCreditos());
    JComboBox<String> categoria = new JComboBox<>(Disciplina.CATEGORIAS);
    categoria.setEditable(true);
    categoria.setSelectedItem(d == null ? "" : d.getCategoria());
    JComboBox<StatusAcademico> status = new JComboBox<>(StatusAcademico.values());
    status.setSelectedItem(d == null ? StatusAcademico.NAO_CURSADA : d.getStatus());
    JTextArea descricao = new JTextArea(d == null ? "" : d.getDescricao(), 3, 25),
        obs = new JTextArea(d == null ? "" : d.getObservacoes(), 3, 25);
    JTextField oferta = campo(d == null ? null : d.getOferta());
    JPanel p =
        grade(
            new String[] {
              "Código *",
              "Nome *",
              "Período oficial (vazio = desconhecido)",
              "Carga horária total",
              "Créditos",
              "Categoria (aceita texto livre)",
              "Status acadêmico",
              "Descrição",
              "Observações",
              "Tipo de oferta"
            },
            codigo,
            nome,
            periodo,
            horas,
            creditos,
            categoria,
            status,
            new JScrollPane(descricao),
            new JScrollPane(obs),
            oferta);
    while (confirmar(pai, p, d == null ? "Adicionar disciplina" : "Editar disciplina"))
      try {
        Integer per = inteiro(periodo);
        return new Disciplina(
            codigo.getText(),
            nome.getText(),
            per == null ? 0 : per,
            decimal(horas),
            decimal(creditos),
            Objects.toString(categoria.getSelectedItem(), ""),
            descricao.getText(),
            obs.getText(),
            oferta.getText(),
            (StatusAcademico) status.getSelectedItem());
      } catch (IllegalArgumentException e) {
        JOptionPane.showMessageDialog(pai, "Confira os campos: " + e.getMessage());
      }
    return null;
  }

  public static MatrizCurricular matriz(Component pai, MatrizCurricular m) {
    JTextField instituicao = campo(m.instituicao()),
        curso = campo(m.curso()),
        codigo = campo(m.codigoCurso()),
        versao = campo(m.versao()),
        ano = campo(m.ano()),
        campus = campo(m.campus()),
        turno = campo(m.turno()),
        periodos = campo(m.periodos()),
        fonte = campo(m.fonte());
    JTextArea obs = new JTextArea(m.observacoes(), 4, 25);
    JPanel p =
        grade(
            new String[] {
              "Instituição",
              "Curso",
              "Código do curso",
              "Versão da matriz",
              "Ano de vigência",
              "Campus",
              "Turno",
              "Quantidade de períodos",
              "Descrição / observações",
              "Fonte oficial (opcional)"
            },
            instituicao,
            curso,
            codigo,
            versao,
            ano,
            campus,
            turno,
            periodos,
            new JScrollPane(obs),
            fonte);
    while (confirmar(pai, p, "Identificação da matriz — campos vazios permitem projeto livre"))
      try {
        return new MatrizCurricular(
            instituicao.getText(),
            curso.getText(),
            codigo.getText(),
            versao.getText(),
            inteiro(ano),
            campus.getText(),
            turno.getText(),
            inteiro(periodos),
            obs.getText(),
            fonte.getText());
      } catch (IllegalArgumentException e) {
        JOptionPane.showMessageDialog(pai, "Confira os campos: " + e.getMessage());
      }
    return null;
  }

  public static List<Requisito> requisitos(Component pai, GrafoDisciplinas g, String codigo) {
    DefaultListModel<Requisito> modelo = new DefaultListModel<>();
    g.requisitos(codigo).forEach(modelo::addElement);
    JList<Requisito> regras = new JList<>(modelo);
    regras.setVisibleRowCount(7);
    String[] tipos = {
      "Concluída antes (regra adicional)",
      "Correquisito: antes ou simultâneo",
      "Mínimo de horas concluídas",
      "Mínimo de créditos concluídos",
      "Conclusão de atividade"
    };
    JComboBox<String> tipo = new JComboBox<>(tipos);
    JComboBox<String> referencia =
        new JComboBox<>(
            g.getCodigos().stream().filter(c -> !c.equals(codigo)).toArray(String[]::new));
    JTextField minimo = campo(null);
    JPanel p = new JPanel(new BorderLayout(8, 8));
    p.add(
        grade(
            new String[] {"Regra", "Disciplina / atividade", "Mínimo (somente horas/créditos)"},
            tipo,
            referencia,
            minimo),
        BorderLayout.NORTH);
    p.add(new JScrollPane(regras), BorderLayout.CENTER);
    JPanel botoes = new JPanel(new GridLayout(0, 2, 5, 5));
    botoes.add(
        Tema.botao(
            "Adicionar regra",
            () -> {
              try {
                Requisito.Tipo t =
                    new Requisito.Tipo[] {
                          Requisito.Tipo.CONCLUIDA,
                          Requisito.Tipo.CORREQUISITO,
                          Requisito.Tipo.HORAS,
                          Requisito.Tipo.CREDITOS,
                          Requisito.Tipo.ATIVIDADE
                        }
                        [tipo.getSelectedIndex()];
                modelo.addElement(
                    t == Requisito.Tipo.HORAS || t == Requisito.Tipo.CREDITOS
                        ? new Requisito(t, "", decimal(minimo), List.of())
                        : Requisito.disciplina(
                            t, Objects.toString(referencia.getSelectedItem(), "")));
              } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(pai, e.getMessage());
              }
            }));
    botoes.add(
        Tema.botao(
            "Remover selecionadas",
            () -> {
              int[] ids = regras.getSelectedIndices();
              for (int i = ids.length - 1; i >= 0; i--) modelo.remove(ids[i]);
            }));
    for (Requisito.Tipo t : List.of(Requisito.Tipo.AND, Requisito.Tipo.OR))
      botoes.add(
          Tema.botao(
              t == Requisito.Tipo.AND ? "Agrupar selecionadas: E" : "Agrupar selecionadas: OU",
              () -> {
                List<Requisito> itens = regras.getSelectedValuesList();
                if (itens.size() < 2) {
                  JOptionPane.showMessageDialog(
                      pai, "Selecione pelo menos duas regras (Ctrl + clique).");
                  return;
                }
                try {
                  Requisito grupo = new Requisito(t, "", null, itens);
                  int[] ids = regras.getSelectedIndices();
                  for (int i = ids.length - 1; i >= 0; i--) modelo.remove(ids[i]);
                  modelo.addElement(grupo);
                } catch (IllegalArgumentException e) {
                  JOptionPane.showMessageDialog(pai, e.getMessage());
                }
              }));
    JPanel sul = new JPanel(new BorderLayout());
    sul.add(botoes, BorderLayout.NORTH);
    sul.add(
        new JLabel(
            "<html>As regras de topo são conjuntivas (E). Use Ctrl para agrupar"
                + " alternativas.<br>Pré-requisitos do grafo continuam obrigatórios. Correquisitos"
                + " não viram arestas topológicas.</html>"),
        BorderLayout.SOUTH);
    p.add(sul, BorderLayout.SOUTH);
    return confirmar(pai, p, "Requisitos de " + codigo)
        ? Collections.list(modelo.elements())
        : null;
  }
}
