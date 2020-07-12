package org.example.te.model;

import java.io.Serializable;

public class OrderId implements Serializable {

    private String tradeId;

    public OrderId(String tradeId) {
        this.tradeId = tradeId;
    }

    public String getTradeId() {
        return tradeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderId)) return false;

        OrderId orderId = (OrderId) o;

        return getTradeId().equals(orderId.getTradeId());
    }

    @Override
    public int hashCode() {
        return getTradeId().hashCode();
    }
}
