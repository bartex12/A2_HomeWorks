package rest.weather_model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CloudsRestModel implements Serializable {
    @SerializedName("all") public int all;
}
