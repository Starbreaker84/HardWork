## Думаем на уровне дизайна

Итак, для примера возьмём модуль сервиса из работчего проекта, поэтому заранее извиняюсь на "простыни" кода. В силу корпоративной культуры, код стараемся писать изходя из дизайна (а точнее спецификации, без "магических" чисел, строк и т.п.). Но как раз на основе тестов для кода можно показать разность между 2 и 3 уровнями.

Сначала опишем сервис описан далее, дизайн (спецификация) каждого метода будет в комментариях.
``` Java
@Service
public class DepartmentServiceImpl implements DepartmentService {

    // Сервис оперирует репозиториями департамента и персоны
    private final DepartmentDao departmentDao;

    private final PersonDao personDao;

    public DepartmentServiceImpl(DepartmentDao departmentDao, PersonDao personDao) {
        this.departmentDao = departmentDao;
        this.personDao = personDao;
    }

    @Nonnull
    @Override
    @Transactional(readOnly = true)
    public List<DepartmentShortResponse> findAll() {
        // Получение краткой информации о всех департаментах
        return departmentDao.findAll().stream()
                .map(DepartmentMapper::toShortDepartment)
                .collect(Collectors.toList());
    }

    @Nonnull
    @Override
    @Transactional(readOnly = true)
    public DepartmentFullResponse getById(int id) {
        // Получение подробной информации о департаменте и краткой информации о людях в нем.
        // Если не найдено, отдавать 404:NotFound
        Department department = departmentDao.findById(id);
        if (department == null) {
            throw new EntityNotFoundException("Department with id " + id + " not found.");
        }
        return DepartmentMapper.toFullDepartment(department);
    }

    @Override
    @Transactional
    public int create(@Nonnull DepartmentRequest request) {
        // Создание нового департамента, принимает запрос и возвращает идентификационный номер 
        return departmentDao.update(DepartmentMapper.fromRequest(request)).getId();
    }

    @Nonnull
    @Override
    @Transactional
    public DepartmentFullResponse update(int id, @Nonnull DepartmentRequest request) {
        // Обновление данных о департаменте.
        // Если не найдено, отдавать 404:NotFound
        Department department = departmentDao.findById(id);
        if (department == null) {
            throw new EntityNotFoundException("Department with id " + id + " not found.");
        }
        return DepartmentMapper.toFullDepartment(departmentDao.update(DepartmentMapper.updateFromRequest(request, department)));
    }

    @Override
    @Transactional
    public void delete(int id) {
        // Удаление всех людей из департамента и удаление самого департамента.
        // Если не найдено, то ничего не делать
        Department department = departmentDao.findById(id);
        if (department != null) {
            department.getPersonList().forEach(person -> {
                person.setDepartment(null);
                personDao.update(person);
            });
            departmentDao.delete(id);
        }
    }

    @Override
    @Transactional
    public void close(int id) {
        // Удаление всех людей из департамента и установка отметки на департаменте,
        // что он закрыт для добавления новых людей. Если не найдено, отдавать 404:NotFound
        Department department = departmentDao.findById(id);
        if (department == null) {
            throw new EntityNotFoundException("Department with id " + id + " not found.");
        }
        department.getPersonList().forEach(person -> {
            person.setDepartment(null);
            personDao.update(person);
        });
        department.setClosed(true);
        department.setPersonList(new ArrayList<>());
        departmentDao.update(department);
    }
}
```
В целом, как видно код следует имеено дизайну, все обращения к внешним источникам обусловлены логически и не техническими условиями.

Теперь взглянем на тест ниже для этого кода:
``` Java
@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentDao departmentDao;

    @Mock
    private PersonDao personDao;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    @Test
    void findAll() {
        Department department1 = new Department()
                .setId(1)
                .setName("Business")
                .setClosed(false)
                .setPersonList(new ArrayList<>());
        Department department2 = new Department()
                .setId(2)
                .setName("Business")
                .setClosed(false)
                .setPersonList(new ArrayList<>());
        List<Department> departments = List.of(department1, department2);

        when(departmentDao.findAll()).thenReturn(departments);

        assertThat(departmentService.findAll().size()).isEqualTo(2);
        assertThat(departmentService.findAll().get(0)).isInstanceOf(DepartmentShortResponse.class);
    }

    @Test
    void findById() {
        when(departmentDao.findById(anyInt())).thenReturn(new Department()
                .setId(1)
                .setName("Commercial")
                .setClosed(false)
                .setPersonList(List.of(new Person()
                        .setId(1)
                        .setFirst_name("Ivan")
                        .setLast_name("Ivanov")
                        .setAge(25))));

        assertThat(departmentService.getById(1)).isNotNull();
        assertThat(Objects.requireNonNull(departmentService.getById(1)).getPersons().get(0)).isInstanceOf(PersonShortResponse.class);
    }

    @Test
    void findById_negative() {
        when(departmentDao.findById(anyInt())).thenReturn(null);

        Assertions.assertThrows(EntityNotFoundException.class, () -> departmentService.getById(1));
    }

    @Test
    void create() {
        DepartmentRequest request = new DepartmentRequest()
                .setName("Business");

        when(departmentDao.update(any())).thenReturn(new Department()
                .setId(1)
                .setName(request.getName())
                .setClosed(false));

        assertThat(departmentService.create(request)).isEqualTo(1);
    }

    @Test
    void update() {
        DepartmentRequest request = new DepartmentRequest()
                .setName("Business");

        when(departmentDao.findById(anyInt())).thenReturn(new Department()
                .setId(1)
                .setName("Commercial")
                .setClosed(false)
                .setPersonList(new ArrayList<>()));

        when(departmentDao.update(any())).thenReturn(new Department()
                .setId(1)
                .setName(request.getName())
                .setClosed(false)
                .setPersonList(new ArrayList<>()));

        assertThat(departmentService.update(1, request))
                .isNotNull()
                .isInstanceOf(DepartmentFullResponse.class);

        verify(departmentDao, times(1)).findById(anyInt());
        verify(departmentDao, times(1)).update(any());
    }

    @Test
    void update_not_found() {
        when(departmentDao.findById(anyInt())).thenReturn(null);

        Assertions.assertThrows(EntityNotFoundException.class, () -> departmentService.update(1, new DepartmentRequest()));
    }

    @Test
    void delete() {
        Department department = new Department()
                .setId(1)
                .setName("Commercial")
                .setClosed(false);

        Person person1 = new Person()
                .setId(1)
                .setFirst_name("Ivan")
                .setLast_name("Ivanov")
                .setAge(25)
                .setDepartment(department);

        Person person2 = new Person()
                .setId(1)
                .setFirst_name("Petr")
                .setLast_name("Petrov")
                .setAge(30)
                .setDepartment(department);

        List<Person> persons = new ArrayList<>();
        persons.add(person1);
        persons.add(person2);

        department.setPersonList(persons);

        when(departmentDao.findById(anyInt())).thenReturn(department);
        when(personDao.update(any())).thenReturn(null);
        when(departmentDao.delete(anyInt())).thenReturn(new Department());

        departmentService.delete(1);

        assertThat(person1.getDepartment()).isNull();
        assertThat(person2.getDepartment()).isNull();

        verify(departmentDao, times(1)).delete(1);
    }

    @Test
    void close() {
        Department department = new Department()
                .setId(1)
                .setName("Commercial")
                .setClosed(false);

        Person person1 = new Person()
                .setId(1)
                .setFirst_name("Ivan")
                .setLast_name("Ivanov")
                .setAge(25)
                .setDepartment(department);

        Person person2 = new Person()
                .setId(1)
                .setFirst_name("Petr")
                .setLast_name("Petrov")
                .setAge(30)
                .setDepartment(department);

        List<Person> persons = new ArrayList<>();
        persons.add(person1);
        persons.add(person2);

        department.setPersonList(persons);

        when(departmentDao.findById(anyInt())).thenReturn(department);
        when(personDao.update(any())).thenReturn(null);
        when(departmentDao.update(any())).thenReturn(null);

        departmentService.close(1);

        assertThat(person1.getDepartment()).isNull();
        assertThat(person2.getDepartment()).isNull();
        assertThat(department.getClosed()).isTrue();
        assertThat(department.getPersonList().size()).isEqualTo(0);
    }

    @Test
    void close_not_found() {
        when(departmentDao.findById(anyInt())).thenReturn(null);

        Assertions.assertThrows(EntityNotFoundException.class, () -> departmentService.close(1));
    }
}
```
Здесь мы явно видим что тесты следуют коду (но это специфика изолированного теста), а именно мы моками оперируем тем, как будет работать внутренняя (уже реализованная структура) внутри метода. Следование дизайну предполагает что мы пишем тест без оглядки на реализацию самого метода. В нашем случае именно в таком ключе будет отрабатывать интеграционный (модульный тест). К сожалению доступа к коду интеграционного теста у меня нет, но я покажу спевдокодом покажу как могла бы вяглядить структура и дизайн такого тестирования.

Итак, наш тест, следующий дизайну мог бы выглядеть так:
``` Java
class DepartmentServiceTest {
    private DepartmentServiceImpl departmentService;

    @BeforeAll
    void context() {
        // Скрипт запуска БД с преодпределёнными тестовыми данными
    }

    @Test
    void findAll() {
        assertThat(departmentService.findAll().size()).isEqualTo(2);
        assertThat(departmentService.findAll().get(0)).isInstanceOf(DepartmentShortResponse.class);
    }

    @Test
    void findById() {
        assertThat(departmentService.getById(1)).isNotNull();
        assertThat(Objects.requireNonNull(departmentService.getById(1)).getPersons().get(0)).isInstanceOf(PersonShortResponse.class);
    }

    ...
}
```
Такой модульный тест уже оперирует чистой логикой, не заботясь о том, как реализован тот или иной метод, а сосредотачиваясь исключительно проверке корректности ожидаемого ответа.

Итоги:
Следование дизайну и логике программы можно и нужно писать код, это сильно облегчает как его понимание так и его поддержку. Но вот с тестированием всё не так однозначно. Концепция TDD сосредоточена на предварительном описании поведения, и как мы увидели, может сильно привязаться к коду, когда малейшее изменение реализации ведёт в крушению тестов. Спасает интеграционное/модульное тестирование, но у него есть обратная сторона - оно начинает пронизывать всю программу в рамках тестируемого метода, что не всегда может быть приемлемо на ранних этапах, особенон когда разные части системы могут разрабатываться разными командами и которые в свою очередь заинтересованы в тестировнии своих конкретных реализаций.

В целом, можно сказать что при внедрении обоих политик мы можем балансировать на 2 и 3 словях рассуждения о программе по мере готовности самого продукта, и также, по мере готовности больше полагаться на 3 уровень отходя от второго. Но это касается только тестирования кода. Сам код, безусловно, можно и нужно писать только в парадигме дизайна всей системы.
