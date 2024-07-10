package com.example.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.Arrays;

@Configuration
public class SecurityConfig {
    //There is no PasswordEncoder mapped for the id "null" <<-- 에러 해결용
    @Autowired PasswordEncoder passwordEncoder;

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
                        .pathMatchers(HttpMethod.POST, "/").hasRole(INVENTORY)//'/'로 들어오는 POST, '/**'로 들어오는 DELETE 요청이
                        .pathMatchers(HttpMethod.DELETE, "/**").hasRole(INVENTORY)//ROLE_INVENTORY라는 역할을 가진 사용자로부터 전송되었을 때만 진입을 허용
                        .anyExchange().authenticated()//위 규칙에 어긋나는 모든 요청은 이 지점에서 더 이상 전진할 수 없다. 인증을 반드시 거쳐야 한다.
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
}
