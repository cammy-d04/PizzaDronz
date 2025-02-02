// RestaurantServiceTest.java
package uk.ac.ed.inf.pizzadronz.servicesTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import uk.ac.ed.inf.pizzadronz.constants.OrderStatus;
import uk.ac.ed.inf.pizzadronz.constants.OrderValidationCode;
import uk.ac.ed.inf.pizzadronz.model.*;
import uk.ac.ed.inf.pizzadronz.services.OrderValidationService;
import uk.ac.ed.inf.pizzadronz.services.RestaurantService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class RestaurantServiceTest {

    RestaurantService restaurantService = new RestaurantService(new RestTemplate());
    OrderValidationService orderValidationService = new OrderValidationService(restaurantService);


    @Test
    void FindRestaurantByPizzaTest_WorldOfPizza() {

        // Create the order object
        Order order = new Order(
                "433B8EC0",
                "2024-11-08",
                2400,
                List.of(
                        new Pizza("R7: Hot, hotter, the hottest", 1400),
                        new Pizza("R7: All you ever wanted", 900)
                ),
                new CreditCardInformation("5530228504351052", "11/25", "927")
        );


        restaurantService.updateRestaurants();

        Restaurant restaurant = orderValidationService.findRestaurant(order.getPizzasInOrder());


        // Assert the order status and validation code
        assertEquals("World of Pizza", restaurant.getName());
    }




    @Test
    void FindRestaurantByPizzaTest_SoraLella() {
        // Create the order object
        Order order = new Order(
                "7114C0D4",
                "2024-11-08",
                2600,
                List.of(
                        new Pizza("R2: Meat Lover", 1400),
                        new Pizza("R2: Vegan Delight", 1100)
                ),
                new CreditCardInformation("6403643676911943", "11/27", "313")
        );

        restaurantService.updateRestaurants();

        Restaurant restaurant = orderValidationService.findRestaurant(order.getPizzasInOrder());

        // Assert the order status and validation code
        assertEquals("Sora Lella Vegan Restaurant", restaurant.getName());
    }

    @Test
    void FindRestaurantByPizzaTest_Civerinos() {
        // Create the order object
        Order order = new Order(
                "5547FBE1",
                "2024-11-08",
                2500,
                List.of(
                        new Pizza("R1: Margarita", 1000),
                        new Pizza("R1: Calzone", 1400)
                ),
                new CreditCardInformation("4672748333160110", "08/24", "126")
        );

        restaurantService.updateRestaurants();

        Restaurant restaurant = orderValidationService.findRestaurant(order.getPizzasInOrder());

        // Assert the order status and validation code
        assertEquals("Civerinos Slice", restaurant.getName());
    }



    @Test
    void FindRestaurantByPizzaTest_Dominoes() {
        // Create the order object
        Order order = new Order(
                "2FADB593",
                "2024-11-08",
                2400,
                List.of(
                        new Pizza("R3: Super Cheese", 1400),
                        new Pizza("R3: All Shrooms", 900)
                ),
                new CreditCardInformation("4982792461195097", "11/28", "555")
        );

        restaurantService.updateRestaurants();

        Restaurant restaurant = orderValidationService.findRestaurant(order.getPizzasInOrder());

        // Assert the order status and validation code
        assertEquals("Domino's Pizza - Edinburgh - Southside", restaurant.getName());
    }

}
