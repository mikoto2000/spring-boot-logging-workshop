package dev.mikoto2000.springboot.logging.filter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class AccessLogFilter extends OncePerRequestFilter {

    private static final Logger accessLogger = LoggerFactory.getLogger("ACCESS_LOG");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        long start = System.currentTimeMillis();

        // 接続元 IP 取得
        String ip = request.getRemoteAddr();

        // 成功・失敗フラグ
        boolean success = false;

        try {
            chain.doFilter(request, response);
            success = true;
        } finally {

            long time = System.currentTimeMillis() - start;

            accessLogger.info("ip={}, method={}, request_url={}, status={}, success={}, time={}ms",
                ip,
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                success ? "SUCCESS" : "FAIL",
                time);
        }
    }
}

