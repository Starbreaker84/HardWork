## Правила простого проектного дизайна

### 1. Запрет ошибочного поведения на уровне интерфейса

**Пример 1.1**
``` Java
public class MessageSender {
    ...
    public void sendMessage(String route) {
        ...
    }
}
```

Достаточно часто встречаю подобный код. Явно видно что можно легко споткнуться если передать неверное значение. Лечится достаточно просто, как правило достаточно хорошо продуманного перечисления - и метод отработает как надо и разработчик не сможет допустить ошибку.
``` Java
public class MessageSender {
    ...
    public void sendMessage(Device device) {
        ...
    }
}

enum Device {
    PHONE, LAPTOP
}
```

**Пример 1.2**
``` Java
public class CreditLoan {
    ...
    public void addCreditLoanFine (CreditLoanFine creditLoanFine) {
        ...
    }
}

public class CreditLoanFine {
    ...
}
```

Тут мы имеем два независимых класса, но при этом один является внутренней сущностью другого. В таком виде можно совершить несколько разных ошибок, начинаю от банального забытия добавить уже созданный объект, до попытки добавить null.
Как вариант решения - запрет создания штрафа вне заёма с корректировкой интерфейса основного класса.
``` Java
public class CreditLoan {
    ...
    public void addCreditLoanFine (// Аргументы, необходимые для создания объекта штрафа) {
        // Конструктор класса и логика добавления в список
    }
}

public class CreditLoanFine {
    ...
}
```
### 2. Отказ от дефолтных конструкторов без параметров

**Пример 2.1**
``` Java
public class Engeneer {
    ...
    public void setGroup (Group group) {
        ...
    }

    public void setPartner (Emloyee emloyee) {
        ...
    }
}
```

На удивление, такой код легко можно встретить в продакшене, с мотивировкой - ну если расширить там придется или еще что. А то что можно посваливаться с NPE людей не особо заботит.
``` Java
public class Engeneer {
    ...
    public Engeneer (Group group, Emloyee emloyee) {
        ...
    }
}
```

**Пример 2.2**
Хорошим примером такого подхода можно назвать DI в современных фреймворках. Там реализация этого паттерна уже сама подталкивает к конструкторам с обязательными параметрами, иначе объект просто не создастся.

``` Java
@Service
public class DepartmentServiceImpl
        implements DepartmentService {

    private final DepartmentDao departmentDao;

    private final PersonDao personDao;

    public DepartmentServiceImpl(DepartmentDao departmentDao, PersonDao personDao) {
        this.departmentDao = departmentDao;
        this.personDao = personDao;
    }
    ...
```

Как только поля объявляются final, IDE как правило, сама формирует конструктор с необходимыми параметрами, тем самым исключая косяков вроде "забыл засетить репохиторий" и т.п.

### 3. Избегать увлечения примитивными типами данных

**Пример 3.1**
``` Java
public class EmloyeeCard {
    private String emloyeeFirstName;
    private String emloyeeMiddleName;
    private String emloyeeLastName;

    ...
```

Пример от коллеги. Из-за того, что передавать в методах и конструкторах можно только строку, произошла реальная путаница с позициями middleName и lastName, причем это зависело от разработчика и его вью на то, что и какдолжно передаваться. Я как раз предложил решение с уходом от примитивов.

``` Java
public class EmloyeeCard {
    private FirstName emloyeeFirstName;
    private MiddleName emloyeeMiddleName;
    private LastName emloyeeLastName;

    ...
```

**Пример 3.2**
``` Java
public Car {
    private Long id;

    private String model;

    private String manufacturer;

    private Integer size;

    ...
}
```

Классический пример junior или middle- кода на сущностях (чего притворятся, я и сам много где так писал). Гораздо проще предметной области будут отвечать специально созданные для этого перечисления, как минимум.

``` Java
public Car {
    private Long id;

    private Model model;

    private Manufacturer manufacturer;

    private PassengerSeats passengerSeats;

    ...
}

public enum Model {
    ...
}

public enum Manufacturer {
    ...
}

public enum PassengerSeats {
    ...
}
```
