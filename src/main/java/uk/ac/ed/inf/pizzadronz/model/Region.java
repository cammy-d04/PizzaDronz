package uk.ac.ed.inf.pizzadronz.model;

import java.util.Arrays;

public class Region {
    private String name;
    private LngLat[] vertices;

    public Region(String name, LngLat[] vertices) {
        this.name = name;
        this.vertices = vertices;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LngLat[] getVertices() {
        return vertices;
    }

    public void setVertices(LngLat[] vertices) {
        this.vertices = vertices;
    }
}

