package com.sushishop.shared.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class ClientIpResolver {

    static final String PROXY_SECRET_HEADER = "X-Proxy-Secret";
    private static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";

    private final String clientIpHeader;
    private final byte[] proxySecret;

    public ClientIpResolver(@Value("${app.rate-limit.client-ip-header:}") String clientIpHeader,
                            @Value("${app.rate-limit.proxy-secret:}") String proxySecret) {
        this.clientIpHeader = clientIpHeader;
        this.proxySecret = proxySecret.getBytes(StandardCharsets.UTF_8);
    }

    public String resolve(HttpServletRequest request) {
        if (isFromTrustedProxy(request)) {
            String forwardedFor = request.getHeader(FORWARDED_FOR_HEADER);
            if (forwardedFor != null && !forwardedFor.isBlank()) {
                return forwardedFor.split(",")[0].strip();
            }
            return request.getRemoteAddr();
        }
        if (!clientIpHeader.isBlank()) {
            String headerValue = request.getHeader(clientIpHeader);
            if (headerValue != null && !headerValue.isBlank()) {
                return headerValue.strip();
            }
        }
        return request.getRemoteAddr();
    }

    private boolean isFromTrustedProxy(HttpServletRequest request) {
        if (proxySecret.length == 0) {
            return false;
        }
        String provided = request.getHeader(PROXY_SECRET_HEADER);
        return provided != null && MessageDigest.isEqual(proxySecret, provided.getBytes(StandardCharsets.UTF_8));
    }
}
