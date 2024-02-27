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


