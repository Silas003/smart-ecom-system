package com.ecom.utils;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for MongoDB connection management.
 * Provides singleton access to MongoDB database for NoSQL operations.
 */
public class MongoDBUtils {
    private static final Logger logger = LoggerFactory.getLogger(MongoDBUtils.class);
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static final String DEFAULT_CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DEFAULT_DATABASE_NAME = "smartEcom";

    static {
        initialize();
    }

    private static void initialize() {
        try {
            String connectionString = System.getenv().getOrDefault("MONGO_URL", DEFAULT_CONNECTION_STRING);
            String databaseName = System.getenv().getOrDefault("MONGO_DB_NAME", DEFAULT_DATABASE_NAME);

            mongoClient = MongoClients.create(connectionString);
            database = mongoClient.getDatabase(databaseName);

            // Test connection with ping command
            Document pingCommand = new Document("ping", 1);
            database.runCommand(pingCommand);
            logger.info("MongoDB connection established (Database: {})", databaseName);
        } catch (Exception e) {
            logger.error("Failed to initialize MongoDB connection: {}", e.getMessage());
            logger.warn("NoSQL features will be unavailable. Please ensure MongoDB is running.");
        }
    }

    /**
     * Gets the MongoDB database instance.
     * @return MongoDatabase instance
     */
    public static MongoDatabase getDatabase() {
        if (database == null) {
            initialize();
        }
        return database;
    }

    /**
     * Checks if MongoDB connection is available.
     * @return true if connected, false otherwise
     */
    public static boolean isAvailable() {
        try {
            if (database == null) {
                return false;
            }
            Document pingCommand = new Document("ping", 1);
            database.runCommand(pingCommand);
            return true;
        } catch (Exception e) {
            logger.warn("MongoDB connection check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Closes the MongoDB connection.
     */
    public static void close() {
        if (mongoClient != null) {
            mongoClient.close();
            logger.info("MongoDB connection closed");
        }
    }

    private MongoDBUtils() {}
}
