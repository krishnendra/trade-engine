package org.example.te.model;

import java.time.LocalDate;

public class OrderBuilder {

    private OrderId orderId;

    private Integer version;

    private LocalDate maturityDate;

    private String counterPartyId;

    private String bookId;

    public OrderBuilder setOrderId(OrderId orderId) {
        this.orderId = orderId;
        return this;
    }

    public OrderBuilder setCounterPartyId(String counterPartyId) {
        this.counterPartyId = counterPartyId;
        return this;
    }

    public OrderBuilder setBookId(String bookId) {
        this.bookId = bookId;
        return this;
    }

    public OrderBuilder setVersion(Integer version) {
        this.version = version;
        return this;
    }

    public OrderBuilder setMaturityDate(LocalDate maturityDate) {
        this.maturityDate = maturityDate;
        return this;
    }

    public Order build(){
        return new Order(orderId,counterPartyId,bookId,version,maturityDate);
    }

}
