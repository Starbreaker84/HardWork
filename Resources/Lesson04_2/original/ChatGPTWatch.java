public class ChatGPTWatch {
    private boolean calendarMode;
    private boolean stopwatchMode;
    private boolean functionAButton;
    private boolean functionBButton;

    public ChatGPTWatch() {
        calendarMode = true; // Default mode is calendar
        stopwatchMode = false;
        functionAButton = false;
        functionBButton = false;
    }

    public void switchToCalendarMode() {
        calendarMode = true;
        stopwatchMode = false;
        System.out.println("Switched to Calendar mode");
    }

    public void switchToStopwatchMode() {
        calendarMode = false;
        stopwatchMode = true;
        System.out.println("Switched to Stopwatch mode");
    }

    public void pressFunctionAButton() {
        if (calendarMode) {
            System.out.println("Function A button is " + (functionAButton ? "pressed" : "released"));
        } else {
            System.out.println("Function A button has no effect in Stopwatch mode");
        }
    }

    public void pressFunctionBButton() {
        if (calendarMode) {
            System.out.println("Function B button is " + (functionBButton ? "pressed" : "released"));
        } else {
            System.out.println("Function B button has no effect in Stopwatch mode");
        }
    }
}
