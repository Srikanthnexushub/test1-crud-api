package org.example.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

@Component
public class HttpRequestAuditInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequestAuditInterceptor.class);
    private static final String REQUEST_START_TIME = "requestStartTime";
    private static final String TRACE_ID = "traceId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Generate trace ID for request tracking
        String traceId = UUID.randomUUID().toString();
        MDC.put(TRACE_ID, traceId);
        request.setAttribute(REQUEST_START_TIME, System.currentTimeMillis());
        request.setAttribute(TRACE_ID, traceId);

        String username = getCurrentUsername();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String clientIp = getClientIP(request);
        String userAgent = request.getHeader("User-Agent");

        logger.info("HTTP Request | TraceID: {} | User: {} | Method: {} | URI: {} | IP: {} | UserAgent: {}",
                traceId, username, method, uri, clientIp, userAgent);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                          ModelAndView modelAndView) {
        // This is called after the controller but before rendering the view
        // We'll do most logging in afterCompletion
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                               Exception ex) {
        Long startTime = (Long) request.getAttribute(REQUEST_START_TIME);
        String traceId = (String) request.getAttribute(TRACE_ID);
        long duration = System.currentTimeMillis() - startTime;

        String username = getCurrentUsername();
        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();

        if (ex != null) {
            logger.error("HTTP Response | TraceID: {} | User: {} | Method: {} | URI: {} | Status: {} | Duration: {}ms | Error: {}",
                    traceId, username, method, uri, status, duration, ex.getMessage());
        } else {
            logger.info("HTTP Response | TraceID: {} | User: {} | Method: {} | URI: {} | Status: {} | Duration: {}ms",
                    traceId, username, method, uri, status, duration);
        }

        // Clean up MDC
        MDC.remove(TRACE_ID);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }
        return "anonymous";
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
