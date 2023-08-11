package hardWork.lesson04_3;

import hardWork.lesson04_3.command.AddStudent;
import hardWork.lesson04_3.command.DeleteStudent;
import hardWork.lesson04_3.command.ListOfStudents;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class DataBaseApp {
    private final Map<Integer, Command> commands = new HashMap<>();

    DataBaseApp(DataBase dataBase) {
        commands.put(1, new AddStudent(dataBase));
        commands.put(2, new DeleteStudent(dataBase));
        commands.put(3, new ListOfStudents(dataBase));
    }

    private void getCommands() {
        for (Map.Entry<Integer, Command> command : commands.entrySet()) {
            System.out.println(command.getKey() + " - " + command.getValue().getNameOfCommand());
        }
    }

    private void commandAction(Integer numberOfCommand) {
        commands.get(numberOfCommand).action();
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("List of commands: ");
            getCommands();
            System.out.println("Enter command number: ");
            commandAction(scanner.nextInt());
        }
    }
}
