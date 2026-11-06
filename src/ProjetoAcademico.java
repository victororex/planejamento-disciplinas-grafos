import java.util.*;
/** Uma matriz por projeto/arquivo; cenários pessoais pertencem somente a essa versão. */
public record ProjetoAcademico(GrafoDisciplinas grafo, Map<String, VerticeVisual> posicoes,
        MatrizCurricular matriz, List<PlanoAcademico> planos) {
    public ProjetoAcademico(GrafoDisciplinas grafo, Map<String,VerticeVisual> posicoes) {
        this(grafo,posicoes,MatrizCurricular.livre(),List.of());
    }
    public ProjetoAcademico {
        Objects.requireNonNull(grafo); Objects.requireNonNull(posicoes); Objects.requireNonNull(matriz);
        if (!grafo.getCodigos().equals(posicoes.keySet())) throw new IllegalArgumentException("Todo vértice precisa de uma posição.");
        Map<String,VerticeVisual> copia = new LinkedHashMap<>(); posicoes.forEach((c,v) -> copia.put(c,v.copiar()));
        posicoes = Collections.unmodifiableMap(copia); planos = List.copyOf(planos);
        Set<String> nomes = new HashSet<>();
        for (PlanoAcademico p : planos) {
            if (!nomes.add(p.nome())) throw new IllegalArgumentException("Nome de cenário duplicado.");
            p.selecionadas().forEach(grafo::obterDisciplina); p.aprovacoesSimuladas().forEach(grafo::obterDisciplina);
        }
    }
}
