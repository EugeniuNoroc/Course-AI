package lab.boids.model;

/**
 * Неизменяемый двумерный вектор. Используется и для позиции, и для скорости, и для ускорения.
 */
public record Vector2D(double x, double y) {

    public static final Vector2D ZERO = new Vector2D(0, 0);

    public Vector2D add(Vector2D o) {
        return new Vector2D(x + o.x, y + o.y);
    }

    public Vector2D sub(Vector2D o) {
        return new Vector2D(x - o.x, y - o.y);
    }

    public Vector2D mul(double k) {
        return new Vector2D(x * k, y * k);
    }

    public Vector2D div(double k) {
        return new Vector2D(x / k, y / k);
    }

    public double length() {
        return Math.sqrt(x * x + y * y);
    }

    /** Вектор той же направленности, но заданной длины. */
    public Vector2D withLength(double len) {
        double l = length();
        return l == 0 ? ZERO : mul(len / l);
    }

    /** Если длина больше max — укорачиваем до max, направление сохраняется. */
    public Vector2D limit(double max) {
        double l = length();
        return l > max ? mul(max / l) : this;
    }
}
