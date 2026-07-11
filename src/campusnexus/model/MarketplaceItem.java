package campusnexus.model;

import java.time.LocalDateTime;

public class MarketplaceItem {
    private final int id;
    private final int sellerId;
    private final String sellerName;
    private final String title;
    private final String description;
    private final double price;
    private final String status;
    private final LocalDateTime createdAt;

    public MarketplaceItem(int id, int sellerId, String sellerName, String title, String description,
                           double price, String status, LocalDateTime createdAt) {
        this.id = id;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.title = title;
        this.description = description;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public int getSellerId() { return sellerId; }
    public String getSellerName() { return sellerName; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
