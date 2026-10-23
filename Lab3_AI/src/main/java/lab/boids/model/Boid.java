package lab.boids.model;

/**
 * Агент-птица: у неё есть только положение и скорость.
 * Всё "поведение" описано правилами в {@link lab.boids.engine.BoidsSimulation}.
 */
public class Boid {

    private final int id;
    private Vector2D position;
    private Vector2D velocity;

    public Boid(int id, Vector2D position, Vector2D velocity) {
        this.id = id;
        this.position = position;
        this.velocity = velocity;
    }

    public int getId() { return id; }
    public Vector2D getPosition() { return position; }
    public Vector2D getVelocity() { return velocity; }
    public void setPosition(Vector2D position) { this.position = position; }
    public void setVelocity(Vector2D velocity) { this.velocity = velocity; }
}
