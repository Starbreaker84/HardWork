## Повышаем полиморфность кода

На самом деле эта идея напрашивается сама собой, в особенности, когда прио-ходится много работать с кодом, который реализует много бизнес-логики.

Действительно, полезно всегда подняться на один уровень абстракции над своим кодом и полумать, а как можно улучшить полиморфность, чтобы не плодить по сути один и тот же код.

Можно привести несколько примеров из практики:

### Пример 1

### Было:
``` Java
public Optional<Employee> findEmloyeeById(EmployeeId emloyeeId) {
    ...
}
```

### Стало:
``` Java
public Optional<T> findById(UUID id) {
    ...
}
```

Как видим, метод сервиса стал полиморфным, и теперь один и тот же сервис, с его полиморфными методами можно применять по всему ландшафту подходящего кода. Также нужно отметить, что по проекту везде принят стандарт для id использовать тип UUID.

### Пример 2

### Было:
``` Java
public Optional<Transporter> mutation(UUID transporterId, String label, Factory owner, Long length, TransporterType type ) {
    ...
}
```

### Стало:
``` Java
public Optional<T> findById(UUID id, Details<T> details) {
    ...
}
```

Было много методов по изменению сущностей, каждый со своей сигнатурой но примерно одной и тойже реализацией. Теперь метод стал одним и полиморфным благодаря соответствующему рефакторингу.

### Пример 3

### Было:
``` Java
public Optional<ConverterMessage> parseMessageFromConvrter(String message) {
    ...
}
```

### Стало:
``` Java
public Optional<T> parseMessage(String message, Parser<T> parser) {
    ...
}
```

Парсим сообщения из стриминговой платформы. Много сервисов, много парсеров, много кода. Рефакторинг однозначно определили реализацию метода и сделал его полиморфным.

### Пример 4

### Было:
``` Java
List<OfficeEmloyee> emloyees;
```

### Стало:
``` Java
EmloyeeList<T> emloyees;

+ ограниченный набор методов:
hire(UUID id)
fire(UUID id)
move(UUID id, Mover<T> mover)
```

Список для кадровой программы создавался под каждый отдел свой и имел кучу лишних методов, которые иногда приводили к ошибках в бизнес логике. После рефакторинга список стал универсальным для всей кадровой службы и имел конкретную реализацию.

### Пример 5

### Было:
``` Java
public EmloyeeDTO createEmloyee(Employee employee) {
    ...
}
```

### Стало:
``` Java
public Optional<T> create(Entity<T> entity) {
    ...
}
```

Полиморфный метод из примера может заменить добрую часть типичных "типовых" методов любого среднего бизнес приложения.

## Выводы:
Мне этот подход открылся интуитивно еще до рассмотрения этого материала всерьез (да, материалы из сильных идей мне открывают свою суть как правило на 3 или 4 раз к обращению к ним). Он чем то схож с типовыми методами репозиториев, которые дотаточно универсальны, чтобы быть библиотекой внутри фреймворка. Подход действительно сильно сокращает и упрощает модификацию кода. На самом деле, приведенные примеры не на 100% полиморфны, можно пойти ещё дальше в полиморфности, но, как говорится. я пока ещё не созрел. Но есть одно но, порогг входждения. Часто, попытка его внедрить находит ожесточенное сопротивление даже у опытных разработчиков. Я же где возможно всегда стараюсь его применить.