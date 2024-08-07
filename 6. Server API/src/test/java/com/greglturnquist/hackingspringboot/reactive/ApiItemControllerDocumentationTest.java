/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.greglturnquist.hackingspringboot.reactive;

import static org.mockito.Mockito.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.*;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(controllers = ApiItemController.class)
@AutoConfigureRestDocs
public class ApiItemControllerDocumentationTest {
	@Autowired private WebTestClient webTestClient;
	@MockBean InventoryService service;
	@MockBean ItemRepository repository;
	
	@Test
	void findingAllItems() {
		when(repository.findAll()).thenReturn(
				Flux.just(new Item("item-1", "Alf alarm clock",
						"nothing I really need", 19.99)));

		this.webTestClient.get().uri("/api/items")
				.exchange() //
				.expectStatus().isOk() //
				.expectBody() //
				.consumeWith(document("findAll", preprocessResponse(prettyPrint())));	//스프링 레스트 독이 적용되는 지점(target/generated-snippets/findAll)
	}

	@Test
	void postNewItem() {
		when(repository.save(any())).thenReturn(
				Mono.just(new Item("1", "Alf alarm clock", "nothing important", 19.99)));

		this.webTestClient.post().uri("/api/items")
				.bodyValue(new Item("Alf alarm clock", "nothing important", 19.99))
				.exchange()
				.expectStatus().isCreated()
				.expectBody()
				.consumeWith(document("post-new-item", preprocessResponse(prettyPrint())));
	}
}
