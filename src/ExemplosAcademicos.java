import java.util.*;

public final class ExemplosAcademicos {
    public static final String[] NOMES = {"1 · DAG acadêmico", "2 · Múltiplas ordens", "3 · Dependência circular", "4 · Componentes e isolada"};
    public static ProjetoAcademico criar(int indice) {
        GrafoDisciplinas g = new GrafoDisciplinas(); Map<String, VerticeVisual> p = new LinkedHashMap<>();
        switch (indice) {
            case 0 -> {
                adicionar(g,p,"A","Algoritmos",1,140,235);
                adicionar(g,p,"B","Estruturas de Dados",2,400,130);
                adicionar(g,p,"C","Banco de Dados",2,400,340);
                adicionar(g,p,"D","Engenharia de Software",3,660,130);
                adicionar(g,p,"E","Desenvolvimento Web",3,660,340);
                ligar(g,"A","B","A","C","B","D","C","E");
            }
            case 1 -> {
                adicionar(g,p,"A","Algoritmos",1,140,130);
                adicionar(g,p,"M","Matemática",1,140,340);
                adicionar(g,p,"B","Estruturas de Dados",2,400,130);
                adicionar(g,p,"C","Banco de Dados",2,400,340);
                adicionar(g,p,"D","Projeto Integrador",4,660,235);
                ligar(g,"A","B","M","C","B","D","C","D");
            }
            case 2 -> {
                adicionar(g,p,"A","Algoritmos",1,140,150);
                adicionar(g,p,"B","Estruturas de Dados",2,400,150);
                adicionar(g,p,"C","Banco de Dados",3,270,360);
                ligar(g,"A","B","B","C","C","A");
            }
            case 3 -> {
                adicionar(g,p,"A","Algoritmos",1,140,130);
                adicionar(g,p,"B","Estruturas de Dados",2,420,130);
                adicionar(g,p,"M","Matemática",1,140,320);
                adicionar(g,p,"F","Física",2,420,320);
                adicionar(g,p,"I","Ética e Sociedade",1,690,235);
                ligar(g,"A","B","M","F");
            }
            default -> throw new IllegalArgumentException("Exemplo não encontrado.");
        }
        return new ProjetoAcademico(g,p);
    }
    private static void adicionar(GrafoDisciplinas g, Map<String, VerticeVisual> p,
            String c, String n, int periodo, double x, double y) {
        g.adicionarDisciplina(new Disciplina(c,n,periodo)); p.put(c,new VerticeVisual(x,y));
    }
    private static void ligar(GrafoDisciplinas g, String... pares) {
        for (int i=0;i<pares.length;i+=2) g.adicionarDependencia(pares[i],pares[i+1]);
    }
    public static void main(String[] args) throws Exception {
        java.nio.file.Path pasta = java.nio.file.Path.of(args.length == 0 ? "exemplos" : args[0]);
        java.nio.file.Files.createDirectories(pasta);
        for (int i=0;i<4;i++) new GerenciadorArquivos().salvar(pasta.resolve("exemplo-"+(i+1)+".pgd"),criar(i));
        System.out.println("4 exemplos .pgd gravados.");
    }
}
