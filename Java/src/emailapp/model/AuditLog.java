package emailapp.model;

import java.time.LocalDateTime;

public class AuditLog {
    private int id;
    private int userId;
    private String action;
    private String entityType;
    private int entityId;
    private String details;
    private LocalDateTime createdAt;

    public AuditLog(int id, int userId, String action, String entityType, int entityId, String details, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
        this.createdAt = createdAt;
    }

    public AuditLog(int userId, String action, String entityType, int entityId, String details) {
        this(-1, userId, action, entityType, entityId, details, LocalDateTime.now());
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getAction() { return action; }
    public String getEntityType() { return entityType; }
    public int getEntityId() { return entityId; }
    public String getDetails() { return details; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(int id) { this.id = id; }

    @Override
    public String toString() {
        return createdAt + " | " + action + " on " + entityType + " #" + entityId + " by user #" + userId;
    }
}
