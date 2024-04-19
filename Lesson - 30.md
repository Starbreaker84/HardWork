## Про раздутость кода

Примеры кода с "неименованными" кусочками кода, которые было бы излишне оформлять как функцию, но которые при этом сами по себе семантически значимы и автономны, хотя, возможно, и смешаны с остальным кодом.

### Приемр 1
``` Java
// Формирование базового результата скоринга клиента, при этом
// результат далее используется в качестве аргумента в смежных расчётах
clientScoringAmount=scoringResult.getCovenant().getCovenantAmount();
```

### Приемр 2
``` Java
// Итоговое значение стратегии скоринга формируется из двух слабо 
// связанных между собой сущностей
scoringStartegy=scoringResult.getStrategy() + product.getType();
```

### Приемр 3
``` Java
// Выполнение сортировки итоговой коллекции для более удобного вывода фронта
// Шок-контент - я продавил использовании коллекции-обёртки!!!
resultEmployeeList = employeeList.sort();
```

### Приемр 4
``` Java
// Формирование адреса залогодателя
if(nonNull(pledgeApplicant.getAddresses())) {
    applicant.setAddresses(collectionMapper(pledgeApplicant.getAddresses(), addr -> {
        ApplicantAddressEntity address=applicantMapper.toAddressEntity(addr);
        address.setApplicant(applicant);
        return address;
    }
    ));
}
```

### Приемр 5
``` Java
// Конструктор DTO продукта получает поля для формирования
entityDto = new EntityDto(entity.getValue(), entity.getStatus(), entity.getCompanion()); 
```

## Выводы:
Их примеров хорошо видно что большинство случаев имеет смысл вынести в отдельные методы или даже применить паттерны для того, чтобы отделить суть происходящего. Но не всегда это возможно, как в примере с коллекцией, если только не сделать её изначально приоритетной. Также иногда встрчаются несколько разных операций в теле одного цикла, и вот тут неименованные куски кода все-таки лучше выполняют свою функцию. В целом, нужно учитывать контекст и целесообразность применеине того или иного подхода. 