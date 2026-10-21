import java.util.*;

public final class ComparadorAlgoritmos {
    public record Comparacao(ResultadoOrdenacao kahn, ResultadoOrdenacao dfs, boolean kahnValida,
            boolean dfsValida, DetectorCiclos.Analise ciclos, PlanejadorPeriodos.Planejamento planejamento) { }
    /** Verificador independente: exige permutação completa e testa cada aresta. */
    public static boolean ordemValida(GrafoDisciplinas g, List<String> ordem) {
        if (ordem == null || ordem.size() != g.quantidadeVertices()) return false;
        Map<String, Integer> pos = new HashMap<>();
        for (int i = 0; i < ordem.size(); i++) {
            String v = ordem.get(i);
            if (!g.contem(v) || pos.put(v, i) != null) return false;
        }
        for (var entrada : g.listaAdjacencia().entrySet()) for (String w : entrada.getValue()) {
            if (pos.get(entrada.getKey()) >= pos.get(w)) return false;
        }
        return true;
    }
    public Comparacao comparar(GrafoDisciplinas g) {
        ResultadoOrdenacao k = new AlgoritmoKahn().executar(g);
        ResultadoOrdenacao d = new AlgoritmoDFS().executar(g);
        return new Comparacao(k, d, !k.possuiCiclo() && ordemValida(g, k.ordem()),
                !d.possuiCiclo() && ordemValida(g, d.ordem()), new DetectorCiclos().analisar(g, d, k),
                k.possuiCiclo() ? null : new PlanejadorPeriodos().planejar(g));
    }
}
