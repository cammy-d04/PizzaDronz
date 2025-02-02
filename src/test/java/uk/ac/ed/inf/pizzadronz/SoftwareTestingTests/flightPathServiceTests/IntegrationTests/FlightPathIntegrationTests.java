package uk.ac.ed.inf.pizzadronz.SoftwareTestingTests.flightPathServiceTests.IntegrationTests;

import org.junit.jupiter.api.Test;
import uk.ac.ed.inf.pizzadronz.model.LngLat;
import uk.ac.ed.inf.pizzadronz.model.Region;
import uk.ac.ed.inf.pizzadronz.services.FlightPathService;
import uk.ac.ed.inf.pizzadronz.services.IsInRegionService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class FlightPathIntegrationTests {

    @Test
    void testFlightPathWithIsInRegionServiceStub() {
        // Create a stub for IsInRegionService
        IsInRegionService isInRegionServiceStub = new IsInRegionServiceStub();

        // Create an instance of FlightPathService and inject the stub
        FlightPathService flightPathService = new FlightPathService(null, isInRegionServiceStub);

        // Define a central area and a no-fly zone (matching the stub's behavior)
        Region centralArea = new Region("Central Area", new LngLat[]{new LngLat(0.0009, 0.0009), new LngLat(0.0011, 0.0009), new LngLat(0.0011, 0.0011), new LngLat(0.0009, 0.0011), new LngLat(0.0009, 0.0009)});
        List<Region> noFlyZones = Arrays.asList(new Region("No Fly Zone 1", new LngLat[]{new LngLat(0.0004, 0.0004), new LngLat(0.0006, 0.0004), new LngLat(0.0006, 0.0006), new LngLat(0.0004, 0.0006), new LngLat(0.0004, 0.0004)}));

        // Define start and end points
        LngLat start = new LngLat(0, 0);
        LngLat end = new LngLat(0.001, 0.001);

        // Call findPath
        List<LngLat> path = flightPathService.findPath(start, end, noFlyZones, centralArea);

        // Assertions
        assertFalse(path.isEmpty(), "A path should have been found.");

        // Verify start and end points
        assertEquals(start, path.get(0), "Path should start at the starting point.");
        assertTrue(path.get(path.size() - 1).isCloseTo(end), "Path should end near the destination.");


        for (LngLat point: path) {
            assertFalse(point.getLng() >= 0.5 && point.getLng() <= 0.6 &&
                    point.getLat() >= 0.5 && point.getLat() <= 0.6, "Path should not cross the no-fly zone.");
        }
    }


}

// A simple stub implementation of IsInRegionService
class  IsInRegionServiceStub extends IsInRegionService {

    @Override
    public boolean isInRegion(LngLat position, Region region) {
        // Define the behavior of the stub
        if (region.getName().equals("Central Area")) {
            // Always inside the central area (for simplicity)
            return true;
        } else if (region.getName().equals("No Fly Zone")) {
            // Define the no-fly zone area
            return position.getLng() >= 0.5 && position.getLng() <= 0.6 &&
                    position.getLat() >= 0.5 && position.getLat() <= 0.6;
        } else {
            return false;
        }
    }






}
