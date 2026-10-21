import java.util.*;
import java.util.function.Consumer;

/** Kahn manual, determinístico pela ordem de cadastro e de inserção das arestas. */
public final class AlgoritmoKahn {
    public ResultadoOrdenacao executar(GrafoDisciplinas grafo) { return executar(grafo, null); }

    public ResultadoOrdenacao executar(GrafoDisciplinas grafo, Consumer<PassoAlgoritmo> observador) {
        Map<String, Set<String>> adj = grafo.listaAdjacencia();
        Map<String, Integer> graus = new LinkedHashMap<>();
        adj.keySet().forEach(c -> graus.put(c, 0));
        adj.values().forEach(vs -> vs.forEach(v -> graus.put(v, graus.get(v) + 1)));
        Deque<String> fila = new ArrayDeque<>();
        List<String> ordem = new ArrayList<>();
        for (String c : adj.keySet()) if (graus.get(c) == 0) fila.add(c);
        emitir(observador, "INÍCIO", null, null, null, graus, fila, ordem,
                "Graus de entrada calculados: " + (observador == null ? "" : graus) + ".",
                "A fila inicial contém " + (observador == null ? "" : fila) + ", em ordem de cadastro.",
                "Um vértice com grau de entrada zero não tem pré-requisitos pendentes.",
                "Somente uma cópia dos graus foi criada; nenhuma aresta foi apagada.",
                fila.isEmpty() ? "Verificar se existem vértices pendentes." : "Retirar " + fila.peek() + " da fila.");
        while (!fila.isEmpty()) {
            String v = fila.remove(); ordem.add(v);
            emitir(observador, "PROCESSAR", v, null, null, graus, fila, ordem,
                    "Processando " + grafo.obterDisciplina(v) + ".",
                    v + " estava na frente da fila e possui grau de entrada residual zero.",
                    "Todos os pré-requisitos de " + v + " já foram processados.",
                    v + " entrou na ordem parcial; a fila agora contém " + (observador == null ? "" : fila) + ".",
                    adj.get(v).isEmpty() ? "Verificar o próximo elemento da fila." : "Reduzir os graus dos sucessores de " + v + ".");
            for (String w : adj.get(v)) {
                int antes = graus.get(w);
                graus.put(w, antes - 1);
                if (graus.get(w) == 0) fila.add(w);
                emitir(observador, "ATUALIZAR", v, v, w, graus, fila, ordem,
                        "A dependência " + v + " → " + w + " foi considerada.",
                        grafo.obterDisciplina(v).getNome() + " precisa ser concluída antes de " + grafo.obterDisciplina(w).getNome() + ".",
                        "Cada aresta que sai de um vértice processado reduz um grau residual em 1.",
                        "Grau de " + w + ": " + antes + " → " + graus.get(w) + (graus.get(w) == 0 ? "; entrou na fila." : "; ainda há pré-requisitos pendentes."),
                        "Continuar os sucessores de " + v + "; depois, retirar o próximo vértice da fila.");
            }
        }
        Set<String> restantes = new LinkedHashSet<>(adj.keySet()); restantes.removeAll(ordem);
        boolean ciclo = !restantes.isEmpty();
        emitir(observador, ciclo ? "CICLO" : "FIM", null, null, null, graus, fila, ordem,
                ciclo ? "A fila esvaziou com " + restantes.size() + " vértice(s) pendente(s)." : "Todos os " + ordem.size() + " vértices foram processados.",
                ciclo ? "Nenhum vértice restante tem grau residual zero." : "Cada escolha respeitou todos os pré-requisitos.",
                "Se a quantidade processada é menor que |V|, existe pelo menos um ciclo.",
                ciclo ? "A ordem parcial não será apresentada como ordenação válida." : "A ordem topológica está completa.",
                ciclo ? "Usar DFS para reconstruir um ciclo real e corrigir as ligações." : "Comparar com DFS ou consultar os períodos recomendados.");
        return new ResultadoOrdenacao("Kahn", ciclo, ciclo ? List.of() : ordem, ordem, List.of(), List.of(), restantes);
    }

    private void emitir(Consumer<PassoAlgoritmo> o, String evento, String atual, String origem, String destino,
            Map<String, Integer> graus, Deque<String> fila, List<String> ordem,
            String a, String m, String r, String d, String p) {
        if (o != null) o.accept(new PassoAlgoritmo("Kahn", evento, atual, origem, destino, graus, Map.of(),
                new ArrayList<>(fila), List.of(), ordem, List.of(), List.of(), a, m, r, d, p));
    }
}
