package hardWork.lesson04_3.command;

import hardWork.lesson04_3.Command;
import hardWork.lesson04_3.DataBase;

import java.util.Scanner;

public class AddStudent implements Command {

    private final DataBase dataBase;

    public AddStudent(DataBase dataBase) {
        this.dataBase = dataBase;
    }

    @Override
    public void action() {
        addStudent();
    }

    @Override
    public String getNameOfCommand() {
        return  "Add a student";
    }

    private void addStudent() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter a student name: ");
        String name = scanner.nextLine();
        dataBase.addStudent(name);
    }
}
