## Абстрагируем управляющие паттерны

### Пример №1
Пример взят из рабочего проекта. Очень часто в коде программы требовалось обновлять поля в хранилище данных заявки (отдельное приложение), взаимодействие с которым происходит через GraphQL запрос.

**Было:**
``` Kotlin
// Код внутри класса сервиса

val addedField = Field(name = TEMPORARY, value = TRUE)
opportunity.patchOpportunity(
    integrationID,
    PatchOpportunity {
      data {
        ...
        field {
          addedField
        }
      ...
      }
    })
```

Несмотря на удобство технологии GraphQL, если работать с ней напрямую с достаточно большими объектами (а вот конкретно в объекте Заявка, в котором обновляются поля, их там около 20), очень часто возникают ошибки чтения/записи в эти поля. Проблема в жестком соблюдении иерархии при запросе.
Было принято решение создать некую абстракцию на изменение самых часто встречающихся полей, чтобы остальные разработчики не думали о том, правильно они обращаются к полям по иерархии, а только указывали что они хотят изменить с соответствующими значениями. Такие ацессоры, так сказать.

Единственное, такие конструкции не работают когда нужно в одном делегате (термин из Camunda) направить много таких запросов. Как правило, в таких случаях создается отдельный useCase.

**Стало:**
``` Kotlin
// Код внутри класса сервиса
fieldAccessor.save(opportunity.integraionId, TEMPORARY, TRUE)
```

Как видно, помимо существенного уменьшения кодовой базы мы дополнительно исключили возникновение непредвиденных ошибок.

### Пример №2
Пример взят из небольшого проекта. На удивление, конвертация ответа базы данных в сервисе в DTO осуществлялась "на лету", хоть и с применением паттерна builder. Тем не менее много лишнего кода лежит непосредственно в описании бизнес логики, что не очень хорошо.

**Было:**
``` Java
@Nonnull
@Override
@Transactional(readOnly = true)
public List<PersonShortResponse> findAll() {
    Person personList = personDao.findAll();
    PersonResponse personResponsList = new ArrayList<>();
    for (Person person : personList) {
        PersonResponse newPersonResponse = PersonShortResponse()
                                                .setId(person.getId())
                                                .setFullName(person.getMiddle_name() == null ?
                                                    person.getLast_name() + " " + person.getFirst_name():
                                                    person.getLast_name() + " " + person.getFirst_name() + " " + person.getMiddle_name());
        personResponsList.add(newPersonResponse);
    }

    return personResponsList;
}
```

Было преложено обернуть все варианты преобразования ответов и запросов в функции статического класса маппера. В месте с этим стало удобно производить ответ с помощью Java Streams.

**Стало:**
``` Java
@Nonnull
@Override
@Transactional(readOnly = true)
public List<PersonShortResponse> findAll() {
    return personDao.findAll().stream()
        .map(PersonMapper::toShortPerson)
        .collect(Collectors.toList());
}
```
Как видим, абстрагирование от маппинга существенно увеличило читаемость логики бизнес прослойки.
