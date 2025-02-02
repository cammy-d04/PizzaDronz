package uk.ac.ed.inf.pizzadronz.model;

import java.util.List;

public class Order {
    private String orderNo;
    private String orderDate;
    private String orderStatus;
    private String orderValidationCode;
    private int priceTotalInPence;
    private List<Pizza> pizzasInOrder;
    private CreditCardInformation creditCardInformation;
    private Restaurant restaurant;

    public Order(String orderNo, String orderDate, int priceTotalInPence, List<Pizza> pizzasInOrder, CreditCardInformation creditCardInformation) {
        this.orderNo = orderNo;
        this.orderDate = orderDate;
        this.priceTotalInPence = priceTotalInPence;
        this.pizzasInOrder = pizzasInOrder;
        this.creditCardInformation = creditCardInformation;
        this.restaurant = null;
    }

    public Order(){
    }

    // Getters and Setters (optional, depending on whether you need them)
    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public int getPriceTotalInPence() {
        return priceTotalInPence;
    }

    public void setPriceTotalInPence(int priceTotalInPence) {
        this.priceTotalInPence = priceTotalInPence;
    }

    public List<Pizza> getPizzasInOrder() {
        return pizzasInOrder;
    }

    public void setPizzasInOrder(List<Pizza> pizzasInOrder) {
        this.pizzasInOrder = pizzasInOrder;
    }

    public CreditCardInformation getCreditCardInformation() {
        return creditCardInformation;
    }

    public void setCreditCardInformation(CreditCardInformation creditCardInformation) {
        this.creditCardInformation = creditCardInformation;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }
    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderValidationCode() {
        return orderValidationCode;
    }

    public void setOrderValidationCode(String orderValidationCode) {
        this.orderValidationCode = orderValidationCode;
    }



}
