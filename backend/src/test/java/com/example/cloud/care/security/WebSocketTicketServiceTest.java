package com.example.cloud.care.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WebSocketTicketServiceTest {

    private final WebSocketTicketService tickets = new WebSocketTicketService();

    @Test
    void genuineTicketResolvesToItsEmail() {
        String ticket = tickets.issue("demo.patient@cloudcare.test");
        assertThat(tickets.verify(ticket)).contains("demo.patient@cloudcare.test");
    }

    @Test
    void tamperedPayloadIsRejected() {
        String ticket = tickets.issue("demo.patient@cloudcare.test");
        String forgedPayload = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString("victim@cloudcare.test|9999999999".getBytes());
        String forged = forgedPayload + ticket.substring(ticket.indexOf('.'));
        assertThat(tickets.verify(forged)).isEmpty();
    }

    @Test
    void ticketFromAnotherServerKeyIsRejected() {
        String foreign = new WebSocketTicketService().issue("demo.patient@cloudcare.test");
        assertThat(tickets.verify(foreign)).isEmpty();
    }

    @Test
    void garbageIsRejected() {
        assertThat(tickets.verify(null)).isEmpty();
        assertThat(tickets.verify("")).isEmpty();
        assertThat(tickets.verify("no-dot")).isEmpty();
        assertThat(tickets.verify("a.b")).isEmpty();
    }
}
