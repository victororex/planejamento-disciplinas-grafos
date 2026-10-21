import java.util.*;

public final class PlanejadorPeriodos {
    public record Planejamento(Map<String, Integer> etapas, List<List<String>> grupos) {
        public Planejamento {
            etapas = Collections.unmodifiableMap(new LinkedHashMap<>(etapas));
            grupos = grupos.stream().map(List::copyOf).toList();
        }
    }
    public Planejamento planejar(GrafoDisciplinas g) {
        ResultadoOrdenacao r = new AlgoritmoKahn().executar(g);
        if (r.possuiCiclo()) throw new IllegalArgumentException("Não é possível gerar um planejamento válido enquanto existir uma dependência circular.");
        Map<String, Integer> etapas = new LinkedHashMap<>();
        g.getCodigos().forEach(c -> etapas.put(c, 1));
        Map<String, Set<String>> adj = g.listaAdjacencia();
        for (String v : r.ordem()) for (String w : adj.get(v)) {
            etapas.put(w, Math.max(etapas.get(w), etapas.get(v) + 1));
        }
        List<List<String>> grupos = new ArrayList<>();
        for (String v : g.getCodigos()) {
            int etapa = etapas.get(v);
            while (grupos.size() < etapa) grupos.add(new ArrayList<>());
            grupos.get(etapa - 1).add(v);
        }
        return new Planejamento(etapas, grupos);
    }
}
