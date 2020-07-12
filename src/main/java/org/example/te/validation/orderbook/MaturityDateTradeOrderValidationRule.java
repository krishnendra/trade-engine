package org.example.te.validation.orderbook;

import org.example.te.model.Order;
import org.example.te.model.OrderStatus;
import org.example.te.validation.Validator;

import java.time.LocalDate;

public class MaturityDateTradeOrderValidationRule extends AbstractOrderBookValidationRule {

    public MaturityDateTradeOrderValidationRule(){}

    public MaturityDateTradeOrderValidationRule(Validator rule){
        super(rule);
    }

    @Override
    public boolean validate(Order oldOrder, Order newOrder) {
        if(newOrder.getMaturityDate().isBefore(LocalDate.now())){
            newOrder.setOrderStatus(OrderStatus.REJECT);
            String reason = "Maturity Date:"+ newOrder.getMaturityDate().toString()+" cannot be prior to today's date";
            newOrder.setRejectReason(reason);
            return false;
        }
        return (this.orderBookValidationRule == null)?true:((AbstractOrderBookValidationRule)this.orderBookValidationRule).validate(oldOrder,newOrder);
    }
}
