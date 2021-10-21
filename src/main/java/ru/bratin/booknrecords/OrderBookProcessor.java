package ru.bratin.booknrecords;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class OrderBookProcessor {

    class ComparatorAsc implements Comparator<Order> {

        @Override
        public int compare(Order o1, Order o2) {
            var price = o1.price.compareTo(o2.price);
            var time = Long.signum(o1.timestamp - o2.timestamp);
            return Integer.signum(price + time);
        }
    }

    class ComparatorDesc implements Comparator<Order> {

        @Override
        public int compare(Order o1, Order o2) {
            var price = 0 - o1.price.compareTo(o2.price);
            var time = Long.signum(o1.timestamp - o2.timestamp);
            return Integer.signum(price + time);
        }
    }

    class OrderBook {

        private final String id;

        private final PriorityQueue<Order> buys = new PriorityQueue<>(new ComparatorDesc());
        private final PriorityQueue<Order> sells = new PriorityQueue<>(new ComparatorAsc());

        OrderBook(String id) {
            this.id = id;
        }

        public void addOrder(Order order) {
            switch (order.operation) {
                case SELL:
                    //O(1)
                    matchOrders(buys, order, (buy, sell) -> buy.price.compareTo(sell.price) >= 0);
                    if(order.volume > 0)
                        //O(ln(n))
                        sells.offer(order);
                    break;

                case BUY:
                    //O(1)
                    matchOrders(sells, order, (sell, buy) -> buy.price.compareTo(sell.price) >= 0);
                    if(order.volume > 0)
                        //O(ln(n))
                        buys.offer(order);
                    break;
            }
        }

        // O(2n)
        public void deleteOrder(int orderId) {
            buys.removeIf(o -> o.orderId == orderId);
            sells.removeIf(o -> o.orderId == orderId);
        }

        private void matchOrders(PriorityQueue<Order> orders, Order counterPartyOrder, BiPredicate<Order, Order> matcher) {
            //O(1)
            var order = orders.peek();
            while(order != null && matcher.test(order, counterPartyOrder)) {
                var vol = Math.min(order.volume, counterPartyOrder.volume);
                order.volume = order.volume - vol;
                counterPartyOrder.volume = counterPartyOrder.volume - vol;

                if (order.volume == 0) {
                    //O(1)
                    orders.poll();
                    //O(1)
                    order = orders.peek();
                } else {
                    break;
                }
            }
        }
    }

    class Order {

        final String book;
        final int orderId;
        final Operation operation;
        final BigDecimal price;
        final long timestamp;

        int volume;

        public Order(String book, int orderId, Operation operation, BigDecimal price, int volume) {
            this.book = book;
            this.orderId = orderId;
            this.operation = operation;
            this.price = price;
            this.volume = volume;
            this.timestamp = System.nanoTime();
        }

        @Override
        public String toString() {
            return volume + "@" + price;
        }
    }

    private class BooksHolder {
        private final HashMap<String, OrderBook> books = new HashMap<>();

        public void addOrder(Order order) {
            //O(1)
            var book = books.computeIfAbsent(order.book, OrderBook::new);
            //O()
            book.addOrder(order);
        }

        public void deleteOrder(OrderDeleteRequest request) {
            //O(1)
            var book = books.computeIfAbsent(request.book, OrderBook::new);
            //O(2n)
            book.deleteOrder(request.orderId);
        }
    }

    class OrderDeleteRequest {
        final String book;

        final int orderId;
        public OrderDeleteRequest (String book, int orderId) {
            this.book = book;
            this.orderId = orderId;
        }

    }
    enum Operation {
        BUY, SELL

    }

    private class XMLElementsHandler extends DefaultHandler {

        private final Consumer<Order> addOrderConsumer;
        private final Consumer<OrderDeleteRequest> dellOrderConsumer;

        private XMLElementsHandler(Consumer<Order> addOrderConsumer, Consumer<OrderDeleteRequest> dellOrderConsumer) {
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

    private final Path messagesFile;

    public OrderBookProcessor(Path file) {
        this.messagesFile = file;
    }

    public BooksHolder processOrderMessages() {
        var holder = new BooksHolder();

        /*
         * Decided to leave processing single threaded
         * because at this data volume
         * concurrency does not give performance profit
         */
        readMessages(

                /* gives moderate time complexity */
                holder::addOrder,

                /* the most time consuming operation.
                 * can be optimized by registering settled orders ids in HasSet
                 * to avoid unnecessary search in queues */
                holder::deleteOrder
        );

        return holder;
    }

    private void readMessages(Consumer<Order> addOrderConsumer, Consumer<OrderDeleteRequest> dellOrderConsumer) {
        try {
            var parser = SAXParserFactory.newDefaultInstance().newSAXParser();
            var xmlElementsHandler = new XMLElementsHandler(addOrderConsumer, dellOrderConsumer);
            parser.parse(Files.newInputStream(messagesFile), xmlElementsHandler);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Testing
     */
    public static void main(String[] args) {
        var startTime = System.nanoTime();

        var file = getFileArg(args);

        var processor = new OrderBookProcessor(file);

        var booksHolder = processor.processOrderMessages();

        var finishTime = System.nanoTime();

        /*
         * on my laptop this implementation completes less that 0.1s (88964601 nanos)
         */
        out(" ");
        out("Done in " + (finishTime - startTime) + " nanosecons");
        out(" ");

        for(OrderBook book : booksHolder.books.values()) {
            out("book: " + book.id);
            out("   Buy -- Sell  ");
            out("================");

            var isells = book.sells.iterator();
            var ibuys = book.buys.iterator();

            for (int i = 0; i < Math.max(book.sells.size(), book.buys.size()); i++) {
                out((ibuys.hasNext() ? ibuys.next().toString() : "") + " -- " + (isells.hasNext() ? isells.next().toString() : ""));
            }

            out(" ");
        }
    }

    private static void out(String val) {
        System.out.println(val);
    }

    private static Path getFileArg(String[] args) {
        if(args.length < 1)
            throw new IllegalArgumentException();

        var path = Path.of(args[0]);

        if(!Files.exists(path) || Files.isDirectory(path) || !Files.isReadable(path))
            throw new IllegalArgumentException();

        return path;
    }
}
