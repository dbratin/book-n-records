package ru.bratin.booknrecords.service.order.listener;

import org.junit.jupiter.api.Test;
import ru.bratin.booknrecords.domain.Order;
import ru.bratin.booknrecords.domain.OrderDeleteRequest;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.bratin.booknrecords.domain.Operation.BUY;
import static ru.bratin.booknrecords.domain.Operation.SELL;

class XmlOrderReaderTest {

    @Test
    void readRecords() {
        var actualOrders = new ArrayList<Order>(3);
        var expectedOrders = List.of(
                new Order("book-1", 1, SELL, BigDecimal.valueOf(100.5), 81),
                new Order("book-1", 3, BUY, BigDecimal.valueOf(99.7), 16),
                new Order("book-3", 4, SELL, BigDecimal.valueOf(100.0), 80)
        );

        var actualRequests = new ArrayList<OrderDeleteRequest>(2);
        var expectedRequest = List.of(
                new OrderDeleteRequest("book-1", 3),
                new OrderDeleteRequest("book-2", 30)
        );

        var input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<Orders>\n" +
                "<AddOrder book=\"book-1\" operation=\"SELL\" price=\"100.5\" volume=\"81\" orderId=\"1\" />\n" +
                "<DeleteOrder book=\"book-1\" orderId=\"3\" />\n" +
                "<AddOrder book=\"book-1\" operation=\"BUY\" price=\" 99.7\" volume=\"16\" orderId=\"3\" />\n" +
                "<AddOrder book=\"book-3\" operation=\"SELL\" price=\"100.0\" volume=\"80\" orderId=\"4\" />\n" +
                "<DeleteOrder book=\"book-2\" orderId=\"30\" />\n" +
                "</Orders>";

        var reader = new XmlOrderReader(new ByteArrayInputStream(input.getBytes()));

        reader.readTo(actualOrders::add, actualRequests::add);

        assertEquals(expectedOrders, actualOrders, "Read orders not match");
        assertEquals(expectedRequest, actualRequests, "Read requests not match");

    }
}