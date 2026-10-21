import java.util.*;
import java.util.function.Consumer;

/** DFS manual. Quadros explícitos simulam a recursão sem risco de StackOverflowError. */
public final class AlgoritmoDFS {
    private record Quadro(String codigo, Iterator<String> sucessores) { }
    public ResultadoOrdenacao executar(GrafoDisciplinas grafo) { return executar(grafo, null); }

    public ResultadoOrdenacao executar(GrafoDisciplinas grafo, Consumer<PassoAlgoritmo> observador) {
        Map<String, Set<String>> adj = grafo.listaAdjacencia();
        Map<String, PassoAlgoritmo.Cor> cores = new LinkedHashMap<>();
        adj.keySet().forEach(c -> cores.put(c, PassoAlgoritmo.Cor.BRANCO));
        List<String> caminho = new ArrayList<>();
        List<String> finalizacao = new ArrayList<>();
        List<String> ciclo = new ArrayList<>();
        Deque<Quadro> pilha = new ArrayDeque<>();
        emitir(observador, "INÍCIO", null, null, null, cores, caminho, finalizacao, ciclo, List.of(),
                "Os " + adj.size() + " vértices começam brancos.", "A DFS examina as raízes em ordem de cadastro.",
                "Branco = não visitado; cinza = ativo; preto = finalizado.", "Pilha e finalização vazias.", "Procurar a primeira raiz branca.");
        for (String raiz : adj.keySet()) {
            if (cores.get(raiz) != PassoAlgoritmo.Cor.BRANCO) continue;
            entrar(raiz, adj, cores, caminho, pilha);
            visita(observador, raiz, grafo, cores, caminho, finalizacao, ciclo);
            while (!pilha.isEmpty()) {
                Quadro q = pilha.peek(); String v = q.codigo();
                if (q.sucessores().hasNext()) {
                    String w = q.sucessores().next();
                    PassoAlgoritmo.Cor cor = cores.get(w);
                    if (cor == PassoAlgoritmo.Cor.CINZA && ciclo.isEmpty()) {
                        // Cinza significa ancestral ativo: o trecho da pilha mais a aresta fecha um ciclo real.
                        int inicio = caminho.indexOf(w);
                        ciclo.addAll(caminho.subList(inicio, caminho.size())); ciclo.add(w);
                    }
                    emitir(observador, cor == PassoAlgoritmo.Cor.CINZA ? "CICLO" : "EXPLORAR", v, v, w,
                            cores, caminho, finalizacao, ciclo, List.of(),
                            "Explorando " + v + " → " + w + "; destino " + cor.name().toLowerCase(Locale.ROOT) + ".",
                            "A aresta pertence à lista de sucessores de " + v + ".",
                            cor == PassoAlgoritmo.Cor.CINZA ? "Aresta para cinza fecha um caminho que ainda está na pilha." :
                                    cor == PassoAlgoritmo.Cor.BRANCO ? "Um sucessor branco precisa ser visitado antes de finalizar a origem." : "Um sucessor preto já concluiu seus sucessores.",
                            cor == PassoAlgoritmo.Cor.CINZA ? "Ciclo real identificado; nenhuma ordem topológica será aceita." : "A aresta foi examinada; o grafo original permanece intacto.",
                            cor == PassoAlgoritmo.Cor.BRANCO ? "Empilhar " + w + " e marcá-lo cinza." : "Continuar a exploração de " + v + ".");
                    if (cor == PassoAlgoritmo.Cor.BRANCO) {
                        entrar(w, adj, cores, caminho, pilha);
                        visita(observador, w, grafo, cores, caminho, finalizacao, ciclo);
                    }
                } else {
                    pilha.pop(); caminho.remove(caminho.size() - 1);
                    cores.put(v, PassoAlgoritmo.Cor.PRETO); finalizacao.add(v);
                    emitir(observador, "FINALIZAR", v, null, null, cores, caminho, finalizacao, ciclo, List.of(),
                            "Finalizando " + grafo.obterDisciplina(v) + ".", "Todos os sucessores de " + v + " foram examinados.",
                            "A entrada na finalização acontece na saída da DFS, depois dos sucessores visitados.",
                            v + " ficou preto e entrou no fim da lista de finalização.",
                            caminho.isEmpty() ? "Procurar outra raiz branca." : "Retomar a exploração de " + caminho.get(caminho.size() - 1) + ".");
                }
            }
        }
        List<String> ordem = new ArrayList<>(finalizacao); Collections.reverse(ordem);
        boolean circular = !ciclo.isEmpty();
        emitir(observador, "FIM", null, null, null, cores, caminho, finalizacao, ciclo, circular ? List.of() : ordem,
                circular ? "A DFS terminou e encontrou dependência circular." : "Finalização invertida para obter a ordem topológica.",
                circular ? "Foi encontrada uma aresta para um ancestral cinza." : "Sem arestas para cinza, o grafo é um DAG.",
                "Em um DAG, inverter a finalização coloca cada origem antes de seus destinos.",
                circular ? "A finalização existe, mas não é uma ordenação topológica válida." : "Ordenação completa disponível.",
                circular ? "Remover ou corrigir uma aresta do ciclo destacado." : "Comparar com Kahn ou planejar as etapas.");
        return new ResultadoOrdenacao("DFS", circular, circular ? List.of() : ordem,
                finalizacao, finalizacao, ciclo, Set.of());
    }
    private void entrar(String c, Map<String, Set<String>> adj, Map<String, PassoAlgoritmo.Cor> cores,
            List<String> caminho, Deque<Quadro> pilha) {
        cores.put(c, PassoAlgoritmo.Cor.CINZA); caminho.add(c);
        pilha.push(new Quadro(c, adj.get(c).iterator()));
    }
    private void visita(Consumer<PassoAlgoritmo> o, String v, GrafoDisciplinas g,
            Map<String, PassoAlgoritmo.Cor> cores, List<String> caminho, List<String> fim, List<String> ciclo) {
        emitir(o, "VISITAR", v, null, null, cores, caminho, fim, ciclo, List.of(),
                "Visitando " + g.obterDisciplina(v) + ".", v + " estava branco e foi alcançado por uma raiz ou aresta de exploração.",
                "Ao entrar, o vértice fica cinza e permanece ativo até concluir seus sucessores.",
                v + " foi empilhado; a pilha representa o caminho ativo.", "Examinar os sucessores de " + v + ".");
    }
    private void emitir(Consumer<PassoAlgoritmo> o, String evento, String atual, String origem, String destino,
            Map<String, PassoAlgoritmo.Cor> cores, List<String> pilha, List<String> fim, List<String> ciclo,
            List<String> ordem, String a, String m, String r, String d, String p) {
        if (o != null) o.accept(new PassoAlgoritmo("DFS", evento, atual, origem, destino, Map.of(), cores,
                List.of(), pilha, ordem, fim, ciclo, a, m, r, d, p));
    }
}
