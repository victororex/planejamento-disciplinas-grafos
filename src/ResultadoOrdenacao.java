import java.util.*;

public record ResultadoOrdenacao(String algoritmo, boolean possuiCiclo, List<String> ordem,
        List<String> processados, List<String> finalizacao, List<String> ciclo,
        Set<String> naoProcessados) {
    public ResultadoOrdenacao {
        ordem = List.copyOf(ordem); processados = List.copyOf(processados);
        finalizacao = List.copyOf(finalizacao); ciclo = List.copyOf(ciclo);
        naoProcessados = Collections.unmodifiableSet(new LinkedHashSet<>(naoProcessados));
        if (possuiCiclo && !ordem.isEmpty()) throw new IllegalArgumentException("Um grafo cíclico não tem ordem topológica.");
    }
}
