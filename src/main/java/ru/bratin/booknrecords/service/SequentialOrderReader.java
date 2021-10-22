package ru.bratin.booknrecords.service;

import ru.bratin.booknrecords.domain.Order;
import ru.bratin.booknrecords.domain.OrderDeleteRequest;

import java.util.function.Consumer;

public interface SequentialOrderReader extends AutoCloseable {
    void readTo(Consumer<Order> orderConsumer, Consumer<OrderDeleteRequest> requestConsumer);
}
