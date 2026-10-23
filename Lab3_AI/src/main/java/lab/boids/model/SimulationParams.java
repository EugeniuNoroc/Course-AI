package lab.boids.model;

/**
 * Все параметры среды, агентов и правил в одном месте.
 * Значения по умолчанию берутся из application.yml (префикс "boids"),
 * а из браузера их можно менять через REST API.
 */
public class SimulationParams {

    // --- среда ---
    private double worldWidth = 100;
    private double worldHeight = 100;
    private BoundaryMode boundaryMode = BoundaryMode.WRAP;

    // --- агенты ---
    private int boidCount = 50;
    private double maxSpeed = 1.0;
    private double minSpeed = 0.3;
    private double maxAcceleration = 0.1;
    private long seed = 42;

    // --- правила Boids ---
    private double perceptionRadius = 10;
    private double separationRadius = 3;
    private double cohesionWeight = 0.01;
    private double alignmentWeight = 0.05;
    private double separationWeight = 0.1;

    // --- метрики ---
    private double collisionDistance = 1.0;
    private double groupDistance = 5.0;

    /** Независимая копия, чтобы эксперименты не портили параметры друг друга. */
    public SimulationParams copy() {
        SimulationParams c = new SimulationParams();
        c.worldWidth = worldWidth;
        c.worldHeight = worldHeight;
        c.boundaryMode = boundaryMode;
        c.boidCount = boidCount;
        c.maxSpeed = maxSpeed;
        c.minSpeed = minSpeed;
        c.maxAcceleration = maxAcceleration;
        c.seed = seed;
        c.perceptionRadius = perceptionRadius;
        c.separationRadius = separationRadius;
        c.cohesionWeight = cohesionWeight;
        c.alignmentWeight = alignmentWeight;
        c.separationWeight = separationWeight;
        c.collisionDistance = collisionDistance;
        c.groupDistance = groupDistance;
        return c;
    }

    public double getWorldWidth() { return worldWidth; }
    public void setWorldWidth(double worldWidth) { this.worldWidth = worldWidth; }
    public double getWorldHeight() { return worldHeight; }
    public void setWorldHeight(double worldHeight) { this.worldHeight = worldHeight; }
    public BoundaryMode getBoundaryMode() { return boundaryMode; }
    public void setBoundaryMode(BoundaryMode boundaryMode) { this.boundaryMode = boundaryMode; }
    public int getBoidCount() { return boidCount; }
    public void setBoidCount(int boidCount) { this.boidCount = boidCount; }
    public double getMaxSpeed() { return maxSpeed; }
    public void setMaxSpeed(double maxSpeed) { this.maxSpeed = maxSpeed; }
    public double getMinSpeed() { return minSpeed; }
    public void setMinSpeed(double minSpeed) { this.minSpeed = minSpeed; }
    public double getMaxAcceleration() { return maxAcceleration; }
    public void setMaxAcceleration(double maxAcceleration) { this.maxAcceleration = maxAcceleration; }
    public long getSeed() { return seed; }
    public void setSeed(long seed) { this.seed = seed; }
    public double getPerceptionRadius() { return perceptionRadius; }
    public void setPerceptionRadius(double perceptionRadius) { this.perceptionRadius = perceptionRadius; }
    public double getSeparationRadius() { return separationRadius; }
    public void setSeparationRadius(double separationRadius) { this.separationRadius = separationRadius; }
    public double getCohesionWeight() { return cohesionWeight; }
    public void setCohesionWeight(double cohesionWeight) { this.cohesionWeight = cohesionWeight; }
    public double getAlignmentWeight() { return alignmentWeight; }
    public void setAlignmentWeight(double alignmentWeight) { this.alignmentWeight = alignmentWeight; }
    public double getSeparationWeight() { return separationWeight; }
    public void setSeparationWeight(double separationWeight) { this.separationWeight = separationWeight; }
    public double getCollisionDistance() { return collisionDistance; }
    public void setCollisionDistance(double collisionDistance) { this.collisionDistance = collisionDistance; }
    public double getGroupDistance() { return groupDistance; }
    public void setGroupDistance(double groupDistance) { this.groupDistance = groupDistance; }
}
