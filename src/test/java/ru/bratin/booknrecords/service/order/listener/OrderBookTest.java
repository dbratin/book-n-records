package ru.bratin.booknrecords.service.order.listener;

import org.junit.jupiter.api.Test;
import ru.bratin.booknrecords.domain.Operation;
import ru.bratin.booknrecords.domain.Order;
import ru.bratin.booknrecords.domain.OrderBook;
import ru.bratin.booknrecords.domain.OrderDeleteRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderBookTest {

    @Test
    public void simplyAddBuySellOrdersTest() {
        var expectedBuyOrders = List.of(
                new Order("book-1", 4, Operation.BUY, BigDecimal.valueOf(9), 100),
                new Order("book-1", 5, Operation.BUY, BigDecimal.valueOf(8), 150),
                new Order("book-1", 7, Operation.BUY, BigDecimal.valueOf(7), 200)
        );

        var expectedSellOrders = List.of(
                new Order("book-1", 1, Operation.SELL, BigDecimal.valueOf(10), 10),
                new Order("book-1", 2, Operation.SELL, BigDecimal.valueOf(11), 20),
                new Order("book-1", 3, Operation.SELL, BigDecimal.valueOf(12), 20),
                new Order("book-1", 6, Operation.SELL, BigDecimal.valueOf(13), 20)
        );

        var all = new ArrayList<Order>();

        all.addAll(expectedBuyOrders);
        all.addAll(expectedSellOrders);

        Collections.shuffle(all);

        var book = new OrderBook("book-1");

        for (var order : all) {
            book.addOrder(order);
        }

        assertEquals(expectedSellOrders, book.getSells(), "Sell orders are not match");
        assertEquals(expectedBuyOrders, book.getBuys(), "Buy orders are not match");
    }

    @Test
    public void simplyAddAndDeleteBuySellOrdersTest() {
        var expectedBuyOrders = List.of(
                new Order("book-1", 5, Operation.BUY, BigDecimal.valueOf(8), 150),
                new Order("book-1", 7, Operation.BUY, BigDecimal.valueOf(7), 200)
        );

        var expectedSellOrders = List.of(
                new Order("book-1", 2, Operation.SELL, BigDecimal.valueOf(11), 20)
        );

        var all = new ArrayList<>(List.of(
                new Order("book-1", 1, Operation.SELL, BigDecimal.valueOf(10), 10),
                new Order("book-1", 2, Operation.SELL, BigDecimal.valueOf(11), 20),
                new Order("book-1", 3, Operation.SELL, BigDecimal.valueOf(12), 20),
                new Order("book-1", 4, Operation.BUY, BigDecimal.valueOf(9), 100),
                new Order("book-1", 5, Operation.BUY, BigDecimal.valueOf(8), 150),
                new Order("book-1", 7, Operation.BUY, BigDecimal.valueOf(7), 200)
        ));

        var requests = List.of(
                new OrderDeleteRequest("book-1", 1),
                new OrderDeleteRequest("book-1", 4),
                new OrderDeleteRequest("book-1", 3),

                // try already deleted
                new OrderDeleteRequest("book-1", 4),

                // try none existed
                new OrderDeleteRequest("book-1", 14)
        );

        var book = new OrderBook("book-1");

        for (var order : all) {
            book.addOrder(order);
        }

        for (var request: requests) {
            book.deleteOrder(request);
        }

        assertEquals(expectedSellOrders, book.getSells(), "Sell orders are not match");
        assertEquals(expectedBuyOrders, book.getBuys(), "Buy orders are not match");
    }

    @Test
    public void addPriceMatchingBuySellOrdersTest() {
        var expectedBuyOrders = List.of(
                new Order("book-1", 4, Operation.BUY, BigDecimal.valueOf(7), 100),
                new Order("book-1", 5, Operation.BUY, BigDecimal.valueOf(7), 150),
                new Order("book-1", 7, Operation.BUY, BigDecimal.valueOf(7), 200)
        );

        var expectedSellOrders = List.of(
                new Order("book-1", 1, Operation.SELL, BigDecimal.valueOf(10), 10),
                new Order("book-1", 2, Operation.SELL, BigDecimal.valueOf(10), 20),
                new Order("book-1", 3, Operation.SELL, BigDecimal.valueOf(10), 30),
                new Order("book-1", 6, Operation.SELL, BigDecimal.valueOf(10), 40)
        );

        var all = new ArrayList<Order>();

        all.addAll(expectedBuyOrders);
        all.addAll(expectedSellOrders);

        Collections.shuffle(all);

        var book = new OrderBook("book-1");

        for (var order : all) {
            book.addOrder(order);
        }

        assertEquals(expectedSellOrders, book.getSells(), "Sell orders are not match");
        assertEquals(expectedBuyOrders, book.getBuys(), "Buy orders are not match");
    }

    @Test
    public void addAndMatchBuyToSellOrdersTest() {
        var expectedBuyOrders = List.of(
                new Order("book-1", 4, Operation.BUY, BigDecimal.valueOf(7), 10)
        );

        var expectedSellOrders = List.of(
                new Order("book-1", 2, Operation.SELL, BigDecimal.valueOf(8), 5),
                new Order("book-1", 6, Operation.SELL, BigDecimal.valueOf(9), 40)
        );

        var all = new ArrayList<>(List.of(
                new Order("book-1", 6, Operation.SELL, BigDecimal.valueOf(9), 40),
                new Order("book-1", 2, Operation.SELL, BigDecimal.valueOf(8), 20),
                new Order("book-1", 1, Operation.SELL, BigDecimal.valueOf(6), 10),

                new Order("book-1", 5, Operation.BUY, BigDecimal.valueOf(8), 5),
                new Order("book-1", 7, Operation.BUY, BigDecimal.valueOf(9), 20),
                new Order("book-1", 4, Operation.BUY, BigDecimal.valueOf(7), 10)
        ));

        var book = new OrderBook("book-1");

        for (var order : all) {
            book.addOrder(order);
        }

        assertEquals(expectedSellOrders, book.getSells(), "Sell orders are not match");
        assertEquals(expectedBuyOrders, book.getBuys(), "Buy orders are not match");
    }

    @Test
    public void addAndMatchSellToBuyOrdersTest() {
        var expectedBuyOrders = List.of(
                new Order("book-1", 5, Operation.BUY, BigDecimal.valueOf(8), 1),
                new Order("book-1", 4, Operation.BUY, BigDecimal.valueOf(7), 10)
        );

        var expectedSellOrders = List.of(
                new Order("book-1", 2, Operation.SELL, BigDecimal.valueOf(8.5), 20)
        );

        var all = new ArrayList<>(List.of(
                new Order("book-1", 7, Operation.BUY, BigDecimal.valueOf(9), 11),
                new Order("book-1", 5, Operation.BUY, BigDecimal.valueOf(8), 5),
                new Order("book-1", 4, Operation.BUY, BigDecimal.valueOf(7), 10),

                new Order("book-1", 6, Operation.SELL, BigDecimal.valueOf(9), 10),
                new Order("book-1", 1, Operation.SELL, BigDecimal.valueOf(6), 5),
                new Order("book-1", 2, Operation.SELL, BigDecimal.valueOf(8.5), 20)
        ));

        var book = new OrderBook("book-1");

        for (var order : all) {
            book.addOrder(order);
        }

        assertEquals(expectedSellOrders, book.getSells(), "Sell orders are not match");
        assertEquals(expectedBuyOrders, book.getBuys(), "Buy orders are not match");
    }
}
