import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * App Class
 */
public final class App {

    private static final String METAL_LEVEL = "metal_level";
    private static final String SILVER = "Silver";
    private static final String STATE = "state";
    private static final String RATE = "rate";
    private static final String RATE_AREA = "rate_area";
    private static final String ZIP_CODE = "zipcode";

    Map<String, Set<Double>> rates = new HashMap<>();
    Map<String, List<Double>> sortedRates = new HashMap<>();
    Map<String, Set<String>> zips = new HashMap<>();

    /**
     * @param args The arguments of the program.
     */
    public static void main(String[] args) {

        App app = new App();
        app.readPlans();
        app.readZips();
        app.outputRates();

    }

    private void readPlans() {

        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("plans.csv");
        try(CSVParser parser = CSVParser.parse(in, StandardCharsets.UTF_8,
          CSVFormat.Builder.create(CSVFormat.DEFAULT).setHeader().build())) {
            for (CSVRecord row : parser) {
                if (row.get(METAL_LEVEL).equalsIgnoreCase(SILVER)) {
                    String key = row.get(STATE).toUpperCase() + " " + row.get(RATE_AREA);
                    if (rates.containsKey(key)) {
                        rates.get(key).add(Double.parseDouble(row.get(RATE)));
                    } else {
                        Set<Double> set = new HashSet<>();
                        set.add(Double.parseDouble(row.get(RATE)));
                        rates.put(key, set);
                    }
                }
            }
            // sort rates in each rate area
            rates.keySet().forEach(key -> {
                Double[] array = rates.get(key).toArray(new Double[0]);
                Arrays.sort(array);
                sortedRates.put(key, Arrays.asList(array));
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }

    private void readZips() {

        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("zips.csv");
        try(CSVParser parser = CSVParser.parse(in, StandardCharsets.UTF_8,
          CSVFormat.Builder.create(CSVFormat.DEFAULT).setHeader().build())) {
            for (CSVRecord row : parser) {
                String key = row.get(ZIP_CODE);
                if (zips.containsKey(key)) {
                    zips.get(key).add(row.get(STATE).toUpperCase() + " " + row.get(RATE_AREA));
                } else {
                    Set<String> set = new HashSet<>();
                    set.add(row.get(STATE).toUpperCase() + " " + row.get(RATE_AREA));
                    zips.put(key, set);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    private void outputRates() {

        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("slcsp.csv");
        try(CSVParser parser = CSVParser.parse(in, StandardCharsets.UTF_8,
          CSVFormat.Builder.create(CSVFormat.DEFAULT).setHeader().build())) {
            List<String> zipcodes = new ArrayList<>();
            for (CSVRecord row : parser) {
                zipcodes.add(row.get(ZIP_CODE));
            }
            System.out.println(ZIP_CODE + "," + RATE);
            zipcodes.forEach(zipcode -> {
                System.out.print(zipcode + ",");
                if (zips.containsKey(zipcode) && zips.get(zipcode).size() == 1) {
                    String rateArea = zips.get(zipcode).iterator().next();
                    if (sortedRates.containsKey(rateArea) && sortedRates.get(rateArea).size() > 1) {
                        System.err.println(String.format("%.2f", sortedRates.get(rateArea).get(1)));
                    } else {
                        System.out.println();
                    }
                } else {
                    System.out.println();
                }
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
    
}
