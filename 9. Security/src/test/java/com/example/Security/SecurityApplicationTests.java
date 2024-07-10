package com.example.Security;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureWebTestClient
class SecurityApplicationTests {
	@Autowired WebTestClient webTestClient;
	@Autowired ItemRepository repository;

	@Test
	@WithMockUser(username = "alice", roles = {"SOME_OTHER_ROLE"})//스프링 시큐리티의 WithMockUser를 사용해서 가짜 사용자로 테스트
	//HTTP 403 Forbidden은 사용자가 인증은 됐지만(authenticated), 특정 웹 호출을 할 수 있도록 인가(승인)받지는 못한(not authorized)상태이다.
	void addingInventoryWithoutProperRoleFails(){
		this.webTestClient.post().uri("/")//'/'에 POST요청
				.exchange()//서버에게 요청을 전송하고 응답을 받는다.
				.expectStatus().isForbidden();//HTTP 403 Forbidden 상태코드가 반환되었는지 확인
	}

	@Disabled
	@Test
	@WithMockUser(username = "bob", roles = {"INVENTORY"})
	//정상적으로 인증 -> 승인까지 완료
	void addingInventoryWithProperRoleSucceeds(){
	 	this.webTestClient.post().uri("/")
				.contentType(MediaType.APPLICATION_JSON)
				.bodyValue("{" +
						"\"name\": \"iPhone 11\", " +
						"\"description\": \"upgrade\", " +
						"\"price\": 999.99" +
						"}")
				.exchange()
				.expectStatus().isOk();

		this.repository.findByName("iPhone 11")
				.as(StepVerifier::create)
				.expectNextMatches(item -> {
					assertThat(item.getDescription()).isEqualTo("upgrade");
					assertThat(item.getPrice()).isEqualTo(999.99);
					return true;
				})
				.verifyComplete();
	}

}
