public enum StatusAcademico {
    NAO_CURSADA("Não cursada"), EM_ANDAMENTO("Em andamento"), APROVADA("Aprovada"),
    REPROVADA("Reprovada"), DISPENSADA("Dispensada ou aproveitada");
    private final String rotulo;
    StatusAcademico(String rotulo) { this.rotulo = rotulo; }
    public boolean concluida() { return this == APROVADA || this == DISPENSADA; }
    @Override public String toString() { return rotulo; }
}
