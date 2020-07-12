package org.example.te.validation.orderbook;

import org.example.te.model.Order;
import org.example.te.validation.Validator;

public abstract class AbstractOrderBookValidationRule implements Validator {

    protected Validator orderBookValidationRule;

    public abstract boolean validate(Order oldOrder, Order newOrder);

    public AbstractOrderBookValidationRule(){}

    public AbstractOrderBookValidationRule(Validator orderBookValidationRule){
        this.orderBookValidationRule = orderBookValidationRule;
    }
}
