package hardWork.lesson04_3.command;

import hardWork.lesson04_3.Command;
import hardWork.lesson04_3.DataBase;

import java.util.Scanner;

public class DeleteStudent implements Command {

    private final DataBase dataBase;

    public DeleteStudent(DataBase dataBase) {
        this.dataBase = dataBase;
    }
    @Override
    public void action() {
        deleteStudent();
    }

    @Override
    public String getNameOfCommand() {
        return "Delete student";
    }

    private void deleteStudent() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter a student name: ");
        String name = scanner.nextLine();
        dataBase.deleteStudent(name);
    }
}
