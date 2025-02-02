package uk.ac.ed.inf.pizzadronz.SoftwareTestingTests.modelLogicTests;

import org.junit.jupiter.api.Test;
import uk.ac.ed.inf.pizzadronz.model.LngLat;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LngLatTests {

    @Test
    void testIsCloseToMethod() {
        LngLat pointA = new LngLat(0, 0);
        LngLat pointB = new LngLat(0.00015, 0); // Very close to pointA

        assertTrue(pointA.isCloseTo(pointB));
    }



}
