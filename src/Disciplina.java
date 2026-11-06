import java.util.Objects;

/** Cadastro imutável; requisitos pertencem ao grafo. Null e período 0 significam não informado. */
public final class Disciplina {
    public static final String[] CATEGORIAS = {"", "Obrigatória", "Optativa", "Eletiva", "Extensão",
        "Atividade complementar", "Trabalho de conclusão", "Estágio", "Outra"};
    private final String codigo, nome, categoria, descricao, observacoes, oferta;
    private final int periodoReferencia;
    private final Double cargaHoraria, creditos;
    private final StatusAcademico status;
    public Disciplina(String codigo, String nome, int periodo) {
        this(codigo, nome, periodo, null, null, "", "", "", "", StatusAcademico.NAO_CURSADA);
        if (periodo < 1) throw new IllegalArgumentException("O período cadastrado deve estar entre 1 e 100.");
    }
    public Disciplina(String codigo, String nome, int periodo, Double horas, Double creditos,
            String categoria, String descricao, String observacoes, String oferta, StatusAcademico status) {
        if (codigo == null || !codigo.strip().matches("[\\p{L}\\p{N}_-]{1,24}"))
            throw new IllegalArgumentException("Use um código de 1 a 24 letras, números, _ ou -.");
        if (nome == null || nome.isBlank() || nome.strip().length() > 120 || nome.chars().anyMatch(Character::isISOControl))
            throw new IllegalArgumentException("Informe um nome de 1 a 120 caracteres, sem quebras de linha.");
        if (periodo < 0 || periodo > 100) throw new IllegalArgumentException("Período entre 1 e 100, ou vazio.");
        validarQuantidade(horas); validarQuantidade(creditos);
        this.codigo = codigo.strip(); this.nome = nome.strip(); this.periodoReferencia = periodo;
        this.cargaHoraria = horas; this.creditos = creditos; this.categoria = MatrizCurricular.texto(categoria);
        this.descricao = MatrizCurricular.texto(descricao); this.observacoes = MatrizCurricular.texto(observacoes);
        this.oferta = MatrizCurricular.texto(oferta); this.status = Objects.requireNonNull(status);
    }
    static void validarQuantidade(Double n) {
        if (n != null && (!Double.isFinite(n) || n < 0 || n > 1000000)) throw new IllegalArgumentException("Horas/créditos devem estar entre 0 e 1000000.");
    }
    public String getCodigo() { return codigo; }
    public String getNome() { return nome; }
    public int getPeriodoReferencia() { return periodoReferencia; }
    public Double getCargaHoraria() { return cargaHoraria; }
    public Double getCreditos() { return creditos; }
    public String getCategoria() { return categoria; }
    public String getDescricao() { return descricao; }
    public String getObservacoes() { return observacoes; }
    public String getOferta() { return oferta; }
    public StatusAcademico getStatus() { return status; }
    public Disciplina comStatus(StatusAcademico s) { return new Disciplina(codigo, nome, periodoReferencia, cargaHoraria, creditos, categoria, descricao, observacoes, oferta, s); }
    @Override public String toString() { return codigo + " — " + nome; }
    @Override public boolean equals(Object o) {
        return o instanceof Disciplina d && codigo.equals(d.codigo) && nome.equals(d.nome)
            && periodoReferencia == d.periodoReferencia && Objects.equals(cargaHoraria,d.cargaHoraria)
            && Objects.equals(creditos,d.creditos) && categoria.equals(d.categoria) && descricao.equals(d.descricao)
            && observacoes.equals(d.observacoes) && oferta.equals(d.oferta) && status == d.status;
    }
    @Override public int hashCode() { return Objects.hash(codigo,nome,periodoReferencia,cargaHoraria,creditos,categoria,descricao,observacoes,oferta,status); }
}
