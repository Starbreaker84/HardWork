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

```

**Стало:**
``` Java

```

### Пример 4
**Было:**
``` Java

```

**Стало:**
``` Java

```

### Пример 5
**Было:**
``` Java

```

**Стало:**
``` Java

```
