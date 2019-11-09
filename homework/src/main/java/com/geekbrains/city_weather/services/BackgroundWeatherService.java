package com.geekbrains.city_weather.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.util.Objects;

import androidx.annotation.Nullable;
import rest.OpenWeatherRepo;
import rest.entities.WeatherRequestRestModel;
import rest.forecast.ForecastRequestRestModel;
import retrofit2.Response;

import static com.geekbrains.city_weather.constants.AppConstants.BROADCAST_WEATHER_ACTION;
import static com.geekbrains.city_weather.constants.AppConstants.CURRENT_CITY;
import static com.geekbrains.city_weather.constants.AppConstants.IS_JSON_NULL;
import static com.geekbrains.city_weather.constants.AppConstants.JAVA_OBJECT;
import static com.geekbrains.city_weather.constants.AppConstants.JAVA_OBJECT_FORECAST;

/*
* отправляем уведомление о завершении сервиса во фрагмент WeatherFragment
* там создаём  private class ServiceFinishedReceiver extends BroadcastReceiver,
* который регистрируем в onStart с фильтром  BROADCAST_WEATHER_ACTION и  в его методе
* оnReceive обрабатываем погодные данные
*/
public class BackgroundWeatherService extends IntentService {

    private static final String TAG = "33333";

    //если создавать через Alt+Enter, будет BackgroundWeatherService(String name), его надо удалить
    public BackgroundWeatherService() {
        super("background_service_for_city_weather");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        //получаем текущий город из интента
        final String currentCity = Objects.requireNonNull(intent).getStringExtra(CURRENT_CITY);
        // создаём интент широковещательного сообщения с фильтром
        Intent broadcastIntent = new Intent(BROADCAST_WEATHER_ACTION);

        //делаем запрос о погоде и получаем ответ от сервера
        //если надо получить сразу WeatherRequestRestModel, то надо .execute().body()
        Response<WeatherRequestRestModel> response = getWeatherResponse(currentCity);
        //если удалось получить ответ от сервера делаем запрос прогноза и посылаем интент с ответом
        if (response.body() != null && response.isSuccessful()) {
            Log.d(TAG, "BackgroundWeatherService loadWeatherEng OK" );

            //делаем запрос о прогнозе погоды и получаем ответ от сервера
            Response<ForecastRequestRestModel> responseForecast = getForecastResponse(currentCity);

            if (responseForecast.body() != null && responseForecast.isSuccessful()) {
                Log.d(TAG, "BackgroundWeatherService loadForecastEng OK" );
                broadcastIntent.putExtra(JAVA_OBJECT_FORECAST, responseForecast.body());
            }
            broadcastIntent.putExtra(JAVA_OBJECT, response.body());
            broadcastIntent.putExtra(CURRENT_CITY, currentCity);
            broadcastIntent.putExtra(IS_JSON_NULL, false);
            sendBroadcast(broadcastIntent);

        //а если не удалось получить ответ- посылаем интент для обработки ошибки
        } else {
            Log.d(TAG, "BackgroundWeatherService loadWeatherEng NO" );
            broadcastIntent.putExtra(CURRENT_CITY, currentCity);
            broadcastIntent.putExtra(IS_JSON_NULL, true);
            sendBroadcast(broadcastIntent);
        }
    }

    private Response<ForecastRequestRestModel> getForecastResponse(String currentCity) {
        //если надо получить сразу WeatherRequestRestModel, то надо .execute().body()
        Response<ForecastRequestRestModel> responseForecast = null;
        try {
            responseForecast = OpenWeatherRepo.getSingleton()
                    .getAPI().loadForecastEng(currentCity,
                    "80bb32e4a0db84762bb04ab2bd724646", "metric")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseForecast;
    }

    private Response<WeatherRequestRestModel> getWeatherResponse(String currentCity) {
        //если надо получить сразу WeatherRequestRestModel, то надо .execute().body()
        Response<WeatherRequestRestModel> response = null;
        try {
            response = OpenWeatherRepo.getSingleton()
                    .getAPI().loadWeatherEng(currentCity,
                    "80bb32e4a0db84762bb04ab2bd724646", "metric")
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
}

