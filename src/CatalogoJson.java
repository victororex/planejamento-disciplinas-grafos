import java.util.*;

/** Conversão do catálogo versionado; mesmos dados em JSON e PGD 2. */
public final class CatalogoJson {
    private final List<String> avisos;
    public CatalogoJson(List<String> avisos) { this.avisos=avisos; }
    @SuppressWarnings("unchecked") static Map<String,Object> objeto(Object o) {
        if(!(o instanceof Map<?,?>))throw new IllegalArgumentException("Esperado objeto JSON.");return (Map<String,Object>)o;
    }
    static List<?> lista(Object o) { if(!(o instanceof List<?> l))throw new IllegalArgumentException("Esperada lista JSON.");return l; }
    static String texto(Object o) { if(o==null)return "";if(!(o instanceof String s))throw new IllegalArgumentException("Esperado texto.");return s; }
    static Double numero(Object o) { if(o==null)return null;if(!(o instanceof Number n))throw new IllegalArgumentException("Esperado número.");return n.doubleValue(); }
    static Integer inteiro(Object o) { Double n=numero(o);if(n==null)return null;if(n!=Math.rint(n)||n<Integer.MIN_VALUE||n>Integer.MAX_VALUE)throw new IllegalArgumentException("Esperado inteiro.");return n.intValue(); }
    private void campos(Map<String,Object> m,String... conhecidos) {
        Set<String> ks=Set.of(conhecidos); for(String k:m.keySet())if(!ks.contains(k))avisos.add("Campo não reconhecido: "+k);
    }
    public ProjetoAcademico ler(Object json) {
        Map<String,Object> raiz=objeto(json);campos(raiz,"versao","matriz","disciplinas","prerequisitos","requisitos","planos");
        if(!Objects.equals(inteiro(raiz.get("versao")),2))throw new IllegalArgumentException("Catálogo exige versao 2.");
        MatrizCurricular matriz=MatrizCurricular.livre();
        if(raiz.get("matriz")!=null) {
            Map<String,Object> m=objeto(raiz.get("matriz"));campos(m,"instituicao","curso","codigoCurso","versao","ano","campus","turno","periodos","observacoes","fonte");
            matriz=new MatrizCurricular(texto(m.get("instituicao")),texto(m.get("curso")),texto(m.get("codigoCurso")),texto(m.get("versao")),inteiro(m.get("ano")),texto(m.get("campus")),texto(m.get("turno")),inteiro(m.get("periodos")),texto(m.get("observacoes")),texto(m.get("fonte")));
        }
        GrafoDisciplinas g=new GrafoDisciplinas();Map<String,VerticeVisual> ps=new LinkedHashMap<>();
        List<?> ds=lista(raiz.get("disciplinas"));if(ds.size()>GerenciadorArquivos.MAX_VERTICES)throw new IllegalArgumentException("Limite de 2000 disciplinas.");
        for(Object v:ds) {
            Map<String,Object> d=objeto(v);campos(d,"codigo","nome","periodo","horas","creditos","categoria","descricao","observacoes","oferta","status","x","y");
            Integer periodo=inteiro(d.get("periodo"));String status=texto(d.get("status"));
            Disciplina disc=new Disciplina(texto(d.get("codigo")),texto(d.get("nome")),periodo==null?0:periodo,numero(d.get("horas")),numero(d.get("creditos")),texto(d.get("categoria")),texto(d.get("descricao")),texto(d.get("observacoes")),texto(d.get("oferta")),status.isEmpty()?StatusAcademico.NAO_CURSADA:StatusAcademico.valueOf(status));
            g.adicionarDisciplina(disc);int i=ps.size();
            if(d.containsKey("x")!=d.containsKey("y"))throw new IllegalArgumentException("Informe x e y juntos.");
            double x=d.containsKey("x")?Objects.requireNonNull(numero(d.get("x"))):140+(i%20)*240;
            double y=d.containsKey("y")?Objects.requireNonNull(numero(d.get("y"))):110+(i/20)*95;
            ps.put(disc.getCodigo(),new VerticeVisual(x,y));
        }
        List<?> arestas=lista(raiz.getOrDefault("prerequisitos",List.of()));
        if(arestas.size()>GerenciadorArquivos.MAX_ARESTAS)throw new IllegalArgumentException("Limite de 20000 relações.");
        for(Object a:arestas){List<?> par=lista(a);if(par.size()!=2)throw new IllegalArgumentException("Pré-requisito exige [origem,destino].");g.adicionarDependencia(texto(par.get(0)),texto(par.get(1)));}
        Map<String,Object> regras=objeto(raiz.getOrDefault("requisitos",Map.of()));
        for(var e:regras.entrySet())g.definirRequisitos(e.getKey(),lista(e.getValue()).stream().map(this::lerRegra).toList());
        List<PlanoAcademico> planos=new ArrayList<>();
        for(Object v:lista(raiz.getOrDefault("planos",List.of()))) {
            Map<String,Object> p=objeto(v);campos(p,"nome","periodo","maxHoras","maxDisciplinas","selecionadas","aprovacoesSimuladas");
            planos.add(new PlanoAcademico(texto(p.get("nome")),texto(p.get("periodo")),numero(p.get("maxHoras")),inteiro(p.get("maxDisciplinas")),codigos(p.getOrDefault("selecionadas",List.of())),codigos(p.getOrDefault("aprovacoesSimuladas",List.of()))));
        }
        return new ProjetoAcademico(g,ps,matriz,planos);
    }
    private static Set<String> codigos(Object o){List<String> l=lista(o).stream().map(CatalogoJson::texto).toList();Set<String>s=new LinkedHashSet<>(l);if(s.size()!=l.size())throw new IllegalArgumentException("Código duplicado em cenário.");return s;}
    public Requisito lerRegra(Object o) {
        Map<String,Object> r=objeto(o);campos(r,"tipo","codigo","minimo","itens");
        return new Requisito(Requisito.Tipo.valueOf(texto(r.get("tipo"))),texto(r.get("codigo")),numero(r.get("minimo")),lista(r.getOrDefault("itens",List.of())).stream().map(this::lerRegra).toList());
    }
    static Map<String,Object> mapa(Object... pares) {
        Map<String,Object> r=new LinkedHashMap<>();for(int i=0;i<pares.length;i+=2)r.put((String)pares[i],pares[i+1]);return r;
    }
    public static Object regraJson(Requisito r) { return mapa("tipo",r.tipo().name(),"codigo",r.codigo(),"minimo",r.minimo(),"itens",r.itens().stream().map(CatalogoJson::regraJson).toList()); }
    public static Object escrever(ProjetoAcademico projeto) {
        MatrizCurricular m=projeto.matriz();List<Object> ds=new ArrayList<>();List<Object> arestas=new ArrayList<>();Map<String,Object> regras=new LinkedHashMap<>();
        for(Disciplina d:projeto.grafo().getDisciplinas()) {
            VerticeVisual p=projeto.posicoes().get(d.getCodigo());
            ds.add(mapa("codigo",d.getCodigo(),"nome",d.getNome(),"periodo",d.getPeriodoReferencia()==0?null:d.getPeriodoReferencia(),"horas",d.getCargaHoraria(),"creditos",d.getCreditos(),"categoria",d.getCategoria(),"descricao",d.getDescricao(),"observacoes",d.getObservacoes(),"oferta",d.getOferta(),"status",d.getStatus().name(),"x",p.getX(),"y",p.getY()));
            if(!projeto.grafo().requisitos(d.getCodigo()).isEmpty())regras.put(d.getCodigo(),projeto.grafo().requisitos(d.getCodigo()).stream().map(CatalogoJson::regraJson).toList());
        }
        projeto.grafo().listaAdjacencia().forEach((a,bs)->bs.forEach(b->arestas.add(List.of(a,b))));
        List<?> planos=projeto.planos().stream().map(p->mapa("nome",p.nome(),"periodo",p.periodo(),"maxHoras",p.maxHoras(),"maxDisciplinas",p.maxDisciplinas(),"selecionadas",p.selecionadas(),"aprovacoesSimuladas",p.aprovacoesSimuladas())).toList();
        return mapa("versao",2,"matriz",mapa("instituicao",m.instituicao(),"curso",m.curso(),"codigoCurso",m.codigoCurso(),"versao",m.versao(),"ano",m.ano(),"campus",m.campus(),"turno",m.turno(),"periodos",m.periodos(),"observacoes",m.observacoes(),"fonte",m.fonte()),"disciplinas",ds,"prerequisitos",arestas,"requisitos",regras,"planos",planos);
    }
}
