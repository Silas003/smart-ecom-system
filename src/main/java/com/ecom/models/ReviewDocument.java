package com.ecom.models;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
            doc.append("_id", new ObjectId(id));
        }
        doc.append("userId", userId)
           .append("productId", productId)
           .append("rating", rating)
           .append("comment", comment)
           .append("createdAt", Date.from(createdAt.atZone(ZoneId.systemDefault()).toInstant()))
           .append("updatedAt", Date.from(updatedAt.atZone(ZoneId.systemDefault()).toInstant()))
           .append("metadata", new Document(metadata))
           .append("helpfulVotes", new Document(helpfulVotes))
           .append("images", java.util.Arrays.asList(images))
           .append("customFields", new Document(customFields));
        return doc;
    }

    /**
     * Creates a ReviewDocument from a MongoDB Document.
     */
    public static ReviewDocument fromDocument(Document doc) {
        ReviewDocument review = new ReviewDocument();
        if (doc.containsKey("_id")) {
            review.id = doc.getObjectId("_id").toString();
        }
        review.userId = doc.getInteger("userId");
        review.productId = doc.getInteger("productId");
        review.rating = doc.getInteger("rating");
        review.comment = doc.getString("comment");
        
        if (doc.containsKey("createdAt")) {
            Date createdAt = doc.getDate("createdAt");
            review.createdAt = LocalDateTime.ofInstant(createdAt.toInstant(), ZoneId.systemDefault());
        }
        if (doc.containsKey("updatedAt")) {
            Date updatedAt = doc.getDate("updatedAt");
            review.updatedAt = LocalDateTime.ofInstant(updatedAt.toInstant(), ZoneId.systemDefault());
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
                    review.helpfulVotes.put(key, (Integer) value);
                });
            }
        }
        
        if (doc.containsKey("images")) {
            java.util.List<String> imagesList = doc.getList("images", String.class);
            review.images = imagesList.toArray(new String[0]);
        }
        
        if (doc.containsKey("customFields")) {
            Document customDoc = doc.get("customFields", Document.class);
            if (customDoc != null) {
                review.customFields.putAll(customDoc);
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

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public Map<String, Integer> getHelpfulVotes() { return helpfulVotes; }
    public void setHelpfulVotes(Map<String, Integer> helpfulVotes) { this.helpfulVotes = helpfulVotes; }

    public String[] getImages() { return images; }
    public void setImages(String[] images) { this.images = images; }

    public Map<String, String> getCustomFields() { return customFields; }
    public void setCustomFields(Map<String, String> customFields) { this.customFields = customFields; }
    
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
