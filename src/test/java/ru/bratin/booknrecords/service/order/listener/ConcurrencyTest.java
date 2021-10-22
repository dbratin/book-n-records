package ru.bratin.booknrecords.service.order.listener;

import org.junit.jupiter.api.Test;
import ru.bratin.booknrecords.domain.Operation;
import ru.bratin.booknrecords.domain.Order;
import ru.bratin.booknrecords.domain.OrderBook;

import java.math.BigDecimal;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.bratin.booknrecords.domain.Operation.BUY;
import static ru.bratin.booknrecords.domain.Operation.SELL;

public class ConcurrencyTest {

    private final AtomicInteger idSequence = new AtomicInteger(10);

    @Test
    public void orderBookConcurrentModificationTest() throws InterruptedException, ExecutionException {
        var book = new OrderBook("book-1");

        Callable<Boolean> sellingJob = () -> repeat(() -> book.addOrder(newOrder("book-1", SELL, BigDecimal.TEN, 10)));
        Callable<Boolean> buyingJob = () -> repeat(() -> book.addOrder(newOrder("book-1", BUY, BigDecimal.TEN, 10)));

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            var results = executor.invokeAll(List.of(buyingJob, sellingJob));
            /*
             * expect that result will look like [true, false]
             * there for sum if booleans converted to ints should be equal to 1
             */
            var sum = 0;
            for (var f : results) {
                sum += f.get() ? 1 : 0;
            }
            assertEquals(1, sum);
        } finally {
            executor.shutdown();
        }

        /*
         * Expected that collision of jobs will prevent one job of doing anything
         * therefore number of positions in one book is 10 and 0 in another
         */
        assertEquals(10, Math.abs(book.getSells().size() - book.getBuys().size()));
    }

    /*
     * returns TRUE if any task run was failed with ConcurrentModificationException
     */
    private boolean repeat(Runnable task) {
        try {
            for (int i = 0; i < 10; i++) task.run();
            return false;
        } catch (ConcurrentModificationException e) {
            return true;
        }
    }

    private Order newOrder(String bookId, Operation operation, BigDecimal price, int volume) {
        return new Order(bookId, nextId(), operation, price, volume);
    }

    private int nextId() {
        return idSequence.incrementAndGet();
    }
}
