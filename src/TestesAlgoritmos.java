import java.awt.geom.Point2D;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Executor de testes independente de JUnit. Cada falha termina o processo com código diferente de zero. */
public final class TestesAlgoritmos {
    @FunctionalInterface private interface Teste { void executar() throws Exception; }
    private static int passou, falhou;
    public static void main(String[] args) throws Exception {
        teste("Cadastro válido e encapsulamento",()->{
            Disciplina d=new Disciplina(" A "," Algoritmos ",1);
            igual("A",d.getCodigo());igual("Algoritmos",d.getNome());igual(1,d.getPeriodoReferencia());
        });
        teste("Nome vazio ou controle rejeitado",()->{rejeita(()->new Disciplina("A"," ",1));rejeita(()->new Disciplina("A","Nome\nquebrado",1));});
        teste("Código e período inválidos rejeitados",()->{
            rejeita(()->new Disciplina("","Nome",1));rejeita(()->new Disciplina("A B","Nome",1));
            rejeita(()->new Disciplina("A","Nome",0));rejeita(()->new Disciplina("A","Nome",101));
        });
        teste("Código duplicado",()->{GrafoDisciplinas g=vertices("A");rejeita(()->g.adicionarDisciplina(new Disciplina("A","Outro",2)));igual(1,g.quantidadeVertices());});
        teste("Dependência duplicada",()->{GrafoDisciplinas g=vertices("A","B");g.adicionarDependencia("A","B");rejeita(()->g.adicionarDependencia("A","B"));igual(1,g.quantidadeArestas());});
        teste("Autodependência",()->{GrafoDisciplinas g=vertices("A");rejeita(()->g.adicionarDependencia("A","A"));});
        teste("Vértice inexistente rejeitado",()->{GrafoDisciplinas g=vertices("A");rejeita(()->g.adicionarDependencia("A","Z"));rejeita(()->g.removerDisciplina("Z"));});
        teste("Aresta é direcionada e graus corretos",()->{
            GrafoDisciplinas g=vertices("A","B","C");g.adicionarDependencia("A","B");g.adicionarDependencia("A","C");
            verifica(!g.existeDependencia("B","A"));igual(0,g.grauEntrada("A"));igual(2,g.grauSaida("A"));igual(Set.of("A"),g.obterPrerequisitos("B"));
        });
        teste("Remoção limpa arestas incidentes",()->{
            GrafoDisciplinas g=vertices("A","B","C");g.adicionarDependencia("A","B");g.adicionarDependencia("B","C");g.removerDisciplina("B");
            igual(0,g.quantidadeArestas());igual(Set.of("A","C"),g.getCodigos());validarDAG(g);
        });
        teste("Remoção de dependência",()->{GrafoDisciplinas g=vertices("A","B");g.adicionarDependencia("A","B");g.removerDependencia("A","B");igual(0,g.grauEntrada("B"));rejeita(()->g.removerDependencia("A","B"));});
        teste("Edição renomeia código e preserva relações e ordem",()->{
            GrafoDisciplinas g=vertices("A","B","C");g.adicionarDependencia("A","B");g.adicionarDependencia("B","C");
            g.editarDisciplina("B",new Disciplina("X","Nova disciplina",9));
            igual(List.of("A","X","C"),new ArrayList<>(g.getCodigos()));verifica(g.existeDependencia("A","X"));verifica(g.existeDependencia("X","C"));igual(9,g.obterDisciplina("X").getPeriodoReferencia());
            rejeita(()->g.editarDisciplina("X",new Disciplina("A","Nome",1)));igual(2,g.quantidadeArestas());validarDAG(g);
        });
        teste("Adjacência e consultas são imutáveis",()->{
            GrafoDisciplinas g=vertices("A","B");g.adicionarDependencia("A","B");Map<String,Set<String>> adj=g.listaAdjacencia();
            imutavel(()->adj.clear());imutavel(()->adj.get("A").clear());imutavel(()->g.obterVizinhos("A").clear());imutavel(()->g.getCodigos().clear());
            g.removerDependencia("A","B");igual(Set.of("B"),adj.get("A"));
        });
        teste("Grafo vazio",()->{GrafoDisciplinas g=vertices();validarDAG(g);igual(List.of(),new PlanejadorPeriodos().planejar(g).grupos());igual(0,g.componentesFracas().size());});
        teste("Um vértice",()->{GrafoDisciplinas g=vertices("A");validarDAG(g);igual(List.of("A"),new AlgoritmoDFS().executar(g).ordem());});
        teste("Isoladas",()->{GrafoDisciplinas g=vertices("A","B","C");validarDAG(g);igual(List.of(List.of("A","B","C")),new PlanejadorPeriodos().planejar(g).grupos());});
        teste("DAG obrigatório e resultados determinísticos",()->{
            GrafoDisciplinas g=ExemplosAcademicos.criar(0).grafo();validarDAG(g);
            igual(List.of("A","B","C","D","E"),new AlgoritmoKahn().executar(g).ordem());
            igual(List.of("A","C","E","B","D"),new AlgoritmoDFS().executar(g).ordem());
            igual(List.of("D","B","E","C","A"),new AlgoritmoDFS().executar(g).finalizacao());
        });
        teste("Múltiplas ordenações válidas",()->{
            GrafoDisciplinas g=ExemplosAcademicos.criar(1).grafo();var c=new ComparadorAlgoritmos().comparar(g);
            verifica(c.kahnValida()&&c.dfsValida());verifica(!c.kahn().ordem().equals(c.dfs().ordem()));
        });
        teste("Ciclo A → B → C → A",()->validarCiclo(ExemplosAcademicos.criar(2).grafo()));
        teste("Ciclo de duas disciplinas",()->{GrafoDisciplinas g=vertices("A","B");g.adicionarDependencia("A","B");g.adicionarDependencia("B","A");validarCiclo(g);igual(List.of("A","B","A"),new AlgoritmoDFS().executar(g).ciclo());});
        teste("Kahn detecta ciclo com componente processável",()->{
            GrafoDisciplinas g=ExemplosAcademicos.criar(2).grafo();g.adicionarDisciplina(new Disciplina("I","Independente",1));
            var r=new AlgoritmoKahn().executar(g);verifica(r.possuiCiclo());igual(List.of("I"),r.processados());igual(List.of(),r.ordem());
        });
        teste("DFS detecta e reconstrói somente arestas existentes",()->{
            GrafoDisciplinas g=ExemplosAcademicos.criar(2).grafo();var r=new AlgoritmoDFS().executar(g);verifica(r.possuiCiclo());caminhoReal(g,r.ciclo());igual(3,r.finalizacao().size());
        });
        teste("Ciclo versus disciplinas apenas bloqueadas",()->{
            GrafoDisciplinas g=ExemplosAcademicos.criar(2).grafo();g.adicionarDisciplina(new Disciplina("D","Bloqueada",4));g.adicionarDependencia("C","D");
            var a=new DetectorCiclos().analisar(g);igual(Set.of("A","B","C"),a.emCiclos());igual(Set.of("D"),a.apenasBloqueados());
        });
        teste("Múltiplos ciclos e caminho cruzado na mesma componente forte",()->{
            GrafoDisciplinas g=vertices("A","B","C","D","E","F","X");
            ligar(g,"A","B","B","C","C","A","A","D","D","C","E","F","F","E","F","X");
            var a=new DetectorCiclos().analisar(g);igual(Set.of("A","B","C","D","E","F"),a.emCiclos());igual(Set.of("X"),a.apenasBloqueados());caminhoReal(g,a.caminho());
        });
        teste("Aresta para preto não é ciclo (diamante)",()->{
            GrafoDisciplinas g=vertices("A","B","C","D");ligar(g,"A","B","A","C","B","D","C","D");validarDAG(g);
        });
        teste("Verificador rejeita inversão, falta, repetição e código desconhecido",()->{
            GrafoDisciplinas g=vertices("A","B");g.adicionarDependencia("A","B");
            verifica(ComparadorAlgoritmos.ordemValida(g,List.of("A","B")));verifica(!ComparadorAlgoritmos.ordemValida(g,List.of("B","A")));
            verifica(!ComparadorAlgoritmos.ordemValida(g,List.of("A")));verifica(!ComparadorAlgoritmos.ordemValida(g,List.of("A","A")));
            verifica(!ComparadorAlgoritmos.ordemValida(g,List.of("A","X")));verifica(!ComparadorAlgoritmos.ordemValida(g,null));
        });
        teste("Planejamento do exemplo 1",()->{
            var p=new PlanejadorPeriodos().planejar(ExemplosAcademicos.criar(0).grafo());igual(List.of(List.of("A"),List.of("B","C"),List.of("D","E")),p.grupos());
        });
        teste("Planejamento considera o pré-requisito mais tardio",()->{
            GrafoDisciplinas g=vertices("A","B","C","D","E");ligar(g,"A","B","B","C","C","E","D","E");
            g.editarDisciplina("A",new Disciplina("A","Referência diferente",8));var p=new PlanejadorPeriodos().planejar(g);
            igual(4,p.etapas().get("E"));igual(1,p.etapas().get("A"));igual(8,g.obterDisciplina("A").getPeriodoReferencia());
        });
        teste("Planejamento rejeita ciclos",()->rejeita(()->new PlanejadorPeriodos().planejar(ExemplosAcademicos.criar(2).grafo())));
        teste("Várias componentes fracas e isolada",()->{
            GrafoDisciplinas g=ExemplosAcademicos.criar(3).grafo();igual(3,g.componentesFracas().size());validarDAG(g);
        });
        teste("Preservação do grafo em todas as análises",()->{
            GrafoDisciplinas g=ExemplosAcademicos.criar(0).grafo();Map<String,Set<String>> antes=g.listaAdjacencia();List<Disciplina> ds=g.getDisciplinas();
            new ComparadorAlgoritmos().comparar(g);new DetectorCiclos().analisar(g);new PlanejadorPeriodos().planejar(g);
            new AlgoritmoKahn().executar(g,p->{});new AlgoritmoDFS().executar(g,p->{});igual(antes,g.listaAdjacencia());igual(ds,g.getDisciplinas());
        });
        teste("Histórico Kahn reflete graus, fila e ordem reais",()->{
            GrafoDisciplinas g=ExemplosAcademicos.criar(0).grafo();List<PassoAlgoritmo> ps=new ArrayList<>();new AlgoritmoKahn().executar(g,ps::add);
            igual(List.of("A"),ps.get(0).fila());igual(1,ps.get(0).graus().get("B"));
            PassoAlgoritmo baixa=ps.stream().filter(p->"B".equals(p.destino())).findFirst().orElseThrow();igual(0,baixa.graus().get("B"));verifica(baixa.fila().contains("B"));
            igual(1,ps.get(0).graus().get("B"));imutavel(()->ps.get(0).graus().clear());igual(List.of("A","B","C","D","E"),ps.get(ps.size()-1).ordem());
        });
        teste("Histórico DFS tem pilha, cores e finalização coerentes",()->{
            GrafoDisciplinas g=ExemplosAcademicos.criar(0).grafo();List<PassoAlgoritmo> ps=new ArrayList<>();new AlgoritmoDFS().executar(g,ps::add);
            PassoAlgoritmo entrou=ps.stream().filter(p->p.evento().equals("VISITAR")&&p.atual().equals("D")).findFirst().orElseThrow();
            igual(List.of("A","B","D"),entrou.pilha());igual(PassoAlgoritmo.Cor.CINZA,entrou.cores().get("D"));
            PassoAlgoritmo ultimo=ps.get(ps.size()-1);igual(List.of("D","B","E","C","A"),ultimo.finalizados());verifica(ComparadorAlgoritmos.ordemValida(g,ultimo.ordem()));imutavel(()->entrou.pilha().clear());
        });
        teste("Histórico cíclico nunca entrega ordem final inválida",()->{
            List<PassoAlgoritmo> ps=new ArrayList<>();new AlgoritmoDFS().executar(ExemplosAcademicos.criar(2).grafo(),ps::add);
            verifica(ps.stream().anyMatch(p->p.evento().equals("CICLO")));igual(List.of(),ps.get(ps.size()-1).ordem());
        });
        teste("Determinismo das duas estratégias",()->{
            GrafoDisciplinas g=ExemplosAcademicos.criar(1).grafo();var k=new AlgoritmoKahn().executar(g);var d=new AlgoritmoDFS().executar(g);
            for(int i=0;i<10;i++){igual(k,new AlgoritmoKahn().executar(g));igual(d,new AlgoritmoDFS().executar(g));}
        });
        teste("Correção após remover dependência circular",()->{
            GrafoDisciplinas g=ExemplosAcademicos.criar(0).grafo();g.adicionarDependencia("D","A");validarCiclo(g);g.removerDependencia("D","A");validarDAG(g);
        });
        teste("Geometria da seta termina na borda do cartão",()->{
            VerticeVisual v=new VerticeVisual(200,200);Point2D direita=PainelGrafo.borda(v,500,200);perto(287,direita.getX());perto(200,direita.getY());
            Point2D cima=PainelGrafo.borda(v,200,50);perto(161,cima.getY());
            v.mover(300,250);Point2D nova=PainelGrafo.borda(v,500,250);perto(387,nova.getX());
        });
        teste("Posições inválidas rejeitadas",()->{rejeita(()->new VerticeVisual(Double.NaN,200));rejeita(()->new VerticeVisual(0,0));rejeita(()->new VerticeVisual(200,Double.POSITIVE_INFINITY));});
        teste("Salvamento e reabertura preservam tudo",()->{
            Path arq=Files.createTempFile("planejamento-roundtrip-",".pgd");
            try{ProjetoAcademico p=ExemplosAcademicos.criar(0);p.grafo().editarDisciplina("C",new Disciplina("C","Banco & Dados <Avançado> ç",7));
                new GerenciadorArquivos().salvar(arq,p);ProjetoAcademico l=new GerenciadorArquivos().abrir(arq);
                igual(p.grafo().getDisciplinas(),l.grafo().getDisciplinas());igual(p.grafo().listaAdjacencia(),l.grafo().listaAdjacencia());
                for(String c:p.grafo().getCodigos()){perto(p.posicoes().get(c).getX(),l.posicoes().get(c).getX());perto(p.posicoes().get(c).getY(),l.posicoes().get(c).getY());}validarDAG(l.grafo());
            }finally{Files.deleteIfExists(arq);}
        });
        teste("Salvamento substitui arquivo existente",()->{
            Path arq=Files.createTempFile("planejamento-replace-",".pgd");try{new GerenciadorArquivos().salvar(arq,ExemplosAcademicos.criar(0));new GerenciadorArquivos().salvar(arq,ExemplosAcademicos.criar(2));validarCiclo(new GerenciadorArquivos().abrir(arq).grafo());}finally{Files.deleteIfExists(arq);}
        });
        teste("Persistência de grafo vazio",()->{
            Path arq=Files.createTempFile("planejamento-empty-",".pgd");try{new GerenciadorArquivos().salvar(arq,new ProjetoAcademico(vertices(),Map.of()));igual(0,new GerenciadorArquivos().abrir(arq).grafo().quantidadeVertices());}finally{Files.deleteIfExists(arq);}
        });
        teste("Arquivo inválido não modifica outro projeto",()->{
            ProjetoAcademico p=ExemplosAcademicos.criar(0);String antes=p.grafo().listaAdjacencia().toString();arquivoInvalido("Não sou um projeto");igual(antes,p.grafo().listaAdjacencia().toString());
        });
        teste("Arquivo rejeita referência a vértice ausente",()->arquivoInvalido("PGD\t1\nD\tQQ==\tTm9tZQ==\t1\t140\t110\nR\tQQ==\tWg==\n"));
        teste("Arquivo rejeita cadastro duplicado",()->arquivoInvalido("PGD\t1\nD\tQQ==\tTm9tZQ==\t1\t140\t110\nD\tQQ==\tTm9tZQ==\t1\t240\t110\n"));
        teste("Arquivo rejeita Base64 e UTF-8 inválidos",()->{arquivoInvalido("PGD\t1\nD\t@@\tTm9tZQ==\t1\t140\t110\n");arquivoInvalido("PGD\t1\nD\tQQ==\t/w==\t1\t140\t110\n");});
        teste("Arquivo rejeita posição não finita",()->arquivoInvalido("PGD\t1\nD\tQQ==\tTm9tZQ==\t1\tNaN\t110\n"));
        teste("Arquivo rejeita aresta duplicada e autodependência",()->{
            String ds="PGD\t1\nD\tQQ==\tTm9tZQ==\t1\t140\t110\nD\tQg==\tTm9tZQ==\t2\t340\t110\n";
            arquivoInvalido(ds+"R\tQQ==\tQg==\nR\tQQ==\tQg==\n");arquivoInvalido(ds+"R\tQQ==\tQQ==\n");
        });
        teste("Resultados, explicações e HTML protegem texto cadastrado",()->{
            igual("A &amp; B &lt;C&gt;",ExplicadorDidatico.html("A & B <C>"));verifica(ExplicadorDidatico.complexidade().contains("O(V + E)"));
        });
        teste("100 DAGs aleatórios: ambas ordens e todas as etapas respeitam cada aresta",()->{
            Random random=new Random(20261020);
            for(int caso=0;caso<100;caso++){
                int n=1+random.nextInt(25);List<String> vertices=new ArrayList<>();for(int i=0;i<n;i++)vertices.add("V"+i);
                GrafoDisciplinas g=vertices(vertices.toArray(String[]::new));List<String> permutacao=new ArrayList<>(vertices);Collections.shuffle(permutacao,random);
                for(int i=0;i<n;i++)for(int j=i+1;j<n;j++)if(random.nextDouble()<.17)g.adicionarDependencia(permutacao.get(i),permutacao.get(j));
                validarDAG(g);var p=new PlanejadorPeriodos().planejar(g);
                for(String v:g.getCodigos()){
                    int esperado=1;for(String a:g.obterPrerequisitos(v))esperado=Math.max(esperado,p.etapas().get(a)+1);igual(esperado,p.etapas().get(v));
                }
            }
        });
        teste("40 grafos cíclicos aleatórios: classificação forte confere com alcançabilidade independente",()->{
            Random random=new Random(73);
            for(int caso=0;caso<40;caso++){
                GrafoDisciplinas g=vertices("A","B","C","D","E","F","G");List<String> vs=new ArrayList<>(g.getCodigos());
                for(String a:vs)for(String b:vs)if(!a.equals(b)&&random.nextDouble()<.2)g.adicionarDependencia(a,b);
                var analise=new DetectorCiclos().analisar(g);Set<String> esperados=new HashSet<>();
                for(String a:vs)for(String b:g.obterVizinhos(a))if(alcanca(g,b,a))esperados.add(a);
                igual(esperados,analise.emCiclos());if(analise.possuiCiclo())validarCiclo(g);else validarDAG(g);
            }
        });
        teste("Cadeia de 10000 vértices sem estouro de pilha",()->{
            GrafoDisciplinas g=new GrafoDisciplinas();for(int i=0;i<10000;i++){g.adicionarDisciplina(new Disciplina("V"+i,"Disciplina "+i,1));if(i>0)g.adicionarDependencia("V"+(i-1),"V"+i);}
            validarDAG(g);igual(10000,new PlanejadorPeriodos().planejar(g).etapas().get("V9999"));
        });
        System.out.println("\nRESULTADO: "+passou+" testes aprovados; "+falhou+" falhas.");
        if(falhou>0)System.exit(1);
    }
    private static boolean alcanca(GrafoDisciplinas g,String inicio,String fim){
        Set<String> vistos=new HashSet<>();Deque<String> fila=new ArrayDeque<>();fila.add(inicio);vistos.add(inicio);
        while(!fila.isEmpty()){String v=fila.remove();if(v.equals(fim))return true;for(String w:g.obterVizinhos(v))if(vistos.add(w))fila.add(w);}return false;
    }
    private static void teste(String nome,Teste t){try{t.executar();passou++;System.out.println("[OK] "+nome);}catch(Throwable e){falhou++;System.out.println("[FALHOU] "+nome+": "+e);e.printStackTrace(System.out);}}
    private static void validarDAG(GrafoDisciplinas g){var c=new ComparadorAlgoritmos().comparar(g);verifica(!c.kahn().possuiCiclo()&&!c.dfs().possuiCiclo());verifica(c.kahnValida()&&c.dfsValida());}
    private static void validarCiclo(GrafoDisciplinas g){var c=new ComparadorAlgoritmos().comparar(g);verifica(c.kahn().possuiCiclo()&&c.dfs().possuiCiclo());igual(List.of(),c.kahn().ordem());igual(List.of(),c.dfs().ordem());verifica(!c.kahnValida()&&!c.dfsValida());verifica(c.planejamento()==null);caminhoReal(g,c.dfs().ciclo());}
    private static void caminhoReal(GrafoDisciplinas g,List<String> caminho){verifica(caminho.size()>=3);igual(caminho.get(0),caminho.get(caminho.size()-1));for(int i=0;i+1<caminho.size();i++)verifica(g.existeDependencia(caminho.get(i),caminho.get(i+1)));}
    private static GrafoDisciplinas vertices(String... codigos){GrafoDisciplinas g=new GrafoDisciplinas();for(String c:codigos)g.adicionarDisciplina(new Disciplina(c,"Disciplina "+c,1));return g;}
    private static void ligar(GrafoDisciplinas g,String... pares){for(int i=0;i<pares.length;i+=2)g.adicionarDependencia(pares[i],pares[i+1]);}
    private static void verifica(boolean condicao){if(!condicao)throw new AssertionError("Condição esperada não foi satisfeita.");}
    private static void igual(Object esperado,Object real){if(!Objects.equals(esperado,real))throw new AssertionError("Esperado: "+esperado+"; obtido: "+real);}
    private static void perto(double esperado,double real){if(Math.abs(esperado-real)>1e-6)throw new AssertionError("Esperado: "+esperado+"; obtido: "+real);}
    private static void rejeita(Teste t)throws Exception{try{t.executar();}catch(IllegalArgumentException e){return;}throw new AssertionError("Era esperada uma rejeição por validação.");}
    private static void imutavel(Runnable r){try{r.run();}catch(UnsupportedOperationException e){return;}throw new AssertionError("A coleção deveria ser imutável.");}
    private static void arquivoInvalido(String conteudo)throws Exception{
        Path arq=Files.createTempFile("planejamento-invalid-",".pgd");try{Files.writeString(arq,conteudo,StandardCharsets.UTF_8);try{new GerenciadorArquivos().abrir(arq);}catch(IOException e){return;}throw new AssertionError("Arquivo inválido foi aceito.");}finally{Files.deleteIfExists(arq);}
    }
}
