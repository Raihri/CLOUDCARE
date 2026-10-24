package com.example.cloud.care.config;

import com.example.cloud.care.security.AllowedOrigins;
import com.example.cloud.care.security.WebSocketTicketService;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP over SockJS at {@code /ws}, used for live notifications.
 *
 * <p>Security model:
 * <ul>
 *   <li><b>CONNECT</b> must carry a {@code ticket} header issued by
 *       {@code GET /api/ws-ticket} to the signed-in user. The ticket's verified
 *       email becomes the session Principal, so a client can only ever receive
 *       its own {@code /user/queue/notifications}.</li>
 *   <li><b>SUBSCRIBE</b> is limited to the caller's user queue and public topics;
 *       raw {@code /queue/**} destinations are refused.</li>
 *   <li><b>SEND</b> is refused: the server is the only publisher, so a client
 *       cannot inject fake notifications into a broker topic.</li>
 *   <li>The SockJS handshake only accepts the configured CloudCare origins.</li>
 * </ul>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketTicketService tickets;
    private final AllowedOrigins allowedOrigins;

    public WebSocketConfig(WebSocketTicketService tickets, AllowedOrigins allowedOrigins) {
        this.tickets = tickets;
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigins.asArray())
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor == null || accessor.getCommand() == null) {
                    return message;
                }

                switch (accessor.getCommand()) {
                    case CONNECT -> {
                        String email = tickets.verify(accessor.getFirstNativeHeader("ticket"))
                                .orElseThrow(() -> new MessageDeliveryException("Invalid or expired WebSocket ticket"));
                        accessor.setUser(() -> email);
                    }
                    case SUBSCRIBE -> {
                        String destination = accessor.getDestination();
                        boolean allowed = destination != null
                                && (destination.startsWith("/user/queue/") || destination.startsWith("/topic/"));
                        if (!allowed || accessor.getUser() == null) {
                            throw new MessageDeliveryException("Subscription not allowed: " + destination);
                        }
                    }
                    case SEND -> throw new MessageDeliveryException("Clients cannot publish messages");
                    default -> { }
                }
                return message;
            }
        });
    }
}
