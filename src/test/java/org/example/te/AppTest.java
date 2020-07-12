package org.example.te;

import org.example.te.dao.OrderBookDAO;
import org.example.te.model.OrderId;
import org.example.te.model.Order;
import org.example.te.model.OrderBuilder;
import org.example.te.model.OrderStatus;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.time.LocalDate;

public class AppTest {

    private OrderBookDAO orderBookDAO;

    @BeforeSuite
    public void init(){
        orderBookDAO = OrderBookDAO.getInstance();
    }

    @Test
    public void testFirstVersionReceived(){
        OrderId orderId = new OrderId("T1");
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
    }

    @Test
    public void testLowerVersionReceived(){
        OrderId orderId = new OrderId("T2");
        OrderBuilder builder = new OrderBuilder();
        Order orderV2 = builder.
                setOrderId(orderId).
                setBookId("B1").
                setCounterPartyId("CP-2").
                setMaturityDate(LocalDate.now().plusDays(4)).
                setVersion(2).build();

        Order orderV1 = builder.
                setOrderId(orderId).
                setBookId("B1").
                setCounterPartyId("CP-1").
                setMaturityDate(LocalDate.now().plusDays(4)).
                setVersion(1).build();
        Throwable t = null;
        try {
            orderBookDAO.saveNewOrder(orderV2);
            orderBookDAO.updateOrder(orderV1);
        }catch(Exception e){
            t = e;
        }
        Assert.assertNotNull(t);
        Assert.assertTrue(orderV1.getOrderStatus().equals(OrderStatus.REJECT));
        Assert.assertTrue(orderBookDAO.containsOrderKey(orderId));
        Assert.assertEquals(orderBookDAO.getOrderSize(orderId),1);
        Assert.assertEquals(orderBookDAO.getLatestVersion(orderId).getVersion().intValue(),2);
    }

    @Test
    public void testSameVersionReceived(){
        OrderId orderId = new OrderId("T3");
        OrderBuilder builder = new OrderBuilder();
        Order orderV1 = builder.
                setOrderId(orderId).
                setBookId("B1").
                setCounterPartyId("CP-1").
                setMaturityDate(LocalDate.now().plusDays(4)).
                setVersion(2).build();

        Order orderV2 = builder.
                setOrderId(orderId).
                setBookId("B2").
                setCounterPartyId("CP-2").
                setMaturityDate(LocalDate.now().plusDays(5)).
                setVersion(2).build();
        Throwable t = null;
        try {
            orderBookDAO.saveNewOrder(orderV1);
            orderBookDAO.updateOrder(orderV2);
        }catch(Exception e){
            t = e;
        }
        Assert.assertNull(t);
        Assert.assertTrue(orderBookDAO.containsOrderKey(orderId));
        Assert.assertEquals(orderBookDAO.getOrderSize(orderId),1);
        Order v3 = orderBookDAO.getLatestVersion(orderId);
        Assert.assertNotNull(v3);
        Assert.assertEquals(v3.getVersion().intValue(),2);
        Assert.assertEquals(v3.getCounterPartyId(),"CP-2");
        Assert.assertEquals(v3.getBookId(),"B2");
        Assert.assertTrue(v3.getMaturityDate().minusDays(5).equals(LocalDate.now()));

    }

    @Test
    public void testHigherVersionReceived(){
        OrderId orderId = new OrderId("T4");
        OrderBuilder builder = new OrderBuilder();
        Order orderV1 = builder.
                setOrderId(orderId).
                setBookId("B1").
                setCounterPartyId("CP-1").
                setMaturityDate(LocalDate.now().plusDays(4)).
                setVersion(1).build();

        Order orderV2 = builder.
                setOrderId(orderId).
                setBookId("B2").
                setCounterPartyId("CP-2").
                setMaturityDate(LocalDate.now().plusDays(5)).
                setVersion(2).build();
        Throwable t = null;
        try {
            orderBookDAO.saveNewOrder(orderV1);
            orderBookDAO.updateOrder(orderV2);
        }catch(Exception e){
            t = e;
        }
        Assert.assertNull(t);
        Assert.assertTrue(orderBookDAO.containsOrderKey(orderId));
        Assert.assertEquals(orderBookDAO.getOrderSize(orderId),2);
        Order v3 = orderBookDAO.getLatestVersion(orderId);
        Assert.assertNotNull(v3);
        Assert.assertEquals(v3.getVersion().intValue(),2);
        Assert.assertEquals(v3.getCounterPartyId(),"CP-2");
        Assert.assertEquals(v3.getBookId(),"B2");
        Assert.assertTrue(v3.getMaturityDate().minusDays(5).equals(LocalDate.now()));
    }

    @Test
    public void testLessMaturityDate(){
        OrderId orderId = new OrderId("T5");
        OrderBuilder builder = new OrderBuilder();
        Order order = builder.
                setOrderId(orderId).
                setBookId("B1").
                setCounterPartyId("CP-1").
                setMaturityDate(LocalDate.now().minusDays(1)).
                setVersion(1).build();
        try {
            orderBookDAO.saveNewOrder(order);
            Assert.assertFalse(orderBookDAO.containsOrderKey(orderId));
            Assert.assertTrue(order.getOrderStatus().equals(OrderStatus.REJECT));
        }catch(Exception e){
            Assert.assertNull(e);
        }
    }

    @Test
    public void testCrossMaturityDate(){
        OrderId orderId = new OrderId("T6");
        OrderBuilder builder = new OrderBuilder();
        Order order = builder.
                setOrderId(orderId).
                setBookId("B1").
                setCounterPartyId("CP-1").
                setMaturityDate(LocalDate.now().plusDays(1)).
                setVersion(1).build();
        try {
            orderBookDAO.saveNewOrder(order);
            Assert.assertTrue(orderBookDAO.containsOrderKey(orderId));
            Order savedOrder = orderBookDAO.getLatestVersion(orderId);
            Assert.assertFalse(savedOrder.getExpire());

            savedOrder.setMaturityDate(LocalDate.now().minusDays(1));
            orderBookDAO.expireOrder();

            Order savedOrderNew = orderBookDAO.getLatestVersion(orderId);
            Assert.assertTrue(savedOrderNew.getExpire());
        }catch(Exception e){
            Assert.assertNull(e);
        }
    }

}
