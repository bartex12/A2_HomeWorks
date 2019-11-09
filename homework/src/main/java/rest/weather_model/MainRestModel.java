package rest.weather_model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class MainRestModel implements Serializable {
    @SerializedName("temp")public float temp;
    @SerializedName("pressure") public float pressure;
    @SerializedName("humidity") public float humidity;
    @SerializedName("temp_min") public float temp_min;
    @SerializedName("temp_max") public float temp_max;
}
