package ru.bratin.booknrecords.domain;

import java.util.Objects;

public class OrderDeleteRequest {
    final String book;

    final int orderId;
    public OrderDeleteRequest (String book, int orderId) {
        this.book = book;
        this.orderId = orderId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderDeleteRequest that = (OrderDeleteRequest) o;
        return orderId == that.orderId &&
                Objects.equals(book, that.book);
    }

    @Override
    public int hashCode() {
        return Objects.hash(book, orderId);
    }

    @Override
    public String toString() {
        return "OrderDeleteRequest{" +
                "book='" + book + '\'' +
                ", orderId=" + orderId +
                '}';
    }
}
