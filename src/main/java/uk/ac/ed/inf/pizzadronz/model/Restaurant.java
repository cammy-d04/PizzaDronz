package uk.ac.ed.inf.pizzadronz.model;

import java.time.DayOfWeek;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class Restaurant {
    private String name;
    private LngLat location;
    private Set<DayOfWeek> openingDays;
    private List<Pizza> menu;

    public Restaurant(String name, LngLat location, Set<DayOfWeek> openingDays, List<Pizza> menu) {
        this.name = name;
        this.location = location;
        this.openingDays = openingDays;
        this.menu = menu;
    }

    public Restaurant(){

    }

    public List<Pizza> getMenu() {
        return menu;
    }



    public Pizza findPizzaByName(String name) {
        for (Pizza pizza : menu) {
            if (pizza.getName().equals(name)) {
                return pizza;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public LngLat getLocation() {
        return location;
    }

    public Set<DayOfWeek> getOpeningDays() {
        return openingDays;
    }
}
