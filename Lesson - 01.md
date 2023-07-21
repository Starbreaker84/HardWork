## Снижаем цикломатическую сложность

1. Изучите материал из СильныхИдей "Снижение цикломатической сложности (ЦС) кода как метод проектирования".

2. Выберите какую-нибудь свою или чужую функцию/метод (не менее 30 инструкций) и понизьте её ЦС в два раза.

3. В решении приведите исходный и результирующий версии кода, исходную и конечную ЦС и приёмы, которые вы использовали (например, избавление от else, полиморфизм, избавление от null, "табличная" логика...) -- не менее двух разных приёмов на каждую функцию.

4. Повторите пункты 2 и 3 ещё два раза с другим кодом.


## Примеры применения техники.

### 1. Метод вычисления цены:
``` Java
// Function to calculate the total price of items in the shopping cart
    public static double calculateTotalPrice(ArrayList<String> cartItems, int loyaltyLevel) {
        double totalPrice = 0.0;

        // Check if cartItems is null or empty
        if (cartItems == null || cartItems.isEmpty()) {
            System.out.println("Your cart is empty.");
            return totalPrice;
        }

        // Calculate the total price based on the items in the cart
        for (String item : cartItems) {
            // Check if the item exists in the itemPrices map
            if (itemPrices.containsKey(item)) {
                double itemPrice = itemPrices.get(item);
                totalPrice += itemPrice;
            } else {
                System.out.println("Item not found: " + item);
            }
        }

        // Apply loyalty discounts based on the user's loyalty level
        switch (loyaltyLevel) {
            case 1:
                totalPrice *= 0.9; // 10% discount for loyalty level 1
                break;
            case 2:
                totalPrice *= 0.85; // 15% discount for loyalty level 2
                break;
            case 3:
                totalPrice *= 0.8; // 20% discount for loyalty level 3
                break;
            default:
                // No discount for other loyalty levels
                break;
        }

        return totalPrice;
    }
```

Рефакторинг:

``` Java
    private static final Map<Integer, Discount> discount = Map.of(
            1, new DiscountLevel1(),
            2, new DiscountLevel2(),
            3, new DiscountLevel3()
    );

    // Function to calculate the total price of items in the shopping cart
    public static double calculateTotalPrice(ArrayList<String> cartItems, int loyaltyLevel) {
        double totalPrice = 0.0;

        // Check if cartItems is null or empty
        if (isNotValidCart(cartItems)) {
            System.out.println("Your cart is empty.");
            return totalPrice;
        }

        // Calculate the total price based on the items in the cart
        for (String item : cartItems) {
            // Check if the item exists in the itemPrices map
            totalPrice += Optional.ofNullable(itemPrices.get(item)).orElse(0.0);
        }

        // Apply loyalty discounts based on the user's loyalty level     
        totalPrice = Optional.ofNullable(discount.get(loyaltyLevel)).orElse((total) -> total).getDiscount(totalPrice);;

        return totalPrice;
    }

    private static boolean isNotValidCart(ArrayList<String> cartItems) {
        return cartItems == null || cartItems.isEmpty();
    }

    interface Discount {
        Double getDiscount(Double total);
    }

    private static class DiscountLevel1 implements Discount {
        @Override
        public Double getDiscount(Double total) {
            return total * 0.9;
        }
    }

    private static class DiscountLevel2 implements Discount {
        @Override
        public Double getDiscount(Double total) {
            return total * 0.85;
        }
    }

    private static class DiscountLevel3 implements Discount {
        @Override
        public Double getDiscount(Double total) {
            return total * 0.8;
        }
    }
```

Здесь применено несколько приёмов:
1.1. Вынесение двойного условия в отдельный метод в блоке Check if cartItems is null or empty.
1.2. Использование Optoinal в блоке Calculate the total price based on the items in the cart.
1.3. Использоание "полиморфизма" в блоке Apply loyalty discounts based on the user's loyalty level. Данная техника уже давно пришла мне в голову, и отлично подходит для замены switch  конструкций. При этом, если по уму вынести мапу в отдельный класс, её и модифицировать будет гораздо проще.

Итог: снизили цикломатическую сложность с 7 до 1.





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
Здесь целесообразно применить Null Object Pattern, что позволяет сократить цикломатическую сложность с 6 до 0, что и было сделано.


