import java.util.*;

public final class DetectorCiclos {
    public record Analise(List<String> caminho, Set<String> emCiclos, Set<String> apenasBloqueados) {
        public Analise {
            caminho = List.copyOf(caminho);
            emCiclos = Collections.unmodifiableSet(new LinkedHashSet<>(emCiclos));
            apenasBloqueados = Collections.unmodifiableSet(new LinkedHashSet<>(apenasBloqueados));
        }
        public boolean possuiCiclo() { return !caminho.isEmpty(); }
    }
    public Analise analisar(GrafoDisciplinas g) {
        return analisar(g, new AlgoritmoDFS().executar(g), new AlgoritmoKahn().executar(g));
    }
    public Analise analisar(GrafoDisciplinas g, ResultadoOrdenacao dfs, ResultadoOrdenacao kahn) {
        if (!dfs.possuiCiclo()) return new Analise(List.of(), Set.of(), Set.of());
        // Segunda passagem de Kosaraju: identifica TODOS os vértices em ciclos, inclusive outros ciclos.
        // O testemunho exibido continua sendo o caminho reconstruído pela pilha da DFS.
        Map<String, Set<String>> reverso = new LinkedHashMap<>();
        g.getCodigos().forEach(c -> reverso.put(c, new LinkedHashSet<>()));
        g.listaAdjacencia().forEach((v, ws) -> ws.forEach(w -> reverso.get(w).add(v)));
        List<String> ordem = new ArrayList<>(dfs.finalizacao()); Collections.reverse(ordem);
        Set<String> vistos = new HashSet<>(); Set<String> ciclicos = new LinkedHashSet<>();
        for (String raiz : ordem) {
            if (!vistos.add(raiz)) continue;
            Set<String> componente = new LinkedHashSet<>();
            Deque<String> pilha = new ArrayDeque<>(); pilha.push(raiz);
            while (!pilha.isEmpty()) {
                String v = pilha.pop(); componente.add(v);
                for (String w : reverso.get(v)) if (vistos.add(w)) pilha.push(w);
            }
            if (componente.size() > 1) ciclicos.addAll(componente);
        }
        Set<String> bloqueados = new LinkedHashSet<>(kahn.naoProcessados()); bloqueados.removeAll(ciclicos);
        return new Analise(dfs.ciclo(), ciclicos, bloqueados);
    }
}
