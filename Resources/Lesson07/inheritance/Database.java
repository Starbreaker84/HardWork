package org.example.inharitance;

public class Database extends ProjectElement {
    @Override
    public void beWritten(JuniorDev developer) {
        System.out.println("Джун роняет базу.");
    }

    @Override
    public void beWritten(SeniorDev developer) {
        System.out.println("Сеньёр поднимает базу.");
    }
}
