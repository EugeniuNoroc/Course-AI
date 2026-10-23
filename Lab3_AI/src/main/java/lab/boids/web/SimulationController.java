package lab.boids.web;

import lab.boids.model.SimulationParams;
import lab.boids.service.ExperimentService;
import lab.boids.service.SimulationService;
import lab.boids.service.SimulationState;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API, через которое браузер управляет симуляцией.
 *
 *   GET  /api/state          — текущее состояние
 *   POST /api/step?count=N   — сделать N шагов и вернуть состояние
 *   POST /api/reset          — перезапуск с параметрами из тела запроса (JSON)
 *   POST /api/params         — поменять веса/радиусы без перезапуска
 *   GET  /api/defaults       — параметры по умолчанию из application.yml
 *   POST /api/experiments    — прогнать все эксперименты (по 500 шагов) и вернуть таблицу
 */
@RestController
@RequestMapping("/api")
public class SimulationController {

    private final SimulationService simulation;
    private final ExperimentService experiments;

    public SimulationController(SimulationService simulation, ExperimentService experiments) {
        this.simulation = simulation;
        this.experiments = experiments;
    }

    @GetMapping("/state")
    public SimulationState state() {
        return simulation.state();
    }

    @PostMapping("/step")
    public SimulationState step(@RequestParam(defaultValue = "1") int count) {
        return simulation.step(Math.max(1, Math.min(count, 50)));
    }

    @PostMapping("/reset")
    public SimulationState reset(@RequestBody(required = false) SimulationParams params) {
        return simulation.reset(params);
    }

    @PostMapping("/params")
    public SimulationState params(@RequestBody SimulationParams params) {
        return simulation.updateParams(params);
    }

    @GetMapping("/defaults")
    public SimulationParams defaults() {
        return simulation.getDefaults();
    }

    @PostMapping("/experiments")
    public List<ExperimentService.ExperimentResult> runExperiments() {
        return experiments.runAll();
    }
}
