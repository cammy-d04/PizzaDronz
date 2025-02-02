package uk.ac.ed.inf.pizzadronz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import uk.ac.ed.inf.pizzadronz.constants.SystemConstants;
import uk.ac.ed.inf.pizzadronz.controllers.Controller;
import uk.ac.ed.inf.pizzadronz.model.LngLat;
import uk.ac.ed.inf.pizzadronz.model.Region;
import uk.ac.ed.inf.pizzadronz.services.FlightPathService;
import uk.ac.ed.inf.pizzadronz.services.IsInRegionService;
import uk.ac.ed.inf.pizzadronz.services.OrderValidationService;
import uk.ac.ed.inf.pizzadronz.services.RestaurantService;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(Controller.class)
class ControllerTests {

    @Autowired
    private MockMvc mockMvc;


    @MockBean
    private FlightPathService flightPathService;

    @MockBean
    private RestaurantService restaurantService;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private OrderValidationService orderValidationService;

    @MockBean
    private IsInRegionService isInRegionService;


    // Test isAlive
    @Test
    public void testIsAlive() throws Exception {
        mockMvc.perform(get("/isAlive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(true)));
    }

    // Test uuid
    @Test
    public void testUUID() throws Exception {
        mockMvc.perform(get("/uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is("s2323215")));
    }

    // Test distanceTo with request with missing positions
    @Test
    public void testDistanceTo_MissingPosition() throws Exception {
        String json = "{\"position1\":{\"lng\":-3.188267,\"lat\":55.944154}}";
        mockMvc.perform(post("/distanceTo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    // Test distanceTo with valid coordinates just within bounds
    @Test
    public void testDistanceTo_JustWithinBounds() throws Exception {
        String json = "{\"position1\":{\"lng\":179.999,\"lat\":89.999},\"position2\":{\"lng\":-179.999,\"lat\":-89.999}}";
        mockMvc.perform(post("/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    // Test distanceTo with invalid coordinates just outwith bounds
    @Test
    public void testDistanceTo_JustOutwithBounds() throws Exception {
        String json = "{\"position1\":{\"lng\":180.001,\"lat\":90.001},\"position2\":{\"lng\":-180.001,\"lat\":-90.001}}";
        mockMvc.perform(post("/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    // Test distance to with unique coordinates
    @Test
    void testDistanceTo_UniqueCoordinates() {
        LngLat point1 = new LngLat(-10, 0);
        LngLat point2 = new LngLat(0, 0);

        double distance = point1.distanceTo(point2);
        assertEquals(10, distance, SystemConstants.DRONE_IS_CLOSE_DISTANCE);
    }

    // Test distanceTo same coordinates
    @Test
    void testDistanceTo_SameCoordinates() {
        LngLat point1 = new LngLat(5, 5);
        LngLat point2 = new LngLat(5, 5);

        double distance = point1.distanceTo(point2);
        assertEquals(0, distance, SystemConstants.DRONE_IS_CLOSE_DISTANCE);
    }



    // Test isCloseTo with distance greater than 0.00015
    @Test
    public void testIsCloseTo_ValidRequest_False() throws Exception {
        String json = "{\"position1\":{\"lng\":-3.188267,\"lat\":55.944154},\"position2\":{\"lng\":-3.202473,\"lat\":55.946233}}";
        mockMvc.perform(post("/isCloseTo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(false)));
    }

    // Test isCloseTo with distance less than 0.00015
    @Test
    public void testIsCloseTo_ValidRequest_True() throws Exception {
        String json = "{\"position1\":{\"lng\":1,\"lat\":1},\"position2\":{\"lng\":1,\"lat\":1.00014}}";
        mockMvc.perform(post("/isCloseTo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(true)));
    }


    // Test nextPosition with a valid request
    @Test
    public void testNextPosition_ValidRequest() throws Exception {
        String json = "{\"start\":{\"lng\":-3.188267,\"lat\":55.944154},\"angle\":45}";
        mockMvc.perform(post("/nextPosition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }


    // Test nextPosition with a negative angle
    @Test
    public void testNextPosition_invalidAngle1() throws Exception {
        String json = "{\"start\":{\"lng\":-3.188267,\"lat\":55.944154},\"angle\":-1}";
        mockMvc.perform(post("/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }



    // Test nextPosition with an angle > 360
    @Test
    public void testNextPosition_invalidAngle2() throws Exception {
        String json = "{\"start\":{\"lng\":-3.188267,\"lat\":55.944154},\"angle\":361}";
        mockMvc.perform(post("/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    // Test nextPosition with an invalid request with missing start
    @Test
    public void testNextPosition_MissingStart() throws Exception {
        String json = "{\"angle\":45}";
        mockMvc.perform(post("/nextPosition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    // Test nextPosition with an invalid request with missing angle
    @Test
    public void testNextPosition_MissingAngle() throws Exception {
        String json = "{\"start\":{\"lng\":-3.188267,\"lat\":55.944154}}";
        mockMvc.perform(post("/nextPosition")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }







// Test isInRegion with a valid request
@Test
public void testIsInRegion_ValidRequest() throws Exception {
    String json = "{\"position\":{\"lng\":0.5,\"lat\":0.5},\"region\":{\"name\":\"central\",\"vertices\":[{\"lng\":0,\"lat\":1},{\"lng\":0,\"lat\":0},{\"lng\":1,\"lat\":0},{\"lng\":1,\"lat\":1},{\"lng\":0,\"lat\":1}]}}";

    // Mock the service to return true using mockito
    when(isInRegionService.isInRegion(any(LngLat.class), any(Region.class))).thenReturn(true);

    mockMvc.perform(post("/isInRegion")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", is(true)));
}



    // Test isInRegion with a request with < 4 vertices
    @Test
    public void testIsInRegion_InvalidRequest() throws Exception {
        String json = "{\"position\":{\"lng\":-3.188267,\"lat\":55.944154},\"region\":[{\"lng\":-3.1871,\"lat\":55.9441},{\"lng\":-3.1871,\"lat\":55.9442}]}";
        mockMvc.perform(post("/isInRegion")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    // Test isInRegion with
}
