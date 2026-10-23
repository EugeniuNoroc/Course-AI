package lab.boids.engine;

import lab.boids.model.Boid;
import lab.boids.model.BoundaryMode;
import lab.boids.model.SimulationParams;
import lab.boids.model.Vector2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Ядро лабораторной: алгоритм Boids Крейга Рейнольдса.
 * Класс не зависит от Spring — его можно запускать из тестов и экспериментов напрямую.
 *
 * Один шаг симуляции:
 *   1) для КАЖДОЙ птицы считаем ускорение по трём правилам (по "старым" позициям всех птиц);
 *   2) только потом применяем ускорения ко всем птицам сразу.
 * Так порядок обхода птиц не влияет на результат (синхронное обновление).
 */
public class BoidsSimulation {

    private SimulationParams params;
    private final List<Boid> boids = new ArrayList<>();
    private final Random random;
    private long step = 0;

    public BoidsSimulation(SimulationParams params) {
        this.params = params.copy();
        this.random = new Random(params.getSeed());
        createBoids();
    }

    /** Случайные начальные позиции по всей плоскости и случайные направления полёта. */
    private void createBoids() {
        for (int i = 0; i < params.getBoidCount(); i++) {
            Vector2D pos = new Vector2D(
                    random.nextDouble() * params.getWorldWidth(),
                    random.nextDouble() * params.getWorldHeight());
            double angle = random.nextDouble() * 2 * Math.PI;
            double speed = params.getMinSpeed()
                    + random.nextDouble() * (params.getMaxSpeed() - params.getMinSpeed());
            Vector2D vel = new Vector2D(Math.cos(angle), Math.sin(angle)).mul(speed);
            boids.add(new Boid(i, pos, vel));
        }
    }

    // ------------------------------------------------------------------ шаг

    public void step() {
        // 1. Считаем ускорения для всех птиц
        Vector2D[] acc = new Vector2D[boids.size()];
        for (int i = 0; i < boids.size(); i++) {
            acc[i] = computeAcceleration(boids.get(i));
        }
        // 2. Применяем их
        for (int i = 0; i < boids.size(); i++) {
            applyAcceleration(boids.get(i), acc[i]);
        }
        step++;
    }

    /**
     * Три правила Boids и их взвешенная сумма:
     *   a = wC * cohesion + wA * alignment + wS * separation
     */
    Vector2D computeAcceleration(Boid self) {
        Vector2D offsetSum = Vector2D.ZERO;   // сумма векторов "от меня к соседу"
        Vector2D velocitySum = Vector2D.ZERO; // сумма скоростей соседей
        Vector2D separation = Vector2D.ZERO;  // отталкивание от слишком близких
        int neighbours = 0;

        for (Boid other : boids) {
            if (other == self) continue;
            Vector2D toOther = offset(self.getPosition(), other.getPosition());
            double dist = toOther.length();

            // Сосед = птица внутри радиуса взаимодействия
            if (dist < params.getPerceptionRadius()) {
                neighbours++;
                offsetSum = offsetSum.add(toOther);
                velocitySum = velocitySum.add(other.getVelocity());
            }
            // Разделение: чем ближе сосед, тем сильнее отталкивание (∝ 1/d)
            if (dist < params.getSeparationRadius() && dist > 1e-9) {
                separation = separation.sub(toOther.div(dist * dist));
            }
        }

        if (neighbours == 0) {
            return Vector2D.ZERO; // соседей нет — летим прямо
        }

        // Центрирование: вектор от птицы к центру масс соседей
        Vector2D cohesion = offsetSum.div(neighbours);
        // Выравнивание: разница между средней скоростью соседей и своей скоростью
        Vector2D alignment = velocitySum.div(neighbours).sub(self.getVelocity());

        Vector2D acceleration = cohesion.mul(params.getCohesionWeight())
                .add(alignment.mul(params.getAlignmentWeight()))
                .add(separation.mul(params.getSeparationWeight()));

        // Ограничение максимального ускорения (птица не может развернуться мгновенно)
        return acceleration.limit(params.getMaxAcceleration());
    }

    /** Обновление скорости и позиции + обработка границ. */
    private void applyAcceleration(Boid b, Vector2D acc) {
        Vector2D v = b.getVelocity().add(acc);

        // Ограничение скорости сверху и снизу
        v = v.limit(params.getMaxSpeed());
        if (v.length() < params.getMinSpeed()) {
            v = v.length() == 0
                    ? new Vector2D(params.getMinSpeed(), 0)
                    : v.withLength(params.getMinSpeed());
        }

        Vector2D p = b.getPosition().add(v);
        double w = params.getWorldWidth();
        double h = params.getWorldHeight();

        if (params.getBoundaryMode() == BoundaryMode.WRAP) {
            p = new Vector2D(wrap(p.x(), w), wrap(p.y(), h));
        } else { // BOUNCE
            double x = p.x(), y = p.y(), vx = v.x(), vy = v.y();
            if (x < 0) { x = -x; vx = -vx; }
            if (x > w) { x = 2 * w - x; vx = -vx; }
            if (y < 0) { y = -y; vy = -vy; }
            if (y > h) { y = 2 * h - y; vy = -vy; }
            p = new Vector2D(x, y);
            v = new Vector2D(vx, vy);
        }

        b.setVelocity(v);
        b.setPosition(p);
    }

    // ------------------------------------------------------------------ геометрия

    /**
     * Вектор от точки a к точке b.
     * В режиме WRAP мир замкнут, поэтому берём кратчайший путь "через край":
     * птица у правой стенки видит соседа у левой как близкого.
     */
    public Vector2D offset(Vector2D a, Vector2D b) {
        double dx = b.x() - a.x();
        double dy = b.y() - a.y();
        if (params.getBoundaryMode() == BoundaryMode.WRAP) {
            double w = params.getWorldWidth(), h = params.getWorldHeight();
            if (dx > w / 2) dx -= w;
            if (dx < -w / 2) dx += w;
            if (dy > h / 2) dy -= h;
            if (dy < -h / 2) dy += h;
        }
        return new Vector2D(dx, dy);
    }

    private static double wrap(double v, double size) {
        v = v % size;
        return v < 0 ? v + size : v;
    }

    // ------------------------------------------------------------------ доступ

    public List<Boid> getBoids() { return Collections.unmodifiableList(boids); }
    public long getStep() { return step; }
    public SimulationParams getParams() { return params; }

    /**
     * Поменять веса/радиусы "на лету", не пересоздавая птиц.
     * Количество птиц, seed и размер мира применяются только при перезапуске.
     */
    public void updateLiveParams(SimulationParams p) {
        SimulationParams n = p.copy();
        n.setBoidCount(params.getBoidCount());
        n.setSeed(params.getSeed());
        n.setWorldWidth(params.getWorldWidth());
        n.setWorldHeight(params.getWorldHeight());
        this.params = n;
    }
}
