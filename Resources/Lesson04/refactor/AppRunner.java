public class AppRunner {
    public static void main(String[] args) {
        GameLogic gameLogic = new VersusPCGameLogicImpl();
        gameLogic.play();
    }
}
