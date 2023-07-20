## Снижаем цикломатическую сложность

1. Изучите материал из СильныхИдей "Снижение цикломатической сложности (ЦС) кода как метод проектирования".

2. Выберите какую-нибудь свою или чужую функцию/метод (не менее 30 инструкций) и понизьте её ЦС в два раза.

3. В решении приведите исходный и результирующий версии кода, исходную и конечную ЦС и приёмы, которые вы использовали (например, избавление от else, полиморфизм, избавление от null, "табличная" логика...) -- не менее двух разных приёмов на каждую функцию.

4. Повторите пункты 2 и 3 ещё два раза с другим кодом.


## Решения:

### 3. Небольшой метод склеивания хэш-таблиц из многопоточного приложения.

**Исходник:**
``` Java
public Map<Character, Long> merge(Map<Character, Long> first, Map<Character, Long> second) {
        if (first == null && second == null) {
            return Collections.emptyMap();
        }
        if (first == null || second == null) {
            return new HashMap<>(first == null ? second : first);
        }

        Map<Character, Long> merged = new HashMap<>(first);
        second.forEach((key, value) -> merged.merge(key, value, Long::sum));
        return merged;
}
```

**Решение:**
``` Java
public Map<Character, Long> merge(Map<Character, Long> first, Map<Character, Long> second) {
        Optional<Map<Character, Long>> maybeFirst = Optional.of(first);
        Optional<Map<Character, Long>> maybeSecond = Optional.of(second);
        Map<Character, Long> merged = new HashMap<>(maybeFirst.orElse(Collections.emptyMap()));
        maybeSecond.orElse(Collections.emptyMap()).forEach((key, value) -> merged.merge(key, value, Long::sum));
        return merged;
}
```
Здесь целесообразно применить Null Object Pattern, что позволяет сократить цикломатическую сложность с 10 до 0, что и было сделано.


