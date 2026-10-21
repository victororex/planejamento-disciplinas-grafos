import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

/** Desenho e interação. Nenhum algoritmo de grafos é implementado neste painel. */
public final class PainelGrafo extends JPanel {
    private static final long serialVersionUID = 1L;
    public enum Modo { SELECIONAR, CRIAR, CONECTAR }
    public interface Acoes {
        void selecionar(String codigo);
        void criar(double x, double y);
        void editar(String codigo);
        void remover(String codigo);
        void conectar(String origem, String destino);
        void posicaoAlterada();
        void mensagem(String texto);
    }
    static final double LARGURA = 174, ALTURA = 78;
    private GrafoDisciplinas grafo;
    private Map<String, VerticeVisual> posicoes;
    private final Acoes acoes;
    private Modo modo = Modo.SELECIONAR;
    private String selecionado, origemConexao, arrastado;
    private double dx,dy, zoom = 1;
    private boolean moveu;
    private PassoAlgoritmo passo;
    private List<String> ciclo = List.of();
    private Map<String,Integer> etapas = Map.of();
    private Set<String> processados = Set.of();

    public PainelGrafo(GrafoDisciplinas g, Map<String, VerticeVisual> p, Acoes acoes) {
        this.grafo=g; this.posicoes=p; this.acoes=acoes;
        setBackground(Tema.CAMPO); setFocusable(true);
        ToolTipManager.sharedInstance().registerComponent(this);
        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (!isEnabled()) return;
                requestFocusInWindow(); String c = encontrar(e.getX()/zoom,e.getY()/zoom);
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) { menu(e,c); return; }
                if (!SwingUtilities.isLeftMouseButton(e)) return;
                if (modo == Modo.CRIAR && c == null) { acoes.criar(Math.max(90,e.getX()/zoom),Math.max(45,e.getY()/zoom)); return; }
                selecionar(c);
                if (modo == Modo.CONECTAR && c != null) {
                    if (origemConexao == null) {
                        origemConexao = c;
                        acoes.mensagem(c+" selecionada como pré-requisito. Clique agora na disciplina que depende dela. Esc cancela.");
                    } else {
                        String origem = origemConexao; origemConexao = null; acoes.conectar(origem,c);
                    }
                    repaint(); return;
                }
                if (modo == Modo.SELECIONAR && c != null) {
                    if (e.getClickCount() == 2) { acoes.editar(c); return; }
                    arrastado=c; moveu=false; VerticeVisual v=posicoes.get(c);
                    dx=e.getX()/zoom-v.getX(); dy=e.getY()/zoom-v.getY();
                }
            }
            @Override public void mouseDragged(MouseEvent e) {
                if (arrastado==null || !isEnabled()) return;
                posicoes.get(arrastado).mover(Math.max(90,Math.min(10000,e.getX()/zoom-dx)),
                        Math.max(45,Math.min(10000,e.getY()/zoom-dy)));
                moveu=true; atualizarTamanho(); repaint();
            }
            @Override public void mouseReleased(MouseEvent e) {
                if (arrastado!=null && moveu) acoes.posicaoAlterada();
                arrastado=null;
            }
            @Override public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown()) { definirZoom(zoom*(e.getWheelRotation()<0?1.12:1/1.12)); e.consume(); }
            }
        };
        addMouseListener(mouse); addMouseMotionListener(mouse); addMouseWheelListener(mouse);
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),"cancelar");
        getActionMap().put("cancelar",new AbstractAction(){public void actionPerformed(ActionEvent e){origemConexao=null; repaint();}});
        getInputMap(WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0),"remover");
        getActionMap().put("remover",new AbstractAction(){public void actionPerformed(ActionEvent e){if(isEnabled()&&selecionado!=null)acoes.remover(selecionado);}});
        atualizarTamanho();
    }
    private void menu(MouseEvent e, String c) {
        if (c==null) return;
        selecionar(c); JPopupMenu m=new JPopupMenu();
        JMenuItem editar=new JMenuItem("Editar disciplina"); editar.addActionListener(a->acoes.editar(c));m.add(editar);
        JMenuItem remover=new JMenuItem("Remover disciplina");remover.addActionListener(a->acoes.remover(c));m.add(remover);
        m.show(this,e.getX(),e.getY());
    }
    public void definirProjeto(GrafoDisciplinas g, Map<String,VerticeVisual> p) {
        grafo=g;posicoes=p;selecionado=null;origemConexao=null;limparAnalise();atualizarTamanho();repaint();
    }
    public void definirModo(Modo m) { modo=m;origemConexao=null;setCursor(Cursor.getPredefinedCursor(m==Modo.SELECIONAR?Cursor.DEFAULT_CURSOR:Cursor.CROSSHAIR_CURSOR));repaint(); }
    public void selecionar(String c) { selecionado=c;acoes.selecionar(c);repaint(); }
    public String getSelecionado() { return selecionado; }
    public void limparAnalise() { passo=null;ciclo=List.of();etapas=Map.of();processados=Set.of();repaint(); }
    public void mostrarPasso(PassoAlgoritmo p) { passo=p;ciclo=p.ciclo();etapas=Map.of();processados=new HashSet<>(p.ordem());repaint(); }
    public void mostrarCiclo(List<String> c) { passo=null;ciclo=List.copyOf(c);etapas=Map.of();processados=Set.of();repaint(); }
    public void destacarCiclo(List<String> c) { ciclo=List.copyOf(c);repaint(); }
    public void mostrarEtapas(Map<String,Integer> e) { limparAnalise();etapas=e;repaint(); }
    public void definirZoom(double z) { zoom=Math.max(0.25,Math.min(2,z));atualizarTamanho();repaint(); }
    public double getZoom() { return zoom; }
    public void ajustar() {
        double maxX=360,maxY=240;
        for(VerticeVisual p:posicoes.values()){maxX=Math.max(maxX,p.getX()+LARGURA/2+35);maxY=Math.max(maxY,p.getY()+ALTURA/2+35);}
        Dimension area=getParent() instanceof JViewport v?v.getExtentSize():getSize();
        definirZoom(Math.min(1.15,Math.min(area.width/maxX,area.height/maxY)));
        if(getParent() instanceof JViewport v)v.setViewPosition(new Point(0,0));
    }
    public void atualizarTamanho() {
        double x=850,y=520;
        for(VerticeVisual p:posicoes.values()){x=Math.max(x,p.getX()+LARGURA);y=Math.max(y,p.getY()+ALTURA);}
        setPreferredSize(new Dimension((int)Math.ceil(x*zoom),(int)Math.ceil(y*zoom)));revalidate();
    }
    private String encontrar(double x,double y) {
        List<String> codigos=new ArrayList<>(posicoes.keySet());Collections.reverse(codigos);
        for(String c:codigos)if(forma(posicoes.get(c)).contains(x,y))return c;
        return null;
    }
    @Override public String getToolTipText(MouseEvent e) {
        String c=encontrar(e.getX()/zoom,e.getY()/zoom);
        if(c==null)return "Clique para selecionar; arraste para mover. No modo Criar, clique numa área vazia.";
        Disciplina d=grafo.obterDisciplina(c);
        return "<html>"+ExplicadorDidatico.html(d.toString())+"<br>Período cadastrado: "+d.getPeriodoReferencia()+"<br>Duplo clique para editar.</html>";
    }
    private static RoundRectangle2D forma(VerticeVisual p) {
        return new RoundRectangle2D.Double(p.getX()-LARGURA/2,p.getY()-ALTURA/2,LARGURA,ALTURA,18,18);
    }
    /** Interseção do raio com a borda arredondada real, não com um ponto aproximado no centro. */
    static Point2D borda(VerticeVisual p,double alvoX,double alvoY) {
        double vx=alvoX-p.getX(),vy=alvoY-p.getY();
        if(Math.hypot(vx,vy)<0.001){vx=1;vy=0;}
        double comprimento=Math.hypot(vx,vy);vx/=comprimento;vy/=comprimento;
        double baixo=0,alto=LARGURA+ALTURA;Shape s=forma(p);
        for(int i=0;i<40;i++){double meio=(baixo+alto)/2;if(s.contains(p.getX()+vx*meio,p.getY()+vy*meio))baixo=meio;else alto=meio;}
        return new Point2D.Double(p.getX()+vx*alto,p.getY()+vy*alto);
    }
    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g=(Graphics2D)graphics.create();g.scale(zoom,zoom);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Tema.GRADE);
        Rectangle clip=g.getClipBounds();
        for(int x=Math.max(0,(clip.x/28)*28);x<clip.x+clip.width;x+=28)
            for(int y=Math.max(0,(clip.y/28)*28);y<clip.y+clip.height;y+=28)g.fillOval(x,y,2,2);
        Map<String,Set<String>> adj=grafo.listaAdjacencia();
        adj.forEach((a,vs)->vs.forEach(b->desenharAresta(g,a,b,adj.get(b).contains(a))));
        for(Disciplina d:grafo.getDisciplinas())desenharVertice(g,d);
        if(grafo.quantidadeVertices()==0){
            g.setColor(Tema.TINTA);g.setFont(new Font("Segoe UI",Font.BOLD,24));g.drawString("Construa seu planejamento",100,140);
            g.setColor(Tema.SUAVE);g.setFont(new Font("Segoe UI",Font.PLAIN,14));
            g.drawString("Escolha Criar e clique aqui para cadastrar uma disciplina.",100,178);
            g.drawString("Depois, conecte o pré-requisito à disciplina dependente.",100,204);
            g.drawString("Você também pode carregar um dos quatro exemplos editáveis.",100,242);
        }
        g.dispose();
    }
    private boolean arestaCiclo(String a,String b) {
        for(int i=0;i+1<ciclo.size();i++)if(ciclo.get(i).equals(a)&&ciclo.get(i+1).equals(b))return true;
        return false;
    }
    private void desenharAresta(Graphics2D g,String a,String b,boolean reciproca) {
        VerticeVisual p=posicoes.get(a),q=posicoes.get(b);
        double vx=q.getX()-p.getX(),vy=q.getY()-p.getY(),dist=Math.max(1,Math.hypot(vx,vy));
        double cx=(p.getX()+q.getX())/2,cy=(p.getY()+q.getY())/2;
        if(reciproca){cx+=-vy/dist*60;cy+=vx/dist*60;}
        Point2D inicio=borda(p,reciproca?cx:q.getX(),reciproca?cy:q.getY());
        Point2D fim=borda(q,reciproca?cx:p.getX(),reciproca?cy:p.getY());
        boolean ativa=passo!=null&&a.equals(passo.origem())&&b.equals(passo.destino());
        g.setColor(arestaCiclo(a,b)?Tema.VERMELHO:ativa?Tema.VERDE:Tema.ARESTA);
        g.setStroke(new BasicStroke(arestaCiclo(a,b)||ativa?3:1.8f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        if(reciproca)g.draw(new QuadCurve2D.Double(inicio.getX(),inicio.getY(),cx,cy,fim.getX(),fim.getY()));
        else g.draw(new Line2D.Double(inicio,fim));
        double angulo=Math.atan2(fim.getY()-(reciproca?cy:inicio.getY()),fim.getX()-(reciproca?cx:inicio.getX()));
        Path2D seta=new Path2D.Double();seta.moveTo(fim.getX(),fim.getY());
        seta.lineTo(fim.getX()-12*Math.cos(angulo-.43),fim.getY()-12*Math.sin(angulo-.43));
        seta.lineTo(fim.getX()-12*Math.cos(angulo+.43),fim.getY()-12*Math.sin(angulo+.43));seta.closePath();g.fill(seta);
    }
    private void desenharVertice(Graphics2D g,Disciplina d) {
        String c=d.getCodigo();VerticeVisual p=posicoes.get(c);Shape forma=forma(p);
        boolean emCiclo=ciclo.contains(c),selecionada=c.equals(selecionado)||c.equals(origemConexao);
        Color fundo=Tema.CARTAO,borda=Tema.LINHA,texto=Tema.TINTA;
        PassoAlgoritmo.Cor estadoDfs=null;
        if(passo!=null){
            if(passo.algoritmo().equals("DFS")){
                PassoAlgoritmo.Cor cor=passo.cores().get(c);
                estadoDfs=cor;
                if(cor==PassoAlgoritmo.Cor.BRANCO)borda=Tema.SUAVE;
                if(cor==PassoAlgoritmo.Cor.CINZA){fundo=Tema.CINZA;borda=Tema.SUAVE;}
                if(cor==PassoAlgoritmo.Cor.PRETO){fundo=Tema.PRETO;borda=Tema.SUAVE;}
            }else if(processados.contains(c)){fundo=Tema.FUNDO_PROCESSADO;borda=Tema.VERDE;}
            else if(passo.fila().contains(c)){fundo=Tema.FUNDO_FILA;borda=Tema.AMARELO;}
        }
        if(emCiclo){fundo=Tema.FUNDO_CICLO;borda=Tema.VERMELHO;texto=Tema.VERMELHO;}
        if(selecionada){g.setColor(Tema.HALO);g.setStroke(new BasicStroke(7));g.draw(forma);}
        g.setColor(fundo);g.fill(forma);g.setColor(selecionada?Tema.VERDE:borda);g.setStroke(new BasicStroke(2));g.draw(forma);
        int x=(int)(p.getX()-LARGURA/2+13),y=(int)(p.getY()-ALTURA/2);
        if(estadoDfs!=null){
            // Indicador literal das três cores, além do estado escrito na tabela.
            g.setColor(switch(estadoDfs){case BRANCO->Color.WHITE;case CINZA->Color.GRAY;case PRETO->Color.BLACK;});
            g.fillOval(x,y+13,8,8);g.setColor(Tema.TINTA);g.setStroke(new BasicStroke(1));g.drawOval(x,y+13,8,8);
        }
        g.setColor(texto);g.setFont(new Font("Segoe UI",Font.BOLD,15));g.drawString(c,x+(estadoDfs==null?0:14),y+23);
        g.setFont(new Font("Segoe UI",Font.PLAIN,10));
        String periodo="cad. "+d.getPeriodoReferencia();
        g.drawString(periodo,(int)(p.getX()+LARGURA/2-12-g.getFontMetrics().stringWidth(periodo)),y+22);
        g.setFont(new Font("Segoe UI",Font.PLAIN,12));
        List<String> linhas=quebrar(d.getNome(),g.getFontMetrics(),(int)LARGURA-24);
        for(int i=0;i<Math.min(2,linhas.size());i++)g.drawString(linhas.get(i),x,y+44+i*16);
        String extra="";
        if(passo!=null&&passo.algoritmo().equals("Kahn"))extra="grau residual: "+passo.graus().get(c);
        if(etapas.containsKey(c))extra="Etapa recomendada "+etapas.get(c);
        if(!extra.isEmpty()){
            g.setFont(new Font("Segoe UI",Font.BOLD,11));g.setColor(Tema.VERDE);int w=g.getFontMetrics().stringWidth(extra);
            g.setColor(Tema.SUPERFICIE);g.fillRoundRect((int)p.getX()-w/2-6,y-23,w+12,18,8,8);
            g.setColor(Tema.VERDE);g.drawString(extra,(int)p.getX()-w/2,y-10);
        }
    }
    private List<String> quebrar(String s,FontMetrics fm,int largura) {
        List<String> r=new ArrayList<>();String atual="";
        for(String palavra:s.split(" ")){
            String tentativa=atual.isEmpty()?palavra:atual+" "+palavra;
            if(fm.stringWidth(tentativa)>largura&&!atual.isEmpty()){r.add(atual);atual=palavra;}else atual=tentativa;
        }
        if(!atual.isEmpty())r.add(atual);
        for(int i=0;i<r.size();i++){
            String t=r.get(i);
            if(i==1&&r.size()>2)t+="…";
            if(fm.stringWidth(t)>largura){while(t.length()>1&&fm.stringWidth(t+"…")>largura)t=t.substring(0,t.length()-1);t+="…";}
            r.set(i,t);
        }
        return r;
    }
}
