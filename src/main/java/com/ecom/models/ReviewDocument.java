package com.ecom.models;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.Arrays;

/**
 * NoSQL document model for reviews.
 * This model supports unstructured data like tags, images, helpful votes, etc.
 * that are better suited for NoSQL storage.
 */
public class ReviewDocument {
    private String id;
    private int userId;
    private int productId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Unstructured fields that benefit from NoSQL flexibility
    private Map<String, Object> metadata; // tags, verified purchase, etc.
    private Map<String, Integer> helpfulVotes; // user_id -> vote (1 for helpful, -1 for not helpful)
    private String[] images; // URLs to review images
    private Map<String, String> customFields; // Additional flexible fields
    
    public ReviewDocument() {
        this.metadata = new HashMap<>();
        this.helpfulVotes = new HashMap<>();
        this.images = new String[0];
        this.customFields = new HashMap<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public ReviewDocument(int userId, int productId, int rating, String comment) {
        this();
        this.userId = userId;
        this.productId = productId;
        this.rating = rating;
        this.comment = comment;
    }

    /**
     * Converts this ReviewDocument to a MongoDB Document.
     */
    public Document toDocument() {
        Document doc = new Document();
        if (id != null && !id.isEmpty()) {
            // store as ObjectId if valid, otherwise store raw string
            if (ObjectId.isValid(id)) {
                doc.append("_id", new ObjectId(id));
            } else {
                doc.append("_id", id);
            }
        }
        doc.append("userId", userId)
           .append("productId", productId)
           .append("rating", rating)
           .append("comment", comment);

        if (createdAt != null) {
            doc.append("createdAt", Date.from(createdAt.atZone(ZoneId.systemDefault()).toInstant()));
        }
        if (updatedAt != null) {
            doc.append("updatedAt", Date.from(updatedAt.atZone(ZoneId.systemDefault()).toInstant()));
        }

        doc.append("metadata", new Document(metadata))
           .append("helpfulVotes", new Document(helpfulVotes))
           .append("images", images != null ? Arrays.asList(images) : Collections.emptyList())
           .append("customFields", new Document(customFields));
        return doc;
    }

    /**
     * Creates a ReviewDocument from a MongoDB Document.
     */
    public static ReviewDocument fromDocument(Document doc) {
        ReviewDocument review = new ReviewDocument();
        if (doc.containsKey("_id")) {
            Object idObj = doc.get("_id");
            if (idObj instanceof ObjectId) {
                review.id = ((ObjectId) idObj).toHexString();
            } else if (idObj != null) {
                review.id = idObj.toString();
            }
        }

        Integer u = doc.get("userId", Integer.class);
        review.userId = (u != null) ? u : 0;
        Integer p = doc.get("productId", Integer.class);
        review.productId = (p != null) ? p : 0;
        Integer r = doc.get("rating", Integer.class);
        review.rating = (r != null) ? r : 0;
        review.comment = doc.getString("comment");
        
        Date createdAtDate = doc.getDate("createdAt");
        if (createdAtDate != null) {
            review.createdAt = LocalDateTime.ofInstant(createdAtDate.toInstant(), ZoneId.systemDefault());
        }
        Date updatedAtDate = doc.getDate("updatedAt");
        if (updatedAtDate != null) {
            review.updatedAt = LocalDateTime.ofInstant(updatedAtDate.toInstant(), ZoneId.systemDefault());
        }
        
        if (doc.containsKey("metadata")) {
            Document metadataDoc = doc.get("metadata", Document.class);
            if (metadataDoc != null) {
                review.metadata.putAll(metadataDoc);
            }
        }
        
        if (doc.containsKey("helpfulVotes")) {
            Document votesDoc = doc.get("helpfulVotes", Document.class);
            if (votesDoc != null) {
                votesDoc.forEach((key, value) -> {
                    if (value == null) return;
                    try {
                        int intVal;
                        if (value instanceof Number) {
                            intVal = ((Number) value).intValue();
                        } else {
                            intVal = Integer.parseInt(String.valueOf(value));
                        }
                        review.helpfulVotes.put(key, intVal);
                    } catch (NumberFormatException ex) {
                        // skip invalid numeric value
                    }
                });
            }
        }
        
        if (doc.containsKey("images")) {
            java.util.List<String> imagesList = doc.getList("images", String.class);
            if (imagesList != null) {
                review.images = imagesList.toArray(new String[0]);
            }
        }
        
        if (doc.containsKey("customFields")) {
            Document customDoc = doc.get("customFields", Document.class);
            if (customDoc != null) {
                // Document is a Map<String,Object>, but customFields expects String values
                customDoc.forEach((k, v) -> {
                    if (v != null) {
                        review.customFields.put(k, String.valueOf(v));
                    }
                });
            }
        }
        
        return review;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = (metadata != null) ? new HashMap<>(metadata) : new HashMap<>(); }

    public Map<String, Integer> getHelpfulVotes() { return new HashMap<>(helpfulVotes); }
    public void setHelpfulVotes(Map<String, Integer> helpfulVotes) { this.helpfulVotes = (helpfulVotes != null) ? new HashMap<>(helpfulVotes) : new HashMap<>(); }

    public String[] getImages() { return images == null ? new String[0] : images.clone(); }
    public void setImages(String[] images) { this.images = (images != null) ? images.clone() : new String[0]; }

    public Map<String, String> getCustomFields() { return new HashMap<>(customFields); }
    public void setCustomFields(Map<String, String> customFields) { this.customFields = (customFields != null) ? new HashMap<>(customFields) : new HashMap<>(); }

    /**
     * Adds a helpful vote from a user.
     */
    public void addHelpfulVote(int userId, boolean helpful) {
        helpfulVotes.put(String.valueOf(userId), helpful ? 1 : -1);
    }
    
    /**
     * Gets the total helpful votes count.
     */
    public int getTotalHelpfulVotes() {
        return helpfulVotes.values().stream()
            .mapToInt(vote -> vote > 0 ? 1 : 0)
            .sum();
    }
}
