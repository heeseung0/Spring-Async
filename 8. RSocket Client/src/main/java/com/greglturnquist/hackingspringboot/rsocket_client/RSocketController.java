package com.greglturnquist.hackingspringboot.rsocket_client;

import static io.rsocket.metadata.WellKnownMimeType.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.*;

import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Duration;

@RestController
public class RSocketController {
    private final Mono<RSocketRequester> requester;

    public RSocketController(RSocketRequester.Builder builder) {
        this.requester = builder
                .dataMimeType(APPLICATION_JSON)
                .metadataMimeType(parseMediaType(MESSAGE_RSOCKET_ROUTING.toString()))
                .connectTcp("localhost", 7000)
                .retry(5)   //메시지 처리 실패 시 Mono가 5번까지 재시도
                .cache();
    }

    @PostMapping("/items/request-response")
    Mono<ResponseEntity<?>> addNewItemUsingRSocketRequestResponse(@RequestBody Item item) {
        return this.requester //
                .flatMap(rSocketRequester -> rSocketRequester
                        .route("newItems.request-response")
                        .data(item)
                        .retrieveMono(Item.class))  //Mono<Item> 응답을 원한 다는 뜻
                .map(savedItem -> ResponseEntity.created(
                        URI.create("/items/request-response")).body(savedItem));    //HTTP 201 Created 응답 반환
    }
}
