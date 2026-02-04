package org.example.service;

import jakarta.servlet.http.HttpServletRequest;
import org.example.entity.AuditLog;
import org.example.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Audit log service with maximum immutability:
 * - Final field for repository
 * - Constructor injection only
 * - Extracted methods for clarity
 */
@Service
@Transactional
public class AuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);

    // Immutable repository dependency
    private final AuditLogRepository auditLogRepository;

    // Constructor injection (no @Autowired needed)
    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Async
    public void logAction(String action, String details) {
        try {
            AuditLog auditLog = createAuditLog(action, details, "SUCCESS");
            auditLogRepository.save(auditLog);
            logger.debug("Audit log created: {} - {} - {}", auditLog.getUsername(), action, details);
        } catch (Exception e) {
            logger.error("Failed to create audit log: {}", e.getMessage(), e);
        }
    }

    @Async
    public void logSuccess(String action, String details, Long resourceId, String resourceType) {
        try {
            AuditLog auditLog = createAuditLog(action, details, "SUCCESS");
            auditLog.setResourceId(resourceId);
            auditLog.setResourceType(resourceType);

            auditLogRepository.save(auditLog);
            logger.debug("Audit log created: {} - {} - {}", auditLog.getUsername(), action, details);
        } catch (Exception e) {
            logger.error("Failed to create audit log: {}", e.getMessage(), e);
        }
    }

    @Async
    public void logFailure(String action, String details, String errorMessage) {
        try {
            AuditLog auditLog = createAuditLog(action, details, "FAILURE");
            auditLog.setErrorMessage(errorMessage);

            auditLogRepository.save(auditLog);
            logger.debug("Audit log (FAILURE) created: {} - {} - {}", auditLog.getUsername(), action, details);
        } catch (Exception e) {
            logger.error("Failed to create audit log: {}", e.getMessage(), e);
        }
    }

    /**
     * Creates an audit log with common fields populated.
     * Extracted method to reduce duplication.
     */
    private AuditLog createAuditLog(String action, String details, String status) {
        String username = getCurrentUsername();
        AuditLog auditLog = new AuditLog(username, action, details);
        auditLog.setStatus(status);

        HttpServletRequest request = getCurrentHttpRequest();
        if (request != null) {
            enrichWithRequestData(auditLog, request);
        }

        return auditLog;
    }

    /**
     * Enriches audit log with HTTP request data.
     * Extracted method for single responsibility.
     */
    private void enrichWithRequestData(AuditLog auditLog, HttpServletRequest request) {
        auditLog.setIpAddress(getClientIP(request));
        auditLog.setUserAgent(request.getHeader("User-Agent"));
    }

    /**
     * Gets current authenticated username or "anonymous".
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }
        return "anonymous";
    }

    /**
     * Gets current HTTP request from Spring context.
     */
    private HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * Extracts client IP address, handling X-Forwarded-For header.
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
