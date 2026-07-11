package campusnexus.ui;

/**
 * Common interface for every menu screen that can be "entered".
 * MainMenu routes to StudentMenu / TeacherMenu / AdminMenu / VisitorMenu
 * purely through this reference type - the actual class used is decided
 * at runtime (Java syllabus topic 1: runtime polymorphism).
 */
public interface DashboardMenu {
    void show();
}
