import java.util.*;

/** Fotografia imutável de um estado real; permite voltar sem executar o algoritmo ao contrário. */
public record PassoAlgoritmo(String algoritmo, String evento, String atual, String origem, String destino,
        Map<String, Integer> graus, Map<String, Cor> cores, List<String> fila,
        List<String> pilha, List<String> ordem, List<String> finalizados, List<String> ciclo,
        String acontecendo, String motivo, String regra, String mudanca, String proximo) {
    public enum Cor { BRANCO, CINZA, PRETO }
    public PassoAlgoritmo {
        graus = Collections.unmodifiableMap(new LinkedHashMap<>(graus));
        cores = Collections.unmodifiableMap(new LinkedHashMap<>(cores));
        fila = List.copyOf(fila); pilha = List.copyOf(pilha); ordem = List.copyOf(ordem);
        finalizados = List.copyOf(finalizados); ciclo = List.copyOf(ciclo);
    }
}
