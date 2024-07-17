package com.example.Security;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.Arrays;

@Configuration
@EnableReactiveMethodSecurity
public class SecurityConfig {
    //There is no PasswordEncoder mapped for the id "null" <<-- 에러 해결용

    private BCryptPasswordEncoder passwordEncoder;

    public SecurityConfig(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public static BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService(UserRepository repository) {
        return username -> repository.findByName(username)
                .map(user -> org.springframework.security.core.userdetails.User.builder()
                        .username(user.getName())
                        .password(user.getPassword())
                        .authorities(user.getRoles().toArray(new String[0]))
                        .build());
    }

    static final String USER = "USER";
    static final String INVENTORY = "INVENTORY";

    @Bean
    SecurityWebFilterChain myCustomSecurityPolicy(ServerHttpSecurity http) {
        return http
                .authorizeExchange(exchanges -> exchanges
                        .anyExchange().authenticated()
                        .and()
                        .httpBasic()
                        .and()
                        .formLogin())
                .csrf().disable()
                .build();
    }

    static String role(String auth) {
        return "ROLE_" + auth;
    }

    //테스트용 사용자
    @Bean
    CommandLineRunner userLoader(MongoOperations operations) {
        return args -> {
            operations.save(new com.example.Security.User( //
                    "greg", passwordEncoder().encode("password"), Arrays.asList(role(USER))));

            operations.save(new com.example.Security.User( //
                    "manager", passwordEncoder().encode("password"), Arrays.asList(role(USER), role(INVENTORY))));
        };
    }

    /*
    //테스트용 사용자
    @Bean
    CommandLineRunner userLoader(MongoOperations operations) {
        return args -> {
            operations.save(new com.example.Security.User(
                    "guest",
                    passwordEncoder.encode("user"),
                    Arrays.asList(role(USER))
            ));

            operations.save(new com.example.Security.User(
                    "manager",
                    passwordEncoder.encode("admin"),
                    Arrays.asList(role(USER),role(INVENTORY))//**역할을 두 가지 이상 가질 수 있다.
            ));
        };
    }
     */
}
