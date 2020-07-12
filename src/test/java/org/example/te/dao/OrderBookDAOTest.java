package org.example.te.dao;

import org.example.te.model.Order;
import org.example.te.model.OrderBuilder;
import org.example.te.model.OrderId;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

public class OrderBookDAOTest {

    private OrderBookDAO orderBookDAO;

    @BeforeSuite
    public void init(){
        orderBookDAO = OrderBookDAO.getInstance();
    }

    @Test
    public void testMultipleThreads_Scenario1() throws Exception {
        //TH-1 Saves the order as new
        //TH-2 Also tries to Save the order as New but finds out Key already Saved
        //TH-3 is waiting for StoreLock to be given
        OrderId orderId = new OrderId("OBDAO-1");
        OrderBuilder builder = new OrderBuilder();
        Order order = builder.
                setOrderId(orderId).
                setBookId("B1").
                setCounterPartyId("CP-1").
                setMaturityDate(LocalDate.now().plusDays(4)).
                setVersion(1).build();
        try {
            orderBookDAO.saveNewOrder(order);
            Assert.assertTrue(orderBookDAO.containsOrderKey(orderId));
        }catch(Exception e){
            Assert.assertNull(e);
        }

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        RunnableFuture<Void> task1 = new FutureTask<>(()->{
            for(int i =0;i < 10;i++){
                OrderId orderId2 = new OrderId("OBDAO-1a");
                OrderBuilder builder2 = new OrderBuilder();
                Order order2 = builder2.
                        setOrderId(orderId2).
                        setBookId("B2").
                        setCounterPartyId("CP-2").
                        setMaturityDate(LocalDate.now().plusDays(4)).
                        setVersion(i+1).build();
                try {
                    orderBookDAO.saveNewOrder(order2);
                } catch (Exception e) {
                    Assert.assertNull(e);
                }
            }
        },null);

        RunnableFuture<Void> task2 = new FutureTask<>(() -> {
            OrderId orderId2 = new OrderId("OBDAO-1");
            OrderBuilder builder2 = new OrderBuilder();
            Order order2 = builder2.
                    setOrderId(orderId2).
                    setBookId("B2").
                    setCounterPartyId("CP-2").
                    setMaturityDate(LocalDate.now().plusDays(4)).
                    setVersion(1).build();
            try {
                orderBookDAO.saveNewOrder(order2);
            } catch (Exception e) {
                Assert.assertNull(e);
            }
        }, null);

        executorService.submit(task1);
        executorService.submit(task2);

        try {
            task1.get();
            task2.get();
            executorService.shutdown();
        } catch (Exception e) {
            throw e;
        }

        Assert.assertEquals(orderBookDAO.getOrderSize(new OrderId("OBDAO-1a")),10);
        Assert.assertEquals(orderBookDAO.getOrderSize(new OrderId("OBDAO-1")),1);
    }

    @Test
    public void testMultipleThreads_Scenario2() throws Exception {
        //TH-1 Saves the order as new
        //TH-2 Saves an updated order
        //TH-3 Also Saves an updated order
        //Also saves with same set of versions
        OrderId orderId = new OrderId("OBDAO-2");
        OrderBuilder builder = new OrderBuilder();
        Order order = builder.
                setOrderId(orderId).
                setBookId("B1").
                setCounterPartyId("CP-1").
                setMaturityDate(LocalDate.now().plusDays(4)).
                setVersion(1).build();
        try {
            orderBookDAO.saveNewOrder(order);
            Assert.assertTrue(orderBookDAO.containsOrderKey(orderId));
        }catch(Exception e){
            Assert.assertNull(e);
        }

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        RunnableFuture<Void> task1 = new FutureTask<>(()->{
            for(int i =0;i < 10;i++){
                OrderId orderId2 = new OrderId("OBDAO-2");
                OrderBuilder builder2 = new OrderBuilder();
                Order order2 = builder2.
                        setOrderId(orderId2).
                        setBookId("B3").
                        setCounterPartyId("CP-3").
                        setMaturityDate(LocalDate.now().plusDays(4)).
                        setVersion(i+1).build();
                try {
                    orderBookDAO.saveNewOrder(order2);
                } catch (Exception e) {
                    Assert.assertNull(e);
                }
            }
        },null);

        RunnableFuture<Void> task2 = new FutureTask<>(() -> {
            for(int i =0;i < 10;i++){
                OrderId orderId2 = new OrderId("OBDAO-1");
                OrderBuilder builder2 = new OrderBuilder();
                Order order2 = builder2.
                        setOrderId(orderId2).
                        setBookId("B4").
                        setCounterPartyId("CP-4").
                        setMaturityDate(LocalDate.now().plusDays(4)).
                        setVersion(i+1).build();
                try {
                    orderBookDAO.saveNewOrder(order2);
                } catch (Exception e) {
                    Assert.assertNull(e);
                }
            }

        }, null);

        executorService.submit(task1);
        executorService.submit(task2);

        try {
            task1.get();
            task2.get();
            executorService.shutdown();
        } catch (Exception e) {
            throw e;
        }

        Assert.assertEquals(orderBookDAO.getOrderSize(new OrderId("OBDAO-1")),10);
    }
}
