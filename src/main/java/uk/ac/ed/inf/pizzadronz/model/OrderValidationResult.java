package uk.ac.ed.inf.pizzadronz.model;

import uk.ac.ed.inf.pizzadronz.constants.OrderStatus;
import uk.ac.ed.inf.pizzadronz.constants.OrderValidationCode;

public class OrderValidationResult {
    private OrderStatus orderStatus;
    private OrderValidationCode orderValidationCode;


    public OrderValidationResult(OrderStatus orderStatus, OrderValidationCode orderValidationCode) {
        this.orderStatus = orderStatus;
        this.orderValidationCode = orderValidationCode;
    }


    public OrderStatus getOrderStatus() {
        return orderStatus;
    }


    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }


    public OrderValidationCode getOrderValidationCode() {
        return orderValidationCode;
    }


    public void setOrderValidationCode(OrderValidationCode orderValidationCode) {
        this.orderValidationCode = orderValidationCode;
    }
}

