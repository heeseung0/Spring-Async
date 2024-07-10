package com.example.Security;

import org.springframework.data.repository.CrudRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends CrudRepository<User, String>{
    Mono<User> findByName(String name); //스프링 시큐리티는 username 기준으로 사용자를 찾는다.
}
