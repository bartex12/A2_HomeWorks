package rest;

import rest.entities.WeatherRequestRestModel;
import rest.forecast.ForecastRequestRestModel;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IOpenWeather {
    @GET("data/2.5/weather")
    Call<WeatherRequestRestModel> loadWeatherEng(@Query("q") String city,
                                                 @Query("appid") String keyApi,
                                                 @Query("units") String units);

    @GET("data/2.5/weather")
    Call<WeatherRequestRestModel> loadWeatherRu(@Query("q") String city,
                                                @Query("appid") String keyApi,
                                                @Query("units") String units,
                                                @Query("lang") String lang);
    @GET("data/2.5/forecast")
    Call<ForecastRequestRestModel> loadForecastRu(@Query("q") String city,
                                                  @Query("appid") String keyApi,
                                                  @Query("units") String units,
                                                  @Query("lang") String lang);
}
