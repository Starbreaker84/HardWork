public enum PlayerSymbol {
    X("X"),
    O("O"),
    BLANK(" ");

    private final String value;

    PlayerSymbol(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
