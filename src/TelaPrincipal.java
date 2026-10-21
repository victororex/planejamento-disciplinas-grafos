import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public final class TelaPrincipal extends JFrame implements PainelGrafo.Acoes {
    private static final long serialVersionUID = 1L;
    private GrafoDisciplinas grafo = new GrafoDisciplinas();
    private Map<String,VerticeVisual> posicoes = new LinkedHashMap<>();
    private final PainelGrafo painelGrafo = new PainelGrafo(grafo,posicoes,this);
    private final JLabel status = new JLabel("Pronto. Crie disciplinas ou carregue um exemplo para começar.");
    private final JLabel resumo = new JLabel();
    private final JEditorPane detalhes = editor(), adjacencia = editor(), explicacao = editor(), comparacao = editor();
    private final DefaultListModel<Disciplina> cadastro = new DefaultListModel<>();
    private final JList<Disciplina> lista = new JList<>(cadastro);
    private final JTabbedPane abas = new JTabbedPane();
    private final JPanel periodos = new JPanel();
    private final DefaultTableModel tabelaModelo = new DefaultTableModel(new String[]{"Código","Grau residual","Estado DFS"},0) {
        @Override public boolean isCellEditable(int r,int c){return false;}
    };
    private final JTextArea estruturas = new JTextArea();
    private final JCheckBox estudo = new JCheckBox("Modo de estudo",true);
    private final JComboBox<String> algoritmo = new JComboBox<>(new String[]{"Kahn","DFS"});
    private final JLabel contador = new JLabel("Sem execução");
    private final List<AbstractButton> acoes = new ArrayList<>();
    private final List<JButton> navegacao = new ArrayList<>();
    private List<PassoAlgoritmo> passos = List.of();
    private int indice = -1;
    private boolean alterado, ocupado;
    private Path arquivo;
    private ComparadorAlgoritmos.Comparacao ultimaAnalise;

    public TelaPrincipal() {
        super("Planejamento Inteligente · Disciplinas e Grafos");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter(){@Override public void windowClosing(WindowEvent e){if(!ocupado&&confirmarDescarte())dispose();}});
        setMinimumSize(new Dimension(1040,720));
        setSize(1450,940);setLocationRelativeTo(null);
        setLayout(new BorderLayout());add(cabecalho(),BorderLayout.NORTH);
        JSplitPane centroDireita = divisor(JSplitPane.HORIZONTAL_SPLIT,areaGrafo(),informacoes(),.77);
        centroDireita.setDividerLocation(880);
        JSplitPane superior = divisor(JSplitPane.HORIZONTAL_SPLIT,gerenciamento(),centroDireita,0);
        superior.setDividerLocation(205);
        JSplitPane vertical = divisor(JSplitPane.VERTICAL_SPLIT,superior,areaAlgoritmos(),.63);
        vertical.setDividerLocation(430);
        add(vertical,BorderLayout.CENTER);
        status.setBorder(new CompoundBorder(new MatteBorder(1,0,0,0,Tema.LINHA),new EmptyBorder(8,14,8,14)));
        status.setFont(new Font("Segoe UI",Font.PLAIN,12));add(status,BorderLayout.SOUTH);
        instalarAtalhos();atualizarDados();limparResultados();
    }
    private JPanel cabecalho() {
        JPanel p=new JPanel(new BorderLayout(20,0));p.setBackground(Tema.AZUL);p.setBorder(new EmptyBorder(17,22,17,22));
        JPanel titulos=new JPanel(new GridLayout(2,1,0,3));titulos.setOpaque(false);
        JLabel t=new JLabel("Planejamento Inteligente");t.setFont(new Font("Segoe UI",Font.BOLD,23));t.setForeground(Color.WHITE);
        JLabel s=new JLabel("Gerenciamento da oferta de disciplinas  /  Algoritmos em Grafos");s.setForeground(new Color(190,216,228));
        titulos.add(t);titulos.add(s);p.add(titulos,BorderLayout.CENTER);
        resumo.setForeground(new Color(210,232,234));resumo.setFont(new Font("Segoe UI",Font.PLAIN,14));p.add(resumo,BorderLayout.EAST);return p;
    }
    private JScrollPane gerenciamento() {
        JPanel p=new JPanel();p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));p.setBorder(Tema.margem(14));p.setMinimumSize(new Dimension(185,200));
        rotuloGrupo(p,"GERENCIAMENTO");
        lateral(p,"+  Adicionar disciplina",()->formulario(null,null));
        lateral(p,"Editar selecionada",()->comSelecionada(this::editar));
        lateral(p,"Remover selecionada",()->comSelecionada(this::remover));
        espaco(p);rotuloGrupo(p,"PRÉ-REQUISITOS");
        lateral(p,"+  Adicionar relação",()->dialogoDependencia(false));
        lateral(p,"Remover relação",()->dialogoDependencia(true));
        espaco(p);rotuloGrupo(p,"PROJETO");
        lateral(p,"Carregar exemplo",this::carregarExemplo);
        lateral(p,"Salvar projeto",this::salvar);
        lateral(p,"Abrir projeto",this::abrir);
        lateral(p,"Limpar grafo",()->{if(confirmarDescarte()&&confirmar("Limpar todas as disciplinas e ligações?")){substituir(new ProjetoAcademico(new GrafoDisciplinas(),Map.of()),null);alterado=true;atualizarTitulo();}});
        p.add(Box.createVerticalGlue());
        JLabel dica=new JLabel("<html><b>Leia a direção</b><br>A → B significa que A<br>é pré-requisito de B.<br><br>Arrastar um vértice altera<br>somente sua posição.</html>");
        dica.setForeground(Tema.SUAVE);dica.setBorder(new EmptyBorder(18,2,0,0));p.add(dica);
        JScrollPane rolagem=new JScrollPane(p);rolagem.setBorder(null);rolagem.setMinimumSize(new Dimension(185,180));
        rolagem.getVerticalScrollBar().setUnitIncrement(20);rolagem.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return rolagem;
    }
    private JPanel areaGrafo() {
        JPanel p=new JPanel(new BorderLayout());p.setMinimumSize(new Dimension(370,220));
        JToolBar barra=new JToolBar();barra.setFloatable(false);barra.setBackground(Color.WHITE);barra.setBorder(Tema.margem(7));
        ButtonGroup grupo=new ButtonGroup();
        String[] nomes={"Selecionar","Criar","Conectar"};
        for(int i=0;i<nomes.length;i++){
            PainelGrafo.Modo m=PainelGrafo.Modo.values()[i];JToggleButton b=new JToggleButton(nomes[i]);b.setFocusPainted(false);
            b.setSelected(i==0);grupo.add(b);barra.add(b);acoes.add(b);
            b.addActionListener(e->{painelGrafo.definirModo(m);mensagem(switch(m){
                case SELECIONAR -> "Clique para selecionar; arraste para mover; duplo clique para editar.";
                case CRIAR -> "Clique numa área vazia para cadastrar uma disciplina nessa posição.";
                case CONECTAR -> "Clique primeiro no PRÉ-REQUISITO e depois na disciplina DEPENDENTE.";
            });});
        }
        barra.addSeparator();barra.add(Tema.botao("−",()->painelGrafo.definirZoom(painelGrafo.getZoom()/1.15)));
        barra.add(Tema.botao("+",()->painelGrafo.definirZoom(painelGrafo.getZoom()*1.15)));
        barra.add(Tema.botao("Ajustar",painelGrafo::ajustar));p.add(barra,BorderLayout.NORTH);
        JScrollPane scroll=new JScrollPane(painelGrafo);scroll.setBorder(new MatteBorder(1,0,1,0,Tema.LINHA));
        scroll.getVerticalScrollBar().setUnitIncrement(24);scroll.getHorizontalScrollBar().setUnitIncrement(24);p.add(scroll,BorderLayout.CENTER);
        JLabel legenda=new JLabel("  Seleção: verde   ·   DFS: branco / cinza / preto   ·   Ciclo: vermelho");
        legenda.setFont(new Font("Segoe UI",Font.PLAIN,11));legenda.setForeground(Tema.SUAVE);legenda.setBorder(Tema.margem(5));p.add(legenda,BorderLayout.SOUTH);return p;
    }
    private JTabbedPane informacoes() {
        JTabbedPane p=new JTabbedPane();p.setMinimumSize(new Dimension(230,180));p.setPreferredSize(new Dimension(290,450));
        p.addTab("Inspeção",new JScrollPane(detalhes));
        lista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);lista.setFixedCellHeight(38);
        lista.setCellRenderer((l,d,i,s,f)->{
            JLabel label=new JLabel("<html><b>"+ExplicadorDidatico.html(d.getCodigo())+"</b>  "+ExplicadorDidatico.html(d.getNome())+"</html>");
            label.setOpaque(true);label.setBackground(s?new Color(222,241,238):Color.WHITE);label.setForeground(Tema.TINTA);label.setBorder(Tema.margem(5));return label;
        });
        lista.addListSelectionListener(e->{if(!e.getValueIsAdjusting()&&lista.getSelectedValue()!=null)painelGrafo.selecionar(lista.getSelectedValue().getCodigo());});
        lista.addMouseListener(new MouseAdapter(){@Override public void mouseClicked(MouseEvent e){if(!ocupado&&e.getClickCount()==2&&lista.getSelectedValue()!=null)editar(lista.getSelectedValue().getCodigo());}});
        p.addTab("Disciplinas",new JScrollPane(lista));p.addTab("Adjacência",new JScrollPane(adjacencia));return p;
    }
    private JPanel areaAlgoritmos() {
        JPanel p=new JPanel(new BorderLayout());p.setMinimumSize(new Dimension(500,250));
        JPanel barra=new JPanel(new FlowLayout(FlowLayout.LEFT,7,7));
        barra.add(acao("Executar Kahn",()->executar("Kahn")));
        barra.add(acao("Executar DFS",()->executar("DFS")));
        barra.add(acao("Comparar resultados",()->analisar("Comparação")));
        barra.add(acao("Detectar ciclos",()->analisar("Ciclos")));
        barra.add(acao("Planejar períodos",()->analisar("Períodos")));
        estudo.setOpaque(false);estudo.addActionListener(e->{if(indice>=0)mostrarPasso();});barra.add(estudo);p.add(barra,BorderLayout.NORTH);
        abas.addTab("Execução e estudo",areaExecucao());abas.addTab("Comparação",new JScrollPane(comparacao));
        periodos.setLayout(new BoxLayout(periodos,BoxLayout.X_AXIS));periodos.setBackground(Color.WHITE);
        JScrollPane sp=new JScrollPane(periodos);sp.getHorizontalScrollBar().setUnitIncrement(30);abas.addTab("Períodos recomendados",sp);
        JEditorPane teoria=editor();teoria.setText(ExplicadorDidatico.complexidade());teoria.setCaretPosition(0);abas.addTab("Complexidade",new JScrollPane(teoria));
        JEditorPane glossario=editor();glossario.setText(ExplicadorDidatico.glossario());glossario.setCaretPosition(0);abas.addTab("Glossário",new JScrollPane(glossario));
        p.add(abas,BorderLayout.CENTER);return p;
    }
    private JPanel areaExecucao() {
        JPanel p=new JPanel(new BorderLayout());JPanel barra=new JPanel(new FlowLayout(FlowLayout.LEFT,6,5));
        barra.add(algoritmo);barra.add(acao("Iniciar",()->executar((String)algoritmo.getSelectedItem())));
        barra.add(navegador("← Anterior",()->{if(indice>0){indice--;mostrarPasso();}}));
        barra.add(navegador("Próximo →",()->{if(indice+1<passos.size()){indice++;mostrarPasso();}}));
        barra.add(navegador("Até o final",()->{indice=passos.size()-1;mostrarPasso();}));
        barra.add(navegador("Reiniciar visualização",()->{indice=0;mostrarPasso();}));barra.add(contador);p.add(barra,BorderLayout.NORTH);
        JPanel estado=new JPanel(new BorderLayout());JTable tabela=new JTable(tabelaModelo);tabela.setRowHeight(24);
        tabela.setFillsViewportHeight(true);JScrollPane tabelaScroll=new JScrollPane(tabela);tabelaScroll.setPreferredSize(new Dimension(300,100));
        estruturas.setEditable(false);estruturas.setFont(new Font("Consolas",Font.PLAIN,12));estruturas.setRows(5);estruturas.setLineWrap(true);estruturas.setWrapStyleWord(true);estruturas.setBorder(Tema.margem(8));
        JSplitPane estadosDivididos=divisor(JSplitPane.VERTICAL_SPLIT,tabelaScroll,new JScrollPane(estruturas),.45);
        estadosDivididos.setDividerLocation(100);estado.add(estadosDivididos,BorderLayout.CENTER);
        JSplitPane divisor=divisor(JSplitPane.HORIZONTAL_SPLIT,new JScrollPane(explicacao),estado,.58);divisor.setDividerLocation(730);p.add(divisor,BorderLayout.CENTER);return p;
    }
    private JEditorPane editor() {
        JEditorPane e=new JEditorPane("text/html","");e.setEditable(false);e.setBackground(Color.WHITE);e.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES,true);return e;
    }
    private JSplitPane divisor(int sentido,Component a,Component b,double peso) {
        JSplitPane s=new JSplitPane(sentido,a,b);s.setResizeWeight(peso);s.setBorder(null);s.setContinuousLayout(true);return s;
    }
    private JButton acao(String texto,Runnable r){JButton b=Tema.botao(texto,r);acoes.add(b);return b;}
    private JButton navegador(String t,Runnable r){JButton b=Tema.botao(t,r);navegacao.add(b);return b;}
    private void lateral(JPanel p,String t,Runnable r){JButton b=acao(t,r);b.setAlignmentX(Component.LEFT_ALIGNMENT);b.setMaximumSize(new Dimension(400,35));p.add(b);p.add(Box.createVerticalStrut(5));}
    private void espaco(JPanel p){p.add(Box.createVerticalStrut(12));}
    private void rotuloGrupo(JPanel p,String texto){JLabel l=new JLabel(texto);l.setFont(new Font("Segoe UI",Font.BOLD,10));l.setForeground(Tema.SUAVE);l.setBorder(new EmptyBorder(0,2,10,0));p.add(l);}
    private void instalarAtalhos(){
        registrar("control S","salvar",this::salvar);registrar("control O","abrir",this::abrir);
        registrar("control N","nova",()->formulario(null,null));
    }
    private void registrar(String tecla,String nome,Runnable r){getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(tecla),nome);getRootPane().getActionMap().put(nome,new AbstractAction(){public void actionPerformed(ActionEvent e){if(!ocupado)r.run();}});}

    @Override public void selecionar(String c){atualizarDetalhes(c);}
    @Override public void criar(double x,double y){formulario(null,new VerticeVisual(Math.min(10000,x),Math.min(10000,y)));}
    @Override public void editar(String c){formulario(c,null);}
    @Override public void remover(String c){
        if(confirmar("Remover "+grafo.obterDisciplina(c)+" e todas as suas ligações?")){
            grafo.removerDisciplina(c);posicoes.remove(c);painelGrafo.selecionar(null);mudanca("Disciplina e dependências removidas.");
        }
    }
    @Override public void conectar(String a,String b){
        tentar(()->conectarOuFalhar(a,b));
    }
    @Override public void posicaoAlterada(){alterado=true;atualizarTitulo();mensagem("Posição atualizada. As dependências e os resultados matemáticos permanecem válidos.");}
    @Override public void mensagem(String texto){status.setText(texto);status.setToolTipText(texto);}
    private void comSelecionada(Consumer<String> r){String c=painelGrafo.getSelecionado();if(c==null)aviso("Selecione uma disciplina no grafo ou na aba Disciplinas.");else r.accept(c);}
    private void formulario(String antigo,VerticeVisual posicao){
        if(ocupado)return;
        Disciplina atual=antigo==null?null:grafo.obterDisciplina(antigo);
        JTextField codigo=new JTextField(atual==null?"":atual.getCodigo(),18);
        JTextField nome=new JTextField(atual==null?"":atual.getNome(),26);
        JSpinner periodo=new JSpinner(new SpinnerNumberModel(atual==null?1:atual.getPeriodoReferencia(),1,100,1));
        JPanel campos=new JPanel(new GridBagLayout());GridBagConstraints c=new GridBagConstraints();c.insets=new Insets(6,8,6,8);c.fill=GridBagConstraints.HORIZONTAL;c.weightx=1;
        String[] rotulos={"Código único","Nome da disciplina","Período cadastrado"};Component[] controles={codigo,nome,periodo};
        for(int i=0;i<3;i++){c.gridx=0;c.gridy=i;campos.add(new JLabel(rotulos[i]),c);c.gridx=1;campos.add(controles[i],c);}
        c.gridx=0;c.gridy=3;c.gridwidth=2;campos.add(new JLabel("<html>Os pré-requisitos são cadastrados pelo modo Conectar.<br>O período cadastrado é diferente da etapa calculada.</html>"),c);
        while(JOptionPane.showConfirmDialog(this,campos,antigo==null?"Adicionar disciplina":"Editar disciplina",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)==JOptionPane.OK_OPTION){
            try{
                periodo.commitEdit();Disciplina d=new Disciplina(codigo.getText(),nome.getText(),((Number)periodo.getValue()).intValue());
                if(antigo==null){
                    if(grafo.quantidadeVertices()>=GerenciadorArquivos.MAX_VERTICES)throw new IllegalArgumentException("Limite de 2000 disciplinas atingido.");
                    int numero=posicoes.size();
                    VerticeVisual p=posicao==null?new VerticeVisual(140+(numero%20)*240,110+(numero/20)*95):posicao;
                    grafo.adicionarDisciplina(d);
                    posicoes.put(d.getCodigo(),p);
                }else{
                    grafo.editarDisciplina(antigo,d);
                    Map<String,VerticeVisual> novas=new LinkedHashMap<>();
                    posicoes.forEach((chave,v)->novas.put(chave.equals(antigo)?d.getCodigo():chave,v));posicoes=novas;
                    painelGrafo.definirProjeto(grafo,posicoes);
                }
                painelGrafo.selecionar(d.getCodigo());mudanca("Cadastro atualizado: "+d+".");return;
            }catch(java.text.ParseException e){aviso("Informe um período inteiro de 1 a 100.");}
            catch(IllegalArgumentException e){aviso(e.getMessage());}
        }
    }
    private void dialogoDependencia(boolean remover){
        if(grafo.quantidadeVertices()<2){aviso("Cadastre pelo menos duas disciplinas para criar uma dependência.");return;}
        Disciplina[] ds=grafo.getDisciplinas().toArray(Disciplina[]::new);
        JComboBox<Disciplina> origem=new JComboBox<>(ds),destino=new JComboBox<>(ds);destino.setSelectedIndex(1);
        JPanel p=new JPanel(new GridLayout(0,1,0,8));p.add(new JLabel("1. Pré-requisito (origem da seta)"));p.add(origem);p.add(new JLabel("2. Disciplina dependente (destino da seta)"));p.add(destino);
        p.add(new JLabel("A origem precisa ser concluída antes do destino."));
        while(JOptionPane.showConfirmDialog(this,p,remover?"Remover pré-requisito":"Adicionar pré-requisito",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)==JOptionPane.OK_OPTION){
            String a=((Disciplina)origem.getSelectedItem()).getCodigo(),b=((Disciplina)destino.getSelectedItem()).getCodigo();
            try{if(remover){grafo.removerDependencia(a,b);mudanca("Dependência "+a+" → "+b+" removida.");}else conectarOuFalhar(a,b);return;}
            catch(IllegalArgumentException e){aviso(e.getMessage());}
        }
    }
    private void conectarOuFalhar(String a,String b){
        if(grafo.quantidadeArestas()>=GerenciadorArquivos.MAX_ARESTAS)throw new IllegalArgumentException("Limite de 20000 dependências atingido.");
        grafo.adicionarDependencia(a,b);mudanca(grafo.obterDisciplina(a).getNome()+" precisa ser concluída antes de "+grafo.obterDisciplina(b).getNome()+".");
    }
    private void carregarExemplo(){
        Object escolha=JOptionPane.showInputDialog(this,"Escolha um grafo totalmente editável:","Exemplos acadêmicos",JOptionPane.PLAIN_MESSAGE,null,ExemplosAcademicos.NOMES,ExemplosAcademicos.NOMES[0]);
        if(escolha==null||!confirmarDescarte())return;
        int i=Arrays.asList(ExemplosAcademicos.NOMES).indexOf(escolha);
        substituir(ExemplosAcademicos.criar(i),null);alterado=true;atualizarTitulo();mensagem("Exemplo carregado. Todas as disciplinas e relações podem ser editadas.");
    }
    private void substituir(ProjetoAcademico p,Path origem){
        grafo=p.grafo();posicoes=new LinkedHashMap<>();p.posicoes().forEach((c,v)->posicoes.put(c,v.copiar()));
        painelGrafo.definirProjeto(grafo,posicoes);arquivo=origem;alterado=false;limparResultados();atualizarDados();atualizarTitulo();
        SwingUtilities.invokeLater(painelGrafo::ajustar);
    }
    private JFileChooser seletor(){JFileChooser c=new JFileChooser(arquivo==null?Path.of(".").toFile():arquivo.toFile());c.setFileFilter(new FileNameExtensionFilter("Projeto de disciplinas (*.pgd)","pgd"));return c;}
    private void salvar(){
        JFileChooser c=seletor();if(arquivo!=null)c.setSelectedFile(arquivo.toFile());
        if(c.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION)return;
        Path destino=c.getSelectedFile().toPath();if(!destino.toString().toLowerCase(Locale.ROOT).endsWith(".pgd"))destino=Path.of(destino+".pgd");
        if(Files.exists(destino)&&!confirmar("Substituir o arquivo existente "+destino.getFileName()+"?"))return;
        final Path alvo=destino;ProjetoAcademico projeto=new ProjetoAcademico(grafo.copiar(),posicoes);
        tarefa("Salvando projeto…",()->{new GerenciadorArquivos().salvar(alvo,projeto);return alvo;},p->{arquivo=p;alterado=false;atualizarTitulo();mensagem("Projeto salvo em "+p.toAbsolutePath());});
    }
    private void abrir(){
        if(!confirmarDescarte())return;JFileChooser c=seletor();if(c.showOpenDialog(this)!=JFileChooser.APPROVE_OPTION)return;
        Path p=c.getSelectedFile().toPath();tarefa("Abrindo e validando o projeto…",()->new GerenciadorArquivos().abrir(p),projeto->{substituir(projeto,p);mensagem("Projeto aberto e validado: "+p.getFileName());});
    }
    /** Permite iniciar por um arquivo informado na linha de comando, usando a mesma validação da interface. */
    public void abrirInicial(Path p){
        tarefa("Abrindo e validando o projeto…",()->new GerenciadorArquivos().abrir(p),projeto->{substituir(projeto,p);mensagem("Projeto aberto e validado: "+p.getFileName());});
    }
    private boolean confirmarDescarte(){return !alterado||confirmar("Há alterações não salvas. Deseja continuar e descartá-las? Use Cancelar para voltar e salvar.");}
    private boolean confirmar(String texto){return JOptionPane.showConfirmDialog(this,texto,"Confirmar operação",JOptionPane.OK_CANCEL_OPTION,JOptionPane.QUESTION_MESSAGE)==JOptionPane.OK_OPTION;}
    private void aviso(String t){JOptionPane.showMessageDialog(this,t,"Planejamento de disciplinas",JOptionPane.INFORMATION_MESSAGE);}
    private void tentar(Runnable r){try{r.run();}catch(IllegalArgumentException e){aviso(e.getMessage());}}
    private void mudanca(String texto){alterado=true;limparResultados();atualizarDados();painelGrafo.atualizarTamanho();painelGrafo.repaint();atualizarTitulo();mensagem(texto);}
    private void atualizarTitulo(){setTitle((alterado?"● ":"")+(arquivo==null?"Novo projeto":arquivo.getFileName())+" · Planejamento Inteligente");}
    private void atualizarDados(){
        resumo.setText(grafo.quantidadeVertices()+" disciplinas   ·   "+grafo.quantidadeArestas()+" dependências");
        cadastro.clear();grafo.getDisciplinas().forEach(cadastro::addElement);
        StringBuilder s=new StringBuilder("<h2>Lista de adjacência</h2><p>A chave é o pré-requisito; seus vizinhos dependem dela.</p>");
        grafo.listaAdjacencia().forEach((c,vs)->s.append("<p><b>").append(ExplicadorDidatico.html(c)).append(" → </b>").append(ExplicadorDidatico.html(vs.isEmpty()?"nenhum":String.join(", ",vs))).append("</p>"));
        adjacencia.setText(ExplicadorDidatico.documento(s.toString()));adjacencia.setCaretPosition(0);atualizarDetalhes(painelGrafo.getSelecionado());
    }
    private void atualizarDetalhes(String c){
        StringBuilder s=new StringBuilder("<h2>Inspeção do grafo</h2>");
        if(c!=null&&grafo.contem(c)){
            Disciplina d=grafo.obterDisciplina(c);s.append("<h3>").append(ExplicadorDidatico.html(d.toString())).append("</h3><p><b>Período cadastrado:</b> ").append(d.getPeriodoReferencia()).append("</p>");
            if(ultimaAnalise!=null&&ultimaAnalise.planejamento()!=null)s.append("<p><b>Etapa recomendada:</b> ").append(ultimaAnalise.planejamento().etapas().get(c)).append("</p>");
            s.append("<p><b>Pré-requisitos diretos:</b> ").append(ExplicadorDidatico.html(String.join(", ",grafo.obterPrerequisitos(c)))).append(grafo.grauEntrada(c)==0?"nenhum":"").append("</p>");
            s.append("<p><b>Grau de entrada:</b> ").append(grafo.grauEntrada(c)).append("<br><b>Grau de saída:</b> ").append(grafo.grauSaida(c)).append("</p>");
            s.append("<p><b>Dependentes:</b> ").append(ExplicadorDidatico.html(String.join(", ",grafo.obterVizinhos(c)))).append(grafo.grauSaida(c)==0?"nenhum":"").append("</p>");
        }else s.append("<p>Selecione uma disciplina para ver seus dados e dependências.</p>");
        List<Set<String>> componentes=grafo.componentesFracas();
        s.append("<h3>Informações gerais</h3><p>V = ").append(grafo.quantidadeVertices()).append("<br>E = ").append(grafo.quantidadeArestas()).append("<br>Componentes fracas = ").append(componentes.size()).append("</p>");
        for(int i=0;i<componentes.size();i++)s.append("<p>Grupo ").append(i+1).append(": ").append(ExplicadorDidatico.html(String.join(", ",componentes.get(i)))).append("</p>");
        s.append("<p class='suave'>A contagem ignora a direção apenas para identificar grupos conectados. Componentes separadas não implicam ciclos.</p>");
        detalhes.setText(ExplicadorDidatico.documento(s.toString()));detalhes.setCaretPosition(0);
    }
    private void limparResultados(){
        passos=List.of();indice=-1;ultimaAnalise=null;painelGrafo.limparAnalise();tabelaModelo.setRowCount(0);estruturas.setText("");contador.setText("Sem execução");
        explicacao.setText(ExplicadorDidatico.documento("<h2>Explore os algoritmos</h2><p>Escolha Kahn ou DFS e clique em Iniciar. Avance e volte pelos estados reais do algoritmo.</p><p>O modo de estudo explica a escolha, a regra aplicada, a mudança e o próximo passo.</p>"));
        comparacao.setText(ExplicadorDidatico.documento("<h2>Kahn × DFS</h2><p>Clique em Comparar resultados para analisar o grafo atual.</p>"));
        periodos.removeAll();periodos.add(new JLabel("   Clique em Planejar períodos para calcular as etapas por dependências."));periodos.revalidate();periodos.repaint();atualizarNavegacao();
    }
    private record Execucao(List<PassoAlgoritmo> passos,ResultadoOrdenacao resultado,ComparadorAlgoritmos.Comparacao comparacao,boolean historico) { }
    private void executar(String tipo){
        if(ocupado)return;algoritmo.setSelectedItem(tipo);GrafoDisciplinas copia=grafo.copiar();
        long estimativa=3L*copia.quantidadeVertices()+copia.quantidadeArestas()+2;
        boolean historico=estimativa<=2000&&estimativa*Math.max(1,copia.quantidadeVertices())<=120000;
        tarefa("Executando "+tipo+"…",()->{
            List<PassoAlgoritmo> ps=new ArrayList<>();Consumer<PassoAlgoritmo> o=historico?ps::add:null;
            ResultadoOrdenacao r=tipo.equals("Kahn")?new AlgoritmoKahn().executar(copia,o):new AlgoritmoDFS().executar(copia,o);
            return new Execucao(List.copyOf(ps),r,new ComparadorAlgoritmos().comparar(copia),historico);
        },r->{
            ultimaAnalise=r.comparacao();preencherComparacao();preencherPeriodos();atualizarDetalhes(painelGrafo.getSelecionado());
            passos=r.passos();indice=passos.isEmpty()?-1:0;abas.setSelectedIndex(0);
            if(indice>=0)mostrarPasso();else{
                explicacao.setText(ExplicadorDidatico.documento("<h2>"+tipo+" · resultado final</h2><p>"+(r.resultado().possuiCiclo()?"Dependência circular detectada; não há ordenação válida.":"Ordenação: "+ExplicadorDidatico.html(ExplicadorDidatico.ordem(r.resultado().ordem())))+"</p><p>Histórico desativado para este tamanho de grafo. Consulte a comparação e o planejamento completos.</p>"));
                painelGrafo.mostrarCiclo(r.comparacao().ciclos().caminho());atualizarNavegacao();
            }
            mensagem(r.historico()?tipo+" pronto: "+passos.size()+" estados. Use Próximo, Anterior ou Até o final.":"Análise concluída sem histórico para limitar o uso de memória.");
        });
    }
    private void analisar(String finalidade){
        if(ocupado)return;GrafoDisciplinas copia=grafo.copiar();
        tarefa("Analisando o grafo…",()->new ComparadorAlgoritmos().comparar(copia),r->{
            ultimaAnalise=r;preencherComparacao();preencherPeriodos();atualizarDetalhes(painelGrafo.getSelecionado());
            if(finalidade.equals("Períodos")){abas.setSelectedIndex(2);if(r.planejamento()!=null)painelGrafo.mostrarEtapas(r.planejamento().etapas());else painelGrafo.mostrarCiclo(r.ciclos().caminho());}
            else{abas.setSelectedIndex(1);painelGrafo.mostrarCiclo(r.ciclos().caminho());}
            mensagem(r.ciclos().possuiCiclo()?"Não é possível gerar um planejamento válido enquanto existir uma dependência circular.":"Grafo acíclico. As duas ordenações foram verificadas independentemente.");
        });
    }
    private void mostrarPasso(){
        if(indice<0||indice>=passos.size())return;PassoAlgoritmo p=passos.get(indice);painelGrafo.mostrarPasso(p);
        String texto=ExplicadorDidatico.passo(p,indice,passos.size(),estudo.isSelected());
        if(p.algoritmo().equals("Kahn")&&p.evento().equals("CICLO")&&ultimaAnalise!=null){
            var ciclos=ultimaAnalise.ciclos();painelGrafo.destacarCiclo(ciclos.caminho());
            texto=texto.replace("</body>","<p><b>Ciclo reconstruído pela DFS:</b> "+ExplicadorDidatico.html(ExplicadorDidatico.ordem(ciclos.caminho()))
                    +"<br><b>Em ciclos:</b> "+ExplicadorDidatico.html(ciclos.emCiclos().toString())+"<br><b>Apenas bloqueados:</b> "+ExplicadorDidatico.html(ciclos.apenasBloqueados().toString())+"</p></body>");
        }
        explicacao.setText(texto);explicacao.setCaretPosition(0);
        contador.setText((indice+1)+" / "+passos.size());tabelaModelo.setRowCount(0);
        for(String c:grafo.getCodigos())tabelaModelo.addRow(new Object[]{c,p.graus().getOrDefault(c,null),p.cores().containsKey(c)?p.cores().get(c):"—"});
        estruturas.setText("Atual: "+(p.atual()==null?"—":p.atual())+"\nFila: "+p.fila()+"\nPilha / caminho ativo (base → topo): "+p.pilha()+"\nFinalização: "+p.finalizados()+"\n"+(p.algoritmo().equals("Kahn")?"Ordem construída (parcial até o fim): ":"Ordem topológica (somente no fim sem ciclo): ")+p.ordem());estruturas.setCaretPosition(0);atualizarNavegacao();
    }
    private void atualizarNavegacao(){
        for(JButton b:navegacao)b.setEnabled(!ocupado&&!passos.isEmpty());
        if(navegacao.size()>=2){navegacao.get(0).setEnabled(!ocupado&&indice>0);navegacao.get(1).setEnabled(!ocupado&&indice>=0&&indice+1<passos.size());}
    }
    private void preencherComparacao(){
        var c=ultimaAnalise;StringBuilder s=new StringBuilder("<h2>Comparação no grafo atual</h2><table width='100%'><tr><th>Aspecto</th><th>Kahn</th><th>DFS</th></tr>");
        linha(s,"Estratégia","Graus de entrada + fila FIFO","Profundidade + pilha e três cores");
        linha(s,"Ordenação",c.kahn().possuiCiclo()?"Indisponível (ciclo)":ExplicadorDidatico.ordem(c.kahn().ordem()),c.dfs().possuiCiclo()?"Indisponível (ciclo)":ExplicadorDidatico.ordem(c.dfs().ordem()));
        linha(s,"Verificação independente",c.kahnValida()?"Válida":"Não há ordem válida",c.dfsValida()?"Válida":"Não há ordem válida");
        linha(s,"Vértices processados",c.kahn().processados().size()+" / "+grafo.quantidadeVertices(),c.dfs().processados().size()+" visitados / "+grafo.quantidadeVertices());
        linha(s,"Detecção de ciclo",c.kahn().possuiCiclo()?"Fila vazia antes de processar todos":"Todos processados",c.dfs().possuiCiclo()?"Aresta alcançou vértice cinza":"Nenhuma aresta para cinza");
        linha(s,"Ordem de finalização","Não utiliza",ExplicadorDidatico.ordem(c.dfs().finalizacao()));s.append("</table>");
        if(c.ciclos().possuiCiclo()){
            s.append("<h3 class='alerta'>Não é possível gerar um planejamento válido enquanto existir uma dependência circular.</h3><p><b>Ciclo real destacado:</b> ").append(ExplicadorDidatico.html(ExplicadorDidatico.ordem(c.ciclos().caminho()))).append("</p>");
            s.append("<p><b>Disciplinas em algum ciclo:</b> ").append(ExplicadorDidatico.html(String.join(", ",c.ciclos().emCiclos()))).append("<br><b>Apenas bloqueadas por ciclos:</b> ").append(c.ciclos().apenasBloqueados().isEmpty()?"nenhuma":ExplicadorDidatico.html(String.join(", ",c.ciclos().apenasBloqueados()))).append("</p><p>O vermelho mostra as arestas do primeiro ciclo reconstruído. A lista identifica também participantes de outros ciclos. Corrija as relações e execute novamente.</p>");
        }else s.append("<p><b>").append(c.kahn().ordem().equals(c.dfs().ordem())?"As ordenações coincidiram.":"As ordenações são diferentes e ambas estão corretas.").append("</b> O verificador confere se cada pré-requisito aparece antes do seu destino. Disciplinas independentes podem trocar de posição.</p>");
        comparacao.setText(ExplicadorDidatico.documento(s.toString()));comparacao.setCaretPosition(0);
    }
    private void linha(StringBuilder s,String titulo,String k,String d){s.append("<tr><td><b>").append(ExplicadorDidatico.html(titulo)).append("</b></td><td>").append(ExplicadorDidatico.html(k)).append("</td><td>").append(ExplicadorDidatico.html(d)).append("</td></tr>");}
    private void preencherPeriodos(){
        periodos.removeAll();var p=ultimaAnalise.planejamento();
        if(p==null){JEditorPane e=editor();e.setText(ExplicadorDidatico.documento("<h2 class='alerta'>Planejamento indisponível</h2><p>Não é possível gerar um planejamento válido enquanto existir uma dependência circular.</p><p>Consulte Comparação para identificar o ciclo e corrigir suas ligações.</p>"));periodos.add(e);}
        else if(p.grupos().isEmpty())periodos.add(new JLabel("   O grafo está vazio. Cadastre disciplinas para planejar."));
        else{
            for(int i=0;i<p.grupos().size();i++){
                JPanel coluna=new JPanel(new BorderLayout());coluna.setBackground(Color.WHITE);coluna.setBorder(new CompoundBorder(Tema.margem(10),new LineBorder(Tema.LINHA,1,true)));
                JLabel titulo=new JLabel("  ETAPA RECOMENDADA "+(i+1));titulo.setOpaque(true);titulo.setBackground(new Color(226,241,238));titulo.setForeground(Tema.VERDE);titulo.setBorder(Tema.margem(10));titulo.setFont(new Font("Segoe UI",Font.BOLD,12));coluna.add(titulo,BorderLayout.NORTH);
                JEditorPane texto=editor();StringBuilder h=new StringBuilder();
                for(String c:p.grupos().get(i)){
                    Disciplina d=grafo.obterDisciplina(c);h.append("<p><b>").append(ExplicadorDidatico.html(d.toString())).append("</b><br><span class='suave'>Período cadastrado: ").append(d.getPeriodoReferencia()).append("<br>Pré-requisitos: ").append(grafo.obterPrerequisitos(c).isEmpty()?"nenhum":ExplicadorDidatico.html(String.join(", ",grafo.obterPrerequisitos(c)))).append("</span></p>");
                }
                texto.setText(ExplicadorDidatico.documento(h.toString()));texto.setCaretPosition(0);coluna.add(new JScrollPane(texto),BorderLayout.CENTER);
                JLabel nota=new JLabel("<html>  etapa(v) = 1 + máximo das etapas<br>  dos pré-requisitos; fontes = 1.</html>");nota.setFont(new Font("Segoe UI",Font.PLAIN,11));nota.setBorder(Tema.margem(7));coluna.add(nota,BorderLayout.SOUTH);
                coluna.setPreferredSize(new Dimension(285,260));coluna.setMinimumSize(new Dimension(250,200));coluna.setMaximumSize(new Dimension(310,Integer.MAX_VALUE));periodos.add(coluna);
                if(i+1<p.grupos().size()){JLabel seta=new JLabel("→");seta.setFont(new Font("Segoe UI",Font.BOLD,22));seta.setForeground(Tema.VERDE);periodos.add(seta);}
            }
            JLabel nota=new JLabel("<html>Planejamento lógico.<br><br>Sem restrições de vagas,<br>carga horária ou horários.<br><br>Pré-requisitos sempre<br>ficam em etapas anteriores.</html>");nota.setBorder(Tema.margem(15));nota.setForeground(Tema.SUAVE);periodos.add(nota);
        }
        periodos.revalidate();periodos.repaint();
    }
    private <T> void tarefa(String texto,Callable<T> trabalho,Consumer<T> concluido){
        definirOcupado(true);mensagem(texto);
        new SwingWorker<T,Void>(){
            protected T doInBackground()throws Exception{return trabalho.call();}
            protected void done(){
                definirOcupado(false);
                try{concluido.accept(get());}
                catch(Exception e){Throwable causa=e.getCause()==null?e:e.getCause();aviso("Não foi possível concluir a operação. "+causa.getMessage());mensagem("Operação não concluída; o projeto continua disponível.");}
            }
        }.execute();
    }
    private void definirOcupado(boolean b){ocupado=b;acoes.forEach(a->a.setEnabled(!b));painelGrafo.setEnabled(!b);lista.setEnabled(!b);algoritmo.setEnabled(!b);atualizarNavegacao();setCursor(Cursor.getPredefinedCursor(b?Cursor.WAIT_CURSOR:Cursor.DEFAULT_CURSOR));}
}
