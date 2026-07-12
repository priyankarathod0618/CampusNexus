package campusnexus.model;

import java.time.LocalDate;

public class Event {
    private final int id;
    private final String title;
    private final LocalDate eventDate;
    private final String venue;
    private final int createdBy;
    private final String description;
    private final int capacity;
    private final int registeredCount;

    public Event(int id, String title, LocalDate eventDate, String venue, int createdBy, String description,
                 int capacity, int registeredCount) {
        this.id = id;
        this.title = title;
        this.eventDate = eventDate;
        this.venue = venue;
        this.createdBy = createdBy;
        this.description = description;
        this.capacity = capacity;
        this.registeredCount = registeredCount;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public LocalDate getEventDate() { return eventDate; }
    public String getVenue() { return venue; }
    public int getCreatedBy() { return createdBy; }
    public String getDescription() { return description; }
    public int getCapacity() { return capacity; }
    public int getRegisteredCount() { return registeredCount; }
    public boolean isFull() { return registeredCount >= capacity; }
}
