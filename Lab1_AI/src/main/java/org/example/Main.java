package org.example;

import java.util.*;

/**
 * ДКА для языка: (abcd)^n (ef)^m,  n >= 1, m >= 1
 *
 * Состояния:
 *   q0 - старт
 *   q1 - прочитан очередной "a" (начало/продолжение блока abcd)
 *   q2 - прочитан "ab"
 *   q3 - прочитан "abc"
 *   q4 - прочитан "abcd" (блок abcd завершён, можно начать ещё один блок abcd,
 *        либо перейти к блокам ef)
 *   q5 - прочитан "e" (начало блока ef)
 *   q6 - прочитан "ef" (блок ef завершён) - ПРИНИМАЮЩЕЕ состояние
 *   TRAP - состояние-ловушка (недопустимый символ / недопустимый порядок)
 */
class DFA {

    private enum State { q0, q1, q2, q3, q4, q5, q6, TRAP }

    private static final State START = State.q0;
    private static final Set<State> ACCEPTING = EnumSet.of(State.q6);

    // Таблица переходов: (состояние, символ) -> состояние
    private static final Map<State, Map<Character, State>> TRANSITIONS = new EnumMap<>(State.class);

    static {
        for (State s : State.values()) {
            TRANSITIONS.put(s, new HashMap<>());
        }
        addTransition(State.q0, 'a', State.q1);
        addTransition(State.q1, 'b', State.q2);
        addTransition(State.q2, 'c', State.q3);
        addTransition(State.q3, 'd', State.q4);
        addTransition(State.q4, 'a', State.q1); // ещё один блок abcd
        addTransition(State.q4, 'e', State.q5); // переход к блокам ef
        addTransition(State.q5, 'f', State.q6);
        addTransition(State.q6, 'e', State.q5); // ещё один блок ef
        // все остальные переходы (не описанные явно) ведут в TRAP
    }

    private static void addTransition(State from, char symbol, State to) {
        TRANSITIONS.get(from).put(symbol, to);
    }

    /**
     * Выполняет последовательный разбор входной строки автоматом.
     * @return true, если строка принадлежит языку (abcd)^n(ef)^m, n>=1, m>=1
     */
    public static boolean accepts(String input) {
        State current = START;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            State next = TRANSITIONS.get(current).get(c);
            current = (next != null) ? next : State.TRAP;
            if (current == State.TRAP) {
                return false; // можно завершить разбор досрочно
            }
        }
        return ACCEPTING.contains(current);
    }

    public static void main(String[] args) {
        // Корректные слова
        List<String> valid = List.of(
                "abcdef",              // n=1, m=1
                "abcdabcdef",          // n=2, m=1
                "abcdefef",            // n=1, m=2
                "abcdabcdabcdefefef"   // n=3, m=3
        );

        // Некорректные слова
        List<String> invalid = List.of(
                "",                    // пустая строка
                "abcd",                // нет блока ef (m=0)
                "ef",                  // нет блока abcd (n=0)
                "abcdeff",             // лишний символ f не по схеме (e f f)
                "abcdefabcd",          // после ef снова abcd - недопустимо
                "abcXdef",             // посторонний символ
                "abcdefe",             // блок ef не завершён (висящий 'e')
                "ABCDEF"               // неверный регистр

        );

        System.out.println("=== Корректные слова (ожидается true) ===");
        for (String s : valid) {
            System.out.printf("%-25s -> %b%n", "\"" + s + "\"", accepts(s));
        }

        System.out.println("\n=== Некорректные слова (ожидается false) ===");
        for (String s : invalid) {
            System.out.printf("%-25s -> %b%n", "\"" + s + "\"", accepts(s));
        }

        // Интерактивная проверка произвольной строки через аргумент командной строки
        if (args.length > 0) {
            String word = args[0];
            System.out.println("\nПроверка слова из аргумента: \"" + word + "\" -> " + accepts(word));
        }
    }
}