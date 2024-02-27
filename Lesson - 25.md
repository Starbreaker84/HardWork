## Ускоряем код фреймворков в 100 раз
Самый популярный framework для работы с реляционными БД в Java, безусловно является Hibernate. И как обычно, за более высокий уровень абстракции и кажущуюся простоту приходится платить производительсностью. Но к счастью, в Hibernate нет никакой магии - под капотом там нативный JDBC.

Далее приведён пример трёх кейсов, по замене Hibernate на JDBC и последствия такой рокировки.

### Пример 1

Реализация пакетной вставки для ускорения отклика базы данных. Изначально запрос был записан в Hibernate, но в данной реализации нужно было предусмотреть периодическую очистку кэша хранения, для предотвращения его переполнения и в целом пакетная вставка отрабатывала вне таймингов. Было принято решение переписать логику на чистом JDBC, что значительно повысило производельность работы с БД, а также избавило от поддержки программной инфраструктуры Hibernate.

### Hibernate
``` Java
try (Session session = HibernateUtil.getSessionFactory().getCurrentSession()) {
    Transaction transaction = session.getTransaction();
    transaction.begin();
    for (int i = 1; i <= SIZE; i++) {
        session.persist(new Opportunity("Batch opportunity insert: " + i));
        if (i % BATCH_SIZE == 0) {
            // Flush and clear the cache every batch
            session.flush();
            session.clear();
        }
    }
    transaction.commit();
}
```

### JDBC
``` Java
connection.setAutoCommit(false);
try (PreparedStatement pstmt = connection.prepareStatement("INSERT INTO opporunity (salesMethod) VALUES (?)")) { //(1)
    for (int i = 1; i <= SIZE; i++) {
        pstmt.setString(1, "Opportunity insert: " + i); //(2)
        pstmt.addBatch(); //(3)
        if (i % BATCH_SIZE == 0 || i == SIZE) {
            try {
                int[] result = pstmt.executeBatch(); //(4)
                connection.commit();
            } catch (BatchUpdateException ex) {
                Log(ex);
                connection.rollback();
            }
        }
    }
}
```

### Пример 2

В проекте использовался Spring Data у которого под капотом как раз работает Hibernate. Очень часто, если этим инструментом пользуется не опытный разработчик, то возникает так называемая проблема n + 1, когда вместо одного запроса Hibernate выполняет целую серию запросов, что сильно ухудшает производительность системы. Нужно было быстрое решение проблемы и я применил возможность прописать для метода нативный запрос. С учетом обстоятельств, решение оказалось крайне эффективным.

### Spring Data (Hibernate)
``` Java
// Метод репозитория
List<ReferenceGroupEntity> findEmployeeGroupsByManagerGroupIds(Collection<Long> refGroupIds);
```
### SQL
``` Java
// Метод репозитория
 @Query("SELECT r FROM ReferenceGroupEntity r inner join ReferenceGroupEntity c on c.managerId = r.id where r.id in :refGroupIds group by r")
List<ReferenceGroupEntity> findEmployeeGroupsByManedgmentGroupIds(@Param(value = "refGroupIds") Collection<Long> refGroupIds);
```

### Пример 3

Не совсем сравниваемый пример, но тем не менее. В новом сервисе, для уменьшения бойлерплейта, но в то же время сохранения вменяемой производительности было решено попробовать альтернативный фреймворк для работы с БД - Jooq. Он позволяет писать запросы очень похоже на нативные, но поскольку представляет это с помощью dsl, ориентироваться в них и контролировать гораздо проще. Ниже приведен пример реализации из текущего проекта. По сравнению с соседними проектами, решения показывает себя достаточно хорошо, сам фреймворк и гораздо легковеснее Hibernate. Код написан на Kotlin.

``` Kotlin
fun fetchPersonbyId(
        personId: UUID
    ): PersonApiEntity {
        return dslContext.select(
            person.ID,
            person.NAME,
            DSL.multiset(
                dslContext.select(
                    country.asterisk()
                )
                    .from(COUNTRY)
                    .where(COUNTRY.PERSON_ID.eq(person.ID).and(COUNTRY.FORBIDDEN.eq(true)))
            ).`as`("prohibitedCountries")
                .convertFrom { prohibitedCountries -> prohibitedCountries.map { it.toCountryApiEntity() } },
            DSL.multiset(
                dslContext.select(country.asterisk())
                    .from(COUNTRY)
                    .where(COUNTRY.PERSON_ID.eq(person.ID).eq(person.ID)).and(COUNTRY.OCEAN.eq(true)))
            ).`as`("oceanCountries")
                .convertFrom { oceanCountries -> oceanCountries.map { it.toCountryApiEntity()}})
            .from(person)
            .where(person.ID.eq(personId))
            .fetchOne { it.personApiEntity() }
            ?: throw NotFoundException("Not found person with id: $personId")
    }                     
```

## Подводя итоги.
Не смотря на попытки освободить разработчиков от мышления в парадигме реляционных БД, а работа с базовыми запросами позволяет это сделать, фреймворки, как только наступает пора серьезных бизнес задач, заствляют разработчика спускаться на уровень абстракции ниже. Отказ от этого серьезно бьет по производительности системы. Да, использование нативных инструментов требует боольше кода, и, самое главное, контроля и внимания, но зато позволяет использовать вычислительную мощь на полную. Возможно, сама концепция упрощения и доступности, заложенная во фреймворках, ошибочна. Не даров тот же Spring выпустил версию своего расширениия Data JDBC, которая позволяет оперировать именно нативными запросами, не уходя в дебри создания и контроля соединения с БД и т.д.

