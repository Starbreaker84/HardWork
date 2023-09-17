package org.example.visitor;

public class SeniorDev implements Developer {
    @Override
    public void create(ProjectClass projectClass) {
        System.out.println("Исправил плохо структурированный класс.");
    }

    @Override
    public void create(Database database) {
        System.out.println("Восстановил базу");
    }

    @Override
    public void create(Test test) {
        System.out.println("Исправил невалидный тест.");
    }
}
