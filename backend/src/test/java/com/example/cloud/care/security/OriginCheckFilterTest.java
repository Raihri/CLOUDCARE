package com.example.cloud.care.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OriginCheckFilterTest {

    private final OriginCheckFilter filter = new OriginCheckFilter(Set.of("http://localhost:3000"));

    private int run(String method, String origin, String referer) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, "/patient/api/password/forgot");
        if (origin != null) request.addHeader("Origin", origin);
        if (referer != null) request.addHeader("Referer", referer);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response.getStatus();
    }

    @Test
    void crossSitePostIsBlocked() throws Exception {
        assertThat(run("POST", "https://evil.example", null)).isEqualTo(403);
        assertThat(run("DELETE", null, "https://evil.example/page")).isEqualTo(403);
        assertThat(run("POST", "null", null)).isEqualTo(403);
    }

    @Test
    void sameSitePostIsAllowed() throws Exception {
        assertThat(run("POST", "http://localhost:3000", null)).isEqualTo(200);
        assertThat(run("POST", null, "http://localhost:3000/login")).isEqualTo(200);
    }

    @Test
    void serverToServerAndSafeMethodsPass() throws Exception {
        // The Next.js server calls the API without browser headers.
        assertThat(run("POST", null, null)).isEqualTo(200);
        assertThat(run("GET", "https://evil.example", null)).isEqualTo(200);
    }
}
