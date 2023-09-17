package org.example.inharitance;

public class Test extends ProjectElement {
    @Override
    public void beWritten(JuniorDev developer) {
        System.out.println("Джун пишет плохой тест.");
    }

    @Override
    public void beWritten(SeniorDev developer) {
        System.out.println("Сеньёр исправляет плохой тест.");
    }
}
