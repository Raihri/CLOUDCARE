package com.example.cloud.care.controller.common;

import com.example.cloud.care.security.WebSocketTicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Issues the short-lived ticket the browser presents when opening the
 * notifications WebSocket. Session-authenticated, so only a signed-in user can
 * get a ticket, and only for their own identity. See {@link WebSocketTicketService}.
 */
@RestController
public class WebSocketTicketController {

    private final WebSocketTicketService tickets;

    public WebSocketTicketController(WebSocketTicketService tickets) {
        this.tickets = tickets;
    }

    @GetMapping("/api/ws-ticket")
    public ResponseEntity<Map<String, String>> ticket() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok()
                .header("Cache-Control", "no-store")
                .body(Map.of("ticket", tickets.issue(auth.getName())));
    }
}
