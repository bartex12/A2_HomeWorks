package com.geekbrains.city_weather.services;

import android.app.IntentService;
import android.content.Intent;
import com.geekbrains.city_weather.data_loader.CityWeatherDataLoader;
import org.json.JSONObject;
import java.util.Objects;
import androidx.annotation.Nullable;
import static com.geekbrains.city_weather.constants.AppConstants.BROADCAST_WEATHER_ACTION;
import static com.geekbrains.city_weather.constants.AppConstants.CURRENT_CITY;
import static com.geekbrains.city_weather.constants.AppConstants.IS_JSON_NULL;
import static com.geekbrains.city_weather.constants.AppConstants.JSON_OBJECT;
import static com.geekbrains.city_weather.constants.AppConstants.JSON_OBJECT_FORECAST;
import static com.geekbrains.city_weather.data_loader.CityWeatherDataLoader.OPEN_FORECAST_API_URL;
import static com.geekbrains.city_weather.data_loader.CityWeatherDataLoader.OPEN_WEATHER_API_URL;

public class BackgroundWeatherService extends IntentService {

    //если создавать через Alt+Enter, будет BackgroundWeatherService(String name), его надо удалить
    public BackgroundWeatherService() {
        super("background_service_for_city_weather");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        String currentCity = Objects.requireNonNull(intent).getStringExtra(CURRENT_CITY);

        final JSONObject jsonObject = CityWeatherDataLoader
                .getJSONDataWithCityAndApiUrl(currentCity, OPEN_WEATHER_API_URL);
        final JSONObject jsonObjectForecast = CityWeatherDataLoader
                .getJSONDataWithCityAndApiUrl(currentCity, OPEN_FORECAST_API_URL);

        //отправляем уведомление о завершении сервиса во фрагмент WeatherFragment
        //там создаём  private class ServiceFinishedReceiver extends BroadcastReceiver,
        //который регистрируем в onStart с фильтром  BROADCAST_WEATHER_ACTION и  в его методе
        //onReceive обрабатываем погодные данные
        Intent broadcastIntent = new Intent(BROADCAST_WEATHER_ACTION);
        if (jsonObject == null){
            broadcastIntent.putExtra(CURRENT_CITY, currentCity);
            broadcastIntent.putExtra(IS_JSON_NULL, true); //признак того.что всё плохо
        }else{
            broadcastIntent.putExtra(JSON_OBJECT, jsonObject.toString());
            broadcastIntent.putExtra(JSON_OBJECT_FORECAST,
                    Objects.requireNonNull(jsonObjectForecast).toString());
            broadcastIntent.putExtra(CURRENT_CITY, currentCity);
            broadcastIntent.putExtra(IS_JSON_NULL, false);
        }
        sendBroadcast(broadcastIntent);
    }
}
