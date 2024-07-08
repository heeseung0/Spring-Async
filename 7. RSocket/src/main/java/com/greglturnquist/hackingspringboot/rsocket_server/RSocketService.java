package com.greglturnquist.hackingspringboot.rsocket_server;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Service;
import reactor.core.publisher.*;

@Service
public class RSocketService {
    private final ItemRepository repository;
    private final EmitterProcessor<Item> itemProcessor;
    private final FluxSink<Item> itemSink;

    public RSocketService(ItemRepository repository) {
        this.repository = repository;
        this.itemProcessor = EmitterProcessor.create(); //Deprecated인 FluxProcessor, EmitterProcessor의 대체 구현
        this.itemSink = this.itemProcessor.sink();  //this.itemsSink = Sinks.many().multicast().onBackpressureBuffer();
    }

    //요청 - 응답
    @MessageMapping("newItem.request-response")
    public Mono<Item> processNewItemsViaRSocketRequestResponse(Item item){
        return this.repository.save(item)
                .doOnNext(savedItem -> this.itemSink.next(savedItem));
    }

    //요청 - 스트림
    @MessageMapping("newItems.request-stream")
    public Flux<Item> findItemsViaRSocketRequestStream(){
        return this.repository.findAll()
                .doOnNext(this.itemSink::next);
    }

    //실행 후 망각
    @MessageMapping("newItems.fire-and-forget")
    public Mono<Void> processNewItemsViaRSocketFireAndForget(Item item){
        return this.repository.save(item)
                .doOnNext(savedItem -> this.itemSink.next(savedItem))
                .then();    //Then 연산자를 이용하면 Mono에 감싸진 데이터를 버린다.(리액티브 제어 신호만 남는다)
    }

    @MessageMapping("newItems.monitor")
    public Flux<Item> monitorNewItems() {   //예제는 요청 데이터가 없지만, 클라이언트가 데이터를 담아 요청 할 수도 있으므로 Mono가 아닌 Flux<Item>다
        return this.itemProcessor;  //return this.itemsSink.asFlux();
    }

}
