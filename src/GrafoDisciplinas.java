import java.util.*;

/** A -> B significa: A é pré-requisito de B. Sem coordenadas ou dependências de Swing. */
public final class GrafoDisciplinas {
    private final Map<String, Disciplina> disciplinas = new LinkedHashMap<>();
    private final Map<String, Set<String>> adjacencia = new LinkedHashMap<>();

    public void adicionarDisciplina(Disciplina d) {
        Objects.requireNonNull(d, "Disciplina obrigatória.");
        if (disciplinas.containsKey(d.getCodigo())) throw new IllegalArgumentException("Já existe uma disciplina com esse código.");
        disciplinas.put(d.getCodigo(), d);
        adjacencia.put(d.getCodigo(), new LinkedHashSet<>());
    }

    public void editarDisciplina(String anterior, Disciplina nova) {
        obterDisciplina(anterior);
        if (!anterior.equals(nova.getCodigo()) && disciplinas.containsKey(nova.getCodigo())) {
            throw new IllegalArgumentException("Já existe uma disciplina com esse código.");
        }
        // Reconstrói mantendo a ordem de cadastro e todas as relações, inclusive ao trocar o código.
        Map<String, Disciplina> ds = new LinkedHashMap<>();
        Map<String, Set<String>> adj = new LinkedHashMap<>();
        disciplinas.forEach((c, d) -> ds.put(c.equals(anterior) ? nova.getCodigo() : c,
                c.equals(anterior) ? nova : d));
        adjacencia.forEach((c, vizinhos) -> {
            Set<String> destinos = new LinkedHashSet<>();
            for (String v : vizinhos) destinos.add(v.equals(anterior) ? nova.getCodigo() : v);
            adj.put(c.equals(anterior) ? nova.getCodigo() : c, destinos);
        });
        disciplinas.clear(); disciplinas.putAll(ds);
        adjacencia.clear(); adjacencia.putAll(adj);
    }

    public void removerDisciplina(String codigo) {
        obterDisciplina(codigo);
        disciplinas.remove(codigo); adjacencia.remove(codigo);
        adjacencia.values().forEach(v -> v.remove(codigo));
    }

    public void adicionarDependencia(String origem, String destino) {
        obterDisciplina(origem); obterDisciplina(destino);
        if (origem.equals(destino)) throw new IllegalArgumentException("Uma disciplina não pode ser pré-requisito dela mesma.");
        if (!adjacencia.get(origem).add(destino)) throw new IllegalArgumentException("Esse pré-requisito já foi cadastrado.");
    }

    public void removerDependencia(String origem, String destino) {
        obterDisciplina(origem); obterDisciplina(destino);
        if (!adjacencia.get(origem).remove(destino)) throw new IllegalArgumentException("Essa dependência não existe.");
    }

    public Disciplina obterDisciplina(String codigo) {
        Disciplina d = disciplinas.get(codigo);
        if (d == null) throw new IllegalArgumentException("Disciplina não encontrada: " + codigo);
        return d;
    }
    public boolean contem(String codigo) { return disciplinas.containsKey(codigo); }
    public int quantidadeVertices() { return disciplinas.size(); }
    public int quantidadeArestas() { return adjacencia.values().stream().mapToInt(Set::size).sum(); }
    public List<Disciplina> getDisciplinas() { return List.copyOf(disciplinas.values()); }
    public Set<String> getCodigos() { return Collections.unmodifiableSet(new LinkedHashSet<>(disciplinas.keySet())); }
    public Set<String> obterVizinhos(String codigo) {
        obterDisciplina(codigo);
        return Collections.unmodifiableSet(new LinkedHashSet<>(adjacencia.get(codigo)));
    }
    public Set<String> obterPrerequisitos(String codigo) {
        obterDisciplina(codigo);
        Set<String> r = new LinkedHashSet<>();
        adjacencia.forEach((c, vs) -> { if (vs.contains(codigo)) r.add(c); });
        return Collections.unmodifiableSet(r);
    }
    public int grauEntrada(String codigo) { return obterPrerequisitos(codigo).size(); }
    public int grauSaida(String codigo) { obterDisciplina(codigo); return adjacencia.get(codigo).size(); }
    public boolean existeDependencia(String a, String b) { return adjacencia.containsKey(a) && adjacencia.get(a).contains(b); }
    public Map<String, Set<String>> listaAdjacencia() {
        Map<String, Set<String>> copia = new LinkedHashMap<>();
        adjacencia.forEach((c, vs) -> copia.put(c, Collections.unmodifiableSet(new LinkedHashSet<>(vs))));
        return Collections.unmodifiableMap(copia);
    }
    public GrafoDisciplinas copiar() {
        GrafoDisciplinas g = new GrafoDisciplinas();
        disciplinas.values().forEach(g::adicionarDisciplina);
        adjacencia.forEach((c, vs) -> vs.forEach(v -> g.adicionarDependencia(c, v)));
        return g;
    }

    /** DFS da Aula 7 numa visão não direcionada: componentes FRACAMENTE conexas do dígrafo. */
    public List<Set<String>> componentesFracas() {
        Map<String, Set<String>> naoDirecionado = new LinkedHashMap<>();
        disciplinas.keySet().forEach(c -> naoDirecionado.put(c, new LinkedHashSet<>()));
        adjacencia.forEach((c, vs) -> vs.forEach(v -> {
            naoDirecionado.get(c).add(v); naoDirecionado.get(v).add(c);
        }));
        Set<String> vistos = new HashSet<>();
        List<Set<String>> grupos = new ArrayList<>();
        for (String c : disciplinas.keySet()) {
            if (!vistos.add(c)) continue;
            Set<String> grupo = new LinkedHashSet<>();
            Deque<String> pilha = new ArrayDeque<>(); pilha.push(c);
            while (!pilha.isEmpty()) {
                String v = pilha.pop(); grupo.add(v);
                for (String w : naoDirecionado.get(v)) if (vistos.add(w)) pilha.push(w);
            }
            grupos.add(Collections.unmodifiableSet(grupo));
        }
        return List.copyOf(grupos);
    }
}
