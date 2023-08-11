public class Watch {
    ModeCollection modes;
    Mode currentMode;

    Watch(ModeCollection modes) {
        this.modes = modes;
        this.currentMode = modes.getMode();
    }

    public void switchMode() {
        currentMode = modes.getMode();
    }

    public void pushButtonA(Mode mode) {
        mode.methodA();
    }

    public void pushButtonB(Mode mode) {
        mode.methodB();
    }
}
