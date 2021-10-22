package ru.bratin.booknrecords.domain;

import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class BooksHolder {
    private final AtomicBoolean somebodyWrites = new AtomicBoolean(false);
    private final HashMap<String, OrderBook> books = new HashMap<>();

    public void addOrder(Order order) {
        if (!somebodyWrites.compareAndSet(false, true))
            throw new ConcurrentModificationException();

        try {
            //O(1)
            var book = books.computeIfAbsent(order.book, OrderBook::new);
            //O()
            book.addOrder(order);
        } finally {
            somebodyWrites.set(false);
        }
    }

    public void deleteOrder(OrderDeleteRequest request) {
        if (!somebodyWrites.compareAndSet(false, true))
            throw new ConcurrentModificationException();

        try {
            //O(1)
            var book = books.computeIfAbsent(request.book, OrderBook::new);
            //O(2n)
            book.deleteOrder(request);
        } finally {
            somebodyWrites.set(false);
        }
    }

    public Map<Object, OrderBook> getBooks() {
        return Collections.unmodifiableMap(books);
    }
}
