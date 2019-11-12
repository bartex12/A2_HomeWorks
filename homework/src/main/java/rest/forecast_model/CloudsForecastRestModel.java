package rest.forecast_model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CloudsForecastRestModel implements Serializable {
    @SerializedName("all") public int all;
}
