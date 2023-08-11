package hardWork.lesson04_3.command;

import hardWork.lesson04_3.Command;
import hardWork.lesson04_3.DataBase;
import hardWork.lesson04_3.Student;

import java.util.List;

public class ListOfStudents implements Command {
    private final DataBase dataBase;

    public ListOfStudents(DataBase dataBase) {
        this.dataBase = dataBase;
    }

    @Override
    public void action() {
        getListOfStudents();
    }

    @Override
    public String getNameOfCommand() {
        return "Get List of All Students";
    }

    private void getListOfStudents() {
        List<Student> students = dataBase.getListOfStudents();
        for (Student student : students) {
            System.out.println(student);
        }
    }
}
