import java.util.*;
/** Cenário independente: nunca altera período oficial ou histórico. */
public record PlanoAcademico(String nome, String periodo, Double maxHoras, Integer maxDisciplinas,
        Set<String> selecionadas, Set<String> aprovacoesSimuladas) {
    public PlanoAcademico {
        nome = MatrizCurricular.texto(nome); periodo = MatrizCurricular.texto(periodo);
        if (nome.isBlank()) throw new IllegalArgumentException("Dê um nome ao cenário.");
        Disciplina.validarQuantidade(maxHoras);
        if (maxDisciplinas != null && (maxDisciplinas < 1 || maxDisciplinas > 2000)) throw new IllegalArgumentException("Limite de disciplinas entre 1 e 2000.");
        selecionadas = Collections.unmodifiableSet(new LinkedHashSet<>(selecionadas));
        aprovacoesSimuladas = Collections.unmodifiableSet(new LinkedHashSet<>(aprovacoesSimuladas));
    }
}
