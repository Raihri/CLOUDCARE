package com.example.cloud.care;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end checks of the security fixes against the running application
 * context. None of these requests change data.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTests {

    @Autowired
    MockMvc mvc;

    @Test
    void forgotPasswordNeverLeaksATokenOrWhetherTheAccountExists() throws Exception {
        String unknown = "{\"email\":\"nobody-" + System.nanoTime() + "@cloudcare.test\"}";
        for (String role : new String[] {"patient", "doctor"}) {
            mvc.perform(post("/" + role + "/api/password/forgot")
                            .contentType(MediaType.APPLICATION_JSON).content(unknown))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.token").doesNotExist())
                    .andExpect(content().string(not(containsString("not registered"))));
        }
    }

    @Test
    void resetWithoutAValidTokenIsRefused() throws Exception {
        String body = "{\"token\":\"forged\",\"password\":\"NewPass123\",\"confirmPassword\":\"NewPass123\"}";
        for (String role : new String[] {"patient", "doctor"}) {
            mvc.perform(post("/" + role + "/api/password/reset")
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(jsonPath("$.success").value(false));
        }
        // The old one-step "email + new password" reset must be gone.
        mvc.perform(post("/patient/api/password/reset").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"demo.patient@cloudcare.test\",\"password\":\"Hijack123\",\"confirmPassword\":\"Hijack123\"}"))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void crossOriginStateChangeIsBlocked() throws Exception {
        mvc.perform(post("/patient/api/password/forgot")
                        .header("Origin", "https://evil.example")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"a@b.c\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void webSocketTicketRequiresASignedInUser() throws Exception {
        mvc.perform(get("/api/ws-ticket")).andExpect(status().isUnauthorized());
    }
}
