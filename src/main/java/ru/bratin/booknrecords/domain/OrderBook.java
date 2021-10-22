package ru.bratin.booknrecords.domain;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiPredicate;

/*
 Thread unsafe!
 */
public class OrderBook {

    private final AtomicBoolean somebodyWrites = new AtomicBoolean(false);

    private final String id;

    private final HashMap<Integer, Order> orderIndex = new HashMap<>();

    private final TreeSet<Order> buys = new TreeSet<>(new ComparatorDesc());
    private final TreeSet<Order> sells = new TreeSet<>(new ComparatorAsc());

    public OrderBook(String id) {
        this.id = id;
    }

    public void addOrder(Order order) {
        if(!somebodyWrites.compareAndSet(false, true))
            throw new ConcurrentModificationException();

        try {
            switch (order.operation) {
                case SELL:
                    //O(1)
                    matchOrders(buys, order, (buy, sell) -> buy.price.compareTo(sell.price) >= 0);
                    if (order.getVolume() > 0)
                        //O(log(n))
                        registerOrder(order);
                    break;

                case BUY:
                    //O(1)
                    matchOrders(sells, order, (sell, buy) -> buy.price.compareTo(sell.price) >= 0);
                    if (order.getVolume() > 0)
                        //O(log(n))
                        registerOrder(order);
                    break;
            }
        } finally {
            somebodyWrites.set(false);
        }
    }

    // O(log(n))
    public void deleteOrder(OrderDeleteRequest request) {
        if(!somebodyWrites.compareAndSet(false, true))
            throw new ConcurrentModificationException();

        try {
            var order = orderIndex.get(request.orderId);
            if (order != null) {
                deleteOrder(order);
            }
        } finally {
            somebodyWrites.set(false);
        }
    }

    public String getId() {
        return id;
    }

    public List<Order> getSells() {
        return List.copyOf(sells);
    }

    public List<Order> getBuys() {
        return List.copyOf(buys);
    }

    private void deleteOrder(Order order) {
        if(orderIndex.remove(order.orderId) != null) {
            switch (order.operation) {
                case SELL:
                    sells.remove(order);
                    break;
                case BUY:
                    buys.remove(order);
                    break;
            }
        }
    }

    private void registerOrder(Order order) {
        orderIndex.put(order.orderId, order);
        switch (order.operation) {
            case SELL:
                sells.add(order);
                break;
            case BUY:
                buys.add(order);
                break;
        }
    }

    private void matchOrders(TreeSet<Order> orders, Order counterPartyOrder, BiPredicate<Order, Order> matcher) {
        if(orders.isEmpty()) return;

        //O(1)
        var order = orders.first();
        while(matcher.test(order, counterPartyOrder)) {
            var vol = Math.min(order.getVolume(), counterPartyOrder.getVolume());
            var newOrderVolume = order.withdrawVolume(vol);
            var newCounterPartyVolume = counterPartyOrder.withdrawVolume(vol);

            if (newOrderVolume == 0) {
                //O(log(n))
                orders.remove(order);
            }

            if(newCounterPartyVolume == 0 || orders.isEmpty()) {
                break;
            } else {
                //O(1)
                order = orders.first();
            }
        }
    }

    class ComparatorAsc implements Comparator<Order> {

        @Override
        public int compare(Order o1, Order o2) {
            var price = o1.price.compareTo(o2.price);
            var time = Long.signum(o1.timestamp - o2.timestamp);
            return price == 0 ? Integer.signum(price + time) : price;
        }
    }

    class ComparatorDesc implements Comparator<Order> {

        @Override
        public int compare(Order o1, Order o2) {
            var price = 0 - o1.price.compareTo(o2.price);
            var time = Long.signum(o1.timestamp - o2.timestamp);
            return price == 0 ? Integer.signum(price + time) : price;
        }
    }
}
