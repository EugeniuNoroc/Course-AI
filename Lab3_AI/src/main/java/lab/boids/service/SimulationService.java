package lab.boids.service;

import lab.boids.engine.BoidsSimulation;
import lab.boids.engine.FlockMetrics;
import lab.boids.model.Boid;
import lab.boids.model.SimulationParams;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Хранит текущую "живую" симуляцию, которую видно в браузере.
 * Методы synchronized — чтобы два запроса одновременно не сделали шаг.
 */
@Service
public class SimulationService {

    private final SimulationParams defaults;
    private BoidsSimulation simulation;

    public SimulationService(SimulationParams defaults) {
        this.defaults = defaults;
        this.simulation = new BoidsSimulation(defaults);
    }

    public synchronized SimulationState step(int count) {
        for (int i = 0; i < count; i++) {
            simulation.step();
        }
        return state();
    }

    /** Перезапуск: новые птицы со случайными позициями. */
    public synchronized SimulationState reset(SimulationParams params) {
        simulation = new BoidsSimulation(params != null ? params : defaults);
        return state();
    }

    /** Изменение весов и радиусов без перезапуска. */
    public synchronized SimulationState updateParams(SimulationParams params) {
        simulation.updateLiveParams(params);
        return state();
    }

    public synchronized SimulationState state() {
        FlockMetrics metrics = FlockMetrics.compute(simulation);
        List<double[]> list = new ArrayList<>();
        int i = 0;
        for (Boid b : simulation.getBoids()) {
            list.add(new double[]{
                    b.getPosition().x(), b.getPosition().y(),
                    b.getVelocity().x(), b.getVelocity().y(),
                    metrics.groupIds()[i++]});
        }
        return new SimulationState(simulation.getStep(), simulation.getParams(), list, metrics);
    }

    public SimulationParams getDefaults() {
        return defaults.copy();
    }
}
