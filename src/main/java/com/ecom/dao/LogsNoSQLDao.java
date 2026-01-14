package com.ecom.dao;

import com.ecom.models.LogDocument;
import com.ecom.utils.MongoDBUtils;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * NoSQL DAO for application logs using MongoDB.
 * Logs are inherently unstructured and benefit from NoSQL's schema flexibility.
 */
public class LogsNoSQLDao {
    private static final Logger logger = LoggerFactory.getLogger(LogsNoSQLDao.class);
    private static final String COLLECTION_NAME = "logs";
    
    private MongoCollection<Document> getCollection() {
        if (!MongoDBUtils.isAvailable()) {
            throw new RuntimeException("MongoDB is not available. Please ensure MongoDB is running.");
        }
        MongoDatabase database = MongoDBUtils.getDatabase();
        return database.getCollection(COLLECTION_NAME);
    }

    /**
     * Creates a new log entry in MongoDB.
     */
    public String create(LogDocument log) {
        try {
            MongoCollection<Document> collection = getCollection();
            Document doc = log.toDocument();
            collection.insertOne(doc);
            String id = doc.getObjectId("_id").toString();
            log.setId(id);
            logger.debug("Log entry created in MongoDB with ID: {}", id);
            return id;
        } catch (Exception e) {
            logger.error("Failed to create log entry in MongoDB", e);
            // Don't throw exception for logs - logging failures shouldn't break the app
            return null;
        }
    }

    /**
     * Creates a log entry with convenience method.
     */
    public String log(String level, String message, String source) {
        LogDocument log = new LogDocument(level, message, source);
        return create(log);
    }

    /**
     * Creates a log entry with user context.
     */
    public String log(String level, String message, String source, String userId, String action) {
        LogDocument log = new LogDocument(level, message, source);
        log.setUserId(userId);
        log.setAction(action);
        return create(log);
    }

    /**
     * Finds a log entry by ID.
     */
    public LogDocument findById(String id) {
        try {
            MongoCollection<Document> collection = getCollection();
            Document doc = collection.find(Filters.eq("_id", new ObjectId(id))).first();
            if (doc != null) {
                return LogDocument.fromDocument(doc);
            }
            return null;
        } catch (Exception e) {
            logger.error("Failed to find log by ID: {}", id, e);
            return null;
        }
    }

    /**
     * Finds logs by level (INFO, WARN, ERROR, DEBUG).
     */
    public List<LogDocument> findByLevel(String level) {
        try {
            MongoCollection<Document> collection = getCollection();
            List<LogDocument> logs = new ArrayList<>();
            collection.find(Filters.eq("level", level))
                     .sort(Sorts.descending("timestamp"))
                     .limit(1000) // Limit to prevent memory issues
                     .forEach(doc -> logs.add(LogDocument.fromDocument(doc)));
            return logs;
        } catch (Exception e) {
            logger.error("Failed to find logs by level: {}", level, e);
            return new ArrayList<>();
        }
    }

    /**
     * Finds logs by user ID.
     */
    public List<LogDocument> findByUserId(String userId) {
        try {
            MongoCollection<Document> collection = getCollection();
            List<LogDocument> logs = new ArrayList<>();
            collection.find(Filters.eq("userId", userId))
                     .sort(Sorts.descending("timestamp"))
                     .limit(1000)
                     .forEach(doc -> logs.add(LogDocument.fromDocument(doc)));
            return logs;
        } catch (Exception e) {
            logger.error("Failed to find logs for user: {}", userId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Finds logs by action.
     */
    public List<LogDocument> findByAction(String action) {
        try {
            MongoCollection<Document> collection = getCollection();
            List<LogDocument> logs = new ArrayList<>();
            collection.find(Filters.eq("action", action))
                     .sort(Sorts.descending("timestamp"))
                     .limit(1000)
                     .forEach(doc -> logs.add(LogDocument.fromDocument(doc)));
            return logs;
        } catch (Exception e) {
            logger.error("Failed to find logs for action: {}", action, e);
            return new ArrayList<>();
        }
    }

    /**
     * Finds logs within a time range.
     */
    public List<LogDocument> findByTimeRange(LocalDateTime start, LocalDateTime end) {
        try {
            MongoCollection<Document> collection = getCollection();
            List<LogDocument> logs = new ArrayList<>();
            Date startDate = Date.from(start.atZone(ZoneId.systemDefault()).toInstant());
            Date endDate = Date.from(end.atZone(ZoneId.systemDefault()).toInstant());
            
            collection.find(Filters.and(
                Filters.gte("timestamp", startDate),
                Filters.lte("timestamp", endDate)
            )).sort(Sorts.descending("timestamp"))
              .limit(5000)
              .forEach(doc -> logs.add(LogDocument.fromDocument(doc)));
            return logs;
        } catch (Exception e) {
            logger.error("Failed to find logs by time range", e);
            return new ArrayList<>();
        }
    }

    /**
     * Finds recent logs (last N hours).
     */
    public List<LogDocument> findRecent(int hours) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusHours(hours);
        return findByTimeRange(start, end);
    }

    /**
     * Finds all logs (with limit).
     */
    public List<LogDocument> findAll(int limit) {
        try {
            MongoCollection<Document> collection = getCollection();
            List<LogDocument> logs = new ArrayList<>();
            collection.find()
                     .sort(Sorts.descending("timestamp"))
                     .limit(limit)
                     .forEach(doc -> logs.add(LogDocument.fromDocument(doc)));
            return logs;
        } catch (Exception e) {
            logger.error("Failed to find all logs", e);
            return new ArrayList<>();
        }
    }

    /**
     * Deletes logs older than specified days.
     */
    public long deleteOldLogs(int days) {
        try {
            MongoCollection<Document> collection = getCollection();
            LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
            Date cutoffDate = Date.from(cutoff.atZone(ZoneId.systemDefault()).toInstant());
            
            long deletedCount = collection.deleteMany(Filters.lt("timestamp", cutoffDate)).getDeletedCount();
            logger.info("Deleted {} old log entries (older than {} days)", deletedCount, days);
            return deletedCount;
        } catch (Exception e) {
            logger.error("Failed to delete old logs", e);
            return 0;
        }
    }

    /**
     * Gets log statistics.
     */
    public LogStatistics getStatistics() {
        try {
            MongoCollection<Document> collection = getCollection();
            long total = collection.countDocuments();
            long errors = collection.countDocuments(Filters.eq("level", "ERROR"));
            long warnings = collection.countDocuments(Filters.eq("level", "WARN"));
            long info = collection.countDocuments(Filters.eq("level", "INFO"));
            
            return new LogStatistics(total, errors, warnings, info);
        } catch (Exception e) {
            logger.error("Failed to get log statistics", e);
            return new LogStatistics(0, 0, 0, 0);
        }
    }

    /**
     * Statistics class for log data.
     */
    public static class LogStatistics {
        private final long total;
        private final long errors;
        private final long warnings;
        private final long info;

        public LogStatistics(long total, long errors, long warnings, long info) {
            this.total = total;
            this.errors = errors;
            this.warnings = warnings;
            this.info = info;
        }

        public long getTotal() { return total; }
        public long getErrors() { return errors; }
        public long getWarnings() { return warnings; }
        public long getInfo() { return info; }
    }
}
