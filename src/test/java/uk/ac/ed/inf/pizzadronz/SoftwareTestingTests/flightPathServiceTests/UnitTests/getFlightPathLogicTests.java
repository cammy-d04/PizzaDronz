package uk.ac.ed.inf.pizzadronz.SoftwareTestingTests.flightPathServiceTests.UnitTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ed.inf.pizzadronz.model.LngLat;
import uk.ac.ed.inf.pizzadronz.model.Region;
import uk.ac.ed.inf.pizzadronz.services.FlightPathService;
import uk.ac.ed.inf.pizzadronz.services.IsInRegionService;
import uk.ac.ed.inf.pizzadronz.services.FlightPathService.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.ac.ed.inf.pizzadronz.constants.SystemConstants.DRONE_MOVE_DISTANCE;
import static uk.ac.ed.inf.pizzadronz.constants.SystemConstants.FLIGHT_ANGLES;

public class getFlightPathLogicTests {
    @Mock
    private IsInRegionService isInRegionService;

    @Mock
    private RestTemplate restTemplate;

    private FlightPathService flightPathService;




    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        flightPathService = new FlightPathService(restTemplate, isInRegionService);
    }




    /*
    Test the getNoFlyZones method
    */

    @Test
    void testGetNoFlyZonesHandlesHttpClientErrorException() {
        // Mock the restTemplate to throw an HttpClientErrorException
        when(restTemplate.getForObject("https://ilp-rest-2024.azurewebsites.net/noFlyZones", Region.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found"));

        // Call the method and assert that it throws a RuntimeException
        assertThrows(RuntimeException.class, () -> flightPathService.getNoFlyZones());
    }

    @Test
    void testGetNoFlyZonesHandlesResourceAccessException() {
        // Mock the restTemplate to throw a ResourceAccessException
        when(restTemplate.getForObject("https://ilp-rest-2024.azurewebsites.net/noFlyZones", Region.class))
                .thenThrow(new ResourceAccessException("Network error"));

        // Call the method and assert that it throws a RuntimeException
        assertThrows(RuntimeException.class, () -> flightPathService.getNoFlyZones());
    }

    @Test
    void testGetNoFlyZones_NullResponse() {
        // Mock the RestTemplate to return null
        when(restTemplate.getForObject("https://ilp-rest-2024.azurewebsites.net/noFlyZones", Region[].class))
                .thenReturn(null);

        // Assert that a RuntimeException is thrown
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            flightPathService.getNoFlyZones();
        });

        // Assert that the cause of the RuntimeException is an IllegalStateException
        assertTrue(exception.getCause() instanceof IllegalStateException);
        assertEquals("No 'No Fly Zones' were returned by the API.", exception.getCause().getMessage());
    }






    /*
    Equivalence testing for calculateAngle method
     */

    @Test
    void testCalculateAnglePositiveSlope() {
        LngLat from = new LngLat(0, 0);
        LngLat to = new LngLat(1, 1);
        double angle = flightPathService.calculateAngle(from, to);
        assertEquals(45.0, angle, 0.1);
    }

    @Test
    void testCalculateAngleNegativeSlope() {
        LngLat from = new LngLat(0, 0);
        LngLat to = new LngLat(1, -1);
        double angle = flightPathService.calculateAngle(from, to);
        assertEquals(-45.0, angle, 0.1);
    }

    @Test
    void testCalculateAngleHorizontalRight() {
        LngLat from = new LngLat(0, 0);
        LngLat to = new LngLat(1, 0);
        double angle = flightPathService.calculateAngle(from, to);
        assertEquals(0.0, angle, 0.1);
    }

    @Test
    void testCalculateAngleHorizontalLeft() {
        LngLat from = new LngLat(1, 0);
        LngLat to = new LngLat(0, 0);
        double angle = flightPathService.calculateAngle(from, to);
        assertEquals(180.0, angle, 0.1);
    }

    @Test
    void testCalculateAngleVerticalUp() {
        LngLat from = new LngLat(0, 0);
        LngLat to = new LngLat(0, 1);
        double angle = flightPathService.calculateAngle(from, to);
        assertEquals(90.0, angle, 0.1);
    }

    @Test
    void testCalculateAngleVerticalDown() {
        LngLat from = new LngLat(0, 1);
        LngLat to = new LngLat(0, 0);
        double angle = flightPathService.calculateAngle(from, to);
        assertEquals(-90.0, angle, 0.1);

    }




    /*
    Testing for isValidMove method
     */

    @Test
    void testValidMoveWithinCentralArea() {
        LngLat current = new LngLat(0.5, 0.5);
        LngLat next = new LngLat(0.6, 0.5);
        Node previous = new Node(new LngLat(0.4, 0.5), null, 0, 0);
        Region centralArea = new Region("Central Area", new LngLat[]{new LngLat(0, 0), new LngLat(1, 0), new LngLat(1, 1), new LngLat(0, 1)});

        when(isInRegionService.isInRegion(current, centralArea)).thenReturn(true);
        when(isInRegionService.isInRegion(next, centralArea)).thenReturn(true);

        assertTrue(flightPathService.isValidMove(current, next, previous, List.of(), centralArea));
    }

    @Test
    void testInvalidMoveOutsideCentralArea() {
        LngLat current = new LngLat(0.5, 0.5);
        LngLat next = new LngLat(1.5, 0.5);
        Node previous = new Node(new LngLat(0.4, 0.5), null, 0, 0);
        Region centralArea = new Region("Central Area", new LngLat[]{new LngLat(0, 0), new LngLat(1, 0), new LngLat(1, 1), new LngLat(0, 1)});

        when(isInRegionService.isInRegion(current, centralArea)).thenReturn(true);
        when(isInRegionService.isInRegion(next, centralArea)).thenReturn(false);

        assertFalse(flightPathService.isValidMove(current, next, previous, List.of(), centralArea));
    }

    @Test
    void testInvalidMoveIntoNoFlyZone() {
        LngLat current = new LngLat(0.5, 0.5);
        LngLat next = new LngLat(2.5, 2.5);
        Node previous = new Node(new LngLat(0.4, 0.5), null, 0, 0);
        Region centralArea = new Region("Central Area", new LngLat[]{new LngLat(0, 0), new LngLat(1, 0), new LngLat(1, 1), new LngLat(0, 1)});
        Region noFlyZone = new Region("NoFlyZone", new LngLat[]{new LngLat(2, 2), new LngLat(3, 2), new LngLat(3, 3), new LngLat(2, 3)});

        when(isInRegionService.isInRegion(next, noFlyZone)).thenReturn(true);

        assertFalse(flightPathService.isValidMove(current, next, previous, List.of(noFlyZone), centralArea));
    }

    @Test
    void testValidMoveWithValidAngle() {
        LngLat current = new LngLat(0.5, 0.5);
        LngLat next = new LngLat(0.6, 0.6);
        Node previous = new Node(new LngLat(0.4, 0.4), null, 0, 0);
        Region centralArea = new Region("Central Area", new LngLat[]{new LngLat(0, 0), new LngLat(1, 0), new LngLat(1, 1), new LngLat(0, 1)});

        assertTrue(flightPathService.isValidMove(current, next, previous, List.of(), centralArea));
    }

    @Test
    void testInvalidMoveWithInvalidAngle() {
        LngLat current = new LngLat(0.5, 0.5);
        LngLat next = new LngLat(0, 0);
        Node previous = new Node(new LngLat(0, 0), null, 0, 0);
        Region centralArea = new Region("Central Area", new LngLat[]{new LngLat(0, 0), new LngLat(1, 0), new LngLat(1, 1), new LngLat(0, 1)});

        assertFalse(flightPathService.isValidMove(current, next, previous, List.of(), centralArea));

    }


    /*
    Boundary testing for isValidMove method
     */
    @Test
    void testInvalidMoveWithBoundaryAngle() {
        LngLat current = new LngLat(1, 1.5);
        LngLat next = new LngLat(2, 0); // Position chosen to be at approximately 112.6 degrees
        Node previous = new Node(new LngLat(0, 0), null, 0, 0);
        Region centralArea = new Region("Central Area", new LngLat[]{new LngLat(0, 0), new LngLat(1, 0), new LngLat(1, 1), new LngLat(0, 1)});

        assertFalse(flightPathService.isValidMove(current, next, previous, List.of(), centralArea));
    }

    @Test
    void testValidMoveWithBoundaryAngle() {
        LngLat current = new LngLat(1, 1.45);
        LngLat next = new LngLat(2, 0); // Position chosen to be at approximately 110 degrees
        Node previous = new Node(new LngLat(0, 0), null, 0, 0);
        Region centralArea = new Region("Central Area", new LngLat[]{new LngLat(0, 0), new LngLat(1, 0), new LngLat(1, 1), new LngLat(0, 1)});

        assertTrue(flightPathService.isValidMove(current, next, previous, List.of(), centralArea));
    }

    @Test
    public void testGetNeighborsSize() {
        FlightPathService flightPathService = new FlightPathService(null, null);
        LngLat currentPosition = new LngLat(-3.186874, 55.944494);
        LngLat endPosition = new LngLat(-3.186874, 55.944494);

        FlightPathService.Node node = new FlightPathService.Node(currentPosition, null, 0, 0);

        List<FlightPathService.Node> neighbors = flightPathService.getNeighbors(node, endPosition);

        assertEquals(FLIGHT_ANGLES.length, neighbors.size());
    }

    @Test
    public void testGetNeighborsPositions() {
        FlightPathService flightPathService = new FlightPathService(null, null);
        LngLat currentPosition = new LngLat(-3.186874, 55.944494);
        LngLat endPosition = new LngLat(-3.19, 55.9446);
        FlightPathService.Node node = new FlightPathService.Node(currentPosition, null, 0, 0);

        List<FlightPathService.Node> neighbors = flightPathService.getNeighbors(node, endPosition);

        for (int i = 0; i < FLIGHT_ANGLES.length; i++) {
            double angle = FLIGHT_ANGLES[i];
            double radian = Math.toRadians(angle);
            double expectedLng = currentPosition.getLng() + DRONE_MOVE_DISTANCE * Math.cos(radian);
            double expectedLat = currentPosition.getLat() + DRONE_MOVE_DISTANCE * Math.sin(radian);

            assertEquals(expectedLng, neighbors.get(i).getPosition().getLng());
            assertEquals(expectedLat, neighbors.get(i).getPosition().getLat());
        }
    }


    @Test
    public void testGetNeighborsGCost() {
        FlightPathService flightPathService = new FlightPathService(null, null);
        LngLat currentPosition = new LngLat(-3.186874, 55.944494);
        LngLat endPosition = new LngLat(-3.19, 55.9446);
        double currentGCost = 5.5;
        FlightPathService.Node node = new FlightPathService.Node(currentPosition, null, currentGCost, 0);

        List<FlightPathService.Node> neighbors = flightPathService.getNeighbors(node, endPosition);

        for (int i = 0; i < FLIGHT_ANGLES.length; i++) {
            double angle = FLIGHT_ANGLES[i];
            double radian = Math.toRadians(angle);
            double expectedLng = currentPosition.getLng() + DRONE_MOVE_DISTANCE * Math.cos(radian);
            double expectedLat = currentPosition.getLat() + DRONE_MOVE_DISTANCE * Math.sin(radian);
            LngLat neighborPosition = new LngLat(expectedLng, expectedLat);
            double expectedGCost = currentGCost + currentPosition.distanceTo(neighborPosition);

            assertEquals(expectedGCost, neighbors.get(i).getG());
        }
    }

    @Test
    public void testGetNeighborsHCost() {
        FlightPathService flightPathService = new FlightPathService(null, null);
        LngLat currentPosition = new LngLat(-3.186874, 55.944494);
        LngLat endPosition = new LngLat(-3.19, 55.9446);
        FlightPathService.Node node = new FlightPathService.Node(currentPosition, null, 0, 0);

        List<FlightPathService.Node> neighbors = flightPathService.getNeighbors(node, endPosition);

        for (int i = 0; i < FLIGHT_ANGLES.length; i++) {
            double angle = FLIGHT_ANGLES[i];
            double radian = Math.toRadians(angle);
            double expectedLng = currentPosition.getLng() + DRONE_MOVE_DISTANCE * Math.cos(radian);
            double expectedLat = currentPosition.getLat() + DRONE_MOVE_DISTANCE * Math.sin(radian);
            LngLat neighborPosition = new LngLat(expectedLng, expectedLat);
            double expectedHCost = neighborPosition.distanceTo(endPosition);

            assertEquals(expectedHCost, neighbors.get(i).getH());
        }

    }














    @Test
    public void testConstructPath_singleNode() {
        FlightPathService flightPathService = new FlightPathService(null, null);
        LngLat position = new LngLat(-3.186874, 55.944494);
        FlightPathService.Node node = new FlightPathService.Node(position, null, 0, 0);

        List<LngLat> path = flightPathService.constructPath(node);

        assertEquals(1, path.size());
        assertEquals(position, path.get(0));
    }

    @Test
    public void testConstructPath_multipleNodes() {
        FlightPathService flightPathService = new FlightPathService(null, null);
        LngLat position1 = new LngLat(-3.186874, 55.944494);
        LngLat position2 = new LngLat(-3.186, 55.9445);
        LngLat position3 = new LngLat(-3.185, 55.9446);

        FlightPathService.Node node3 = new FlightPathService.Node(position3, null, 0, 0);
        FlightPathService.Node node2 = new FlightPathService.Node(position2, node3, 0, 0);
        FlightPathService.Node node1 = new FlightPathService.Node(position1, node2, 0, 0);

        List<LngLat> path = flightPathService.constructPath(node1);

        assertEquals(3, path.size());
        assertEquals(position1, path.get(2));
        assertEquals(position2, path.get(1));
        assertEquals(position3, path.get(0));
    }

    @Test
    public void testConstructPath_noNodes() {
        FlightPathService flightPathService = new FlightPathService(null, null);

        List<LngLat> path = flightPathService.constructPath(null);

        assertEquals(Collections.emptyList(), path);
    }














    @Test
    void testPathFindingWithNoObstacles() {
        LngLat start = new LngLat(0, 0);
        LngLat end = new LngLat(0.001, 0.001);
        List<Region> noFlyZones = Collections.emptyList();
        Region centralArea = new Region("Central Area", new LngLat[]{new LngLat(0, 0), new LngLat(10, 0), new LngLat(10, 10), new LngLat(0, 10)});

        List<LngLat> path = flightPathService.findPath(start, end, noFlyZones, centralArea);

        assertFalse(path.isEmpty());
        assertEquals(start, path.get(0));
        assertTrue(path.get(path.size() - 1).isCloseTo(end));
    }

    @Test
    void testPathFindingWithObstacles() {
        LngLat start = new LngLat(0, 0);
        LngLat end = new LngLat(0.001, 0.001);
        List<Region> noFlyZones = Arrays.asList(new Region("No Fly Zone 1", new LngLat[]{new LngLat(0.0004, 0.0004), new LngLat(0.0006, 0.0004), new LngLat(0.0006, 0.0006), new LngLat(0.0004, 0.0006), new LngLat(0.0004, 0.0004)}));
        Region centralArea = new Region("Central Area", new LngLat[]{new LngLat(0.0009, 0.0009), new LngLat(0.0011, 0.0009), new LngLat(0.0011, 0.0011), new LngLat(0.0009, 0.0011), new LngLat(0.0009, 0.0009)});

        List<LngLat> path = flightPathService.findPath(start, end, noFlyZones, centralArea);

        assertFalse(path.isEmpty());
        assertEquals(start, path.get(0));
        assertTrue(path.get(path.size() - 1).isCloseTo(end));
    }

    @Test
    void testPathFindingSameStartEnd() {
        LngLat start = new LngLat(0, 0);
        LngLat end = new LngLat(0, 0);
        List<Region> noFlyZones = Collections.emptyList();
        Region centralArea = new Region("Central Area", new LngLat[]{new LngLat(0, 0), new LngLat(10, 0), new LngLat(10, 10), new LngLat(0, 10)});

        List<LngLat> path = flightPathService.findPath(start, end, noFlyZones, centralArea);

        assertEquals(1, path.size());
        assertEquals(start, path.get(0));
    }








    @Test
    void testGetCentralArea_NullResponse() {
        // Mock the RestTemplate to return null
        when(restTemplate.getForObject("https://ilp-rest-2024.azurewebsites.net/centralArea", Region.class))
                .thenReturn(null);

        // Assert that an IllegalStateException is thrown
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            flightPathService.getCentralArea();
        });

        // Assert that the cause of the RuntimeException is an IllegalStateException
        assertTrue(exception.getCause() instanceof IllegalStateException);
        assertEquals("No 'Central Area' was returned by the API.", exception.getCause().getMessage());
    }

    @Test
    void testGetCentralArea_Exception() {
        // Mock the RestTemplate to throw an exception
        when(restTemplate.getForObject("https://ilp-rest-2024.azurewebsites.net/centralArea", Region.class))
                .thenThrow(new RuntimeException("API error"));

        // Assert that a RuntimeException is thrown
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            flightPathService.getCentralArea();
        });

        // Assert that the message of the RuntimeException is correct
        assertEquals("An error occurred while fetching 'Central Area': API error", exception.getMessage());
    }

}
