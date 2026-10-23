package com.example.cloud.care.config;

import com.example.cloud.care.dao.doctor_dao;
import com.example.cloud.care.dao.AdminDao;
import com.example.cloud.care.service.CustomUserDetailsService;
import com.example.cloud.care.service.DoctorUserDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService patientUserDetailsService;
    private final doctor_dao doctorDao;
    private final AdminDao adminDao;

    public SecurityConfig(CustomUserDetailsService patientUserDetailsService,
            doctor_dao doctorDao,
            AdminDao adminDao) {
        this.patientUserDetailsService = patientUserDetailsService;
        this.doctorDao = doctorDao;
        this.adminDao = adminDao;
    }

    // ---------------------------
    // Password encoder
    // ---------------------------
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ---------------------------
    // Doctor UserDetailsService
    // ---------------------------
    @Bean
    public UserDetailsService doctorUserDetailsService() {
        return email -> doctorDao.findByEmail(email)
                .map(DoctorUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("Doctor not found"));
    }

    // ---------------------------
    // Admin UserDetailsService
    // ---------------------------
    @Bean
    public UserDetailsService adminUserDetailsService() {
        return email -> adminDao.findByEmail(email)
                .map(admin -> org.springframework.security.core.userdetails.User.builder()
                        .username(admin.getEmail())
                        .password(admin.getPassword())
                        .roles("ADMIN")
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Admin not found"));
    }

    // ============================
    // 0️⃣ WebSocket Security (Order 0 - highest priority)
    // ============================
    @Bean
    @Order(0)
    public SecurityFilterChain webSocketSecurity(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/ws/**")
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll());
        return http.build();
    }

    // ============================
    // 1️⃣ Patient Security
    // ============================
    @Bean
    @Order(1)
    public SecurityFilterChain patientSecurity(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/patient/**")
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/patient/register", "/patient/login",
                                "/patient/reset-password",
                                "/patient/", "/patient/css/**", "/patient/js/**",
                                "/ws/**", "/app/**", "/patient/current")
                        .permitAll()
                        // Password reset has to work while signed out, so it is
                        // reachable anonymously. It sits under /patient/api/**,
                        // which is otherwise PATIENT-only, so it must be listed
                        // before that rule — order decides which one matches.
                        // These handlers act on an email supplied in the request
                        // body, not on the session, so they grant nothing.
                        .requestMatchers("/patient/api/password/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/patient/*/book/*").authenticated()
                        // Same reasoning as the doctor chain: these handlers
                        // read the current patient from the authenticated
                        // email, so a doctor or admin session would otherwise
                        // receive an empty 200 instead of a 403.
                        .requestMatchers("/patient/api/**").hasRole("PATIENT")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/patient/login")
                        .loginProcessingUrl("/patient/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/patient/dashboard", true))
                .logout(logout -> logout
                        .logoutUrl("/patient/logout")
                        .logoutSuccessUrl("/patient/?logout"))
                .userDetailsService(patientUserDetailsService)
                .authenticationManager(http.getSharedObject(AuthenticationManagerBuilder.class)
                        .userDetailsService(patientUserDetailsService)
                        .passwordEncoder(passwordEncoder())
                        .and()
                        .build());

        return http.build();
    }

    // ============================
    // 2️⃣ Doctor Security
    // ============================
    @Bean
    @Order(2)
    public SecurityFilterChain doctorSecurity(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/doctor/**")
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/doctor/login", "/doctor/signup", "/doctor/signup/request",
                                "/doctor/check-email", "/doctor/check-bmdc", "/doctor/forgot-password",
                                "/doctor/reset-password**", "/doctor/css/**", "/doctor/js/**")
                        .permitAll()
                        // Reachable while signed out, and listed ahead of the
                        // DOCTOR-only rule below for the same reason as the
                        // patient reset flow. Token-based, so it grants nothing
                        // on its own.
                        .requestMatchers("/doctor/api/password/**").permitAll()
                        // The JSON API is explicitly role-gated. "authenticated()"
                        // alone let a patient session through, and since these
                        // handlers resolve the doctor from the authenticated
                        // email, the patient simply got an empty result — a
                        // silent 200 instead of a 403.
                        .requestMatchers("/doctor/api/**").hasRole("DOCTOR")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/doctor/login")
                        .loginProcessingUrl("/doctor/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/doctor/dashboard", true))
                .logout(logout -> logout
                        .logoutUrl("/doctor/logout")
                        .logoutSuccessUrl("/doctor/login?logout"))
                .userDetailsService(doctorUserDetailsService())
                .authenticationManager(http.getSharedObject(AuthenticationManagerBuilder.class)
                        .userDetailsService(doctorUserDetailsService())
                        .passwordEncoder(passwordEncoder())
                        .and()
                        .build());

        return http.build();
    }

    // ============================
    // 3️⃣ Admin Security
    // ============================
    @Bean
    @Order(3)
    public SecurityFilterChain adminSecurity(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/admin/**")
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/login", "/admin/css/**", "/admin/js/**").permitAll()
                        .anyRequest().hasRole("ADMIN"))
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/admin/home", true))
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login?logout"))
                .userDetailsService(adminUserDetailsService())
                .authenticationManager(http.getSharedObject(AuthenticationManagerBuilder.class)
                        .userDetailsService(adminUserDetailsService())
                        .passwordEncoder(passwordEncoder())
                        .and()
                        .build());

        return http.build();
    }

@Bean
@Order(99)
public SecurityFilterChain publicPages(HttpSecurity http) throws Exception {
    http
            .securityMatcher("/**") // global match for last priority
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/", "/landing", "/css/**", "/js/**", "/image/**","/video/**").permitAll()
                    // Role-agnostic identity endpoint used by the Next.js frontend.
                    // Must be reachable for all three roles so it can report which one
                    // is active; it answers 401 itself when nobody is signed in.
                    .requestMatchers("/api/me").permitAll()
                    .anyRequest().permitAll() // final fallback
            )
            .csrf(csrf -> csrf.disable());

    return http.build();
}
}