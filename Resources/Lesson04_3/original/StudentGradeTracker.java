import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class StudentGradeTracker {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<Student> students = new ArrayList<>();

        System.out.println("Welcome to Student Grade Tracker!");

        // Main menu loop
        boolean exit = false;
        while (!exit) {
            System.out.println("\nSelect an option:");
            System.out.println("a. Add a new student");
            System.out.println("b. Delete a student");
            System.out.println("c. Update a student's grade");
            System.out.println("d. View grades of all students");
            System.out.println("e. View grades of a particular student");
            System.out.println("x. Exit");

            char choice = scanner.next().charAt(0);
            scanner.nextLine(); // Consume the newline character

            switch (choice) {
                case 'a':
                    addStudent(scanner, students);
                    break;
                case 'b':
                    deleteStudent(scanner, students);
                    break;
                case 'c':
                    updateGrade(scanner, students);
                    break;
                case 'd':
                    viewAllGrades(students);
                    break;
                case 'e':
                    viewStudentGrades(scanner, students);
                    break;
                case 'x':
                    exit = true;
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid choice. Please select a valid option.");
            }
        }
    }

    // Helper class to represent a student
    static class Student {
        String name;
        List<Integer> grades;

        public Student(String name) {
            this.name = name;
            this.grades = new ArrayList<>();
        }
    }

    // Method to add a new student
    static void addStudent(Scanner scanner, List<Student> students) {
        System.out.println("Enter student's name:");
        String name = scanner.nextLine();
        students.add(new Student(name));
        System.out.println("Student " + name + " added successfully.");
    }

    // Method to delete a student
    static void deleteStudent(Scanner scanner, List<Student> students) {
        System.out.println("Enter the index of the student to delete:");
        try {
            int index = scanner.nextInt();
            if (index >= 0 && index < students.size()) {
                String name = students.get(index).name;
                students.remove(index);
                System.out.println("Student " + name + " deleted successfully.");
            } else {
                System.out.println("Invalid index.");
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid index.");
        } finally {
            scanner.nextLine(); // Consume the newline character
        }
    }

    // Method to update a student's grade
    static void updateGrade(Scanner scanner, List<Student> students) {
        System.out.println("Enter the index of the student to update:");
        try {
            int index = scanner.nextInt();
            if (index >= 0 && index < students.size()) {
                Student student = students.get(index);
                System.out.println("Enter the new grade:");
                int grade = scanner.nextInt();
                student.grades.add(grade);
                System.out.println("Grade added for student " + student.name + ".");
            } else {
                System.out.println("Invalid index.");
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid index and grade.");
        } finally {
            scanner.nextLine(); // Consume the newline character
        }
    }

    // Method to view grades of all students
    static void viewAllGrades(List<Student> students) {
        if (students.isEmpty()) {
            System.out.println("No students to display.");
        } else {
            System.out.println("Student Grades:");
            for (int i = 0; i < students.size(); i++) {
                Student student = students.get(i);
                System.out.print(i + ". " + student.name + ": ");
                for (int grade : student.grades) {
                    System.out.print(grade + " ");
                }
                System.out.println();
            }
        }
    }

    // Method to view grades of a particular student
    static void viewStudentGrades(Scanner scanner, List<Student> students) {
        System.out.println("Enter the index of the student:");
        try {
            int index = scanner.nextInt();
            if (index >= 0 && index < students.size()) {
                Student student = students.get(index);
                System.out.println("Grades for student " + student.name + ": " + student.grades);
            } else {
                System.out.println("Invalid index.");
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a valid index.");
        } finally {
            scanner.nextLine(); // Consume the newline character
        }
    }
}
