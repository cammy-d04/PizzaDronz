package uk.ac.ed.inf.pizzadronz.IsInRegionTest;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.util.Random;

public class IsInRegionJFreeChart {

    /*
    PROVIDES A VISUAL REPRESENTATION OF THE isInRegion ENDPOINT USING JFREECHART FOR TESTING

    INSTRUCTIONS
    1. Ensure the Spring Boot application is running
    2. Adjust the number of coordinates as needed
    3. Run the test
     */


    public static void main(String[] args) throws JSONException {
        // Ensure the Spring Boot application is running
        String baseUrl = "http://localhost:8080";

        // This is pretty compute intensive, at least for my poor old laptop, so adjust the number of points as needed
        int numberOfCoordinates = 10000;

        // Create series for the plot
        XYSeries inRegionSeries = new XYSeries("In Region");
        XYSeries outRegionSeries = new XYSeries("Out of Region");

        RestTemplate restTemplate = new RestTemplate();

        // Generate test data
        for (int i = 0; i < numberOfCoordinates; i++) {

            // Generate random longitude and latitude as multiples of 0.25 so that the points are more likely to be on interesting points
            double lng = -1 + (new Random().nextInt(49) * 0.25);
            double lat = -1 + (new Random().nextInt(49) * 0.25);

            // Create the expected JSON format
            JSONObject json = new JSONObject();
            JSONObject position = new JSONObject();
            position.put("lng", lng);
            position.put("lat", lat);
            json.put("position", position);

            JSONObject region = new JSONObject();
            region.put("name", "central");
            JSONArray vertices = new JSONArray();

            // PLots a basic square region
            vertices.put(new JSONObject().put("lng", 0).put("lat", 10));
            vertices.put(new JSONObject().put("lng", 0).put("lat", 0));
            vertices.put(new JSONObject().put("lng", 10).put("lat", 0));
            vertices.put(new JSONObject().put("lng", 10).put("lat", 10));
            vertices.put(new JSONObject().put("lng", 0).put("lat", 10));
            region.put("vertices", vertices);
            json.put("region", region);

            // Create HTTP entity
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(json.toString(), headers);

            // Send request and check response
            ResponseEntity<Boolean> response = restTemplate.exchange(baseUrl + "/isInRegion", HttpMethod.POST, entity, Boolean.class);
            boolean isInRegion = response.getBody();

            // Add points to the series
            if (isInRegion) {
                inRegionSeries.add(lng, lat);
            } else {
                outRegionSeries.add(lng, lat);
            }
        }

        // Create dataset and add the series
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(inRegionSeries);
        dataset.addSeries(outRegionSeries);

        // Create chart
        JFreeChart chart = ChartFactory.createScatterPlot(
                "PizzaDronz Region Test",
                "Longitude",
                "Latitude",
                dataset
        );

        // Customize plot
        XYPlot plot = chart.getXYPlot();
        XYItemRenderer renderer = new XYLineAndShapeRenderer(false, true);
        renderer.setSeriesPaint(0, Color.GREEN);
        renderer.setSeriesPaint(1, Color.RED);
        plot.setRenderer(renderer);


        // Set the range for the x and y axes
        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setRange(-2, 12); // Adjust the range as needed

        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setRange(-2, 12); // Adjust the range as needed


        // Display chart
        ApplicationFrame frame = new ApplicationFrame("PizzaDronz Region Plot");
        frame.setContentPane(new ChartPanel(chart));
        frame.pack();
        RefineryUtilities.centerFrameOnScreen(frame);
        frame.setVisible(true);
    }
}
