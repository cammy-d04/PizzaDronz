package uk.ac.ed.inf.pizzadronz.services;
import org.springframework.stereotype.Service;
import uk.ac.ed.inf.pizzadronz.constants.OrderValidationCode;
import uk.ac.ed.inf.pizzadronz.model.*;
import uk.ac.ed.inf.pizzadronz.model.OrderValidationResult;
import uk.ac.ed.inf.pizzadronz.constants.*;
import uk.ac.ed.inf.pizzadronz.model.Pizza;
import uk.ac.ed.inf.pizzadronz.model.Restaurant;

import static uk.ac.ed.inf.pizzadronz.constants.SystemConstants.MAX_PIZZAS_PER_ORDER;
import static uk.ac.ed.inf.pizzadronz.constants.SystemConstants.ORDER_CHARGE_IN_PENCE;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;


@Service
public class OrderValidationService {

    private final RestaurantService restaurantService;

    public OrderValidationService(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    public OrderValidationResult validateOrder(Order order) {
        // get the most recent restaurant data
        restaurantService.updateRestaurants();

        // Validate order and pizzas
        if (order == null || order.getPizzasInOrder() == null || order.getPizzasInOrder().isEmpty()) {

            return new OrderValidationResult(OrderStatus.INVALID, OrderValidationCode.EMPTY_ORDER);
        }

        // Validate total price
        if (validatePrice(order).getOrderStatus() == OrderStatus.INVALID) {
            return validatePrice(order);
        }
        System.out.println("price valid");
        // Validate pizzas
        OrderValidationResult pizzaValidationResult = validatePizzas(order.getPizzasInOrder());
        if (pizzaValidationResult.getOrderStatus() == OrderStatus.INVALID) {
            return pizzaValidationResult;
        }
        System.out.println("pizzas valid");
        // Restaurant validation
        Restaurant restaurant = findRestaurant(order.getPizzasInOrder());
        System.out.println("found restaurant");
        order.setRestaurant(restaurant);
        System.out.println("restaurant set");

        OrderValidationResult restaurantValidationResult = validateRestaurant(restaurant, order.getOrderDate());

        if (restaurantValidationResult.getOrderStatus() == OrderStatus.INVALID) {
            System.out.println("Restaurant is invalid.");
            return restaurantValidationResult;
        }

        System.out.println("Restaurant is valid.");

        // Validate credit card

        OrderValidationResult ccValidation = validateCC(order.getCreditCardInformation());

        if (order.getCreditCardInformation() == null) {
            return new OrderValidationResult(OrderStatus.INVALID, OrderValidationCode.CARD_NUMBER_INVALID);
        }else if (ccValidation.getOrderStatus() == OrderStatus.INVALID) {
            return ccValidation;
        }

        // If all validations pass, return success

        System.out.println("Order is valid.");
        return new OrderValidationResult(OrderStatus.VALID, OrderValidationCode.NO_ERROR);
    }


    public OrderValidationResult validatePizzas(List<Pizza> pizzas) {

        if (pizzas.size() > MAX_PIZZAS_PER_ORDER) {
            // Check if over max number of pizzas allowed
            return new OrderValidationResult(OrderStatus.INVALID, OrderValidationCode.MAX_PIZZA_COUNT_EXCEEDED);
        }


        for (Pizza pizza : pizzas) {
            OrderValidationResult result = validateSinglePizza(pizza);
            if (result.getOrderStatus() == OrderStatus.INVALID) {
                return result; // Return the first invalid result
            }

        }


        // Test if pizzas are from multiple restaurants
        if (restaurantService.isPizzasFromMultipleRestaurants(pizzas)) {
            return new OrderValidationResult(OrderStatus.INVALID, OrderValidationCode.PIZZA_FROM_MULTIPLE_RESTAURANTS);
        }

        return new OrderValidationResult(OrderStatus.VALID, OrderValidationCode.NO_ERROR);
    }


    private OrderValidationResult validateSinglePizza(Pizza pizza) {
        Restaurant restaurant = restaurantService.findRestaurantByPizza(pizza.getName());

        if (restaurant == null) {
            return new OrderValidationResult(OrderStatus.INVALID, OrderValidationCode.PIZZA_NOT_DEFINED);
        }


        // Check order price matches restaurant price
        Pizza menuPizza = restaurant.findPizzaByName(pizza.getName());
        if (pizza.getPriceInPence() <= 0 || !(pizza.getPriceInPence() == menuPizza.getPriceInPence())) {
            return new OrderValidationResult(OrderStatus.INVALID, OrderValidationCode.PRICE_FOR_PIZZA_INVALID);
        }

        return new OrderValidationResult(OrderStatus.VALID, OrderValidationCode.NO_ERROR);
    }

    public Restaurant findRestaurant(List<Pizza> pizzas) {

        // Find the restaurant for the first pizza
        Restaurant firstRestaurant = restaurantService.findRestaurantByPizza(pizzas.get(0).getName());

        // Check if all pizzas belong to the same restaurant
        for (Pizza pizza : pizzas) {
            Restaurant currentRestaurant = restaurantService.findRestaurantByPizza(pizza.getName());
            if (currentRestaurant == null || !currentRestaurant.equals(firstRestaurant)) {
                return null;
            }
        }

        System.out.println("Restaurant found: " + firstRestaurant.getName());

        // All pizzas belong to the same restaurant, return it
        return firstRestaurant;


    }

    private OrderValidationResult validateRestaurant(Restaurant restaurant, String orderDate) {

        LocalDate localDate = LocalDate.parse(orderDate);
        DayOfWeek dayOfWeek = localDate.getDayOfWeek();

        if (restaurant == null) {
            return new OrderValidationResult(OrderStatus.INVALID, OrderValidationCode.PIZZA_NOT_DEFINED);
        } else if (!restaurant.getOpeningDays().contains(dayOfWeek)) {
            // Check if restaurant is open on the order date
            return new OrderValidationResult(OrderStatus.INVALID, OrderValidationCode.RESTAURANT_CLOSED);
        }
        return new OrderValidationResult(OrderStatus.VALID, OrderValidationCode.NO_ERROR);
    }


    private OrderValidationResult validateCC(CreditCardInformation creditCardInformation) {

        String cvv = creditCardInformation.getCvv();
        String creditCardNumber = creditCardInformation.getCreditCardNumber();
        String creditCardExpiry = creditCardInformation.getCreditCardExpiry();

        // Check credit card number validity
        if (creditCardInformation.getCreditCardNumber() == null ||
                !creditCardNumber.matches("\\d{16}")) {
            return new OrderValidationResult(OrderStatus.INVALID, OrderValidationCode.CARD_NUMBER_INVALID); // Invalid credit card number
        }

        // Check CVV validity
        if (cvv == null || !cvv.matches("\\d{3}")) {
            return new OrderValidationResult(OrderStatus.INVALID, OrderValidationCode.CVV_INVALID);
        }

        // Check expiry date validity
        if (creditCardExpiry == null ||
                !creditCardExpiry.matches("\\d{2}/\\d{2}")) {
            return new OrderValidationResult(OrderStatus.INVALID, OrderValidationCode.EXPIRY_DATE_INVALID);
        }

        try {;
            LocalDate currentDate = LocalDate.now();
            LocalDate formattedExpiryDate = LocalDate.parse("01/" + creditCardExpiry, DateTimeFormatter.ofPattern("dd/MM/yy"));

            // Check if expiry date is before the current date
            if (formattedExpiryDate.isBefore(currentDate)){
                return new OrderValidationResult(OrderStatus.INVALID, OrderValidationCode.EXPIRY_DATE_INVALID); // Invalid expiry date
            }
        } catch (DateTimeParseException e) {
            return new OrderValidationResult(OrderStatus.INVALID, OrderValidationCode.EXPIRY_DATE_INVALID); // Invalid expiry date
        }
        return new OrderValidationResult(OrderStatus.VALID, OrderValidationCode.NO_ERROR);
    }


    private OrderValidationResult validatePrice(Order order) {
        int providedTotal = order.getPriceTotalInPence();
        int calculatedTotal = ORDER_CHARGE_IN_PENCE; // Add service charge

        for (Pizza pizza : order.getPizzasInOrder()) {

            calculatedTotal += pizza.getPriceInPence();
        }
        // check if total price = sum of pizza prices plus service fee
        if (providedTotal == calculatedTotal)
            return new OrderValidationResult(OrderStatus.VALID, OrderValidationCode.NO_ERROR);
        else
            return new OrderValidationResult(OrderStatus.INVALID, OrderValidationCode.TOTAL_INCORRECT);
    }

}

