package com.greglturnquist.hackingspringboot.reactive;

import java.util.Objects;

public class CartItem {
    private Item item;
    private int quantity;

    private CartItem(){}

    CartItem(Item item){
        this.item = item;
        this.quantity = 1;
    }

    public void increment() {
        this.quantity++;
    }

    public void decrement() {
        this.quantity--;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return getQuantity() == cartItem.getQuantity() && Objects.equals(getItem(), cartItem.getItem());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getItem(), getQuantity());
    }
}
