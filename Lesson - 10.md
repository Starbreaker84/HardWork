## Божественная линия кода

### Пример 1
**Было:**
``` Java
return PersonMapper.toFullPerson(personDao.update(PersonMapper.updateFromRequest(request, person)));
```

**Стало:**
``` Java
  Person personFromRequest = PersonMapper.updateFromRequest(request, person)
  Person updatedPerson = personDao.update(personFromRequest)
  FullPerson personForResponse = PersonMapper.toFullPerson(updatedPerson);
return personForResponse;
```

### Пример 2
**Было:**
``` Java
return DepartmentMapper.toFullDepartment(departmentDao.update(DepartmentMapper.updateFromRequest(request, department)));
```

**Стало:**
``` Java
Department departmentFromRequest = DepartmentMapper.updateFromRequest(request, department);
Department updatedDepartment = departmentDao.update();
FullDepartment departmentForResponse = DepartmentMapper.toFullDepartment();
return departmentForResponse;
```

### Пример 3
**Было:**
``` Java
return new Extractor(
    new Pipeline(pipelinePath),
    items,
    executors);
```

**Стало:**
``` Java
    Pipeline pipeline = new Pipeline(pipePath);
    Extractor extractor = new Exctractor(pipeline, items, executors);
return extractor;
```

### Пример 4
**Было:**
``` Java
if (createTransaction(items, department) && getSlot(dao.getSlot()) ||
    createTransaction(items, employees) && getSlot(dao.getSlot()))
```

**Стало:**
``` Java
Transaction departmentTransaction = createTransaction(items, department);
Transaction employeeTransaction = createTransaction(items, employees);
Boolean isSlotFree = getSlot(dao.getSlot());
Boolean isDepartmentSlot = departmentTransaction && isSlotFree;
Boolean isEmploueeSlot = employeeTransaction && isSlotFree;

if (isDepartmentSlot || isEmploueeSlot)
```

### Пример 5
**Было:**
``` Java
String result = employee.getName() + " was given a benefit in the amount of " +  benefits(employee.getId(), size);
```

**Стало:**
``` Java
String employeeName = employee.getName();
Long employeeBenefits = benefits(employee.getId(), size);

String result = employeeName + " was given a benefit in the amount of " + employeeBenefits;
```

## Итоги:
Безусловно "Божественные строки" значительно затрудняют как чтение, так и отладку кода. Особенон опасны места в кторых параметры конструктора вычисляются "на ходу". Добавление нескольких промежуточных переменных позволяет сделать код гораздо более читабельным и чистым, что в свою очередь ускорит кк разработку так и отладку кода в случае внесения изменений.
