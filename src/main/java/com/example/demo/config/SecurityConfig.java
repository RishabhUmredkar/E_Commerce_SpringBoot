package com.example.demo.config;

import com.example.demo.service.AdminDetailsService;
import com.example.demo.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.core.annotation.Order;
@Configuration
public class SecurityConfig {

    @Autowired
    private AdminDetailsService adminDetailsService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider adminAuthProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(adminDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public DaoAuthenticationProvider userAuthProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .antMatcher("/admin/**")
            .authorizeRequests()
                .antMatchers("/admin/login", "/admin/css/**", "/admin/js/**", "/admin/img/**") // Ensure image access for admins
                .permitAll()
                .anyRequest().hasRole("ADMIN")
            .and()
            .formLogin()
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/login")
                .defaultSuccessUrl("/admin/dashboard", true)
                .failureUrl("/admin/login?error")
                .permitAll()
            .and()
            .logout()
                .logoutUrl("/admin/logout")
                .logoutSuccessUrl("/admin/login?logout")
            .and()
            .authenticationProvider(adminAuthProvider())
            .csrf().disable();

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain userSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                // Permit all static resources (images, css, js, etc.)
                .antMatchers(
                    "/", "/login", "/register", "/products/image/**",
                    "/css/**", "/js/**", "/images/**", "/static/**", "/img/**" // Allowing /img/** for public access
                ).permitAll()
                .antMatchers("/home", "/add-to-cart", "/cart", "/checkout", "/view-cart", "/order/**")
                .hasRole("USER")
                .anyRequest().authenticated()
            .and()
            .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/home", true)
                .failureUrl("/login?error")
                .permitAll()
            .and()
            .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
            .and()
            .authenticationProvider(userAuthProvider())
            .csrf().disable();

        return http.build();
    }
}
