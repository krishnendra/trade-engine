package org.example.te.model;

import java.io.Serializable;
import java.time.LocalDate;

public class Order implements Serializable,Comparable<Order> {

    private OrderId orderId;

    private Integer version;

    private Boolean expire = false;

    private LocalDate maturityDate;

    private LocalDate createdDate;

    private OrderStatus orderStatus;

    private String counterPartyId;

    private String bookId;

    private String rejectReason;

    public Order(OrderId orderId, String counterPartyId, String bookId, Integer version, LocalDate maturityDate) {
        this.orderId = orderId;
        this.counterPartyId = counterPartyId;
        this.bookId = bookId;
        this.version = version;
        this.maturityDate = maturityDate;
        this.createdDate = LocalDate.now();
        this.orderStatus = OrderStatus.NEW;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public Integer getVersion() {
        return version;
    }

    public Boolean getExpire() {
        return expire;
    }

    public void setExpire(Boolean expire) {
        this.expire = expire;
    }

    public LocalDate getMaturityDate() {
        return maturityDate;
    }

    public void setMaturityDate(LocalDate maturityDate) {
        this.maturityDate = maturityDate;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public String getCounterPartyId() {
        return counterPartyId;
    }

    public void setCounterPartyId(String counterPartyId) {
        this.counterPartyId = counterPartyId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;

        Order order = (Order) o;

        if (!getOrderId().equals(order.getOrderId())) return false;
        if (!getVersion().equals(order.getVersion())) return false;
        if (!getExpire().equals(order.getExpire())) return false;
        if (!getMaturityDate().equals(order.getMaturityDate())) return false;
        if (!getCreatedDate().equals(order.getCreatedDate())) return false;
        return getOrderStatus() == order.getOrderStatus();
    }

    @Override
    public int hashCode() {
        int result = getOrderId().hashCode();
        result = 31 * result + getVersion().hashCode();
        result = 31 * result + getExpire().hashCode();
        result = 31 * result + getMaturityDate().hashCode();
        result = 31 * result + getCreatedDate().hashCode();
        result = 31 * result + getOrderStatus().hashCode();
        return result;
    }

    @Override
    public int compareTo(Order target) {
        return Integer.compare(this.version,target.version);
    }
}
