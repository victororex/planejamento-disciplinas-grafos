import java.util.*;

/** Avalia elegibilidade curricular, sem presumir oferta, vagas, horários ou autorização institucional. */
public final class ServicoAcademico {
    public record Progresso(int aprovadas, int dispensadas, int pendentes, int andamento,
            double horas, double creditos, double percentual, int cargasDesconhecidas, int creditosDesconhecidos) { }
    public record Avaliacao(boolean disponivel, List<String> motivos) {
        public Avaliacao { motivos = List.copyOf(motivos); }
    }
    public record ValidacaoPlano(boolean valido, Double horas, List<String> conflitos) {
        public ValidacaoPlano { conflitos = List.copyOf(conflitos); }
    }
    public Set<String> concluidas(GrafoDisciplinas g, Set<String> simuladas) {
        Set<String> r = new LinkedHashSet<>(simuladas); simuladas.forEach(g::obterDisciplina);
        for (Disciplina d : g.getDisciplinas()) if (d.getStatus().concluida()) r.add(d.getCodigo()); return r;
    }
    public Progresso progresso(GrafoDisciplinas g) {
        int aprovadas=0, dispensadas=0, andamento=0, semHoras=0, semCreditos=0; double horas=0, creditos=0;
        for (Disciplina d : g.getDisciplinas()) {
            if (d.getStatus()==StatusAcademico.APROVADA) aprovadas++;
            if (d.getStatus()==StatusAcademico.DISPENSADA) dispensadas++;
            if (d.getStatus()==StatusAcademico.EM_ANDAMENTO) andamento++;
            if (d.getStatus().concluida()) {
                if (d.getCargaHoraria()==null) semHoras++; else horas+=d.getCargaHoraria();
                if (d.getCreditos()==null) semCreditos++; else creditos+=d.getCreditos();
            }
        }
        int total=g.quantidadeVertices(), concluidas=aprovadas+dispensadas;
        return new Progresso(aprovadas,dispensadas,total-concluidas,andamento,horas,creditos,
            total==0?0:100.0*concluidas/total,semHoras,semCreditos);
    }
    public Avaliacao avaliar(GrafoDisciplinas g, String codigo, Set<String> simuladas, Set<String> mesmoPeriodo) {
        Disciplina d=g.obterDisciplina(codigo); mesmoPeriodo.forEach(g::obterDisciplina);
        Set<String> feitas=concluidas(g,simuladas); List<String> motivos=new ArrayList<>();
        if (feitas.contains(codigo)) motivos.add("Disciplina já concluída neste cenário.");
        if (d.getStatus()==StatusAcademico.EM_ANDAMENTO && !feitas.contains(codigo)) motivos.add("Disciplina já está em andamento; ainda não foi concluída.");
        for (String p : g.obterPrerequisitos(codigo)) if (!feitas.contains(p)) motivos.add(p+" ainda não foi concluída (pré-requisito).");
        for (Requisito r : g.requisitos(codigo)) motivos.addAll(pendencias(g,r,feitas,mesmoPeriodo));
        return new Avaliacao(motivos.isEmpty(),motivos);
    }
    private List<String> pendencias(GrafoDisciplinas g, Requisito r, Set<String> feitas, Set<String> mesmoPeriodo) {
        switch (r.tipo()) {
            case AND, OR:
                List<String> faltas=new ArrayList<>();
                for (Requisito i : r.itens()) {
                    List<String> f=pendencias(g,i,feitas,mesmoPeriodo);
                    if (r.tipo()==Requisito.Tipo.OR && f.isEmpty()) return List.of();
                    faltas.addAll(f);
                }
                return r.tipo()==Requisito.Tipo.OR ? List.of("Satisfazer uma alternativa: "+r+". Pendências: "+String.join("; ",faltas)) : faltas;
            case CONCLUIDA, ATIVIDADE:
                return feitas.contains(r.codigo())?List.of():List.of(r.codigo()+" ainda não foi concluída.");
            case CORREQUISITO:
                return feitas.contains(r.codigo())||mesmoPeriodo.contains(r.codigo())?List.of():List.of("Correquisito "+r.codigo()+": concluir antes ou incluir no mesmo período.");
            case HORAS, CREDITOS:
                double soma=0; int desconhecidas=0;
                for (String c : feitas) {
                    Disciplina d=g.obterDisciplina(c); Double n=r.tipo()==Requisito.Tipo.HORAS?d.getCargaHoraria():d.getCreditos();
                    if (n==null) desconhecidas++; else soma+=n;
                }
                if (soma>=r.minimo()) return List.of();
                return List.of("Exigidos "+r+"; total conhecido: "+soma+(desconhecidas>0?" (há valores não informados).":"."));
            default: throw new IllegalStateException("Tipo de requisito sem avaliador.");
        }
    }
    public Map<String,Avaliacao> disponibilidade(GrafoDisciplinas g, Set<String> simuladas, Set<String> mesmoPeriodo) {
        Map<String,Avaliacao> r=new LinkedHashMap<>();
        for (String c:g.getCodigos()) r.put(c,avaliar(g,c,simuladas,mesmoPeriodo)); return Collections.unmodifiableMap(r);
    }
    public ValidacaoPlano validar(GrafoDisciplinas g, PlanoAcademico plano) {
        List<String> conflitos=new ArrayList<>(); double horas=0; boolean desconhecida=false;
        plano.aprovacoesSimuladas().forEach(g::obterDisciplina);
        for (String c:plano.selecionadas()) {
            Disciplina d=g.obterDisciplina(c);
            for (String motivo:avaliar(g,c,plano.aprovacoesSimuladas(),plano.selecionadas()).motivos()) conflitos.add(c+": "+motivo);
            if (d.getCargaHoraria()==null) desconhecida=true; else horas+=d.getCargaHoraria();
        }
        if (plano.maxDisciplinas()!=null && plano.selecionadas().size()>plano.maxDisciplinas()) conflitos.add("Quantidade máxima de disciplinas excedida.");
        if (plano.maxHoras()!=null) {
            if (desconhecida) conflitos.add("Não é possível verificar a carga máxima: há carga horária não informada.");
            if (horas>plano.maxHoras()) conflitos.add("Carga horária máxima excedida: "+horas+" h.");
        }
        return new ValidacaoPlano(conflitos.isEmpty(),desconhecida?null:horas,conflitos);
    }
    /** Sugestão gulosa determinística, não ótima; inclui fecho de correquisitos e só aceita conjuntos válidos. */
    public PlanoAcademico sugerir(GrafoDisciplinas g, PlanoAcademico base) {
        Set<String> escolhidas=new LinkedHashSet<>();
        for (String c:g.getCodigos()) {
            Set<String> tentativa=new LinkedHashSet<>(escolhidas); tentativa.add(c);
            Deque<String> fila=new ArrayDeque<>(); fila.add(c);
            Set<String> feitas=concluidas(g,base.aprovacoesSimuladas());
            while (!fila.isEmpty()) for (Requisito r:g.requisitos(fila.remove())) incluirCorrequisitos(r,feitas,tentativa,fila);
            PlanoAcademico p=new PlanoAcademico(base.nome(),base.periodo(),base.maxHoras(),base.maxDisciplinas(),tentativa,base.aprovacoesSimuladas());
            if (validar(g,p).valido()) escolhidas=tentativa;
        }
        return new PlanoAcademico(base.nome(),base.periodo(),base.maxHoras(),base.maxDisciplinas(),escolhidas,base.aprovacoesSimuladas());
    }
    private void incluirCorrequisitos(Requisito r, Set<String> feitas, Set<String> selecao, Deque<String> fila) {
        if (r.tipo()==Requisito.Tipo.CORREQUISITO && !feitas.contains(r.codigo()) && selecao.add(r.codigo())) fila.add(r.codigo());
        // Em OR, a sugestão tenta somente a primeira alternativa; a validação manual aceita todas.
        List<Requisito> itens=r.tipo()==Requisito.Tipo.OR?r.itens().subList(0,1):r.itens();
        for (Requisito i:itens) incluirCorrequisitos(i,feitas,selecao,fila);
    }
}
