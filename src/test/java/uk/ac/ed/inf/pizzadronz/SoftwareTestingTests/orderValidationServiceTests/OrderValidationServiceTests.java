package uk.ac.ed.inf.pizzadronz.SoftwareTestingTests.orderValidationServiceTests;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;
import uk.ac.ed.inf.pizzadronz.model.Order;
import uk.ac.ed.inf.pizzadronz.model.OrderValidationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.ac.ed.inf.pizzadronz.services.OrderValidationService;
import uk.ac.ed.inf.pizzadronz.services.RestaurantService;
import org.springframework.core.io.ClassPathResource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class OrderValidationServiceTests {

    @Autowired
    private OrderValidationService orderValidationService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        orderValidationService = new OrderValidationService(new RestaurantService(new RestTemplate()));
    }

    @Test
    void testOrdersFromJson() throws IOException {
        // Load orders from JSON file (both valid and invalid orders)
        String filePath = new ClassPathResource("test-orders.json").getFile().getAbsolutePath();
        String jsonContent = new String(Files.readAllBytes(Paths.get(filePath)));
        List<Order> orders = objectMapper.readValue(jsonContent, new TypeReference<>() {
        });

        // Iterate through the orders and perform validation
        for (Order order: orders) {
            OrderValidationResult result = orderValidationService.validateOrder(order);

            // Assert that the orderStatus and orderValidationCode match the expected values
            assertEquals(order.getOrderStatus(), result.getOrderStatus().toString());
            assertEquals(order.getOrderValidationCode(), result.getOrderValidationCode().toString());
        }
    }



}
