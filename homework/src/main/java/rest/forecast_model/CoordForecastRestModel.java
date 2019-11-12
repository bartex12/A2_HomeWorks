package rest.forecast_model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CoordForecastRestModel implements Serializable {
    @SerializedName("lat") public float lat;
    @SerializedName("lon") public float lon;
}