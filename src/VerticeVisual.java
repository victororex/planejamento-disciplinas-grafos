/** Posição do centro do vértice, separada do modelo matemático. */
public final class VerticeVisual {
    private double x;
    private double y;
    public VerticeVisual(double x, double y) { mover(x, y); }
    public double getX() { return x; }
    public double getY() { return y; }
    public void mover(double x, double y) {
        if (!Double.isFinite(x) || !Double.isFinite(y) || x < 90 || y < 45 || x > 10000 || y > 10000) {
            throw new IllegalArgumentException("A posição deve estar dentro da área de desenho (90 ≤ x ≤ 10000; 45 ≤ y ≤ 10000).");
        }
        this.x = x; this.y = y;
    }
    public VerticeVisual copiar() { return new VerticeVisual(x, y); }
}
