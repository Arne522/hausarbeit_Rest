package test.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import test.entity.AppUser;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtFilter jwtFilter, RateLimitFilter rateLimitFilter) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable())) // für H2-Console
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    @Bean
    public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    @Bean
    CommandLineRunner run(test.Repository.UserRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (repo.findByUsername("User").isEmpty()) {
                AppUser user = new AppUser();
                user.setUsername("User");
                user.setPassword(encoder.encode("passwort"));
                user.setRole("USER");
                repo.save(user);
            }
            if (repo.findByUsername("admin").isEmpty()) {
                AppUser admin = new AppUser();
                admin.setUsername("admin");
                admin.setPassword(encoder.encode("adminpasswort"));
                admin.setRole("ADMIN");
                repo.save(admin);
            }
            if (repo.findByUsername("User2").isEmpty()) {
                AppUser user2 = new AppUser();
                user2.setUsername("User2");
                user2.setPassword(encoder.encode("passwort2"));
                user2.setRole("USER");
                repo.save(user2);
            }
        };
    }


}

