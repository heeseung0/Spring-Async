package com.greglturnquist.hackingspringboot.rsocket_client;
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootTest
@AutoConfigureWebTestClient
public class RSocketTest {
    @Autowired WebTestClient webTestClient;
    @Autowired ItemRepository repository;

    @Test
    void verifyRemoteOperationsThroughRSocketRequestResponse() throws InterruptedException{
        //데이터 초기화
        this.repository.deleteAll()
                .as(StepVerifier::create)
                .verifyComplete();

        //새 Item 생성
        this.webTestClient.post().uri("/items/request-response")
                .bodyValue(new Item("Alf alarm clock", "nothing important", 19.99))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Item.class)
                .value(item -> {
                    assertThat(item.getId()).isNotNull();
                    assertThat(item.getName()).isEqualTo("Alf alarm clock");
                    assertThat(item.getDescription()).isEqualTo("nothing important");
                    assertThat(item.getPrice()).isEqualTo(19.99);
                });

        Thread.sleep(500);  //스레드를 잠시 중지해서 새 Item이 R소켓 서버를 거쳐 MongoDB에 저장될 시간 여유를 줌

        //Item이 DB에 저장됐는지 확인
        this.repository.findAll()
                .as(StepVerifier::create)
                .expectNextMatches(item -> {
                    assertThat(item.getId()).isNotNull();
                    assertThat(item.getName()).isEqualTo("Alf alarm clock");
                    assertThat(item.getDescription()).isEqualTo("nothing important");
                    assertThat(item.getPrice()).isEqualTo(19.99);
                    return true;
                })
                .verifyComplete();

    }
}
