package campusnexus.model;

import java.time.LocalDate;

public class Event {
    private final int id;
    private final String title;
    private final LocalDate eventDate;
    private final String venue;
    private final int createdBy;
    private final String description;

    public Event(int id, String title, LocalDate eventDate, String venue, int createdBy, String description) {
        this.id = id;
        this.title = title;
        this.eventDate = eventDate;
        this.venue = venue;
        this.createdBy = createdBy;
        this.description = description;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public LocalDate getEventDate() { return eventDate; }
    public String getVenue() { return venue; }
    public int getCreatedBy() { return createdBy; }
    public String getDescription() { return description; }
}
