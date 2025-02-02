package uk.ac.ed.inf.pizzadronz.controllers;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.ed.inf.pizzadronz.constants.OrderStatus;
import uk.ac.ed.inf.pizzadronz.model.*;

import java.util.List;
import uk.ac.ed.inf.pizzadronz.model.LngLat;
import uk.ac.ed.inf.pizzadronz.services.*;


@RestController
public class Controller {

    private final OrderValidationService orderValidationService;
    private final FlightPathService flightPathService;
    private final IsInRegionService isInRegionService;

    public Controller(OrderValidationService orderValidationService, FlightPathService flightPathService,
                      IsInRegionService isInRegionService) {
        this.orderValidationService = orderValidationService;
        this.flightPathService = flightPathService;
        this.isInRegionService = isInRegionService;

    }


    @GetMapping("/isAlive")
    public boolean isAlive() {
        return true;
    }

    @GetMapping("/uuid")
    public String uuid() {
        return "s2323215";
    }

    @PostMapping("/distanceTo")
    public ResponseEntity<Double> distanceTo(@RequestBody LngLatPairRequest request) {

    LngLat position1 = request.getPosition1();
    LngLat position2 = request.getPosition2();

    // Validate request
    if (request == null || request.getPosition1() == null || request.getPosition2() == null ||
            !isValidPosition(request.getPosition1()) || !isValidPosition(request.getPosition2())) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }

    double distance = position1.distanceTo(position2);

    return ResponseEntity.ok(distance);
}

    @PostMapping("/isCloseTo")
    public ResponseEntity<Boolean> isCloseTo(@RequestBody LngLatPairRequest request) {

        LngLat position1 = request.getPosition1();
        LngLat position2 = request.getPosition2();

        // Validates request
        if (request == null || request.getPosition1() == null || request.getPosition2() == null ||
        !isValidPosition(request.getPosition1()) || !isValidPosition(request.getPosition2())) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        // Return true if distance satisfactory otherwise false
        return ResponseEntity.ok(position1.isCloseTo(position2));
    }



    @PostMapping(value = "/nextPosition", produces = "application/json")
    public ResponseEntity<LngLat> nextPosition(@RequestBody NextPositionRequest request) {

        // Validate the request
        if (request == null || request.getStart() == null || request.getAngle() <= 0 || request.getAngle() >= 360) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        double nextLng = request.getStart().getLng() + 0.00015 * Math.cos(request.getAngle() * Math.PI / 180);
        double nextLat = request.getStart().getLat() + 0.00015 * Math.sin(request.getAngle() * Math.PI / 180);

        return ResponseEntity.ok(new LngLat(nextLng, nextLat));
    }


    @PostMapping("/isInRegion")
    public ResponseEntity<Boolean> isInRegion(@RequestBody IsInRegionRequest request) {

        LngLat position = request.getPosition();
        Region region = request.getRegion();

        // Validate request
        if (!request.isValidRequest()) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        boolean result = isInRegionService.isInRegion(position, region);

        return ResponseEntity.ok(result);
    }


    @PostMapping("/validateOrder")
    public ResponseEntity<OrderValidationResult> validateOrder(@RequestBody Order order){
        OrderValidationResult orderValidationResult = orderValidationService.validateOrder(order);

        return new ResponseEntity<>(orderValidationResult, HttpStatus.OK);
    }


    @PostMapping("/calcDeliveryPath")
    public ResponseEntity<List<LngLat>> calcDeliveryPath(@RequestBody Order order){

        //check order valid
        if (orderValidationService.validateOrder(order).getOrderStatus() == OrderStatus.INVALID) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        List<LngLat> flightPath = flightPathService.getFlightPath(order);


        return new ResponseEntity<>(flightPath, HttpStatus.OK);
    }

    @PostMapping("/calcDeliveryPathAsGeoJson")
    public ResponseEntity<String> calcDeliveryPathAsGeoJson(@RequestBody Order order) {

        OrderValidationResult orderValidationResult = orderValidationService.validateOrder(order);

        //check order valid
        if (orderValidationResult.getOrderStatus() == OrderStatus.INVALID) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        List<LngLat> path = flightPathService.getFlightPath(order);

        return ResponseEntity.ok(flightPathService.convertPathToGeoJson(path));
    }


    /*
    Auxiliary functions
     */

    // Checks if position is semantically valid
    private boolean isValidPosition(LngLat position) {

        double lng = position.getLng();
        double lat = position.getLat();
        return lng >= -180 && lng <= 180 && lat <= 90 && lat >= -90;
    }

}