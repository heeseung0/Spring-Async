package com.greglturnquist.hackingspringboot.reactive;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Mono;

@Service
public class CartService {
    private final ItemRepository itemRepository;
    private final CartRepository cartRepository;

    CartService(ItemRepository itemRepository, CartRepository cartRepository){
        this.itemRepository = itemRepository;
        this.cartRepository = cartRepository;
    }

    Mono<Cart> addToCart(String cartId, String id) {
        return this.cartRepository.findById(cartId)
                .defaultIfEmpty(new Cart(cartId)).log("emptyCart")  //로그1
                .flatMap(cart -> cart.getCartItems().stream()
                        .filter(cartItem -> cartItem.getItem().getId().equals(id))
                        .findAny()
                        .map(cartItem -> {
                            cartItem.increment();
                            return Mono.just(cart).log("newCartItem");  //로그2
                        })
                        .orElseGet(() -> this.itemRepository.findById(id).log("fetchedItem")    //로그3
                                .map(CartItem::new).log("cartItem") //로그4
                                .doOnNext(cartItem -> cart.getCartItems().add(cartItem)).log("addedCartItem")   //로그5
                                .map(cartItem -> cart)
                        )
                ).log("cartWithAnotherItem")    //로그6
                .flatMap(this.cartRepository::save).log("savedCart");    //로그7
    }
}
