package com.nexora.elegance.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DirectionsResponse {
    @SerializedName("routes")
    public List<Route> routes;

    public static class Route {
        @SerializedName("legs")
        public List<Leg> legs;
        @SerializedName("overview_polyline")
        public Polyline overviewPolyline;
    }

    public static class Leg {
        @SerializedName("duration")
        public Duration duration;
        @SerializedName("distance")
        public Distance distance;
    }

    public static class Duration {
        @SerializedName("text")
        public String text;
    }
    
    public static class Distance {
        @SerializedName("text")
        public String text;
    }

    public static class Polyline {
        @SerializedName("points")
        public String points;
    }
}
