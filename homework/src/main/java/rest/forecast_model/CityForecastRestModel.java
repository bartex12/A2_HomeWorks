package rest.forecast_model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CityForecastRestModel implements Serializable {
    @SerializedName("id") public long id;
    @SerializedName("name") public String name;
    @SerializedName("coord") public CoordForecastRestModel coord;
    @SerializedName("timezone")public long timezone;
    @SerializedName("country") public String country;
    @SerializedName("population") public long population;
    @SerializedName("sunrise")public long sunrise;
    @SerializedName("sunset")public long sunset;
}
