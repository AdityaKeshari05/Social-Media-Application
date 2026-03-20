package com.intermediate.Blog.Application.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    @Autowired
    private JwtTokenHelper jwtTokenHelper;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        StompCommand cmd = accessor.getCommand();
        if (cmd == null) return message;

        // If CONNECT doesn't result in a user being stored on the session (client quirks),
        // we still want SEND/SUBSCRIBE frames to be authenticated. We accept the same header
        // on those frames as a fallback.
        if (StompCommand.CONNECT.equals(cmd) || StompCommand.SEND.equals(cmd) || StompCommand.SUBSCRIBE.equals(cmd)) {
            if (accessor.getUser() == null) {
                String auth = accessor.getFirstNativeHeader("Authorization");
                if (auth == null) auth = accessor.getFirstNativeHeader("authorization");

                if (auth == null || !auth.startsWith("Bearer ")) {
                    if (StompCommand.CONNECT.equals(cmd)) {
                        throw new IllegalArgumentException("Missing Authorization header for STOMP CONNECT");
                    }
                    // For SEND/SUBSCRIBE we simply leave it unauthenticated; handlers can reject.
                    return message;
                }

                String token = auth.substring(7);
                if (!jwtTokenHelper.validateToken(token)) {
                    if (StompCommand.CONNECT.equals(cmd)) {
                        throw new IllegalArgumentException("Invalid JWT for STOMP CONNECT");
                    }
                    return message;
                }

                String email = jwtTokenHelper.extractUsername(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                accessor.setUser(authentication);
            }
        }

        return message;
    }
}

