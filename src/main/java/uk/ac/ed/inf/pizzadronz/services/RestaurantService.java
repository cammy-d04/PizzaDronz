package uk.ac.ed.inf.pizzadronz.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.ed.inf.pizzadronz.model.Pizza;
import uk.ac.ed.inf.pizzadronz.model.Restaurant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import uk.ac.ed.inf.pizzadronz.constants.SystemConstants;


@Service
public class RestaurantService {

    private volatile List<Restaurant> restaurantList;
    private final RestTemplate restTemplate;

    public RestaurantService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.restaurantList = new ArrayList<>();
    }


    public void updateRestaurants() {
        try {
            // Fetch the list of restaurants from the API
            Restaurant[] restaurantsArray = restTemplate.getForObject(
                    "https://ilp-rest-2024.azurewebsites.net/restaurants",
                    Restaurant[].class
            );
            // Check if the response is null
            if (restaurantsArray == null) {
                throw new IllegalStateException("No restaurants were returned by the API.");
            }

            // Update the restaurant list
            this.restaurantList = Arrays.asList(restaurantsArray);
            System.out.println("Restaurants successfully updated");

        } catch (Exception e) {
            throw new RuntimeException("An error occurred while updating restaurants: " + e.getMessage(), e);
        }
    }

    public List<Restaurant> getRestaurants() {
        return restaurantList;
    }


    public Restaurant findRestaurantByPizza(String pizzaName) {
        // Iterate through the list of restaurants
        for (Restaurant restaurant : restaurantList) {

            // Iterate through the menu of each restaurant
            for (Pizza pizza : restaurant.getMenu()) {

                // Check if the pizza name matches
                if (Objects.equals(pizzaName, pizza.getName())) {
                    return restaurant;
                }
            }
        }
        return null;
    }

    public boolean isPizzasFromMultipleRestaurants(List<Pizza> pizzas) {

        // Find the restaurant for the first pizza
        Restaurant firstRestaurant = findRestaurantByPizza(pizzas.get(0).getName());

        // Check if all pizzas belong to the same restaurant
        for (Pizza pizza : pizzas) {
            Restaurant currentRestaurant = findRestaurantByPizza(pizza.getName());
            if (currentRestaurant == null || !currentRestaurant.equals(firstRestaurant)) {
                return true;
            }
        }

        return false;
    }
}