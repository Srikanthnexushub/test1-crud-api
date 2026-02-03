package org.example.service;

import jakarta.servlet.http.HttpServletRequest;
import org.example.entity.AuditLog;
import org.example.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@Transactional
public class AuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Async
    public void logAction(String action, String details) {
        try {
            String username = getCurrentUsername();
            AuditLog auditLog = new AuditLog(username, action, details);
            auditLog.setStatus("SUCCESS");

            HttpServletRequest request = getCurrentHttpRequest();
            if (request != null) {
                auditLog.setIpAddress(getClientIP(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
            }

            auditLogRepository.save(auditLog);
            logger.debug("Audit log created: {} - {} - {}", username, action, details);
        } catch (Exception e) {
            logger.error("Failed to create audit log: {}", e.getMessage(), e);
        }
    }

    @Async
    public void logSuccess(String action, String details, Long resourceId, String resourceType) {
        try {
            String username = getCurrentUsername();
            AuditLog auditLog = new AuditLog(username, action, details);
            auditLog.setStatus("SUCCESS");
            auditLog.setResourceId(resourceId);
            auditLog.setResourceType(resourceType);

            HttpServletRequest request = getCurrentHttpRequest();
            if (request != null) {
                auditLog.setIpAddress(getClientIP(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
            }

            auditLogRepository.save(auditLog);
            logger.debug("Audit log created: {} - {} - {}", username, action, details);
        } catch (Exception e) {
            logger.error("Failed to create audit log: {}", e.getMessage(), e);
        }
    }

    @Async
    public void logFailure(String action, String details, String errorMessage) {
        try {
            String username = getCurrentUsername();
            AuditLog auditLog = new AuditLog(username, action, details);
            auditLog.setStatus("FAILURE");
            auditLog.setErrorMessage(errorMessage);

            HttpServletRequest request = getCurrentHttpRequest();
            if (request != null) {
                auditLog.setIpAddress(getClientIP(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
            }

            auditLogRepository.save(auditLog);
            logger.debug("Audit log (FAILURE) created: {} - {} - {}", username, action, details);
        } catch (Exception e) {
            logger.error("Failed to create audit log: {}", e.getMessage(), e);
        }
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }
        return "anonymous";
    }

    private HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
