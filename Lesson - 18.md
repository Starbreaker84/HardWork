## Интерфейс компактнее реализации?
На данных примерах мы разберем случаи, когда интерфейс не обязан быть компанктнее реализациии лучше решает поставленную задачу проектирования системы.

## Призрачное состояние

### Пример 1

Код крпоративный, защищенный NDA, но суть примера я постараюсь передать.
Достаточно большой методот в котором результат различных вычислений 3(Три, Карл!!!) перезаписывается в одну и туже переменную. Более того, код не подвергался рефакторингу, даже когда из-за этой переменной долго приходилось отлавливать ошибку.

Лечится такое поведение банальным добавлением отдельных финальных переменных в код, каждая из которых хранит своё уникальное значение.

### Пример 2
В системе на базе фреймворка Camunda была зашита логика вызова отдельного метода класса для формирования сообщения для пушинга клиента на основе сочетания разных уникальных полей, при этом конфигурации таких сочетаний зачастую никак между собой не пересекались и одни и те же переменные перезаписывались по несколько раз, пока проходили проверки.

Решением выступил рефакторинг - разделение логики пушинга на отдельные классы, который вызываются фабрикой на основе уникального идентификатора(обячная энамка), который вычисляется в отдельном классе.

Таким образои удалось избежать не только призрочного состояния большого первоначального метода, но и добиться гибкости и расширяемости кода в дальнейшем.


## Пример погрешности/неточности
Данные примеры взяты с проекта знакомого, мы вместе придумали как в рамках одной транзакции можно выполнять серию нескольких операций.

### Пример 1
**Было:**
``` Java
public interface DepartmentService {

    @Nonnull
    List<DepartmentShortResponse> findAll();

    @Nonnull
    DepartmentFullResponse getById(Long id);
}
```

**Стало:**
``` Java
public interface DepartmentService {

    @Nonnull
    List<DepartmentShortResponse> findAll();

    @Nonnull
    DepartmentFullResponse getById(List<Long> id);
```

### Пример 2
**Было:**
``` Java
public interface PersonService {

    @Nonnull
    List<PersonShortResponse> findAll();

    @Nonnull
    PersonFullResponse getById(Long id);
}
```

**Стало:**
``` Java
public interface PersonService {

    @Nonnull
    List<PersonShortResponse> findAll();

    @Nonnull
    PersonFullResponse getById(List<Long> id);
}
```

### Пример 3
**Было:**
``` Java
public interface ApplicationService {

    @Nonnull
    List<PersonShortResponse> findAll();

    @Nonnull
    ApplicationResponse getById(String id);
}
```

**Стало:**
``` Java
public interface ApplicationService {

    @Nonnull
    List<PersonShortResponse> findAll();

    @Nonnull
    ApplicationResponse getById(List<String> id);
}
```
Казалось бы, небольшое расширение возможности на практике при адекватном обращении и соответствующей настройке БД дало очень хорошую прибавку производительности. Плюс добавили кэш прослойку на редко обновляющиеся данные и приложение "ожило"!

## Интерфейс не проще реализации

Не так давно консультировал одного значкомого, который трудится джуниором на небольшом проекте таксомторной компании. Многие очевидные для меня вещи показались ему неочевидными, но отлично подходят как демонстрация данного пункта.

### Пример 1
**Было:**
``` Java
import java.util.Optional;

public class CarDeliveryRepository extends CrudRepository {

    Optional find(Long id);

    void create(CarDeliveryCreateDto dto);

    void update(Long id, CarDeliveryUpdateDto dto);

    void delete(Long id);
}
```
**Стало:**
``` Java
import java.util.Optional;

public class CarDeliveryRepository extends CrudRepository {

    Optional<CarDeliveryDto> find(Long id);

    CarDeliveryDto create(CarDeliveryCreateDto dto);

    CarDeliveryDto update(Long id, CarDeliveryUpdateDto dto);

    void delete(Long id);
}
```
### Пример 2
**Было:**
``` Java
import java.util.Optional;

public interface VehicleRepository extends CrudRepository {

    Optional find(Long id);

    void create(VehicleCreateDto dto);

    void update(Long id, VehicleUpdateDto dto);

    void delete(Long id);
}
```
**Стало:**
``` Java
import java.util.Optional;

public interface VehicleRepository extends CrudRepository {

    Optional<VehicleDto> find(Long id);

    VehicleDto create(VehicleCreateDto dto);

    VehicleDto update(Long id, VehicleUpdateDto dto);

    void delete(Long id);
}
```
### Пример 3
**Было:**
``` Java
import java.util.Optional;

public interface ReportRepository extends PagingAndSortingRepository {

    Optional<ReportDto> find(Long id);

    ReportDto create(Long id, Period period);

    void delete(Long id);
}
```
**Стало:**
``` Java
import java.util.Optional;

public interface ReportRepository extends PagingAndSortingRepository {

    Optional find(Long id);

    void create(Long id, Period period);

    void delete(Long id);
}
```

Тут мы явно расширили спецификацию с помощью дополнительного определения ответа запросов. А это, в свою очередь позволит более корректно интерпритировать результаты таких запросов и налету обрабатывать нестандартные ситуации.

## Вывод:
Необходимо больше внимания уделять проектированию интерфейса под конретную задачу, но не забывая при этом об основном предназначении такой архитетуры - гибкость и масштабируемость. Внимание к детялям поможет защитить код от неадекватного поведения в дальнейшем.
