package com.ecom.models;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * NoSQL document model for application logs.
 * Logs are unstructured by nature and benefit from NoSQL's schema flexibility.
 */
public class LogDocument {
    private String id;
    private String level; // INFO, WARN, ERROR, DEBUG
    private String message;
    private String source; // class/method that generated the log
    private LocalDateTime timestamp;
    private Map<String, Object> context; // Additional context data
    private String userId; // Optional: user who triggered the action
    private String action; // Optional: action that was performed
    private Map<String, Object> metadata; // Additional flexible fields
    
    public LogDocument() {
        this.context = new HashMap<>();
        this.metadata = new HashMap<>();
        this.timestamp = LocalDateTime.now();
    }

    public LogDocument(String level, String message, String source) {
        this();
        this.level = level;
        this.message = message;
        this.source = source;
    }

    /**
     * Converts this LogDocument to a MongoDB Document.
     */
    public Document toDocument() {
        Document doc = new Document();
        if (id != null && !id.isEmpty()) {
            doc.append("_id", new ObjectId(id));
        }
        doc.append("level", level)
           .append("message", message)
           .append("source", source)
           .append("timestamp", Date.from(timestamp.atZone(ZoneId.systemDefault()).toInstant()))
           .append("context", new Document(context))
           .append("metadata", new Document(metadata));
        
        if (userId != null) {
            doc.append("userId", userId);
        }
        if (action != null) {
            doc.append("action", action);
        }
        
        return doc;
    }

    /**
     * Creates a LogDocument from a MongoDB Document.
     */
    public static LogDocument fromDocument(Document doc) {
        LogDocument log = new LogDocument();
        if (doc.containsKey("_id")) {
            log.id = doc.getObjectId("_id").toString();
        }
        log.level = doc.getString("level");
        log.message = doc.getString("message");
        log.source = doc.getString("source");
        
        if (doc.containsKey("timestamp")) {
            Date timestamp = doc.getDate("timestamp");
            log.timestamp = LocalDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault());
        }
        
        if (doc.containsKey("context")) {
            Document contextDoc = doc.get("context", Document.class);
            if (contextDoc != null) {
                log.context.putAll(contextDoc);
            }
        }
        
        if (doc.containsKey("metadata")) {
            Document metadataDoc = doc.get("metadata", Document.class);
            if (metadataDoc != null) {
                log.metadata.putAll(metadataDoc);
            }
        }
        
        if (doc.containsKey("userId")) {
            log.userId = doc.getString("userId");
        }
        if (doc.containsKey("action")) {
            log.action = doc.getString("action");
        }
        
        return log;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Map<String, Object> getContext() { return context; }
    public void setContext(Map<String, Object> context) { this.context = context; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}
