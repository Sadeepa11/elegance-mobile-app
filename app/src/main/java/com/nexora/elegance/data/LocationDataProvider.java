package com.nexora.elegance.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LocationDataProvider provides static hierarchical data for countries, states,
 * districts, and cities.
 * This is used to populate address forms across the application.
 */
public class LocationDataProvider {

    // Country -> States
    private static final Map<String, List<String>> statesMap = new HashMap<>();
    // State -> Districts
    private static final Map<String, List<String>> districtsMap = new HashMap<>();
    // District (or State if no District) -> Cities
    private static final Map<String, List<String>> citiesMap = new HashMap<>();

    static {
        // COUNTRIES
        List<String> countries = Arrays.asList("United States", "Sri Lanka", "United Kingdom", "Singapore");

        // STATES
        statesMap.put("United States", Arrays.asList("California", "New York", "Texas"));
        statesMap.put("Sri Lanka", Arrays.asList("Western Province", "Central Province", "Southern Province"));
        statesMap.put("United Kingdom", Arrays.asList("England", "Scotland", "Wales"));
        // Singapore has no states

        // DISTRICTS
        // Sri Lanka has districts
        districtsMap.put("Western Province", Arrays.asList("Colombo", "Gampaha", "Kalutara"));
        districtsMap.put("Central Province", Arrays.asList("Kandy", "Matale", "Nuwara Eliya"));
        districtsMap.put("Southern Province", Arrays.asList("Galle", "Matara", "Hambantota"));
        // UK has counties (mocking as districts)
        districtsMap.put("England", Arrays.asList("Greater London", "West Midlands", "Greater Manchester"));
        // US does not use our "District" level here, they just have State -> City for
        // simplicity

        // CITIES
        // US (State -> City directly, since district is null)
        citiesMap.put("California", Arrays.asList("Los Angeles", "San Francisco", "San Diego"));
        citiesMap.put("New York", Arrays.asList("New York City", "Buffalo", "Albany"));
        citiesMap.put("Texas", Arrays.asList("Houston", "Austin", "Dallas"));

        // Sri Lanka (District -> City)
        citiesMap.put("Colombo", Arrays.asList("Colombo", "Dehiwala", "Moratuwa"));
        citiesMap.put("Gampaha", Arrays.asList("Gampaha", "Negombo", "Kelaniya"));
        citiesMap.put("Kandy", Arrays.asList("Kandy", "Peradeniya", "Katugastota"));
        // ... (mocking some defaults if not found)

        // UK (District -> City)
        citiesMap.put("Greater London", Arrays.asList("London", "Westminster"));
        citiesMap.put("West Midlands", Arrays.asList("Birmingham", "Coventry"));

        // Singapore (Country -> City directly)
        citiesMap.put("Singapore", Arrays.asList("Singapore City", "Woodlands", "Jurong"));
    }

    public static List<String> getCountries() {
        return Arrays.asList("Select Country", "United States", "Sri Lanka", "United Kingdom", "Singapore");
    }

    public static List<String> getStates(String country) {
        List<String> states = statesMap.get(country);
        if (states == null || states.isEmpty())
            return new ArrayList<>();
        List<String> ret = new ArrayList<>();
        ret.add("Select State/Province");
        ret.addAll(states);
        return ret;
    }

    public static List<String> getDistricts(String state) {
        List<String> districts = districtsMap.get(state);
        if (districts == null || districts.isEmpty())
            return new ArrayList<>();
        List<String> ret = new ArrayList<>();
        ret.add("Select District");
        ret.addAll(districts);
        return ret;
    }

    public static List<String> getCities(String dependentKey) {
        // depending on structure, the dependent key can be Country, State, or District
        List<String> cities = citiesMap.get(dependentKey);
        if (cities == null || cities.isEmpty()) {
            return Arrays.asList("Select City", "Default City 1", "Default City 2");
        }
        List<String> ret = new ArrayList<>();
        ret.add("Select City");
        ret.addAll(cities);
        return ret;
    }
}
