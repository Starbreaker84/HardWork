public class AppRunner {
    public static void main(String[] args) {
        DataBaseApp dataBaseApp = new DataBaseApp(new DataBase());
        dataBaseApp.run();
    }
}
