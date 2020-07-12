package org.example.te.model;

import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class OrderSet {

    private Lock orderLock = new ReentrantLock();

    private TreeSet<Order> orderSet;

    public OrderSet(TreeSet<Order> orderSet) {
        this.orderSet = orderSet;
    }

    public Lock getOrderLock() {
        return orderLock;
    }

    public TreeSet<Order> getOrderSet() {
        return orderSet;
    }
}
