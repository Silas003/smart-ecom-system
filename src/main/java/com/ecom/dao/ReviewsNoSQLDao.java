package com.ecom.dao;

import com.ecom.models.ReviewDocument;
import com.ecom.utils.MongoDBUtils;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * NoSQL DAO for reviews using MongoDB.
 * This implementation provides flexibility for unstructured review data
 * such as tags, images, helpful votes, and custom metadata.
 */
public class ReviewsNoSQLDao {
    private static final Logger logger = LoggerFactory.getLogger(ReviewsNoSQLDao.class);
    private static final String COLLECTION_NAME = "reviews";
    
    private MongoCollection<Document> getCollection() {
        if (!MongoDBUtils.isAvailable()) {
            throw new RuntimeException("MongoDB is not available. Please ensure MongoDB is running.");
        }
        MongoDatabase database = MongoDBUtils.getDatabase();
        return database.getCollection(COLLECTION_NAME);
    }

    /**
     * Creates a new review document in MongoDB.
     */
    public String create(ReviewDocument review) {
        try {
            MongoCollection<Document> collection = getCollection();
            Document doc = review.toDocument();
            collection.insertOne(doc);
            String id = doc.getObjectId("_id").toString();
            review.setId(id);
            logger.info("Review created in MongoDB with ID: {}", id);
            return id;
        } catch (Exception e) {
            logger.error("Failed to create review in MongoDB", e);
            throw new RuntimeException("Failed to create review: " + e.getMessage(), e);
        }
    }

    /**
     * Finds a review by its ID.
     */
    public ReviewDocument findById(String id) {
        try {
            MongoCollection<Document> collection = getCollection();
            Document doc = collection.find(Filters.eq("_id", new ObjectId(id))).first();
            if (doc != null) {
                return ReviewDocument.fromDocument(doc);
            }
            return null;
        } catch (Exception e) {
            logger.error("Failed to find review by ID: {}", id, e);
            throw new RuntimeException("Failed to find review: " + e.getMessage(), e);
        }
    }

    /**
     * Finds all reviews for a specific product.
     */
    public List<ReviewDocument> findByProductId(int productId) {
        try {
            MongoCollection<Document> collection = getCollection();
            List<ReviewDocument> reviews = new ArrayList<>();
            collection.find(Filters.eq("productId", productId))
                     .sort(Sorts.descending("createdAt"))
                     .forEach(doc -> reviews.add(ReviewDocument.fromDocument(doc)));
            return reviews;
        } catch (Exception e) {
            logger.error("Failed to find reviews for product: {}", productId, e);
            throw new RuntimeException("Failed to find reviews: " + e.getMessage(), e);
        }
    }

    /**
     * Finds all reviews by a specific user.
     */
    public List<ReviewDocument> findByUserId(int userId) {
        try {
            MongoCollection<Document> collection = getCollection();
            List<ReviewDocument> reviews = new ArrayList<>();
            collection.find(Filters.eq("userId", userId))
                     .sort(Sorts.descending("createdAt"))
                     .forEach(doc -> reviews.add(ReviewDocument.fromDocument(doc)));
            return reviews;
        } catch (Exception e) {
            logger.error("Failed to find reviews for user: {}", userId, e);
            throw new RuntimeException("Failed to find reviews: " + e.getMessage(), e);
        }
    }

    /**
     * Finds all reviews.
     */
    public List<ReviewDocument> findAll() {
        try {
            MongoCollection<Document> collection = getCollection();
            List<ReviewDocument> reviews = new ArrayList<>();
            collection.find()
                     .sort(Sorts.descending("createdAt"))
                     .forEach(doc -> reviews.add(ReviewDocument.fromDocument(doc)));
            return reviews;
        } catch (Exception e) {
            logger.error("Failed to find all reviews", e);
            throw new RuntimeException("Failed to find reviews: " + e.getMessage(), e);
        }
    }

    /**
     * Updates a review document.
     */
    public void update(ReviewDocument review) {
        try {
            MongoCollection<Document> collection = getCollection();
            review.setUpdatedAt(java.time.LocalDateTime.now());
            Document doc = review.toDocument();
            collection.replaceOne(
                Filters.eq("_id", new ObjectId(review.getId())),
                doc
            );
            logger.info("Review updated in MongoDB: {}", review.getId());
        } catch (Exception e) {
            logger.error("Failed to update review: {}", review.getId(), e);
            throw new RuntimeException("Failed to update review: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a review by ID.
     */
    public void delete(String id) {
        try {
            MongoCollection<Document> collection = getCollection();
            collection.deleteOne(Filters.eq("_id", new ObjectId(id)));
            logger.info("Review deleted from MongoDB: {}", id);
        } catch (Exception e) {
            logger.error("Failed to delete review: {}", id, e);
            throw new RuntimeException("Failed to delete review: " + e.getMessage(), e);
        }
    }

    /**
     * Finds reviews by rating range.
     */
    public List<ReviewDocument> findByRatingRange(int minRating, int maxRating) {
        try {
            MongoCollection<Document> collection = getCollection();
            List<ReviewDocument> reviews = new ArrayList<>();
            collection.find(Filters.and(
                Filters.gte("rating", minRating),
                Filters.lte("rating", maxRating)
            )).sort(Sorts.descending("createdAt"))
              .forEach(doc -> reviews.add(ReviewDocument.fromDocument(doc)));
            return reviews;
        } catch (Exception e) {
            logger.error("Failed to find reviews by rating range", e);
            throw new RuntimeException("Failed to find reviews: " + e.getMessage(), e);
        }
    }

    /**
     * Finds reviews with images.
     */
    public List<ReviewDocument> findReviewsWithImages() {
        try {
            MongoCollection<Document> collection = getCollection();
            List<ReviewDocument> reviews = new ArrayList<>();
            // Find documents where images array is not empty
            collection.find(Filters.exists("images", true))
                     .forEach(doc -> {
                         ReviewDocument review = ReviewDocument.fromDocument(doc);
                         if (review.getImages() != null && review.getImages().length > 0) {
                             reviews.add(review);
                         }
                     });
            return reviews;
        } catch (Exception e) {
            logger.error("Failed to find reviews with images", e);
            throw new RuntimeException("Failed to find reviews: " + e.getMessage(), e);
        }
    }

    /**
     * Gets average rating for a product.
     */
    public double getAverageRating(int productId) {
        try {
            MongoCollection<Document> collection = getCollection();
            List<ReviewDocument> reviews = findByProductId(productId);
            if (reviews.isEmpty()) {
                return 0.0;
            }
            return reviews.stream()
                .mapToInt(ReviewDocument::getRating)
                .average()
                .orElse(0.0);
        } catch (Exception e) {
            logger.error("Failed to calculate average rating for product: {}", productId, e);
            return 0.0;
        }
    }
}
