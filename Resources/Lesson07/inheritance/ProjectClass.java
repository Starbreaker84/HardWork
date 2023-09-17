package org.example.inharitance;

public class ProjectClass extends ProjectElement{
    @Override
    public void beWritten(JuniorDev developer) {
        System.out.println("Джун пишет плохой класс.");
    }

    @Override
    public void beWritten(SeniorDev developer) {
        System.out.println("Сеньёр пишет плохой класс.");
    }
}
