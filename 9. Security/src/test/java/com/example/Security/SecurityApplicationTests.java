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
	@WithMockUser(username = "alice", roles = { "SOME_OTHER_ROLE" }) // <1>
	void addingInventoryWithoutProperRoleFails() {
		this.webTestClient //
				.post().uri("/api/items/add") // <2>
				.contentType(MediaType.APPLICATION_JSON) //
				.bodyValue("{" + //
						"\"name\": \"iPhone X\", " + //
						"\"description\": \"upgrade\", " + //
						"\"price\": 999.99" + //
						"}") //
				.exchange() //
				.expectStatus().isForbidden(); // <3>
	}

}
