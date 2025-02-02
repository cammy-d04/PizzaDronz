package uk.ac.ed.inf.pizzadronz.model;
import uk.ac.ed.inf.pizzadronz.model.LngLat;

public class IsInRegionRequest {
    private LngLat position;
    private Region region;


    public IsInRegionRequest(LngLat position, Region region) {
        this.position = position;
        this.region = region;
    }

    public LngLat getPosition() {
        return position;
    }

    public void setPosition(LngLat position) {
        this.position = position;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public boolean isValidRequest() {
        if (this.region.getName() == null) {
            System.out.println("Region name is null");
            return false;
        }

        // Check for empty values
        if (this.position == null || this.region == null || this.region.getVertices() == null ||
                this.region.getVertices().length == 0) {

            return false;
        }

        // For each vertex, check if null or not valid
        for (LngLat vertex : this.region.getVertices()) {
            if (vertex == null || !vertex.isValidPosition()) {

                return false;
            }
        }

        // If a line or point return false
        if (this.region.getVertices().length < 4) {

            return false;
        }

        // Check if the polygon is closed by comparing the first and last vertex
        LngLat firstVertex = this.region.getVertices()[0];
        LngLat lastVertex = this.region.getVertices()[this.region.getVertices().length - 1];
        if (firstVertex.getLng() != lastVertex.getLng() || firstVertex.getLat() != lastVertex.getLat()) {

            return false;
        }

        // Check position is valid
        return this.position.isValidPosition();
    }

}

