import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Лабораторная работа: мини-макс с альфа-бета отсечением.
 * Вариант 4: глубина дерева = 6, ширина (коэффициент ветвления) = 3,
 * в дереве есть отрицательные значения.
 *
 * Что делает программа:
 *  1. Строит полное игровое дерево глубины 6 и ширины 3 (3^6 = 729 листьев, 1093 узла),
 *     значения листьев генерируются случайно.
 *  2. Считает оценку корня обычным мини-максом и мини-максом с альфа-бета отсечением.
 *  3. Сравнивает: число проверенных узлов, число отсечений, время работы.
 *  4. Прогоняет три набора листьев: только положительные, смешанные [-100..100],
 *     только отрицательные - чтобы показать, как отрицательные значения влияют на работу.
 *  5. Показывает типичную ошибку: если начать с alpha = 0 (как будто значения
 *     всегда >= 0), то на дереве с отрицательными значениями ответ будет неверным.
 */
public class AlphaBetaVariant4 {

    // ---------------- Параметры варианта ----------------
    static final int DEPTH = 6;            // глубина: число ходов от корня до листа
    static final int WIDTH = 3;            // у каждого внутреннего узла 3 потомка
    static final int RUNS_FOR_TIME = 2000; // сколько раз повторяем поиск для замера времени
    static final int RANDOM_TREES = 200;   // сколько случайных деревьев для усреднения

    // ---------------- Узел дерева ----------------
    static class Node {
        final int level;                 // 0 = корень (MAX), 1 = MIN, 2 = MAX, ...
        int value;                       // у листа - сгенерированное значение
        final List<Node> children = new ArrayList<>();

        Node(int level) { this.level = level; }
        boolean isLeaf() { return children.isEmpty(); }
    }

    // ---------------- Построение дерева ----------------
    /** Рекурсивно строит полное дерево; листьям даёт случайные целые из [min, max]. */
    static Node buildRandomTree(int depth, int width, Random rnd, int min, int max) {
        return build(0, depth, width, rnd, min, max);
    }

    private static Node build(int level, int depth, int width, Random rnd, int min, int max) {
        Node node = new Node(level);
        if (level == depth) {                          // дошли до листа
            node.value = min + rnd.nextInt(max - min + 1);
            return node;
        }
        for (int i = 0; i < width; i++) {
            node.children.add(build(level + 1, depth, width, rnd, min, max));
        }
        return node;
    }

    static int countNodes(Node n) {
        int c = 1;
        for (Node ch : n.children) c += countNodes(ch);
        return c;
    }

    // ---------------- Статистика ----------------
    static class Stats {
        long visited = 0;          // сколько узлов проверено (вызовов функции)
        long leaves = 0;           // сколько листьев оценено
        long cutoffs = 0;          // сколько раз сработало отсечение
        long[] visitedByLevel = new long[DEPTH + 1];
    }

    // ---------------- 1. Обычный мини-макс ----------------
    /**
     * Полный перебор: заходим в КАЖДЫЙ узел дерева.
     * MAX выбирает максимум среди потомков, MIN - минимум.
     */
    static int minimax(Node node, boolean isMax, Stats s) {
        s.visited++;
        s.visitedByLevel[node.level]++;
        if (node.isLeaf()) { s.leaves++; return node.value; }

        int best = isMax ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (Node child : node.children) {
            int v = minimax(child, !isMax, s);
            best = isMax ? Math.max(best, v) : Math.min(best, v);
        }
        return best;
    }

    // ---------------- 2. Мини-макс с альфа-бета отсечением ----------------
    /**
     * alpha - сколько MAX уже гарантировал себе в проверенных ветках.
     * beta  - до скольки MIN может опустить MAX в текущей ветке.
     * Если alpha >= beta, текущая ветка не лучше того, что уже есть:
     * либо MIN сюда не пустит, либо MAX сам сюда не пойдёт.
     * Остальных потомков не проверяем - это и есть отсечение.
     */
    static int alphaBeta(Node node, int alpha, int beta, boolean isMax, Stats s) {
        s.visited++;
        s.visitedByLevel[node.level]++;
        if (node.isLeaf()) { s.leaves++; return node.value; }

        if (isMax) {
            int best = Integer.MIN_VALUE;
            for (Node child : node.children) {
                int v = alphaBeta(child, alpha, beta, false, s);
                best = Math.max(best, v);
                alpha = Math.max(alpha, best);      // MAX улучшает свою гарантию
                if (alpha >= beta) {                // MIN сюда не пустит
                    s.cutoffs++;
                    break;
                }
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            for (Node child : node.children) {
                int v = alphaBeta(child, alpha, beta, true, s);
                best = Math.min(best, v);
                beta = Math.min(beta, best);        // MIN улучшает свою гарантию
                if (alpha >= beta) {                // MAX сюда не пойдёт
                    s.cutoffs++;
                    break;
                }
            }
            return best;
        }
    }

    // ---------------- Замер времени ----------------
    static double avgMicrosMinimax(Node root) {
        long t0 = System.nanoTime();
        for (int i = 0; i < RUNS_FOR_TIME; i++) minimax(root, true, new Stats());
        return (System.nanoTime() - t0) / 1000.0 / RUNS_FOR_TIME;
    }

    static double avgMicrosAlphaBeta(Node root) {
        long t0 = System.nanoTime();
        for (int i = 0; i < RUNS_FOR_TIME; i++)
            alphaBeta(root, Integer.MIN_VALUE, Integer.MAX_VALUE, true, new Stats());
        return (System.nanoTime() - t0) / 1000.0 / RUNS_FOR_TIME;
    }

    // ---------------- Эксперимент на одном наборе значений ----------------
    // !SEED
    static void experiment(String title, int min, int max, long seed) {
        System.out.println("\n=== " + title + "   листья из [" + min + ", " + max + "] ===");

        Node root = buildRandomTree(DEPTH, WIDTH, new Random(), min, max);

        Stats mm = new Stats();
        int vMM = minimax(root, true, mm);

        Stats ab = new Stats();
        int vAB = alphaBeta(root, Integer.MIN_VALUE, Integer.MAX_VALUE, true, ab);

        double tMM = avgMicrosMinimax(root);
        double tAB = avgMicrosAlphaBeta(root);

        System.out.printf("Всего узлов в дереве: %d (листьев: %d)%n",
                countNodes(root), (int) Math.pow(WIDTH, DEPTH));
        System.out.printf("%-22s | %8s | %9s | %7s | %9s | %11s%n",
                "Алгоритм", "Оценка", "Узлов", "Листьев", "Отсечений", "Время, мкс");
        System.out.println("-".repeat(80));
        System.out.printf("%-22s | %8d | %9d | %7d | %9s | %11.2f%n",
                "Мини-макс", vMM, mm.visited, mm.leaves, "-", tMM);
        System.out.printf("%-22s | %8d | %9d | %7d | %9d | %11.2f%n",
                "Альфа-бета", vAB, ab.visited, ab.leaves, ab.cutoffs, tAB);
        System.out.printf("Результаты совпадают: %s. Альфа-бета проверил %.1f%% узлов, "
                        + "сэкономил %d узлов, быстрее в %.2f раза.%n",
                vMM == vAB ? "ДА" : "НЕТ", 100.0 * ab.visited / mm.visited,
                mm.visited - ab.visited, tMM / tAB);

        System.out.println("Посещено узлов по уровням (уровень: мини-макс / альфа-бета):");
        for (int l = 0; l <= DEPTH; l++) {
            System.out.printf("  уровень %d (%s): %4d / %4d%n", l,
                    l % 2 == 0 ? "MAX" : "MIN", mm.visitedByLevel[l], ab.visitedByLevel[l]);
        }
    }

    /** Усреднение по многим случайным деревьям - одно дерево может быть «удачным». */
    static void averageOverTrees(String title, int min, int max) {
        double sumVisited = 0, sumCut = 0;
        int full = 0;
        for (int t = 0; t < RANDOM_TREES; t++) {
            Node root = buildRandomTree(DEPTH, WIDTH, new Random(1000 + t), min, max); // !SEED
            Stats mm = new Stats(), ab = new Stats();
            int a = minimax(root, true, mm);
            int b = alphaBeta(root, Integer.MIN_VALUE, Integer.MAX_VALUE, true, ab);
            if (a != b) throw new IllegalStateException("Ошибка: результаты не совпали!");
            full = (int) mm.visited;
            sumVisited += ab.visited;
            sumCut += ab.cutoffs;
        }
        System.out.printf("%-28s | %9d | %12.1f | %9.1f | %6.1f%%%n", title, full,
                sumVisited / RANDOM_TREES, sumCut / RANDOM_TREES,
                100.0 * sumVisited / RANDOM_TREES / full);
    }

    // ---------------- Главный метод ----------------
    public static void main(String[] args) {
        System.out.println("МИНИ-МАКС С АЛЬФА-БЕТА ОТСЕЧЕНИЕМ. Вариант 4: глубина "
                + DEPTH + ", ширина " + WIDTH);

        // Прогрев JVM: JIT-компилятор должен успеть оптимизировать обе функции,
        // иначе первый замер времени будет нечестным.
        Node warm = buildRandomTree(DEPTH, WIDTH, new Random(7), -100, 100);
        for (int i = 0; i < 5000; i++) {
            minimax(warm, true, new Stats());
            alphaBeta(warm, Integer.MIN_VALUE, Integer.MAX_VALUE, true, new Stats());
        }

        // --- Часть 1. Подробное сравнение на трёх деревьях ---
        // !SEED
        experiment("Только положительные значения", 0, 100, 42);
        experiment("Смешанные значения (есть отрицательные)", -100, 100, 42);
        experiment("Только отрицательные значения", -100, -1, 42);

        // --- Часть 2. Среднее по 200 случайным деревьям ---
        System.out.println("\n=== Среднее по " + RANDOM_TREES + " случайным деревьям ===");
        System.out.printf("%-28s | %9s | %12s | %9s | %7s%n",
                "Набор значений", "Мини-макс", "Альфа-бета", "Отсечений", "Доля");
        System.out.println("-".repeat(80));
        averageOverTrees("[0, 100]", 0, 100);
        averageOverTrees("[-100, 100]", -100, 100);
        averageOverTrees("[-100, -1]", -100, -1);
    }
}
