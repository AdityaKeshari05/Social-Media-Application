package com.intermediate.Blog.Application.Security;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Map;

/**
 * Ensures the WebSocket session has an authenticated Principal.
 *
 * We authenticate during the WebSocket handshake using a JWT passed as a query parameter:
 *   /ws?token=JWT
 *
 * This is more reliable than relying on STOMP CONNECT headers (which vary across clients/tools).
 */
public class JwtHandshakeHandler extends DefaultHandshakeHandler {

    private final JwtTokenHelper jwtTokenHelper;
    private final UserDetailsService userDetailsService;

    public JwtHandshakeHandler(JwtTokenHelper jwtTokenHelper, UserDetailsService userDetailsService) {
        this.jwtTokenHelper = jwtTokenHelper;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = null;

        if (request instanceof ServletServerHttpRequest servletReq) {
            String query = servletReq.getServletRequest().getQueryString(); // e.g. token=...
            token = extractQueryParam(query, "token");
        }

        if (token == null || token.isBlank()) {
            return null;
        }

        // URL-decode just in case
        token = URLDecoder.decode(token, StandardCharsets.UTF_8);

        if (!jwtTokenHelper.validateToken(token)) {
            return null;
        }

        String email = jwtTokenHelper.extractUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }

    private String extractQueryParam(String query, String key) {
        if (query == null || query.isBlank()) return null;
        String[] parts = query.split("&");
        for (String p : parts) {
            int idx = p.indexOf('=');
            if (idx <= 0) continue;
            String k = p.substring(0, idx);
            if (key.equals(k)) {
                return p.substring(idx + 1);
            }
        }
        return null;
    }
}

