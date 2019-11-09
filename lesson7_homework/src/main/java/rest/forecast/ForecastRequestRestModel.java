package rest.forecast;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class ForecastRequestRestModel implements Serializable {

    @SerializedName("cod") public String cod;
    @SerializedName("message") public int message;
    @SerializedName("city") public CityForecastRestModel city;
    @SerializedName("cnt") public int cnt;
    @SerializedName("list") public ListForecastRestModel[] list;

    @SerializedName("country") public String country;
    @SerializedName("population") public long population;
    @SerializedName("timezone")public long timezone;
    @SerializedName("sunrise")public long sunrise;
    @SerializedName("sunset")public long sunset;
}
