import java.util.*;

/** Agrega o modelo e as posições para persistência; não contém estados de algoritmos. */
public record ProjetoAcademico(GrafoDisciplinas grafo, Map<String, VerticeVisual> posicoes) {
    public ProjetoAcademico {
        Objects.requireNonNull(grafo); Objects.requireNonNull(posicoes);
        if (!grafo.getCodigos().equals(posicoes.keySet())) throw new IllegalArgumentException("Todo vértice precisa de uma posição.");
        Map<String, VerticeVisual> copia = new LinkedHashMap<>();
        posicoes.forEach((c, v) -> copia.put(c, v.copiar()));
        posicoes = Collections.unmodifiableMap(copia);
    }
}
