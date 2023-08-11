public class DataBase {
    Map<String, Student> dataBase;

    DataBase() {
        this.dataBase = new HashMap<String, Student>();
    }

    DataBase(Map<String, Student> dataBase) {
        this.dataBase = dataBase;
    }

    public void addStudent(String name) {
        if (dataBase.containsKey(name))
            System.out.println("Student with name " + name + " is already exist.");
        else {
            dataBase.put(name, new Student(name));
            System.out.println("Student with name " + name + " was successfully added.");
        }
    }

    public void deleteStudent(String name) {
        if (dataBase.containsKey(name)) {
            dataBase.remove(name);
            System.out.println("Student with name " + name + " was deleted.");
        }
        else
            System.out.println("Student with name " + name + " is not exist.");
    }

    public List<Student> getListOfStudents() {
        return dataBase.values().stream().toList();
    }
}
