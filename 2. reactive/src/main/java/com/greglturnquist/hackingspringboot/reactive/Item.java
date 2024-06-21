package com.greglturnquist.hackingspringboot.reactive;

import org.springframework.data.annotation.Id;

import java.util.Objects;

public class Item {
    private @Id String id;
    private String name;
    private double price;

    private Item(){}

    Item(String name, double price){
        this.name = name;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Double.compare(item.getPrice(), getPrice()) == 0 && Objects.equals(getId(), item.getId()) && Objects.equals(getName(), item.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getPrice());
    }
}
