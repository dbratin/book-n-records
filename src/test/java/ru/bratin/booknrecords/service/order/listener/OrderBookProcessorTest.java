package ru.bratin.booknrecords.service.order.listener;

import org.junit.jupiter.api.Test;
import ru.bratin.booknrecords.OrderBookProcessor;
import ru.bratin.booknrecords.domain.Operation;
import ru.bratin.booknrecords.domain.Order;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class OrderBookProcessorTest {

    @Test
    public void regularCaseTest() {
        var orderBooksHolder = OrderBookProcessor.doProcessing(new String[]{"./target/test-classes/test-orders.xml"});

        var book1 = orderBooksHolder.getBooks().get("book-1");
        assertNotNull(book1);
        assertEquals(List.of(
                new Order("book-1", 15, Operation.BUY, BigDecimal.valueOf(100.5), 80),
                new Order("book-1", 14, Operation.BUY, BigDecimal.valueOf(98.0), 9)
        ), book1.getBuys());
        assertEquals(List.of(
                new Order("book-1", 13, Operation.SELL, BigDecimal.valueOf(100.51), 90)
        ), book1.getSells());

        var book2 = orderBooksHolder.getBooks().get("book-2");
        assertNotNull(book2);
        assertEquals(List.of(
                new Order("book-2", 23, Operation.BUY, BigDecimal.valueOf(90.50), 45)
        ), book2.getBuys());
        assertEquals(List.of(
                new Order("book-2", 21, Operation.SELL, BigDecimal.valueOf(100.50), 18),
                new Order("book-2", 22, Operation.SELL, BigDecimal.valueOf(101.50), 10)
        ), book2.getSells());

        var book3 = orderBooksHolder.getBooks().get("book-3");
        assertNotNull(book3);
        assertEquals(List.of(
                new Order("book-3", 34, Operation.BUY, BigDecimal.valueOf(99.60), 79),
                new Order("book-3", 36, Operation.BUY, BigDecimal.valueOf(99.60), 80)
        ), book3.getBuys());
        assertEquals(List.of(), book3.getSells());
    }
}
