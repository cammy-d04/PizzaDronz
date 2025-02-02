package uk.ac.ed.inf.pizzadronz.services;

import org.springframework.stereotype.Service;
import uk.ac.ed.inf.pizzadronz.model.IsInRegionRequest;
import uk.ac.ed.inf.pizzadronz.model.LngLat;
import uk.ac.ed.inf.pizzadronz.model.Region;

import java.awt.geom.Line2D;

@Service
public class IsInRegionService {

    public boolean isInRegion(LngLat position, Region region) {

        LngLat point = position;
        LngLat[] vertices = region.getVertices();
        int rightCounter = 0;

        // Make ray slightly further right than the furthest right vertex
        LngLat ray = new LngLat(181, point.getLat());

        // Loop through each vertex
        for (int i = 0; i < vertices.length; i++) {
            LngLat v1 = vertices[i];
            LngLat v2 = vertices[(i + 1) % vertices.length];

            // Check if point on a vertex
            if (isPointOnVertex(point, v1) || isPointOnVertex(point, v2)) {
                return true;
            }

            // Check if point on the edge
            if (isPointOnLine(point, v1, v2)) {
                return true;
            }

            // Count ray intersections
            if (Line2D.linesIntersect(point.getLng(), point.getLat(), ray.getLng(),
                    ray.getLat(), v1.getLng(), v1.getLat(), v2.getLng(), v2.getLat())) {

                // Additional logic covers case where point is collinear with two vertices
                if (!(point.getLat() == v1.getLat() && point.getLat() == v2.getLat() && !isPointOnLine(point, v1, v2))) {
                    rightCounter++;
                }
            }
        }

        return rightCounter % 2 != 0;
    }

    private boolean isPointOnVertex(LngLat point, LngLat vertex) {
        return point.getLng() == vertex.getLng() && point.getLat() == vertex.getLat();
    }

    private boolean isPointOnLine(LngLat point, LngLat v1, LngLat v2) {
        // Ensure line segment is not vertical
        if (v1.getLng() == v2.getLng()) {
            return point.getLng() == v1.getLng() &&
                    point.getLat() >= Math.min(v1.getLat(), v2.getLat()) &&
                    point.getLat() <= Math.max(v1.getLat(), v2.getLat());
        }

        // Calculate the slope
        double slope = (v2.getLat() - v1.getLat()) / (v2.getLng() - v1.getLng());
        // Calculate y intercept
        double intercept = v1.getLat() - (slope * v1.getLng());

        // Check if the point lies on the line
        return point.getLat() == slope * point.getLng() + intercept &&
                point.getLng() >= Math.min(v1.getLng(), v2.getLng()) &&
                point.getLng() <= Math.max(v1.getLng(), v2.getLng());
    }

}
