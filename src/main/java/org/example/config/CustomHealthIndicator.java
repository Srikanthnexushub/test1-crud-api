package org.example.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.File;

@Component("custom")
public class CustomHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        Health.Builder healthBuilder = new Health.Builder();

        try {
            // Check memory usage
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            double memoryUsagePercent = (double) usedMemory / totalMemory * 100;

            // Check disk space
            File root = new File("/");
            long totalSpace = root.getTotalSpace();
            long freeSpace = root.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            double diskUsagePercent = (double) usedSpace / totalSpace * 100;

            healthBuilder.withDetail("memory", new MemoryDetails(totalMemory, freeMemory, usedMemory, memoryUsagePercent));
            healthBuilder.withDetail("disk", new DiskDetails(totalSpace, freeSpace, usedSpace, diskUsagePercent));

            // Determine overall health status
            if (memoryUsagePercent > 90 || diskUsagePercent > 90) {
                healthBuilder.down().withDetail("reason", "Critical resource usage");
            } else if (memoryUsagePercent > 80 || diskUsagePercent > 80) {
                healthBuilder.status("WARNING").withDetail("reason", "High resource usage");
            } else {
                healthBuilder.up();
            }

        } catch (Exception e) {
            healthBuilder.down().withDetail("error", e.getMessage());
        }

        return healthBuilder.build();
    }

    private static class MemoryDetails {
        public final long totalBytes;
        public final long freeBytes;
        public final long usedBytes;
        public final double usagePercent;

        public MemoryDetails(long total, long free, long used, double percent) {
            this.totalBytes = total;
            this.freeBytes = free;
            this.usedBytes = used;
            this.usagePercent = Math.round(percent * 100.0) / 100.0;
        }
    }

    private static class DiskDetails {
        public final long totalBytes;
        public final long freeBytes;
        public final long usedBytes;
        public final double usagePercent;

        public DiskDetails(long total, long free, long used, double percent) {
            this.totalBytes = total;
            this.freeBytes = free;
            this.usedBytes = used;
            this.usagePercent = Math.round(percent * 100.0) / 100.0;
        }
    }
}
