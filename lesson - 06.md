## Минимизируем шаги изменений

### Пример №1

Код взят из небольшого CRUD-проекта, слой репозитория.

Тесты:
``` Java
@Import(DepartmentDaoImpl.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DataJpaTest
class DepartmentDaoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DepartmentDao departmentDao;

    @BeforeEach
    void createDepartment() {
        Department department1 = new Department();
        department1.setName("Software");
        department1.setClosed(false);
        Department department2 = new Department();
        department2.setName("Commercial");
        department2.setClosed(false);
        Department department3 = new Department();
        department3.setName("Business");
        department3.setClosed(false);
        entityManager.persist(department1);
        entityManager.persist(department2);
        entityManager.persist(department3);
    }

    @Test
    void findById() {
        Department department = departmentDao.findById(1);

        assert department != null;
        assertEquals(1, (int) department.getId());
        assertEquals("Software", department.getName());
        assertEquals(false, department.getClosed());
    }

    @Test
    void findById_negative() {
        Department department = departmentDao.findById(4);

        assertNull(department);
    }

    @Test
    void findAll() {
        List<Department> departments = departmentDao.findAll();

        assertEquals(3, departments.size());
    }

    @Test
    void update() {
        Department department = new Department();
        department.setId(2);
        department.setName("Commercial");
        department.setClosed(true);

        departmentDao.update(department);

        Department stagedDepartment = departmentDao.findById(department.getId());
        assert stagedDepartment != null;
        assertEquals(2, stagedDepartment.getId());
        assertEquals("Commercial", stagedDepartment.getName());
        assertEquals(true, stagedDepartment.getClosed());
    }

    @Test
    void delete() {
        departmentDao.delete(3);
        List<Department> departments = departmentDao.findAll();

        assertEquals(2, departments.size());
    }

    @Test
    void delete_negative() {
        assertNull(departmentDao.delete(4));
    }
}
```

Реализация:
``` Java
@Repository
public class DepartmentDaoImpl
        implements DepartmentDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Nullable
    @Override
    public Department findById(@Nonnull Integer id) {
        return entityManager.find(Department.class, id);
    }

    @Nonnull
    @Override
    public List<Department> findAll() {
        return entityManager.createQuery("FROM Department", Department.class).getResultList();
    }

    @Nonnull
    @Override
    public Department update(@Nonnull Department department) {
        return entityManager.merge(department);
    }

    @Nullable
    @Override
    public Department delete(@Nonnull Integer id) {
        Department department = entityManager.find(Department.class, id);
        if (department == null) {
            return null;
        }
        entityManager.remove(department);
        return department;
    }
}

```

### Рефлексия:

Изначально я написал сразу все тесты. Потом делал коммит и только потом начал писать реализацию. Коммитты делал редко. Когда не проходил тест, ковырялся с кодом не откатываясь назад. Также иногда приходилось править тесты из-за их некорректной работы.

Затем полностью удалил тесты и реализацию и уже во второй итерации писал так: сначала пишем один тест, коммитим его, затм пишем реализацию. Если всё ок - коммит и идём дальше. Если нет, откатываемся к придыдущему коммиту безжалостно избавляясь от всего написанного но не закомичеснного кода.

Кажется что такой подход дольше, но на практике он оказался быстрее, не сильно но всё таки.

### Пример №2

Проект по многопоточной обработке файла.

Тесты:
``` Java
public class ImplTests {

    @Test
    void file_reader_impl_should_return_1000_strings() {
        File file = getFile("test.txt");
        assertThat(new FileReaderImpl().readLines(file).count()).isEqualTo(1000);
    }

    private File getFile(String name) {
        return new File(getResource(name).getPath());
    }

    @Test
    void string_letter_counter_should_return_predicted_count() {
        String string = "cdccfdbfeadebaee";
        Map<Character, Long> charsCount = new StringLetterCounterImpl().getChars(string);

        assertThat(charsCount).containsOnly(
                entry('a', 2L),
                entry('b', 2L),
                entry('c', 3L),
                entry('d', 3L),
                entry('e', 4L),
                entry('f', 2L)
        );
    }

    @Test
    void letter_count_merger_should_return_predicted_count() {
        Map<Character, Long> first = new HashMap<>();
        first.put('a', 2L);
        first.put('b', 2L);
        Map<Character, Long> second = new HashMap<>();
        second.put('a', 3L);
        second.put('b', 1L);
        second.put('c', 7L);

        Map<Character, Long> merged = new LetterCountMergerImpl().merge(first, second);

        assertThat(merged).containsOnly(
                entry('a', 5L),
                entry('b', 3L),
                entry('c', 7L)
        );
    }

    @Test
    void async_file_letter_counting_should_return_predicted_count() {
        final int NUMBER_OF_THREADS = 10;

        var file = getFile("test.txt");
        var counter = new AsyncFileLetterCounter(
                new StringLetterCounterImpl(),
                new LetterCountMergerImpl(),
                new FileReaderImpl(),
                Executors.newFixedThreadPool(NUMBER_OF_THREADS)
        );

        Map<Character, Long> count = counter.count(file);

        assertThat(count).containsOnly(
                entry('a', 2697L),
                entry('b', 2683L),
                entry('c', 2647L),
                entry('d', 2613L),
                entry('e', 2731L),
                entry('f', 2629L)
        );
    }

    private File getFile(String name) {
        return new File(getResource(name).getPath());
    }
}
```

Реализация:
``` Java
@AllArgsConstructor
public class AsyncFileLetterCounter implements FileLetterCounter {
    private final StringLetterCounter letterCounter;
    private final LetterCountMerger letterMerger;
    private final FileReader fileReader;
    private final ExecutorService service;

    @Override
    @SneakyThrows({InterruptedException.class, ExecutionException.class})
    public Map<Character, Long> count(File input) {
        return fileReader.readLines(input)
                .map(str ->
                        service.submit(() -> letterCounter.getChars(str)))
                .reduce((first, second) ->
                        service.submit(() -> letterMerger.merge(first.get(), second.get())))
                .orElseThrow().get();
    }
}
```

``` Java
public class FileReaderImpl implements FileReader {
    @Override
    @SneakyThrows (IOException.class)
    public Stream<String> readLines(File file) {
        return Files.lines(Paths.get(file.toString()));
    }
}
```

``` Java
public class LetterCountMergerImpl implements LetterCountMerger {
    @Override
    public Map<Character, Long> merge(Map<Character, Long> first, Map<Character, Long> second) {
        Optional<Map<Character, Long>> maybeFirst = Optional.of(first);
        Optional<Map<Character, Long>> maybeSecond = Optional.of(second);
        Map<Character, Long> merged = new HashMap<>(maybeFirst.orElse(Collections.emptyMap()));
        maybeSecond.orElse(Collections.emptyMap()).forEach((key, value) -> merged.merge(key, value, Long::sum));
        return merged;
    }
}
```

``` Java
public class StringLetterCounterImpl implements StringLetterCounter {
    @Override
    public Map<Character, Long> getChars(String string) {
        return string == null ?
                Collections.emptyMap() :
                string.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.groupingBy(c -> c, Collectors.counting()));
    }
}
```

### Рефлексия:
Здесь был реализован подход, аналогичный предыдущему примеру.

## Выводы:
Не знаю как другим, но мне кажется абсолютно естественным подход, когда мы сначала пишем тест (тест на один метод), делаем коммит и затем пишем его реализацию. Если тест прошёл - коммит, нет - откатываемся назад и пишем реализацию заново. Такой подход позволяет во-первых, подругому взглянуть на код, котрый не удался, во-вторых позволяет не потерять много времени если мы вдруг обнаружили что тест написан некорректно, так как у нас в потере окажется всего лишь небольшой коммит. Более того, на практике этот подход оказывается быстрее типичного TDD, так как мы на ходу по-немногу но продвигаемся в решении проблемы зная что назад возвращаться не придётся.

Разумеется такой подход будет продуктивен при адекватной общей архитектуре и соблюдении SOLID в реализации класов.

Для себя я данный стиль написания кода оставил приоритетным, хотя многие знакомые на него смотрят странно, но работе в команде этот стиль не мешает, поэтому ломать себя не приходится.
