package uk.ac.ed.inf.pizzadronz.SoftwareTestingTests.flightPathServiceTests.SystemTests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONString;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ed.inf.pizzadronz.constants.OrderStatus;
import uk.ac.ed.inf.pizzadronz.constants.OrderValidationCode;
import uk.ac.ed.inf.pizzadronz.model.*;
import uk.ac.ed.inf.pizzadronz.services.FlightPathService;
import uk.ac.ed.inf.pizzadronz.services.IsInRegionService;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import uk.ac.ed.inf.pizzadronz.model.LngLat;
import uk.ac.ed.inf.pizzadronz.model.Order;
import uk.ac.ed.inf.pizzadronz.services.OrderValidationService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static uk.ac.ed.inf.pizzadronz.constants.SystemConstants.APPLETON_LAT;
import static uk.ac.ed.inf.pizzadronz.constants.SystemConstants.APPLETON_LNG;
import uk.ac.ed.inf.pizzadronz.controllers.Controller;
import uk.ac.ed.inf.pizzadronz.services.RestaurantService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
public class FlightPathServiceSystemTests {

    @Autowired
    private RestTemplate restTemplate;

    @Mock
    private FlightPathService flightPathService;


    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private IsInRegionService isInRegionService;

    @Autowired
    private RestaurantService restaurantService;


    @Autowired
    private MockMvc mockMvc;





    /*
    Test the endpoint runs in less than 1 minute
     */

    @Test
    void testDeliveryPathCalculationTime() throws Exception {
        // Sample order
        Order order = new Order(
                "5547FBE1",
                "2024-11-08",
                2500,
                List.of(
                        new Pizza("R1: Margarita", 1000),
                        new Pizza("R1: Calzone", 1400)
                ),
                new CreditCardInformation("4672748333160110", "08/25", "126")
        );

        String orderJson = objectMapper.writeValueAsString(order);

        // Measure the time taken to calculate the delivery path
        long startTime = System.currentTimeMillis();

        MvcResult result = mockMvc.perform(post("/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andReturn();

        long endTime = System.currentTimeMillis();

        // Calculate the elapsed time in milliseconds
        long elapsedTime = endTime - startTime;

        // Assert that the elapsed time is less than 1 minute
        assertTrue(elapsedTime < 60000, "Delivery path calculation took too long: " + elapsedTime + " ms");

        // also assert the response
        assertEquals(200, result.getResponse().getStatus());
    }



    @Test
    void testDeliveryPathsEndAtAppletonTower() throws Exception {



        // Sample order
        Order order = new Order(
                "5547FBE1",
                "2024-11-08",
                2500,
                List.of(
                        new Pizza("R1: Margarita", 1000),
                        new Pizza("R1: Calzone", 1400)
                ),
                new CreditCardInformation("4672748333160110", "08/25", "126")
        );

            String orderJson = objectMapper.writeValueAsString(order);


        // Make the POST request to /calcDeliveryPath
            MvcResult result = mockMvc.perform(post("/calcDeliveryPath")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(String.valueOf(orderJson)))
                    .andReturn();

            // Extract the response content as a string
            String responseJson = result.getResponse().getContentAsString();


            // Parse the JSON string into a List<LngLat>
            List<LngLat> response = objectMapper.readValue(responseJson, new TypeReference<List<LngLat>>() {});

            // Assert that the last LngLat in the response is close to Appleton Tower
            LngLat lastLngLat = response.get(response.size() - 1);
            assertTrue(isCloseToAppletonTower(lastLngLat));


    }

    private boolean isCloseToAppletonTower(LngLat lngLat) {
        LngLat appletonTower = new LngLat(APPLETON_LNG, APPLETON_LAT);
        return lngLat.isCloseTo(appletonTower);
    }






    @Test
    void testDeliveryPathsAvoidNoFlyZones() throws Exception {


        List<Region> noFlyZones = flightPathService.getNoFlyZones(); // Get no-fly zones


        // Sample order
        Order order = new Order(
                "5547FBE1",
                "2024-11-08",
                2500,
                List.of(
                        new Pizza("R1: Margarita", 1000),
                        new Pizza("R1: Calzone", 1400)
                ),
                new CreditCardInformation("4672748333160110", "08/25", "126")
        );

        String orderJson = objectMapper.writeValueAsString(order);


        // Make the POST request to /calcDeliveryPath
        MvcResult result = mockMvc.perform(post("/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(orderJson)))
                .andReturn();

            // Extract the response content as a string
            String responseJson = result.getResponse().getContentAsString();


            // Parse the JSON string into a List<LngLat>
            List<LngLat> path = objectMapper.readValue(responseJson, new TypeReference<List<LngLat>>() {
            });


            for (LngLat point : path) {
                for (Region noFlyZone : noFlyZones) {
                    assertFalse(isInRegionService.isInRegion(point, noFlyZone),
                            "Path crosses no-fly zone: " + noFlyZone.getName());
                }
            }
    }





    static List<Order> loadOrders() throws IOException {
        String filePath = new ClassPathResource("test-orders.json").getFile().getAbsolutePath();
        String jsonContent = new String(Files.readAllBytes(Paths.get(filePath)));
        return objectMapper.readValue(jsonContent, new TypeReference<>() {});
    }

    @ParameterizedTest
    @MethodSource("loadOrders")
    void testCalculateFlightPathInvalidOrders(Order order) throws Exception {
        if (Objects.equals(order.getOrderValidationCode(), "NO_ERROR")) {
            return;
        }

        // Set up headers for the POST request
        String orderJson = objectMapper.writeValueAsString(order);

        // Make the POST request to /calcDeliveryPath
        MvcResult result = mockMvc.perform(post("/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andReturn();

        // Assert that the response status is BAD_REQUEST
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }


}


@SpringBootTest
@AutoConfigureMockMvc
class testCalculateFlightPathValidOrders {

    @Autowired
    private RestTemplate restTemplate;

    @MockBean
    private FlightPathService flightPathService;


    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private IsInRegionService isInRegionService;

    @Autowired
    private RestaurantService restaurantService;


    @Autowired
    private MockMvc mockMvc;


    static List<Order> loadOrders() throws IOException {
        String filePath = new ClassPathResource("test-orders.json").getFile().getAbsolutePath();
        String jsonContent = new String(Files.readAllBytes(Paths.get(filePath)));
        return objectMapper.readValue(jsonContent, new TypeReference<>() {});
    }

    @ParameterizedTest
    @MethodSource("loadOrders")
    void testCalculateFlightPathValidOrders(Order order) throws Exception {
        if (Objects.equals(order.getOrderStatus(), "INVALID")) {
            return;
        }

        when(flightPathService.getFlightPath(any(Order.class))).thenReturn(List.of(new LngLat(0, 0)));

        // Set up headers for the POST request
        String orderJson = objectMapper.writeValueAsString(order);

        // Make the POST request to /calcDeliveryPath
        MvcResult result = mockMvc.perform(post("/calcDeliveryPath")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andReturn();

        // Assert that the response status is OK
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }
}