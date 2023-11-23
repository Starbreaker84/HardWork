## Неочевидные проектные ошибки - 1

### Пример 1

#### Было:
``` Java
public class OpportunityDTO {
    ...
    private String referenceType;
    ...
}
```
Класс включал в себя поле типа документа основания представляющег особой строку, при этом в коде постоянно были проверки на соответсвие значение поля одному из 3 дейстивельных. Решение было предложено достаточно элегантное - создать перечисление с возможными значениями и передавать в поле только такой тип значений. Тем самым мы избавились от ненужных проверок на валидность и оперируем только действитльными значениями.

#### Стало:
``` Java
enum ReferenceType {
    DOC, LETTER, BANK
}

public class OpportunityDTO {
    ...
    private ReferenceType referenceType;
    ...
}
```

### Пример 2

#### Было:
``` Java
public class Person {
    private String id;

    private String name;

    private String label;

    Person();

    //геттеры и сеттеры
```

Очевидно "дырявое" решение (реальный код с внутреннего проекта небольшого банка), из-за этого в коде огромное число проверок на валидность полей (проверки на то, что там есть хоть какие-то значения). Предложенным решением было сделать свойства объекта неизменяемыми и запретить возможность обойти инициализацию свойств с помощью конструктора с обязательными параметрами.

#### Стало:
``` Java
public class Person {
    private final String id;

    private final String name;

    private finalString label;

    private Person();

    Person(String id, String name, String label){
        //инициализация свойств
    }

    //геттеры
}
```

### Пример 3

#### Было:
``` Java
@Service
@RequiredArgsConstructor
public class PersonServiceImpl
        implements PersonService {

    private final PersonDao personDao;
    private final DepartmentDao departmentDao;

    @Override
    @Transactional(readOnly = true)
    public List<PersonShortResponse> findAll() {
        //логика с проверкой что вернулось не null значение
    }

    @Override
    @Transactional(readOnly = true)
    public PersonFullResponse getById(int id) {
        //логика с проверкой что вернулось не null значение
    }

    @Override
    @Transactional
    public PersonFullResponse update(int id, PersonRequest request) {
        //логика с проверкой что вернулось не null значение
    }
```

Сервис работы с сущностью Person изначально включал в себя помимо бизнеас-логики проверку на null в каждом методе, тем самым уже наручая принцип SRP. Хорошо что фреймворк из коробки предоставляет решение - аннотация @Nonnull - она валидирует поля и сообщает о том, что метод не может вернуть null значение. Соответственно, проверка на null переносится в репозиторий по части возвращения значений, и в контроллер п очасти входящих значений.

#### Стало:
``` Java
@Service
@RequiredArgsConstructor
public class PersonServiceImpl
        implements PersonService {

    private final PersonDao personDao;
    private final DepartmentDao departmentDao;

    @Nonnull
    @Override
    @Transactional(readOnly = true)
    public List<PersonShortResponse> findAll() {
        //остается только бизнес логика
    }

    @Nonnull
    @Override
    @Transactional(readOnly = true)
    public PersonFullResponse getById(int id) {
        //остается только бизнес логика
    }

    @Nonnull
    @Override
    @Transactional
    public PersonFullResponse update(int id, @Nonnull PersonRequest request) {
        //остается только бизнес логика
    }
```

### Пример 4

#### Было:
``` Java
public class RoboBlox{
    ...
    private int oilBank;
    ...
}
```

В классе робоблока есть поле, хранящее кол-во топлива в литрах, но при этом имеющее класс примитива. Это в свою очередь создает широкий пласт возможностей выстрелить себе в ногу по ходу исполнения программы. Очевилным решением здесь было создать отдельный класс под хранение топлива, который бы не допускал уже в себе некорретных значений (переполнение бака или отрицательные значения).


#### Стало:
``` Java
public class RoboBlox{
    ...
    private OilBank oilBank;
    ...
    public addOil(int vol){
        oilBank.addVolume(int vol);
    }
}

class OilBank{
    private int maxCapasity = 10
    private int capasity = maxCapasity;

    Oil(int maxCapasity) {
        //проверка валидности значения
    }

    public addVolume(int volume) {
        //добавление объема с проверкой валидности (в случае переполнения остается максимум)
    }

    public removeVolume(int volume) { 
        //уменьшение объема с проверкой валидности (в случае минуса остается 0)
    }
}
```

### Пример 5

#### Было:
``` Java
@Service
@RequiredArgsConstructor
public class VehicleService {
    private final VehicleRepository repository;

    public VhicleDto getVehicle(String id) {
        return repository.findById(id);
    }

    public void removeVehicle(String id) {
        repository.delete(id);
    }
}
```

Классический пример применения String в качестве Id. Потенциально можно получить NPE, причем в коде я вообще не нашел нигже проверки такого случая. Предложил как минимм обернуть идентификатор в отдельный класс и в нем же запечатать процерку с помощью аннотации.

#### Стало:
``` Java
@Service
@RequiredArgsConstructor
public class VehicleService {
    private final VehicleRepository repository;

    public VhicleDto getVehicle(VehicleId id) {
        return repository.findById(id);
    }

    public void removeVehicle(VehicleId id) {
        repository.delete(id);
    }
}

@Data
public class VehicleId {
    @NonNull private final String id;
}
```



### Выводы: 
безусловно, на такие вещи лучше всего обращать внимание еще на стадии проектирования, но к сожалению, миддлы как правило попадают уже на готовые проекты в которых много вот такого "незащищенного" кода. Мне данный подход сейчас очень помогает подмечать такие проблемы в продакшен коде и предлагать решения на месте, которые ингода (да, бизнесу пофиг на все если система сейчас работает и приносит деньги) удается внедрять и скрывать потенциальные ситуации с ошибками. Конечно я чувствую что все-таки не хватает опыта именно поектной работы, когда можно с нуля подумать и продумать все возможные исходы и принять наиболее эффективные проектные решения. Но я стараюсь подчемать и сразу внедрять такие вещи, помогат не окуклиться в рутине релизов и развиваться постоянно.
