package org.example.visitor;

public class ProjectRunner {
    public static void main(String[] args) {
        Project project = new Project();

        Developer juniorDeveloper = new JuniorDev();
        Developer seniorDeveloper = new SeniorDev();

        System.out.println("++ Работает Junior разработчик. ++");
        project.beWritten(juniorDeveloper);
        System.out.println("\n================================\n");
        System.out.println("++ Работает Senior разработчик. ++");
        project.beWritten(seniorDeveloper);
    }
}
