package com.ecom.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility class for measuring and tracking query execution times.
 * Provides functionality to record query performance metrics for optimization analysis.
 */
public class QueryTimer {
    private static final Logger logger = LoggerFactory.getLogger(QueryTimer.class);
    
    // Store query execution times: queryName -> execution time in milliseconds
    private static final Map<String, AtomicLong> queryTimes = new ConcurrentHashMap<>();
    private static final Map<String, AtomicLong> queryCounts = new ConcurrentHashMap<>();
    
    /**
     * Executes a query and measures its execution time.
     * 
     * @param queryName A descriptive name for the query (e.g., "product_search", "order_findAll")
     * @param query The query operation to execute
     * @return The result of the query
     */
    public static <T> T measure(String queryName, QueryOperation<T> query) {
        long startTime = System.nanoTime();
        try {
            T result = query.execute();
            long endTime = System.nanoTime();
            long durationMs = (endTime - startTime) / 1_000_000; // Convert to milliseconds
            
            recordQueryTime(queryName, durationMs);
            logger.debug("Query '{}' executed in {} ms", queryName, durationMs);
            
            return result;
        } catch (Exception e) {
            long endTime = System.nanoTime();
            long durationMs = (endTime - startTime) / 1_000_000;
            recordQueryTime(queryName, durationMs);
            logger.error("Query '{}' failed after {} ms", queryName, durationMs, e);
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Records the execution time for a query.
     */
    private static void recordQueryTime(String queryName, long durationMs) {
        queryTimes.computeIfAbsent(queryName, k -> new AtomicLong(0)).addAndGet(durationMs);
        queryCounts.computeIfAbsent(queryName, k -> new AtomicLong(0)).incrementAndGet();
    }
    
    /**
     * Gets the average execution time for a query.
     */
    public static double getAverageTime(String queryName) {
        AtomicLong totalTime = queryTimes.get(queryName);
        AtomicLong count = queryCounts.get(queryName);
        
        if (totalTime == null || count == null || count.get() == 0) {
            return 0.0;
        }
        
        return (double) totalTime.get() / count.get();
    }
    
    /**
     * Gets the total execution time for a query.
     */
    public static long getTotalTime(String queryName) {
        AtomicLong totalTime = queryTimes.get(queryName);
        return totalTime != null ? totalTime.get() : 0;
    }
    
    /**
     * Gets the execution count for a query.
     */
    public static long getExecutionCount(String queryName) {
        AtomicLong count = queryCounts.get(queryName);
        return count != null ? count.get() : 0;
    }
    
    /**
     * Gets all recorded query metrics.
     */
    public static Map<String, QueryMetrics> getAllMetrics() {
        Map<String, QueryMetrics> metrics = new ConcurrentHashMap<>();
        
        for (String queryName : queryTimes.keySet()) {
            metrics.put(queryName, new QueryMetrics(
                queryName,
                getExecutionCount(queryName),
                getTotalTime(queryName),
                getAverageTime(queryName)
            ));
        }
        
        return metrics;
    }
    
    /**
     * Clears all recorded metrics.
     */
    public static void clearMetrics() {
        queryTimes.clear();
        queryCounts.clear();
        logger.info("Query metrics cleared");
    }
    
    /**
     * Functional interface for query operations.
     */
    @FunctionalInterface
    public interface QueryOperation<T> {
        T execute() throws Exception;
    }
    
    /**
     * Data class for query metrics.
     */
    public static class QueryMetrics {
        private final String queryName;
        private final long executionCount;
        private final long totalTimeMs;
        private final double averageTimeMs;
        
        public QueryMetrics(String queryName, long executionCount, long totalTimeMs, double averageTimeMs) {
            this.queryName = queryName;
            this.executionCount = executionCount;
            this.totalTimeMs = totalTimeMs;
            this.averageTimeMs = averageTimeMs;
        }
        
        public String getQueryName() { return queryName; }
        public long getExecutionCount() { return executionCount; }
        public long getTotalTimeMs() { return totalTimeMs; }
        public double getAverageTimeMs() { return averageTimeMs; }
        
        @Override
        public String toString() {
            return String.format("QueryMetrics{query='%s', count=%d, totalTime=%d ms, avgTime=%.2f ms}",
                queryName, executionCount, totalTimeMs, averageTimeMs);
        }
    }
}
