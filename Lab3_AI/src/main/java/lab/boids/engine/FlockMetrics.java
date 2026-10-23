package lab.boids.engine;

import lab.boids.model.Boid;
import lab.boids.model.SimulationParams;
import lab.boids.model.Vector2D;

import java.util.List;

/**
 * Числовые характеристики стаи.
 *
 * @param polarization       упорядоченность: длина среднего ЕДИНИЧНОГО вектора скорости.
 *                           1 — все летят в одну сторону, ~0 — кто куда.
 * @param groups             число групп (птицы ближе groupDistance, связанные цепочкой)
 * @param largestGroup       размер самой большой группы
 * @param avgNearestDistance среднее расстояние до ближайшего соседа (плотность стаи)
 * @param collisions         число пар птиц ближе collisionDistance
 * @param groupIds           номер группы для каждой птицы (для раскраски на экране)
 */
public record FlockMetrics(
        double polarization,
        int groups,
        int largestGroup,
        double avgNearestDistance,
        int collisions,
        int[] groupIds) {

    public static FlockMetrics compute(BoidsSimulation sim) {
        List<Boid> boids = sim.getBoids();
        SimulationParams p = sim.getParams();
        int n = boids.size();

        // Поляризация
        Vector2D dirSum = Vector2D.ZERO;
        for (Boid b : boids) {
            dirSum = dirSum.add(b.getVelocity().withLength(1));
        }
        double polarization = n == 0 ? 0 : dirSum.length() / n;

        // Попарные расстояния: ближайший сосед, столкновения, объединение в группы
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;
        double[] nearest = new double[n];
        java.util.Arrays.fill(nearest, Double.MAX_VALUE);
        int collisions = 0;

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double d = sim.offset(boids.get(i).getPosition(), boids.get(j).getPosition()).length();
                nearest[i] = Math.min(nearest[i], d);
                nearest[j] = Math.min(nearest[j], d);
                if (d < p.getCollisionDistance()) collisions++;
                if (d < p.getGroupDistance()) union(parent, i, j);
            }
        }

        double nnSum = 0;
        for (double d : nearest) nnSum += d;
        double avgNearest = n < 2 ? 0 : nnSum / n;

        // Нумеруем группы 0,1,2,... и считаем их размеры
        int[] ids = new int[n];
        int[] rootToId = new int[n];
        java.util.Arrays.fill(rootToId, -1);
        int[] sizes = new int[n];
        int groups = 0;
        for (int i = 0; i < n; i++) {
            int r = find(parent, i);
            if (rootToId[r] == -1) rootToId[r] = groups++;
            ids[i] = rootToId[r];
            sizes[ids[i]]++;
        }
        int largest = 0;
        for (int s : sizes) largest = Math.max(largest, s);

        return new FlockMetrics(polarization, groups, largest, avgNearest, collisions, ids);
    }

    // Система непересекающихся множеств (union-find) для подсчёта групп
    private static int find(int[] parent, int i) {
        while (parent[i] != i) {
            parent[i] = parent[parent[i]];
            i = parent[i];
        }
        return i;
    }

    private static void union(int[] parent, int a, int b) {
        parent[find(parent, a)] = find(parent, b);
    }
}
