package ru.bratin.booknrecords.service.order.listener;

import org.xml.sax.SAXException;
import ru.bratin.booknrecords.domain.Order;
import ru.bratin.booknrecords.domain.OrderDeleteRequest;
import ru.bratin.booknrecords.service.SequentialOrderReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public class XmlOrderReader implements SequentialOrderReader {

    private final InputStream messagesStream;

    public XmlOrderReader(InputStream messagesStream) {
        this.messagesStream = messagesStream;
    }

    @Override
    public void readTo(Consumer<Order> orderConsumer, Consumer<OrderDeleteRequest> requestConsumer) {
        try(messagesStream) {
            var parser = SAXParserFactory.newDefaultInstance().newSAXParser();
            var xmlElementsHandler = new XMLElementsHandler(orderConsumer, requestConsumer);
            parser.parse(messagesStream, xmlElementsHandler);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        //resources are closed
    }
}
