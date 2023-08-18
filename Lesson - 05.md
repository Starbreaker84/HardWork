## Совмещаем несовместимое

### Пример №1:

``` Java
/*
Класс, реализующий управление Департаментами предприятия.
Позволяет получить справочную информацию и произвести соответсвующие
корректные изменения в структуре департаментов.
Данный класс является связующим звеном между слоем получения и обрботки запросов и хранилищем данных.
*/

@Service
public class DepartmentServiceImpl
        implements DepartmentService {

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
        return departmentDao.findAll().stream()
                .map(DepartmentMapper::toShortDepartment)
                .collect(Collectors.toList());
    }

    @Nonnull
    @Override
    @Transactional(readOnly = true)
    public DepartmentFullResponse getById(int id) {
        Department department = departmentDao.findById(id);
        if (department == null) {
            throw new EntityNotFoundException("Department with id " + id + " not found.");
        }
        return DepartmentMapper.toFullDepartment(department);
    }

    @Override
    @Transactional
    public int create(@Nonnull DepartmentRequest request) {
        return departmentDao.update(DepartmentMapper.fromRequest(request)).getId();
    }

    @Nonnull
    @Override
    @Transactional
    public DepartmentFullResponse update(int id, @Nonnull DepartmentRequest request) {
        Department department = departmentDao.findById(id);
        if (department == null) {
            throw new EntityNotFoundException("Department with id " + id + " not found.");
        }
        return DepartmentMapper.toFullDepartment(departmentDao.update(DepartmentMapper.updateFromRequest(request, department)));
    }

    @Override
    @Transactional
    public void delete(int id) {
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

### Пример №2
``` Java

/*
Класс, реализующий управление Персоналом предприятия.
Позволяет получить справочную информацию и произвести соответствующие
корректные изменения в структуре персонала, в том числе отноистельно департамента
за которым закреплён сотрудник.
Данный класс является связующим звеном между слоем получения и обрботки запросов и хранилищем данных.
*/

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
        return personDao.findAll()
                .stream()
                .map(PersonMapper::toShortPerson)
                .collect(Collectors.toList());
    }

    @Nonnull
    @Override
    @Transactional(readOnly = true)
    public PersonFullResponse getById(int id) {
        Person person = personDao.findById(id);
        if (person == null) {
            throw new EntityNotFoundException("Person with id " + id + " not found.");
        }
        return PersonMapper.toFullPerson(person);
    }

    @Override
    @Transactional
    public int create(@Nonnull PersonRequest request) {
        return personDao.update(PersonMapper.fromRequest(request)).getId();
    }

    @Nonnull
    @Override
    @Transactional
    public PersonFullResponse update(int id, @Nonnull PersonRequest request) {
        Person person = personDao.findById(id);
        if (person == null) {
            throw new EntityNotFoundException("Person with id " + id + " not found.");
        }

        return PersonMapper.toFullPerson(personDao.update(PersonMapper.updateFromRequest(request, person)));
    }

    @Override
    @Transactional
    public void delete(int id) {
        personDao.delete(id);
    }


    @Override
    @Transactional
    public void addPersonToDepartment(int departmentId, int personId) {
        Person person = personDao.findById(personId);
        Department department = departmentDao.findById(departmentId);
        if (person == null) {
            throw new EntityNotFoundException("Person with id " + personId + " not found.");
        }
        if (department == null) {
            throw new EntityNotFoundException("Department with id " + departmentId + " not found.");
        }
        if (department.getClosed()) {
            throw new IllegalStateException("Department with id " + departmentId + " is closed.");
        }
        personDao.update(person.setDepartment(department));
    }

    @Override
    @Transactional
    public void removePersonFromDepartment(int departmentId, int personId) {
        Department department = departmentDao.findById(departmentId);
        if (department == null) {
            throw new EntityNotFoundException("Department with id " + departmentId + " not found.");
        }
        Person person = personDao.findById(personId);
        if (person == null) {
            throw new EntityNotFoundException("Person with id " + personId + " not found.");
        }
        if (Objects.equals(department.getId(), person.getDepartment().getId())) {
            person.setDepartment(null);
            department.getPersonList().remove(person);
            personDao.update(person);
            departmentDao.update(department);
        }
    }
}

```

### Пример №3
``` Java

/*
Класс, позволяющий получить информацию о персонале в формате,
зависящем от характера запроса.
Применяется только в отношении персонала.
Класс является утилитой. Применим для формирования формата ответа данный на уровне
бизнес-логики приложения.
*/
public class PersonMapper {

    public static PersonShortResponse toShortPerson(Person person) {
        return new PersonShortResponse()
                .setId(person.getId())
                .setFullName(person.getMiddle_name() == null ?
                        person.getLast_name() + " " + person.getFirst_name():
                        person.getLast_name() + " " + person.getFirst_name() + " " + person.getMiddle_name());
    }

    public static PersonFullResponse toFullPerson(Person person) {
        DepartmentShortResponse department = person.getDepartment() == null ? null : new DepartmentShortResponse()
                .setId(person.getDepartment().getId())
                .setName(person.getDepartment().getName());
        return new PersonFullResponse()
                .setId(person.getId())
                .setFullName(person.getMiddle_name() == null ?
                        person.getLast_name() + " " + person.getFirst_name():
                        person.getLast_name() + " " + person.getFirst_name() + " " + person.getMiddle_name())
                .setAge(person.getAge())
                .setDepartment(department);
    }

    public static Person fromRequest(PersonRequest request) {
        return new Person()
                .setFirst_name(request.getFirstName())
                .setMiddle_name(request.getMiddleName())
                .setLast_name(request.getLastName())
                .setAge(request.getAge());
    }

    public static Person updateFromRequest(PersonRequest request, Person person) {
        return person
                .setFirst_name(request.getFirstName() == null ? person.getFirst_name() : request.getFirstName())
                .setMiddle_name(request.getMiddleName() == null ? person.getMiddle_name() : request.getMiddleName())
                .setLast_name(request.getLastName() == null ? person.getLast_name() : request.getLastName())
                .setAge(request.getAge() == null ? person.getAge() : request.getAge());
    }
}

```

## Выводы:
На удивление, на текущий момент самое сложное задание. Уместить в нескольких строках суть класса, не описывая его методы - та ещё задачка. Но я проверил эффект на нескольких коллегах, показав им что-то вроде UML диаграммы приложения, добавив туда данные описания классов. Несмотря на то, что наименование самих классов достаточно привычны для индустрии, такое описание на уровне дизайна сразу однозначно определяет характер класса. Более того, мне сейчас кажется, что именно так нужно начинать проектирование ПО, давая описание каждой сущности на уровне дизайна, без углубления в реализацию и её детали.
