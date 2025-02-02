package uk.ac.ed.inf.pizzadronz.services;

import org.springframework.stereotype.Service;
import uk.ac.ed.inf.pizzadronz.model.LngLat;
import uk.ac.ed.inf.pizzadronz.model.Order;
import uk.ac.ed.inf.pizzadronz.model.Region;
import uk.ac.ed.inf.pizzadronz.model.Restaurant;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static uk.ac.ed.inf.pizzadronz.constants.SystemConstants.*;

@Service
public class FlightPathService {

    private final IsInRegionService isInRegionService;
    private final RestTemplate restTemplate;

    public FlightPathService(RestTemplate restTemplate, IsInRegionService isInRegionService) {

        this.isInRegionService = isInRegionService;
        this.restTemplate = restTemplate;
    }


    public List<Region> getNoFlyZones() {
        try {
            // Fetch the list of noFlyZones from the API
            Region[] noFlyZones = restTemplate.getForObject(
                    "https://ilp-rest-2024.azurewebsites.net/noFlyZones",
                    Region[].class
            );
            // Check if the response is null
            if (noFlyZones == null) {
                throw new IllegalStateException("No 'No Fly Zones' were returned by the API.");
            }

            // Log the updated list
            System.out.println("'No Fly Zones' successfully retrieved");

            return new ArrayList<>(Arrays.asList(noFlyZones));

        } catch (Exception e) {
            throw new RuntimeException("An error occurred while fetching 'No Fly Zones': " + e.getMessage(), e);
        }
    }




    public Region getCentralArea() {
        try {
            // Fetch the list of noFlyZones from the API
            Region centralArea = restTemplate.getForObject(
                    "https://ilp-rest-2024.azurewebsites.net/centralArea",
                    Region.class
            );
            // Check if the response is null
            if (centralArea == null) {
                throw new IllegalStateException("No 'Central Area' was returned by the API.");
            }

            // Log the updated list
            System.out.println("'Central Area' successfully retrieved");

            return centralArea;

        } catch (Exception e) {
            throw new RuntimeException("An error occurred while fetching 'Central Area': " + e.getMessage(), e);
        }
    }

    public List<LngLat> getFlightPath(Order order) {
        List<Region> noFlyZones = getNoFlyZones();
        Region centralArea = getCentralArea();
        Restaurant restaurant = order.getRestaurant();
        LngLat start = restaurant.getLocation();
        LngLat end = new LngLat(APPLETON_LNG, APPLETON_LAT);

        System.out.println("Calculating Flight Path...");

        return findPath(start, end, noFlyZones, centralArea);
    }



    public List<LngLat> findPath(LngLat start, LngLat end, List<Region> noFlyZones, Region centralArea) {
        // Initialize a priority queue for nodes sorted by f cost
        // where g is the cost from the start node and h is the heuristic
        PriorityQueue<Node> openNodes = new PriorityQueue<>(Comparator.comparingDouble(Node::getF).thenComparingDouble(Node::getH));

        // Maintain a set of already visited positions to avoid processing them again
        Set<String> closedPositions = new HashSet<>();

        // Create the starting node with an initial cost of 0 and heuristic distance to end
        Node startNode = new Node(start, null, 0, start.distanceTo(end));
        openNodes.add(startNode);

        // Safety limit to prevent infinite loops during pathfinding
        final int MAX_ITERATIONS = 100000;
        int iterations = 0;

        // Main loop for pathfinding
        while (!openNodes.isEmpty() && iterations < MAX_ITERATIONS) {
            // Get the node with the lowest cost from the open list
            Node current = openNodes.poll();
            String currentPositionKey = getPositionKey(current.getPosition());

            // If the current position is close enough to the destination construct the path and return it
            if (current.getPosition().isCloseTo(end)) {
                return constructPath(current);
            }

            // Skip processing if this position has already been visited
            if (closedPositions.contains(currentPositionKey)) {
                continue;
            }
            closedPositions.add(currentPositionKey);

            // Get neighboring nodes
            List<Node> neighbors = getNeighbors(current, end);

            for (Node neighbor : neighbors) {
                String neighborPositionKey = getPositionKey(neighbor.getPosition());

                // Skip this neighbor if it's already in the closed list
                if (closedPositions.contains(neighborPositionKey)) {
                    continue;
                }

                // Check if the move to this neighbor avoids no-fly zones and central area
                if (!isValidMove(current.getPosition(), neighbor.getPosition(), current.getParent(), noFlyZones, centralArea)) {
                    continue;
                }

                // Calculate the g cost for this neighbor
                double newG = current.getG() + current.getPosition().distanceTo(neighbor.getPosition());

                // If this path to the neighbor is better update its costs and add it to the open list
                if (!openNodes.contains(neighbor) || newG < neighbor.getG()) {
                    neighbor.setParent(current);
                    neighbor.setG(newG);

                    // Remove and re add the neighbor to the open list to update its priority
                    if (openNodes.contains(neighbor)) {
                        openNodes.remove(neighbor);
                    }
                    openNodes.add(neighbor);
                }
            }

            iterations++;
        }

        System.out.println("Path finding exceeded maximum iterations or no path found");
        return Collections.emptyList();
    }


    // Helper method to generate a unique key for a position
    private String getPositionKey(LngLat position) {
        // Round to 6 decimal places to handle floating point precision
        return String.format("%.6f,%.6f", position.getLng(), position.getLat());
    }



    public List<LngLat> constructPath(Node node) {
        List<LngLat> path = new ArrayList<>();
        while (node != null) {
            path.add(node.getPosition());
            node = node.getParent();
        }
        Collections.reverse(path);
        return path;
    }



    // Get the neighbors of a position
    public List<Node> getNeighbors(Node current, LngLat end) {
    List<Node> neighbors = new ArrayList<>();
    LngLat position = current.getPosition();

    //for each angle calculate the next position and add it to the neighbors list
    for (double angle : FLIGHT_ANGLES) {
        double radian = Math.toRadians(angle);
        double nextLng = position.getLng() + DRONE_MOVE_DISTANCE * Math.cos(radian);
        double nextLat = position.getLat() + DRONE_MOVE_DISTANCE * Math.sin(radian);
        LngLat neighborPosition = new LngLat(nextLng, nextLat);
        double gCost = current.getG() + position.distanceTo(neighborPosition);
        double hCost = neighborPosition.distanceTo(end);

        neighbors.add(new Node(neighborPosition, current, gCost, hCost));
    }
    return neighbors;
}



    public boolean isValidMove(LngLat current, LngLat next, Node previous, List<Region> noFlyZones, Region centralArea) {
        // Check if the current position is in the central area and the next position is not
        if (isInRegionService.isInRegion(current, centralArea) && !isInRegionService.isInRegion(next, centralArea)) {
            return false;
        }

        // Check if the next position is in a no-fly zone

        for (Region noFlyZone : noFlyZones) {
            if (isInRegionService.isInRegion(next, noFlyZone)) {
                return false;
            }
        }

        // Check the angle of movement
        if (previous != null) {
            LngLat previousPosition = previous.getPosition();

            double previousAngle = calculateAngle(previousPosition, current);
            double nextAngle = calculateAngle(current, next);
            double angleDifference = Math.abs(previousAngle - nextAngle);

            // Normalize the angle difference to 0, 180
            if (angleDifference > 180) {
                angleDifference = 360 - angleDifference;
            }

            // Ensure the turn angle does not exceed 112.5 degrees
            return !(angleDifference > 112.5);
        }

        return true;
    }

    // Helper method to calculate the angle between two points
    public double calculateAngle(LngLat from, LngLat to) {
        double deltaX = to.getLng() - from.getLng();
        double deltaY = to.getLat() - from.getLat();
        return Math.toDegrees(Math.atan2(deltaY, deltaX));
    }




    // Node class to store the position, parent, gcost and hcost of a node

    public static class Node {
        private LngLat position;
        private Node parent;
        private double g;
        private double h;

        public Node(LngLat position, Node parent, double g, double h) {
            this.position = position;
            this.parent = parent;
            this.g = g;
            this.h = h;
        }

        public LngLat getPosition() {
            return position;
        }

        public Node getParent() {
            return parent;
        }

        public double getG() {
            return g;
        }

        public double getF() {
            return g + h;
        }

        public double getH() {
            return h;
        }

        public void setG(double g) {
            this.g = g;
        }

        public void setH(double h)
        {
            this.h = h;
        }

        public void setParent(Node parent) {
            this.parent = parent;
        }

    }




    public String convertPathToGeoJson(List<LngLat> path) {
        StringBuilder geoJson = new StringBuilder();

        // Start of the GeoJSON structure
        geoJson.append("{\"type\":\"FeatureCollection\",\"features\":[{")
                .append("\"type\":\"Feature\",\"geometry\":{")
                .append("\"type\":\"LineString\",\"coordinates\":[");

        // Add coordinates
        for (LngLat point : path) {
            geoJson.append("[").append(point.getLng()).append(",").append(point.getLat()).append("],");
        }

        // Remove the trailing comma from the coordinates
        if (!path.isEmpty()) {
            geoJson.setLength(geoJson.length() - 1);
        }

        // Close GeoJSON structure
        geoJson.append("]},\"properties\":{}}]}");

        // Return the GeoJSON string
        return geoJson.toString();
    }

}
