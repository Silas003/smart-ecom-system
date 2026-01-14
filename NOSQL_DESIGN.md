# NoSQL Design for Reviews and Logs

## Executive Summary

This document describes the NoSQL implementation for storing customer reviews and application logs in the Smart E-Commerce System. MongoDB was chosen as the NoSQL database to handle unstructured and semi-structured data that doesn't fit well into a rigid relational schema.

---

## Why NoSQL for Reviews and Logs?

### 1. **Schema Flexibility**

**Problem with Relational Database:**
- Reviews have varying structures: some have images, some have tags, some have helpful votes
- Adding new review features requires schema migrations (ALTER TABLE statements)
- Logs have completely different structures depending on the event type
- Different log entries may have different fields

**NoSQL Solution:**
- MongoDB's document model allows each review/log to have different fields
- No schema migrations needed when adding new features
- Easy to store nested data (arrays, objects) without normalization

### 2. **Unstructured Data Nature**

**Reviews:**
- User-generated content is inherently unstructured
- Different products may need different review metadata
- Images, tags, helpful votes vary per review
- Custom fields for different product categories

**Logs:**
- Application logs have varying structures based on event type
- Different actions generate different log fields
- Context data varies significantly
- Metadata is dynamic and unpredictable

### 3. **Performance Benefits**

**Read Performance:**
- No JOINs needed - all data in one document
- Faster retrieval of complete review with all metadata
- Better for time-series queries (logs)

**Write Performance:**
- No foreign key constraints to check
- Faster inserts for high-volume logging
- No transaction overhead for simple writes

### 4. **Scalability**

- Horizontal scaling (sharding) is easier with NoSQL
- Better for high-volume write scenarios (logging)
- Can handle millions of documents efficiently

---

## Data Models

### Review Document Structure

```json
{
  "_id": ObjectId("..."),
  "userId": 123,
  "productId": 456,
  "rating": 5,
  "comment": "Great product!",
  "createdAt": ISODate("2024-01-15T10:30:00Z"),
  "updatedAt": ISODate("2024-01-15T10:30:00Z"),
  "metadata": {
    "verifiedPurchase": true,
    "reviewerType": "verified_buyer",
    "helpfulCount": 42
  },
  "helpfulVotes": {
    "101": 1,
    "102": 1,
    "103": -1
  },
  "images": [
    "https://example.com/image1.jpg",
    "https://example.com/image2.jpg"
  ],
  "customFields": {
    "size": "Large",
    "color": "Blue",
    "usageDuration": "6 months"
  }
}
```

**Key Features:**
- Flexible metadata object for any additional data
- Array of images (variable length)
- Map of helpful votes (user_id -> vote)
- Custom fields for product-specific attributes

### Log Document Structure

```json
{
  "_id": ObjectId("..."),
  "level": "ERROR",
  "message": "Failed to process order",
  "source": "com.ecom.services.OrderService.processOrder",
  "timestamp": ISODate("2024-01-15T10:30:00Z"),
  "userId": "123",
  "action": "place_order",
  "context": {
    "orderId": 789,
    "errorCode": "INSUFFICIENT_STOCK",
    "productId": 456
  },
  "metadata": {
    "ipAddress": "192.168.1.1",
    "userAgent": "Mozilla/5.0...",
    "sessionId": "abc123"
  }
}
```

**Key Features:**
- Dynamic context object for event-specific data
- Flexible metadata for additional information
- Time-series optimized for log queries
- No fixed schema - each log can have different fields

---

## Implementation Details

### MongoDB Connection

- **Connection String:** `mongodb://localhost:27017` (configurable via `MONGO_URL` env var)
- **Database Name:** `smartEcom` (configurable via `MONGO_DB_NAME` env var)
- **Collections:**
  - `reviews` - Customer review documents
  - `logs` - Application log entries

### Indexes

**Reviews Collection:**
```javascript
// Index on productId for fast product review queries
db.reviews.createIndex({ "productId": 1 })

// Index on userId for user review queries
db.reviews.createIndex({ "userId": 1 })

// Compound index for rating queries
db.reviews.createIndex({ "productId": 1, "rating": -1 })

// Text index for comment search
db.reviews.createIndex({ "comment": "text" })
```

**Logs Collection:**
```javascript
// Index on timestamp for time-range queries
db.logs.createIndex({ "timestamp": -1 })

// Index on level for filtering by log level
db.logs.createIndex({ "level": 1 })

// Compound index for user action queries
db.logs.createIndex({ "userId": 1, "timestamp": -1 })

// TTL index to auto-delete old logs (optional)
db.logs.createIndex({ "timestamp": 1 }, { expireAfterSeconds: 2592000 }) // 30 days
```

---

## Use Cases

### Reviews Use Cases

1. **Product Reviews with Images**
   - Store multiple images per review
   - No need for separate image table
   - Easy to retrieve all review data in one query

2. **Helpful Votes**
   - Store user votes as a map (user_id -> vote)
   - Easy to update without complex queries
   - Can track who voted and how

3. **Custom Product Attributes**
   - Different products need different review fields
   - Electronics: "battery life", "screen quality"
   - Clothing: "fit", "material", "size"
   - All stored in flexible `customFields` object

4. **Review Metadata**
   - Verified purchase status
   - Reviewer type (verified buyer, expert, etc.)
   - Review helpfulness score
   - All stored without schema changes

### Logs Use Cases

1. **Application Error Logging**
   - Store error context dynamically
   - Different errors have different context fields
   - Easy to query by error type, user, or time

2. **User Activity Tracking**
   - Track user actions with varying metadata
   - Login events, purchase events, search events
   - Each event type has different fields

3. **Performance Monitoring**
   - Log query execution times
   - Store performance metrics
   - Time-series queries for analysis

4. **Audit Trail**
   - Track all system changes
   - Store before/after values
   - Flexible structure for different change types

---

## Comparison: SQL vs NoSQL

### Reviews Storage

| Aspect | SQL (PostgreSQL) | NoSQL (MongoDB) |
|--------|------------------|-----------------|
| **Schema** | Fixed schema, requires migrations | Flexible schema, no migrations |
| **Images** | Separate table with JOINs | Array in same document |
| **Helpful Votes** | Separate table with JOINs | Map in same document |
| **Custom Fields** | Requires schema changes | Dynamic fields, no changes needed |
| **Query Complexity** | Multiple JOINs for complete data | Single document query |
| **Write Performance** | Multiple table inserts | Single document insert |
| **Scalability** | Vertical scaling | Horizontal scaling (sharding) |

### Logs Storage

| Aspect | SQL (PostgreSQL) | NoSQL (MongoDB) |
|--------|------------------|-----------------|
| **Schema** | Fixed columns | Dynamic fields per log type |
| **High Volume** | Slower inserts, table locks | Fast inserts, no locks |
| **Time Queries** | Index on timestamp | Optimized time-series queries |
| **Context Data** | Separate table or JSON column | Native nested objects |
| **Retention** | Manual cleanup required | TTL indexes for auto-cleanup |
| **Query Performance** | Complex WHERE clauses | Simple document queries |

---

## Migration Strategy

### Hybrid Approach

The system uses a **hybrid approach**:
- **Relational Database (PostgreSQL):** Core business data (users, products, orders)
- **NoSQL Database (MongoDB):** Unstructured data (reviews, logs)

### Benefits of Hybrid Approach

1. **Best of Both Worlds**
   - ACID transactions for critical data (orders, payments)
   - Flexibility for unstructured data (reviews, logs)

2. **Performance Optimization**
   - Fast relational queries for structured data
   - Fast document queries for unstructured data

3. **Scalability**
   - Scale each database independently
   - Use appropriate tool for each use case

---

## Code Examples

### Creating a Review with NoSQL

```java
ReviewDocument review = new ReviewDocument(userId, productId, rating, comment);
review.getMetadata().put("verifiedPurchase", true);
review.getMetadata().put("reviewerType", "verified_buyer");
review.setImages(new String[]{"image1.jpg", "image2.jpg"});
review.addHelpfulVote(101, true);
review.getCustomFields().put("size", "Large");

ReviewsNoSQLDao dao = new ReviewsNoSQLDao();
String reviewId = dao.create(review);
```

### Creating a Log Entry

```java
LogsNoSQLDao logsDao = new LogsNoSQLDao();
logsDao.log("ERROR", "Failed to process order", 
             "OrderService.processOrder", 
             "123", 
             "place_order");
```

### Querying Reviews

```java
// Get all reviews for a product
List<ReviewDocument> reviews = reviewsDao.findByProductId(productId);

// Get reviews with images
List<ReviewDocument> reviewsWithImages = reviewsDao.findReviewsWithImages();

// Get average rating
double avgRating = reviewsDao.getAverageRating(productId);
```

---

## Performance Considerations

### Indexing Strategy

- Index frequently queried fields (productId, userId, timestamp)
- Use compound indexes for multi-field queries
- Text indexes for full-text search on comments

### Data Retention

- **Reviews:** Keep indefinitely (valuable user-generated content)
- **Logs:** Auto-delete after 30 days using TTL index (configurable)

### Query Optimization

- Use projection to limit returned fields
- Limit result sets for large collections
- Use aggregation pipeline for complex queries

---

## Conclusion

NoSQL (MongoDB) is the right choice for reviews and logs because:

1. **Flexibility:** Schema can evolve without migrations
2. **Performance:** Faster writes and reads for unstructured data
3. **Scalability:** Better horizontal scaling for high-volume data
4. **Simplicity:** No complex JOINs, simpler queries
5. **Natural Fit:** Document model matches the data structure

The hybrid approach (PostgreSQL + MongoDB) provides:
- **Reliability** for critical business data
- **Flexibility** for unstructured content
- **Performance** optimization for each use case
- **Scalability** for future growth

---

## References

- MongoDB Documentation: https://docs.mongodb.com/
- MongoDB Java Driver: https://mongodb.github.io/mongo-java-driver/
- NoSQL vs SQL Comparison: Industry best practices

---

**Document Version:** 1.0  
**Last Updated:** 2024-01-15  
**Author:** Smart E-Commerce System Development Team
