package uk.ac.ed.inf.pizzadronz.SoftwareTestingTests.controllerTests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.ac.ed.inf.pizzadronz.constants.OrderStatus;
import uk.ac.ed.inf.pizzadronz.constants.OrderValidationCode;
import uk.ac.ed.inf.pizzadronz.controllers.Controller;
import uk.ac.ed.inf.pizzadronz.model.Order;
import uk.ac.ed.inf.pizzadronz.model.OrderValidationResult;
import uk.ac.ed.inf.pizzadronz.services.FlightPathService;
import uk.ac.ed.inf.pizzadronz.services.IsInRegionService;
import uk.ac.ed.inf.pizzadronz.services.OrderValidationService;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.ac.ed.inf.pizzadronz.SoftwareTestingTests.testUtils.JsonUtil.asJsonString;

@WebMvcTest(Controller.class)
public class ControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IsInRegionService isInRegionService;

    @MockBean
    private FlightPathService flightPathService;

    @MockBean
    private OrderValidationService orderValidationService;

    private static final ObjectMapper objectMapper = new ObjectMapper();


    @Test
    void testFlightPathsFromJson() throws Exception {
        // Load orders from JSON file
        String filePath = new ClassPathResource("test-orders.json").getFile().getAbsolutePath();
        String jsonContent = new String(Files.readAllBytes(Paths.get(filePath)));
        List<Order> orders = objectMapper.readValue(jsonContent, new TypeReference<>() {
        });

        // Iterate through the orders and perform flight path tests
        for (Order order: orders) {
            // Mock the flightPathService.getFlightPath method
            when(flightPathService.getFlightPath(any(Order.class)))
                    .thenReturn(Collections.emptyList()); // Provide a dummy path for now

            // Mock the orderValidationService to always return VALID
            when(orderValidationService.validateOrder(any(Order.class)))
                    .thenReturn(new OrderValidationResult(OrderStatus.VALID, OrderValidationCode.NO_ERROR));

            // Perform the POST requests and assert the status
            mockMvc.perform(post("/calcDeliveryPath")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(order)))
                    .andExpect(status().isOk());

        }

    }


}