package com.greglturnquist.hackingspringboot.reactive.domain;

public class Dish {
    private String description;
    private boolean delivered = false;

    public static Dish deliver(Dish dish) {
        Dish deliverDish = new Dish(dish.description);
        deliverDish.delivered = true;
        return deliverDish;
    }

    public Dish(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void SetDescription(String description) {
        this.description = description;
    }

    public boolean isDelivered() {
        return delivered;
    }

    @Override
    public String toString() {
        return "Dish{" + "description='" + description + '\'' + ", delivered=" + delivered + '}';
    }
}
