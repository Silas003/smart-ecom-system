package com.ecom.services;

import com.ecom.utils.QueryTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Service for generating performance reports comparing query execution times
 * before and after optimization (indexing, caching).
 */
public class PerformanceReportService {
    private static final Logger logger = LoggerFactory.getLogger(PerformanceReportService.class);
    private static final PerformanceReportService INSTANCE = new PerformanceReportService();
    
    private Map<String, QueryTimer.QueryMetrics> baselineMetrics;
    private Map<String, QueryTimer.QueryMetrics> optimizedMetrics;
    private boolean baselineCaptured = false;
    
    public static PerformanceReportService getInstance() {
        return INSTANCE;
    }
    
    private PerformanceReportService() {}
    
    /**
     * Captures the current query metrics as baseline (before optimization).
     */
    public void captureBaseline() {
        baselineMetrics = QueryTimer.getAllMetrics();
        baselineCaptured = true;
        logger.info("Baseline metrics captured: {} queries", baselineMetrics.size());
    }
    
    /**
     * Captures the current query metrics as optimized (after optimization).
     */
    public void captureOptimized() {
        optimizedMetrics = QueryTimer.getAllMetrics();
        logger.info("Optimized metrics captured: {} queries", optimizedMetrics.size());
    }
    
    /**
     * Generates a performance report comparing baseline vs optimized metrics.
     */
    public String generateReport() {
        if (!baselineCaptured) {
            return "Error: Baseline metrics not captured. Call captureBaseline() first.";
        }
        
        if (optimizedMetrics == null || optimizedMetrics.isEmpty()) {
            optimizedMetrics = QueryTimer.getAllMetrics();
        }
        
        StringBuilder report = new StringBuilder();
        report.append("=".repeat(80)).append("\n");
        report.append("PERFORMANCE OPTIMIZATION REPORT\n");
        report.append("=".repeat(80)).append("\n");
        report.append("Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
        
        // Summary section
        report.append("SUMMARY\n");
        report.append("-".repeat(80)).append("\n");
        report.append(String.format("%-30s %15s %15s %15s %15s\n", 
            "Query", "Baseline (ms)", "Optimized (ms)", "Improvement", "Improvement %"));
        report.append("-".repeat(80)).append("\n");
        
        double totalBaselineTime = 0;
        double totalOptimizedTime = 0;
        
        for (String queryName : baselineMetrics.keySet()) {
            QueryTimer.QueryMetrics baseline = baselineMetrics.get(queryName);
            QueryTimer.QueryMetrics optimized = optimizedMetrics.get(queryName);
            
            if (optimized == null) {
                optimized = new QueryTimer.QueryMetrics(queryName, 0, 0, 0);
            }
            
            double baselineAvg = baseline.getAverageTimeMs();
            double optimizedAvg = optimized.getAverageTimeMs();
            double improvement = baselineAvg - optimizedAvg;
            double improvementPercent = baselineAvg > 0 ? (improvement / baselineAvg) * 100 : 0;
            
            totalBaselineTime += baselineAvg;
            totalOptimizedTime += optimizedAvg;
            
            String improvementStr = improvement > 0 
                ? String.format("+%.2f ms", improvement)
                : String.format("%.2f ms", improvement);
            String improvementPercentStr = improvementPercent > 0
                ? String.format("+%.1f%%", improvementPercent)
                : String.format("%.1f%%", improvementPercent);
            
            report.append(String.format("%-30s %15.2f %15.2f %15s %15s\n",
                queryName, baselineAvg, optimizedAvg, improvementStr, improvementPercentStr));
        }
        
        report.append("-".repeat(80)).append("\n");
        double totalImprovement = totalBaselineTime - totalOptimizedTime;
        double totalImprovementPercent = totalBaselineTime > 0 ? (totalImprovement / totalBaselineTime) * 100 : 0;
        report.append(String.format("%-30s %15.2f %15.2f %15.2f ms %15.1f%%\n",
            "TOTAL AVERAGE", totalBaselineTime, totalOptimizedTime, totalImprovement, totalImprovementPercent));
        report.append("\n");
        
        // Detailed metrics section
        report.append("DETAILED METRICS\n");
        report.append("-".repeat(80)).append("\n");
        
        for (String queryName : baselineMetrics.keySet()) {
            QueryTimer.QueryMetrics baseline = baselineMetrics.get(queryName);
            QueryTimer.QueryMetrics optimized = optimizedMetrics.getOrDefault(queryName, 
                new QueryTimer.QueryMetrics(queryName, 0, 0, 0));
            
            report.append(String.format("\nQuery: %s\n", queryName));
            report.append(String.format("  Baseline:   Count=%d, Total=%d ms, Avg=%.2f ms\n",
                baseline.getExecutionCount(), baseline.getTotalTimeMs(), baseline.getAverageTimeMs()));
            report.append(String.format("  Optimized:  Count=%d, Total=%d ms, Avg=%.2f ms\n",
                optimized.getExecutionCount(), optimized.getTotalTimeMs(), optimized.getAverageTimeMs()));
            
            double improvement = baseline.getAverageTimeMs() - optimized.getAverageTimeMs();
            double improvementPercent = baseline.getAverageTimeMs() > 0 
                ? (improvement / baseline.getAverageTimeMs()) * 100 : 0;
            
            report.append(String.format("  Improvement: %.2f ms (%.1f%%)\n", improvement, improvementPercent));
        }
        
        // Optimization techniques section
        report.append("\n").append("OPTIMIZATION TECHNIQUES APPLIED\n");
        report.append("-".repeat(80)).append("\n");
        report.append("1. Database Indexing:\n");
        report.append("   - Index on products.name (for search operations)\n");
        report.append("   - Index on products.category_id (for category filtering)\n");
        report.append("   - Index on orders.user_id (for user order queries)\n");
        report.append("   - Index on order_items.order_id (for order item lookups)\n");
        report.append("\n");
        report.append("2. In-Memory Caching:\n");
        report.append("   - Product cache using ConcurrentHashMap\n");
        report.append("   - Query result cache for search operations\n");
        report.append("   - Cache invalidation on data updates\n");
        report.append("\n");
        report.append("3. Connection Pooling:\n");
        report.append("   - HikariCP connection pool (max 10 connections)\n");
        report.append("   - Prepared statement caching\n");
        report.append("\n");
        report.append("4. Query Optimization:\n");
        report.append("   - Parameterized queries to prevent SQL injection\n");
        report.append("   - Pagination to limit result sets\n");
        report.append("   - Efficient sorting using database ORDER BY\n");
        
        report.append("\n").append("=".repeat(80)).append("\n");
        
        return report.toString();
    }
    
    /**
     * Saves the performance report to a file.
     */
    public void saveReportToFile(String filePath) throws IOException {
        String report = generateReport();
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.print(report);
        }
        logger.info("Performance report saved to: {}", filePath);
    }
    
    /**
     * Gets a comparison summary for display in UI.
     */
    public String getSummary() {
        if (!baselineCaptured || optimizedMetrics == null) {
            return "No performance data available. Run baseline and optimized captures.";
        }
        
        double totalBaseline = baselineMetrics.values().stream()
            .mapToDouble(QueryTimer.QueryMetrics::getAverageTimeMs)
            .sum();
        double totalOptimized = optimizedMetrics.values().stream()
            .mapToDouble(QueryTimer.QueryMetrics::getAverageTimeMs)
            .sum();
        double improvement = totalBaseline - totalOptimized;
        double improvementPercent = totalBaseline > 0 ? (improvement / totalBaseline) * 100 : 0;
        
        return String.format(
            "Total Average Query Time: %.2f ms → %.2f ms (%.1f%% improvement)",
            totalBaseline, totalOptimized, improvementPercent
        );
    }
    
    public boolean isBaselineCaptured() {
        return baselineCaptured;
    }
}
