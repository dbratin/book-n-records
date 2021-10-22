package ru.bratin.booknrecords;

import ru.bratin.booknrecords.domain.BooksHolder;
import ru.bratin.booknrecords.domain.OrderBook;
import ru.bratin.booknrecords.service.order.listener.XmlOrderReader;

import java.io.IOException;
import java.nio.file.Files;

import static ru.bratin.booknrecords.service.CliArgsHelper.getFileArg;

public class OrderBookProcessor {

    public static BooksHolder doProcessing(String[] args) {
        var file = getFileArg(args);

        var booksHolder = new BooksHolder();

        try(var reader = new XmlOrderReader(Files.newInputStream(file))) {
            reader.readTo(booksHolder::addOrder, booksHolder::deleteOrder);
        } catch (IOException e) {
            out("Error reading file: " + e.getMessage());
            System.exit(1);
        }

        return booksHolder;
    }

    /*
     * Testing
     */
    public static void main(String[] args) {
        var startTime = System.nanoTime();

        var booksHolder = doProcessing(args);

        var finishTime = System.nanoTime();

        /*
         * on my laptop this implementation completes less that 0.1s (88964601 nanos)
         */
        out(" ");
        out("Done in " + (finishTime - startTime) + " nanosecons");
        out(" ");

        for(OrderBook book : booksHolder.getBooks().values()) {
            out("book: " + book.getId());
            out("   Buy -- Sell  ");
            out("================");

            var sells = book.getSells();
            var buys = book.getBuys();

            var isells = sells.iterator();
            var ibuys = buys.iterator();

            for (int i = 0; i < Math.max(sells.size(), buys.size()); i++) {
                out((ibuys.hasNext() ? ibuys.next().toString() : "") + " -- " + (isells.hasNext() ? isells.next().toString() : ""));
            }

            out(" ");
        }
    }

    private static void out(String val) {
        System.out.println(val);
    }
}
