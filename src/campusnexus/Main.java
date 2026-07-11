package campusnexus;

import campusnexus.ui.MainMenu;
import campusnexus.util.ReminderScheduler;

public class Main {
    public static void main(String[] args) {
        ReminderScheduler reminderScheduler = new ReminderScheduler();
        reminderScheduler.start();

        try {
            new MainMenu().start();
        } finally {
            reminderScheduler.stop();
        }
    }
}
