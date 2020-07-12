package org.example.te.validation.orderbook;

import org.example.te.exception.OldVersionException;
import org.example.te.model.Order;
import org.example.te.model.OrderStatus;
import org.example.te.validation.Validator;

public class VersionOrderOrderBookValidationRule extends AbstractOrderBookValidationRule {

    public VersionOrderOrderBookValidationRule(){}

    public VersionOrderOrderBookValidationRule(Validator orderBookValidationRule){
        super(orderBookValidationRule);
    }

    @Override
    public boolean validate(Order oldOrder, Order newOrder) {
        if(oldOrder != null){
            if(newOrder.getVersion() < oldOrder.getVersion()){
                //REJECT
                String reason = "Order rejected as version of updated order is old.";
                newOrder.setOrderStatus(OrderStatus.REJECT);
                throw new OldVersionException(reason);
            }
            if(newOrder.getVersion() == oldOrder.getVersion()){
                //OVERRIDE
                newOrder.setOrderStatus(OrderStatus.REPLACE);
            }
        }

        return (this.orderBookValidationRule == null)?true:((AbstractOrderBookValidationRule)this.orderBookValidationRule).validate(oldOrder,newOrder);
    }
}
