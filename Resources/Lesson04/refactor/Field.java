import java.util.Arrays;

public class Field {

    private final PlayerSymbol[] storage;

    public Field() {
        storage = new PlayerSymbol[9];
        Arrays.fill(storage, PlayerSymbol.BLANK);
    }

    public Field(PlayerSymbol[] storage) {
        this.storage = storage;
    }

    public PlayerSymbol[] getStorage() {
        return storage;
    }

    public void printBoard() {
        for (int i = 0; i < storage.length; i++) {
            if(i % 3 == 0 && i != 0) {
                System.out.println();
            }

            System.out.print((storage[i] == PlayerSymbol.BLANK ? i + 1 : storage[i].getValue()) + " ");

        }
        System.out.println();
    }
}
