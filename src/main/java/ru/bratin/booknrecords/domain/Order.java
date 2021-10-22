package ru.bratin.booknrecords.domain;

import java.math.BigDecimal;
import java.util.Objects;

public class Order {

    final String book;
    final int orderId;
    final Operation operation;
    final BigDecimal price;
    final long timestamp;

    private int volume;

    public Order(String book, int orderId, Operation operation, BigDecimal price, int volume) {
        this.book = book;
        this.orderId = orderId;
        this.operation = operation;
        this.price = price;
        this.volume = volume;
        this.timestamp = System.nanoTime();
    }

    public int withdrawVolume(int vol) {
        if(vol > volume)
            throw new IllegalArgumentException("Not enough of volume to withdraw");

        volume = volume - vol;

        return volume;
    }

    public int getVolume() {
        return volume;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return orderId == order.orderId &&
                Objects.equals(book, order.book) &&
                operation == order.operation &&
                price.compareTo(order.price) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(book, orderId, operation, price, volume);
    }

    @Override
    public String toString() {
        return volume + "@" + price;
    }
}