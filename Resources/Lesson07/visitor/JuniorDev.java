package org.example.visitor;

public class JuniorDev implements Developer{
    @Override
    public void create(ProjectClass projectClass) {
        System.out.println("Написал плохо структурированный класс.");
    }

    @Override
    public void create(Database database) {
        System.out.println("Уронил базу");
    }

    @Override
    public void create(Test test) {
        System.out.println("Написал невалидный тест.");
    }
}
