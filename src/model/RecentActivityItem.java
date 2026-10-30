package model;

/**
 * Data model representing a recent activity or notice item on the FAMS Admin Dashboard.
 */
public class RecentActivityItem {
    private String id;
    private String title;
    private String description;
    private String timestamp;
    private String category;
    private String status;

    public RecentActivityItem() {
    }

    public RecentActivityItem(String id, String title, String description, String timestamp, String category, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.timestamp = timestamp;
        this.category = category;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "RecentActivityItem{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", category='" + category + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
