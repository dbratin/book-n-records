package ru.bratin.booknrecords.service.order.listener;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import ru.bratin.booknrecords.domain.Operation;
import ru.bratin.booknrecords.domain.Order;
import ru.bratin.booknrecords.domain.OrderDeleteRequest;

import java.math.BigDecimal;
import java.util.function.Consumer;

class XMLElementsHandler extends DefaultHandler {

    private final Consumer<Order> addOrderConsumer;
    private final Consumer<OrderDeleteRequest> dellOrderConsumer;

    XMLElementsHandler(Consumer<Order> addOrderConsumer, Consumer<OrderDeleteRequest> dellOrderConsumer) {
        this.addOrderConsumer = addOrderConsumer;
        this.dellOrderConsumer = dellOrderConsumer;
    }

    @Override
    public void startElement(String uri, String lName, String qName, Attributes attr) {
        switch (qName) {
            case "AddOrder": addOrderConsumer.accept(createOrder(attr)); break;
            case "DeleteOrder": dellOrderConsumer.accept(createDeleteRequest(attr)); break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {

    }

    private OrderDeleteRequest createDeleteRequest(Attributes attr) {
        return new OrderDeleteRequest(
                attr.getValue("book"),
                Integer.parseInt(attr.getValue("orderId"))
        );
    }

    private Order createOrder(Attributes attr) {
        return new Order(
                attr.getValue("book"),
                Integer.parseInt(attr.getValue("orderId")),
                Operation.valueOf(attr.getValue("operation")),
                new BigDecimal(attr.getValue("price").trim()),
                Integer.parseInt(attr.getValue("volume"))
        );
    }
}