package lab.boids.service;

import lab.boids.engine.FlockMetrics;
import lab.boids.model.SimulationParams;

import java.util.List;

/**
 * То, что сервер отдаёт браузеру после каждого шага.
 * boids — массив [x, y, vx, vy, номерГруппы] для каждой птицы (компактно для JSON).
 */
public record SimulationState(
        long step,
        SimulationParams params,
        List<double[]> boids,
        FlockMetrics metrics) {
}
