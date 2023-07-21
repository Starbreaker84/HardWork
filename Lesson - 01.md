## Снижаем цикломатическую сложность

1. Изучите материал из СильныхИдей "Снижение цикломатической сложности (ЦС) кода как метод проектирования".

2. Выберите какую-нибудь свою или чужую функцию/метод (не менее 30 инструкций) и понизьте её ЦС в два раза.

3. В решении приведите исходный и результирующий версии кода, исходную и конечную ЦС и приёмы, которые вы использовали (например, избавление от else, полиморфизм, избавление от null, "табличная" логика...) -- не менее двух разных приёмов на каждую функцию.

4. Повторите пункты 2 и 3 ещё два раза с другим кодом.


## Примеры применения техники.

### 1. Метод вычисления цены.

**Исходник:**
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

**Рефакторинг:**

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
    totalPrice = cartItems.stream()
            .map(itemPrices::get)
            .filter(Objects::nonNull)
            .reduce(Double::sum)
            .orElse(0.0);

    // Apply loyalty discounts based on the user's loyalty level     
    totalPrice = Optional.ofNullable(discount.get(loyaltyLevel))
            .orElse((total) -> total)
            .getDiscount(totalPrice);

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
1. Вынесение двойного условия в отдельный метод в блоке Check if cartItems is null or empty.
2. В блоке Calculate the total price based on the items in the cart избавляемся от цикла с условием с помощью стрима и Optional.
3. Использоание "полиморфизма" с помощью интерфейса и имплементирующих классов в блоке Apply loyalty discounts based on the user's loyalty level. Данная техника уже давно пришла мне в голову, и отлично подходит для замены switch конструкций. При этом, если по уму вынести мапу в отдельный класс, её и модифицировать будет гораздо проще.

**Итог**: снизили цикломатическую сложность с 7 до 1.

### 2. Какой-то реальный корпоративный банковский метод, хот-фикс от тимлида, задача стояла "как-то посимпатичнее сделать".

**Исходник:**
``` Java
private Specification<FxDealAutoQuotingDoc> getBranchBankSpec(UUID userId) {
    BankUserDto user = bankUaaService.findById(userId).orElse(null);
    if (user != null) {
      UaaUserRoleDto role = user.getUaaUser().getUaaRoles().stream()
          .filter(r -> UaaRoleName.BANK_MANAGER_SSP.name().equals(r.getRole().getCode()) &&
              UaaSegmentName.BANK_TREASURY.name().equals(r.getRole().getSegment().getCode()))
          .findFirst()
          .orElse(null);
      if (role != null) {
        List<UUID> branchIds = role.getBranches().stream()
            .map(UaaUserRoleBranchDto::getParentBranchId)
            .distinct().collect(Collectors.toList());
        if (!branchIds.isEmpty()) {
          return fieldIn(BRANCH_ID, branchIds);
        } else {
          return (r, cq, cb) -> null;
        }
       } else {
        return (r, cq, cb) -> null;
      }
    }
    return BaseFilterSpecification.falseSpecification();
  }
```

**Рефакторинг:**
``` Java
private Specification<FxDealAutoQuotingDoc> getBranchBankSpec(UUID userId) {
	BankUserDto user = bankUaaService.findById(userId).orElse(null);
	if (user == null) 
		return BaseFilterSpecification.falseSpecification();
	
	UaaUserRoleDto role = getRole(user);
	
	if (role == null) 
		return (r, cq, cb) -> null;
	
	List<UUID> branchIds = getBranchIds(role);
	
	return branchIds.isEmpty() ? (r, cq, cb) -> null : fieldIn(BRANCH_ID, branchIds);
	
}

private UaaUserRoleDto getRole(BankUserDto user) {
	return user.getUaaUser().getUaaRoles().stream()
          .filter(r -> UaaRoleName.BANK_MANAGER_SSP.name().equals(r.getRole().getCode()) &&
              UaaSegmentName.BANK_TREASURY.name().equals(r.getRole().getSegment().getCode()))
          .findFirst()
          .orElse(null);
}

private List<UUID> getBranchIds (UaaUserRoleDto role) {
	return role.getBranches().stream()
            .map(UaaUserRoleBranchDto::getParentBranchId)
            .distinct().collect(Collectors.toList());
}
```

Тут конечно вложенность if-else удручающая. Применил технику "от обратного" - выходим из метода по мере поступления неготивных сценариев, избавляемся от else. Плюс применяем технику вынесения действий в отдельные методы лдя читабельности. Также применили технику тернарного оператора для окончательного возврата значения.

**Итог**: снизили цикломатическую сложность с 5 до 2.


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

**Рефакторинг:**
``` Java
public Map<Character, Long> merge(Map<Character, Long> first, Map<Character, Long> second) {
    Optional<Map<Character, Long>> maybeFirst = Optional.of(first);
    Optional<Map<Character, Long>> maybeSecond = Optional.of(second);
    Map<Character, Long> merged = new HashMap<>(maybeFirst.orElse(Collections.emptyMap()));
    maybeSecond.orElse(Collections.emptyMap()).forEach((key, value) -> merged.merge(key, value, Long::sum));
    return merged;
}
```
Здесь целесообразно применить Null Object Pattern.

**Итог**: снизили цикломатическую сложность с 6 до 0.

## Итоговые выводы:
В целом, техники позволяют сделать код чище как и в плане читаемости для программиста, так и в архитектурном плане. Безусловно в Java не хватает более развитой системы типов на подобие Ada, тогда можно было бы применять ad-hoc полиморфизм на полную катушку, а так приходится немного усложнять общую конструкцию. Кстати, среди своего кода примеры было найти достаточно сложно. Техники просты и лаконичны и при осознанной практике быстро входят в привычку. Перестать пользоваться else не только эстетически полезно, но и заставляет более конструктивно подумать над логикой программы. От циклов удобно уходить с помощью фукнциональных интерфейсов Java, достаточно богатых технически. Optional отлично подходит для работы с потенциальными null объектами.
