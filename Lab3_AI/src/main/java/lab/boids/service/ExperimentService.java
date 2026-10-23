package lab.boids.service;

import lab.boids.engine.BoidsSimulation;
import lab.boids.engine.FlockMetrics;
import lab.boids.model.BoundaryMode;
import lab.boids.model.SimulationParams;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Серия экспериментов из задания. Каждый эксперимент:
 *   - берёт параметры по умолчанию и меняет один-два параметра;
 *   - прогоняет симуляцию STEPS шагов без визуализации для нескольких seed;
 *   - усредняет метрики по последним AVG_WINDOW шагам (когда стая уже "устоялась").
 */
@Service
public class ExperimentService {

    public static final int STEPS = 500;
    public static final int AVG_WINDOW = 100;
    public static final long[] SEEDS = {1, 2, 3, 4, 5};

    public record Experiment(String series, String name, Consumer<SimulationParams> change) {}

    public record ExperimentResult(
            String series,
            String name,
            SimulationParams params,
            double polarization,
            double groups,
            double largestGroup,
            double avgNearestDistance,
            double collisionsPerStep) {}

    private final SimulationParams defaults;

    public ExperimentService(SimulationParams defaults) {
        this.defaults = defaults;
    }

    public List<Experiment> experiments() {
        List<Experiment> list = new ArrayList<>();

        // Серия A: радиус взаимодействия (веса базовые 0.01 / 0.05 / 0.1)
        for (double r : new double[]{2, 5, 10, 20, 40}) {
            list.add(new Experiment("A. Радиус взаимодействия", "R = " + (int) r,
                    p -> p.setPerceptionRadius(r)));
        }

        // Серия B: веса правил (R = 10)
        list.add(new Experiment("B. Веса правил", "Базовые: C=0.01, A=0.05, S=0.1", p -> {}));
        list.add(new Experiment("B. Веса правил", "Без правил: C=0, A=0, S=0",
                p -> w(p, 0, 0, 0)));
        list.add(new Experiment("B. Веса правил", "Только разделение: S=0.1",
                p -> w(p, 0, 0, 0.1)));
        list.add(new Experiment("B. Веса правил", "Без разделения: S=0",
                p -> w(p, 0.01, 0.05, 0)));
        list.add(new Experiment("B. Веса правил", "Без выравнивания: A=0",
                p -> w(p, 0.01, 0, 0.1)));
        list.add(new Experiment("B. Веса правил", "Без центрирования: C=0",
                p -> w(p, 0, 0.05, 0.1)));
        list.add(new Experiment("B. Веса правил", "Сильное центрирование: C=0.05",
                p -> w(p, 0.05, 0.05, 0.1)));
        list.add(new Experiment("B. Веса правил", "Сильное разделение: S=0.5",
                p -> w(p, 0.01, 0.05, 0.5)));

        // Серия C: способ обработки границ
        list.add(new Experiment("C. Границы", "WRAP (тор)",
                p -> p.setBoundaryMode(BoundaryMode.WRAP)));
        list.add(new Experiment("C. Границы", "BOUNCE (отражение)",
                p -> p.setBoundaryMode(BoundaryMode.BOUNCE)));
        return list;
    }

    private static void w(SimulationParams p, double c, double a, double s) {
        p.setCohesionWeight(c);
        p.setAlignmentWeight(a);
        p.setSeparationWeight(s);
    }

    public List<ExperimentResult> runAll() {
        List<ExperimentResult> results = new ArrayList<>();
        for (Experiment e : experiments()) {
            results.add(run(e));
        }
        return results;
    }

    public ExperimentResult run(Experiment e) {
        SimulationParams base = defaults.copy();
        e.change().accept(base);

        double pol = 0, groups = 0, largest = 0, nn = 0, coll = 0;
        int samples = 0;
        for (long seed : SEEDS) {
            SimulationParams p = base.copy();
            p.setSeed(seed);
            BoidsSimulation sim = new BoidsSimulation(p);
            for (int s = 1; s <= STEPS; s++) {
                sim.step();
                if (s > STEPS - AVG_WINDOW) {
                    FlockMetrics m = FlockMetrics.compute(sim);
                    pol += m.polarization();
                    groups += m.groups();
                    largest += m.largestGroup();
                    nn += m.avgNearestDistance();
                    coll += m.collisions();
                    samples++;
                }
            }
        }
        return new ExperimentResult(e.series(), e.name(), base,
                pol / samples, groups / samples, largest / samples, nn / samples, coll / samples);
    }
}
