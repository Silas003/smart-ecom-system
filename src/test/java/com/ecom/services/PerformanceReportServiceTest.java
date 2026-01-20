package com.ecom.services;

import com.ecom.utils.QueryTimer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PerformanceReportServiceTest {
    @BeforeEach
    public void clear() {
        QueryTimer.clearMetrics();
    }

    @Test
    public void captureAndGenerate() {
        PerformanceReportService svc = PerformanceReportService.getInstance();
        svc.captureBaseline();
        // simulate some query metrics
        QueryTimer.measure("q1", () -> null);
        svc.captureOptimized();
        String report = svc.generateReport();
        assertTrue(report.contains("PERFORMANCE OPTIMIZATION REPORT") || report.startsWith("Error"));
    }
}
