import java.util.*;

public final class ExplicadorDidatico {
    private ExplicadorDidatico() { }
    public static String html(String texto) {
        return texto.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");
    }
    public static String ordem(Collection<String> itens) { return itens.isEmpty() ? "∅" : String.join(" → ",itens); }
    public static String documento(String conteudo) {
        return "<html><head><style>body{font-family:Segoe UI,sans-serif;font-size:12px;color:#25354a;margin:12px;}"
                + "h2{font-size:17px;color:#173e52;}h3{font-size:13px;color:#176b68;}p{margin:7px 0;}"
                + "td,th{padding:7px;text-align:left;}th{background:#e6efef;}table{border:1px solid #d7e0e5;}"
                + ".alerta{color:#b13c40;} .suave{color:#617186;}</style></head><body>" + conteudo + "</body></html>";
    }
    public static String passo(PassoAlgoritmo p, int indice, int total, boolean estudo) {
        String base = "<h2>"+p.algoritmo()+" · passo "+(indice+1)+" de "+total+"</h2><p><b>"+html(p.evento())+"</b> — "+html(p.acontecendo())+"</p>";
        if (!estudo) return documento(base);
        return documento(base+"<p><b>Por que esta escolha?</b> "+html(p.motivo())+"</p><p><b>Regra matemática:</b> "+html(p.regra())
                +"</p><p><b>O que mudou?</b> "+html(p.mudanca())+"</p><p><b>Próximo passo:</b> "+html(p.proximo())+"</p>");
    }
    public static String complexidade() {
        return documento("<h2>Complexidade e decisões</h2><p><b>V</b> é o número de disciplinas; <b>E</b> é o número de dependências.</p>"
                +"<h3>Kahn — O(V + E)</h3><p>Inicializa V graus e percorre E arestas para calculá-los. Cada vértice entra e sai da fila no máximo uma vez, e cada aresta é examinada uma vez ao reduzir graus. Memória auxiliar: O(V), além da representação do grafo.</p>"
                +"<h3>DFS — O(V + E)</h3><p>Cada vértice muda de branco para cinza e preto uma vez. Cada lista de sucessores é percorrida uma vez. Cores, finalização e pilha ocupam O(V). Os quadros explícitos simulam chamadas recursivas e evitam estouro da pilha nativa.</p>"
                +"<p>O modelo usa mapas e conjuntos com operações de custo médio O(1). Uma cópia segura da adjacência custa O(V + E) em tempo e espaço nesta implementação; por isso a memória total adicional das execuções inclui O(V + E), embora o estado de trabalho dos algoritmos seja O(V).</p>"
                +"<p>O planejamento propaga etapa(w) = máximo(etapa(w), etapa(v) + 1) em ordem topológica, também em O(V + E). A classificação de ciclos usa uma segunda passagem DFS no grafo transposto (Kosaraju), O(V + E).</p>"
                +"<table><tr><th>Aspecto</th><th>Kahn</th><th>DFS</th></tr><tr><td>Estrutura</td><td>Fila + graus</td><td>Pilha + três cores</td></tr>"
                +"<tr><td>Vantagem</td><td>Expõe disciplinas elegíveis</td><td>Reconstrói ciclo por ancestral cinza</td></tr><tr><td>Limitação</td><td>Resíduo não identifica sozinho o ciclo</td><td>Finalização precisa ser invertida; com ciclo é inválida</td></tr>"
                +"<tr><td>Aplicações</td><td>Oferta de disciplinas, tarefas de compilação</td><td>Análise de dependências, inspeção de fluxos</td></tr></table>"
                +"<h3>O custo da visualização é separado</h3><p>Copiar todos os estados para T passos custa O(T·V) e textos adicionais. A animação e o desenho não fazem parte da complexidade teórica do algoritmo. O histórico didático é limitado a entradas com estimativa de até 2000 passos e 120000 células de estado; grafos maiores oferecem análise final sem histórico.</p>");
    }
    public static String glossario() {
        return documento("<h2>Glossário e leitura do grafo</h2><p><b>A → B:</b> A precisa ser concluída antes de B.</p>"
                +"<p><b>Vértice:</b> disciplina cadastrada.</p><p><b>Aresta direcionada:</b> pré-requisito que liga uma origem a um destino.</p>"
                +"<p><b>Grau de entrada:</b> quantidade de pré-requisitos diretos. No Kahn, o grau residual conta os ainda pendentes.</p>"
                +"<p><b>Grau de saída:</b> quantidade de disciplinas que dependem diretamente do vértice.</p>"
                +"<p><b>DAG:</b> grafo direcionado acíclico.</p><p><b>Ordenação topológica:</b> sequência que coloca toda origem antes de seus destinos.</p>"
                +"<p><b>Fonte:</b> vértice de grau de entrada zero.</p><p><b>Sumidouro:</b> vértice de grau de saída zero.</p>"
                +"<p><b>DFS:</b> busca em profundidade; avança por sucessores antes de retornar.</p><p><b>Kahn:</b> retira fontes sucessivamente usando uma fila.</p>"
                +"<p><b>Dependência circular:</b> caminho direcionado que retorna à disciplina de origem.</p>"
                +"<p><b>Complexidade O(V + E):</b> trabalho proporcional ao total de vértices e arestas.</p>"
                +"<p><b>Período cadastrado:</b> informação acadêmica editável. <b>Etapa recomendada:</b> calculada exclusivamente pelas dependências.</p>"
                +"<p><b>Componente fraca:</b> grupo conectado ao ignorar a direção apenas para essa contagem. Grupos separados e vértices isolados não impedem uma ordenação topológica.</p>");
    }
}
