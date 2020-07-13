package org.example.te.model;

import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class OrderSet {

    private Lock orderLock = new ReentrantLock();

    private AtomicReference<TreeSet<Order>> orderSet;

    public OrderSet(AtomicReference<TreeSet<Order>> orderSet) {
        this.orderSet = orderSet;
    }

    public Lock getOrderLock() {
        return orderLock;
    }

    public AtomicReference<TreeSet<Order>> getOrderSet() {
        return orderSet;
    }
}
