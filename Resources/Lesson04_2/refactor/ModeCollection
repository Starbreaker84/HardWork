public class ModeCollection {
    Deque<Mode> list = new LinkedList<>();

    ModeCollection() {
        list.add(new ClockModeImpl());
        list.add(new CalendarModeImpl());
    }

    public Mode getMode() {
        Mode currentMode = list.removeFirst();
        list.add(currentMode);
        return currentMode;
    }
}
