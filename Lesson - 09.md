## Ясный код-2

**1.1** Данная проблема часто появляется в коде, где по идее должен быть некоторый приватный метод, поддерживающий логику какого-то публичного метода. Для примера возьмём класс менеджера работников в котором помимо основного метода, существует метод проверки полей для его обновления. Метод isValidEmployee имеет модифкатор доступа public только по причине того, что он используется в тестах.

Оригинальный код:
``` Java
class EmployeeManager {
    // Поля и конструктор

    public void update(Employee employee) {
        if (!isValidEmployee(employee))
            throws new InvalidEmplyeeException();
        dao.update(employee);
    }

    public Boolean isValidEmployee(Employee employee) {
        // Описана логика валидации работника
    }
}

class EmployeeManagerTest {
    // Тесты основоного метода

    @Test
    void isValidEmployeeTest() {
        // Логика проверки метода
    }
}
```

Как видно, помимо того, что метод напрашивается быть приватным (ибо в этом виде ни о каком single responsibility и речи быть не может), по хорошему его нужно вообще выделить в отдельный класс и внедрять в класс менеджера работников. Иначе, нам придётся фактически тестировать приватный метод с помощью рефлексии, а это и звучит и выглядит так себе.

Код после рефакторинга:

``` Java
class EmployeeManager {
    // Поля и конструктор

    public void update(Employee employee) {
        if (!employeeValidator.isValid(employee))
            throws new InvalidEmplyeeException();
        dao.update(employee);
    }
}

class EmployeeManagerTest {
    // Тесты единственного метода
}

class EmployeeValidator {
    // Поля и конструктор

    public Boolean isValid(Employee employee) {
        // Логика отработки валидации
    }
}

class EmployeeValidatorTest {
    // Тесты единственного метода
}
```
Таким образом мы избавились и от метода в классе, необходимого только для тестов, и заодно сделали код гибче и расширяемее.


**1.2** Очень часто сталкиваюсь с кодом, где встречаются длинные цепочки вызовов методов, передаваемых в качестве параметров (обчно это сервисные классы). Понять и поддерживать такой код как правило затруднительно.

Оригинальный код:
``` Java
public PersonFullResponse update(int id, @Nonnull PersonRequest request) {
    Person person = personDao.findById(id);
    if (person == null) {
        throw new EntityNotFoundException("Person with id " + id + " not found.");
    }

    return PersonMapper.toFullPerson(personDao.update(PersonMapper.updateFromRequest(request, person)));
}
```

Тут видно как происходит следующая последовательность вызова методов: update -> updateFromRequest -> update -> toFullPerson. Один им решений может быть явный последовательный вызов методов для форимровании соответствующих объектов.

Код после рефакторинга:
``` Java
public PersonFullResponse update(int id, @Nonnull PersonRequest request) {
    Person person = personDao.findById(id);
    if (person == null) {
        throw new EntityNotFoundException("Person with id " + id + " not found.");
    }
    Person personForUpdate = PersonMapper.updateFromRequest(request, person);
    Person personAfterUpdate = personDao.update(personForUpdate);
    FullPerson updatedPerson = PersonMapper.toFullPerson(personAfterUpdate);

    return updatedPerson;
}
```
Теперь уже каждый шаг преобразования отражен отдельно и легко прослеживается. Также данный приём делает код более читаемым и легче поддерживаемым.

**1.3** Также достаточно часто встречаемый в легаси коде паттерн, когда в качестве аргументов передаётся 3 и более параметра, относящихся к одной, максимум двум сущностям. Как вариант решения проблемы - передача самой сущности, а метод внутри уже сам разберётся что ему оттуда понадобится или нет (что, опять таки, сделает код более удобным для модификации).

Оригинальный код:
``` Java
public Integer processEmployee(int id, String, name, Factory factory) {
    // Логика бработки всех трёх аргументов и вычиследния искомого значения
}
```

На самом деле нам достаточно передвать одyу единственую сущность, как показано далее.

Код после рефакторинга:
``` Java
public Integer processEmployee(Employee employee) {
    // Логика бработки любых необходимых параметров, даже если нужны сторонние сущности, сязанные посредством Hibernate с изначальной сущностью
}
```

**1.4** Данный антипаттерн часто встречается у новичков, незнакомых с паттернами GOF и не программируюущих на уровне интерфейсов.

Приведём пример отправки уведомления пользоватею в зависиомсти от его регестрационных данных.

Оригинальный код:
``` Java
class NotificationService {
    // Поле дл пользователя и соответствующий конструктор

    public void notification(User user) {
        if (User.device() instance of Email.class)
            emailNotification(user);
        else if (User.device() instance of Phone.class)
            phoneNotification(user);
        else
            telegramNotification(user);
    }

    private emailNotification(User user) {
        // Лоика отправки уведомления по email
    }

    private phoneNotification(User user) {
        // Лоика отправки уведомления по телефону
    }

    private telegramNotification(User user) {
        // Лоика отправки уведомления через мессенджер
    }
}
```
Здесь следует вынести логику увдомления в реализации интерфейса уведомлений. Девайс будет реализовывать данный интерфейс, и уже сервис уведомлений всего лишь будет вызывать данную логику.

Код после рефакторинга:
``` Java
interface Notification {
    void notificate();
}

class Email implements Notification {
    void notificate() {
        // Реализация логики уведомления
    }
}

class Phone implements Notification {
    void notificate() {
        // Реализация логики уведомления
    }
}

class Telegram implements Notification {
    void notificate() {
        // Реализация логики уведомления
    }
}

class NotificationService {

    public void notification(User user) {
        user.device.notificate();
    }
}
```

Теперь нам больше не нжно думать каким образом мы должны уведомлять каждого пользователя.

**1.5** Встречается часто в древнем легаси, особенно написанным тогдашними новичками. Часто выражается в отдаче всей сущности в ответ сервера без проработки действительно необходимых данных.

Оригинальный код:
``` Java
public List<Person> findAll() {
        return personDao.findAll();
    }
```

Проблема данного кода может быть в том, что класс Person может быть достаточно объёмным - раз, и список тоже может оказаться внушительных размеров - два. А в запросе нужны только определённые данные по каждому экземпляру.

Код после рефакторинга:
``` Java
public List<PersonShortResponse> findAll() {
        return personDao.findAll()
                .stream()
                .map(PersonMapper::toShortPerson)
                .collect(Collectors.toList());
    }
```

Теперь уже мы отдаём ровно то, что от нас требуется, фильтруя данные с помощью класса `PersonShortResponse`. Таким образом мы ещё и память сэконоимм, и канал передачи данных разгрузим.

## Итого:
Недостатки в том, как пишут код, влекут за собой неудобства. Это приводит к тому, что программистам сложнее разбираться в коде, что, в свою очередь, увеличивает время, потраченное на разработку. А увеличение времени разработки, как мы понимаем, приводит к потере денег для компании. Поэтому необходимо наиболее тщательно и дотошно относится к таким вещам как ясность и чистота кодовой базы, в идеале не допуская появления технического долга и тем самым позволяя бизнесу более эффективно расходовать средства.
