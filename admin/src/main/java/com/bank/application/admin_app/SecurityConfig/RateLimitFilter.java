package com.bank.application.admin_app.SecurityConfig;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 3;
    private static final long TIME_FRAME = 10_000;

    private final Map<String, RequestData> requestMap = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = request.getRemoteAddr();
        long currentTime = System.currentTimeMillis();

        requestMap.putIfAbsent(clientIp, new RequestData(0, currentTime));

        RequestData requestData = requestMap.get(clientIp);

        synchronized (requestData) {
            if (currentTime - requestData.lastRequestTime > TIME_FRAME) {
                requestData.lastRequestTime = currentTime;
                requestData.requestCount = 0;
            }

            requestData.requestCount++;

            if (requestData.requestCount > MAX_REQUESTS) {
                response.setStatus(HttpServletResponse.SC_REQUEST_TIMEOUT);
                response.getWriter().write("Prekrocen limit pozadavku, opakujte volani pozdeji");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private static class RequestData {
        private int requestCount;
        private long lastRequestTime;

        public RequestData(int requestCount, long lastRequestTime) {
            this.requestCount = requestCount;
            this.lastRequestTime = lastRequestTime;
        }
    }
}
