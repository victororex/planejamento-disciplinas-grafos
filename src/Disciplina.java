import java.util.Objects;

/** Cadastro imutável. Os pré-requisitos são consultados no grafo, a única fonte das relações. */
public final class Disciplina {
    private final String codigo;
    private final String nome;
    private final int periodoReferencia;

    public Disciplina(String codigo, String nome, int periodoReferencia) {
        if (codigo == null || !codigo.strip().matches("[\\p{L}\\p{N}_-]{1,24}")) {
            throw new IllegalArgumentException("Use um código de 1 a 24 letras, números, _ ou -.");
        }
        if (nome == null || nome.isBlank() || nome.strip().length() > 120
                || nome.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Informe um nome de 1 a 120 caracteres, sem quebras de linha.");
        }
        if (periodoReferencia < 1 || periodoReferencia > 100) {
            throw new IllegalArgumentException("O período cadastrado deve estar entre 1 e 100.");
        }
        this.codigo = codigo.strip();
        this.nome = nome.strip();
        this.periodoReferencia = periodoReferencia;
    }

    public String getCodigo() { return codigo; }
    public String getNome() { return nome; }
    public int getPeriodoReferencia() { return periodoReferencia; }
    @Override public String toString() { return codigo + " — " + nome; }
    @Override public boolean equals(Object o) {
        return o instanceof Disciplina d && codigo.equals(d.codigo) && nome.equals(d.nome)
                && periodoReferencia == d.periodoReferencia;
    }
    @Override public int hashCode() { return Objects.hash(codigo, nome, periodoReferencia); }
}
