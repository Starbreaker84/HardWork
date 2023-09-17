package org.example.inharitance;

public class ProjectRunner {
    public static void main(String[] args) {
        Project project = new Project();

        JuniorDev junior = new JuniorDev();
        SeniorDev senior = new SeniorDev();

        System.out.println("Работает джуниор.");
        project.beWritten(junior);
        System.out.println("\n===============================\n");
        System.out.println("Работает синьер-помидор.");
        project.beWritten(senior);
    }
}
