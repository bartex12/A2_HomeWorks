package rest.weather_model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CoordRestModel implements Serializable {
    @SerializedName("lon") public float lon;
    @SerializedName("lat") public float lat;
}
