package org.example.te.dao;

import org.example.te.exception.OldVersionException;
import org.example.te.model.Order;
import org.example.te.model.OrderId;
import org.example.te.model.OrderSet;
import org.example.te.model.OrderStatus;
import org.example.te.validation.orderbook.AbstractOrderBookValidationRule;
import org.example.te.validation.orderbook.MaturityDateTradeOrderValidationRule;
import org.example.te.validation.orderbook.VersionOrderOrderBookValidationRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class OrderBookDAO{

    private static Logger LOGGER = LoggerFactory.getLogger(OrderBookDAO.class);

    private Map<OrderId, OrderSet> orderMap = new HashMap<>();

    private ReentrantLock storeLock = new ReentrantLock();

    private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);

    private OrderBookDAO(){
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        ZonedDateTime nextRun = now.withHour(0).withMinute(0).withSecond(0);
        if(now.compareTo(nextRun) > 0)
            nextRun = nextRun.plusDays(1);

        Duration duration = Duration.between(now, nextRun);
        long initalDelay = duration.getSeconds();

        scheduledThreadPool.scheduleAtFixedRate(() -> {
            try {
                LOGGER.info("Scheduled Thread Started at :{}", LocalDateTime.now().toString());
                expireOrder();
            } catch (Exception e) {
                LOGGER.info("Exception to expire orders :{}",e);
            }
        }, initalDelay, TimeUnit.DAYS.toSeconds(1),TimeUnit.SECONDS);
    }

    private static class OrderBookHelper{
        private static final OrderBookDAO instance = new OrderBookDAO();
    }

    public static OrderBookDAO getInstance(){
        return OrderBookHelper.instance;
    }

    public boolean containsOrderKey(OrderId orderId){
        return orderMap.containsKey(orderId);
    }

    public int getOrderSize(OrderId orderId){
        return orderMap.get(orderId).getOrderSet().size();
    }

    public Order getLatestVersion(OrderId orderId){
        return orderMap.get(orderId).getOrderSet().last();
    }

    public void saveNewOrder(Order order,AbstractOrderBookValidationRule rule) throws Exception{
        try{
            storeLock.lock();
            if(!orderMap.containsKey(order.getOrderId())){
                if(validateOrder(order,rule)){
                    TreeSet<Order> orders = new TreeSet<>();
                    orders.add(order);
                    orderMap.put(order.getOrderId(),new OrderSet(orders));
                }else{
                    LOGGER.info("Not able to add order as its status is {}, Reason is {}",order.getOrderStatus(),order.getRejectReason());
                }
            }else{
                storeLock.unlock();
                updateOrder(order);
            }
        }catch (Exception exp){
            LOGGER.info("Order with Order Id:{} could not be saved in the Store. Reason :{}", order.getOrderId(),exp);
            throw exp;
        }finally{
            if(storeLock.isHeldByCurrentThread()){
                storeLock.unlock();
            }
        }
    }

    public void saveNewOrder(Order order) throws Exception{
        saveNewOrder(order,new MaturityDateTradeOrderValidationRule());
    }

    public void updateOrder(Order order,AbstractOrderBookValidationRule rule) throws Exception{
        try{
            orderMap.get(order.getOrderId()).getOrderLock().lock();
            if(validateOrder(order,rule)){
                if(order.getOrderStatus().equals(OrderStatus.REPLACE)){
                    orderMap.get(order.getOrderId()).getOrderSet().pollLast();
                    orderMap.get(order.getOrderId()).getOrderSet().add(order);
                }else{
                    orderMap.get(order.getOrderId()).getOrderSet().add(order);
                }
                LOGGER.info("Order with Order Id:{} saved in the store", order.getOrderId());
            }
        }catch(Exception exp){
            LOGGER.info("Order with Order Id:{} could not be saved in the Store. Reason :", order.getOrderId().getTradeId(),exp);
            throw exp;
        }finally{
            orderMap.get(order.getOrderId()).getOrderLock().unlock();
        }
    }

    public void updateOrder(Order order) throws Exception{
        updateOrder(order,new VersionOrderOrderBookValidationRule(new MaturityDateTradeOrderValidationRule()));
    }

    public void expireOrder() throws Exception{
        try{
            storeLock.lock();
            orderMap.values().stream().forEach((orderSet)->{
                orderSet.getOrderSet().stream().filter((o)->o.getMaturityDate().isBefore(LocalDate.now())).forEach((o)->o.setExpire(true));
            });
        }catch(Exception exp){
            LOGGER.info("Exception in expired order :{}", exp);
            throw exp;
        }finally{
            storeLock.unlock();
        }
    }

    public boolean validateOrder(Order newOrder, AbstractOrderBookValidationRule parentRule) throws Exception{
        Order oldOrderLatestVersion = null;
        try{
            if(orderMap.containsKey(newOrder.getOrderId())){
                orderMap.get(newOrder.getOrderId()).getOrderLock().lock();
                oldOrderLatestVersion = orderMap.get(newOrder.getOrderId()).getOrderSet().last();
            }
            if(parentRule != null){
                return parentRule.validate(oldOrderLatestVersion, newOrder);
            }
        }catch(OldVersionException ove){
            LOGGER.info("New Order:{} has old version:{}", newOrder.getOrderId().getTradeId(), newOrder.getVersion());
            throw ove;
        }catch(Exception exp){
            LOGGER.info("Order with Order Id:{} could not be retrieved from the Store. Reason :{}", newOrder.getOrderId(),exp);
            throw exp;
        }finally{
            if(orderMap.containsKey(newOrder.getOrderId())){
                orderMap.get(newOrder.getOrderId()).getOrderLock().unlock();
            }
        }
        return false;
    }




}
