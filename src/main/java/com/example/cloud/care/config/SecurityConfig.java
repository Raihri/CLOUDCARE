package com.example.cloud.care.config;

import com.example.cloud.care.dao.AdminDao;
import com.example.cloud.care.dao.doctor_dao;
import com.example.cloud.care.service.CustomUserDetailsService;
import com.example.cloud.care.service.DoctorUserDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService patientUserDetailsService;

    public SecurityConfig(CustomUserDetailsService patientUserDetailsService) {
        this.patientUserDetailsService = patientUserDetailsService;
    }

    // ============================
    // Password encoder
    // ============================
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ============================
    // AuthenticationManager
    // ============================
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http,
                                                       doctor_dao doctorDao,
                                                       AdminDao adminDao) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(patientUserDetailsService)
                .passwordEncoder(passwordEncoder())
                .and()
                .userDetailsService(doctorUserDetailsService(doctorDao))
                .passwordEncoder(passwordEncoder())
                .and()
                .userDetailsService(adminUserDetailsService(adminDao))
                .passwordEncoder(passwordEncoder())
                .and()
                .build();
    }

    // ============================
    // UserDetailsService for Doctor
    // ============================
    @Bean
    public UserDetailsService doctorUserDetailsService(doctor_dao doctorDao) {
        return username -> doctorDao.findByEmail(username)
                .map(DoctorUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("Doctor not found"));
    }

    // ============================
    // UserDetailsService for Admin
    // ============================
    @Bean
    public UserDetailsService adminUserDetailsService(AdminDao adminDao) {
        return username -> adminDao.findByUsername(username)
                .map(admin -> org.springframework.security.core.userdetails.User.builder()
                        .username(admin.getUsername())
                        .password(admin.getPassword())
                        .roles("ADMIN")
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Admin not found"));
    }

    // ============================
    // 1️⃣ PATIENT SECURITY
    // ============================
    @Bean
    @Order(1)
    public SecurityFilterChain patientSecurity(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/patient/**")
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/patient/register", "/patient/login", "/patient/otpverify",
                    "/patient/reset-password", "/patient/forgot-password",
                    "/patient/", "/patient/verify",
                    "/patient/forgot-password-submit", "/patient/reset-otp",
                    "/patient/reset-otp-verify",
                    "/patient/css/**", "/patient/js/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/patient/login")
                .loginProcessingUrl("/patient/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/patient/dashboard", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/patient/logout")
                .logoutSuccessUrl("/patient/?logout")
                .permitAll()
            );
        return http.build();
    }

    // ============================
    // 2️⃣ DOCTOR SECURITY
    // ============================
    @Bean
    @Order(2)
    public SecurityFilterChain doctorSecurity(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/doctor/**")
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/doctor/login", "/doctor/signup", "/doctor/signup/request",
                    "/doctor/check-email", "/doctor/forgot-password",
                    "/doctor/reset-password**", "/doctor/css/**", "/doctor/js/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/doctor/login")
                .loginProcessingUrl("/doctor/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/doctor/dashboard", true)
                .failureUrl("/doctor/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/doctor/logout")
                .logoutSuccessUrl("/doctor/login?logout")
                .permitAll()
            );
        return http.build();
    }

    // ============================
    // 3️⃣ ADMIN SECURITY
    // ============================
    @Bean
    @Order(3)
    public SecurityFilterChain adminSecurity(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/admin/**")
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/login", "/admin/css/**", "/admin/js/**").permitAll()
                .anyRequest().hasRole("ADMIN")
            )
            .formLogin(form -> form
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/admin/doctor/status", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/admin/logout")
                .logoutSuccessUrl("/admin/login?logout")
                .permitAll()
            );
        return http.build();
    }
}