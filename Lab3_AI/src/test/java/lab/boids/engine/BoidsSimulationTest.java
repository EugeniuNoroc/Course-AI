package lab.boids.engine;

import lab.boids.model.Boid;
import lab.boids.model.BoundaryMode;
import lab.boids.model.SimulationParams;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoidsSimulationTest {

    private static final double EPS = 1e-9;

    @Test
    void runs500StepsWithinLimits() {
        for (BoundaryMode mode : BoundaryMode.values()) {
            SimulationParams p = new SimulationParams();
            p.setBoundaryMode(mode);
            BoidsSimulation sim = new BoidsSimulation(p);
            for (int i = 0; i < 500; i++) {
                sim.step();
                for (Boid b : sim.getBoids()) {
                    double speed = b.getVelocity().length();
                    assertTrue(speed <= p.getMaxSpeed() + EPS, "скорость превышена");
                    assertTrue(speed >= p.getMinSpeed() - EPS, "скорость ниже минимума");
                    assertTrue(b.getPosition().x() >= 0 && b.getPosition().x() <= p.getWorldWidth());
                    assertTrue(b.getPosition().y() >= 0 && b.getPosition().y() <= p.getWorldHeight());
                }
            }
            assertEquals(500, sim.getStep());
        }
    }

    @Test
    void sameSeedGivesSameResult() {
        BoidsSimulation a = new BoidsSimulation(new SimulationParams());
        BoidsSimulation b = new BoidsSimulation(new SimulationParams());
        for (int i = 0; i < 100; i++) { a.step(); b.step(); }
        assertEquals(a.getBoids().get(7).getPosition(), b.getBoids().get(7).getPosition());
    }

    @Test
    void rulesMakeFlockOrdered() {
        SimulationParams noRules = new SimulationParams();
        noRules.setCohesionWeight(0);
        noRules.setAlignmentWeight(0);
        noRules.setSeparationWeight(0);

        double chaos = polarizationAfter(noRules, 500);
        double flock = polarizationAfter(new SimulationParams(), 500);

        assertTrue(chaos < 0.4, "без правил стая должна быть хаотичной: " + chaos);
        assertTrue(flock > 0.8, "с правилами стая должна лететь согласованно: " + flock);
    }

    private static double polarizationAfter(SimulationParams p, int steps) {
        BoidsSimulation sim = new BoidsSimulation(p);
        for (int i = 0; i < steps; i++) sim.step();
        return FlockMetrics.compute(sim).polarization();
    }
}
