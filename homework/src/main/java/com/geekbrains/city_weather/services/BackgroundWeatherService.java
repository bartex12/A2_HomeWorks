package com.geekbrains.city_weather.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.Nullable;
import rest.OpenWeatherRepo;
import rest.forecast_model.ForecastRequestRestModel;
import rest.weather_model.WeatherRequestRestModel;
import retrofit2.Response;

import static com.geekbrains.city_weather.constants.AppConstants.BROADCAST_WEATHER_ACTION;
import static com.geekbrains.city_weather.constants.AppConstants.CURRENT_CITY;
import static com.geekbrains.city_weather.constants.AppConstants.IS_JSON_NULL;
import static com.geekbrains.city_weather.constants.AppConstants.IS_RESPONS_NULL;
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
        Log.d(TAG, "BackgroundWeatherService Язык системы= " + Locale.getDefault().getLanguage());

        //получаем текущий город из интента
        final String currentCity = Objects.requireNonNull(intent).getStringExtra(CURRENT_CITY);
        Log.d(TAG, "BackgroundWeatherService currentCity = " + currentCity);
        // создаём интент широковещательного сообщения с фильтром и ловим его в WeatherFragment
        Intent broadcastIntent = new Intent(BROADCAST_WEATHER_ACTION);
        //делаем запрос о погоде и получаем ответ от сервера
        //если надо получить сразу WeatherRequestRestModel, то надо .execute().body()
        Response<WeatherRequestRestModel> response = getWeatherResponse(currentCity);
        Log.d(TAG, "BackgroundWeatherService response = " + response);

        //если телефон не может посылать запросы, response=null, обрабатываем эту ситуацию
        if (response!=null){
            //если удалось получить ответ от сервера делаем запрос прогноза и посылаем интент с ответом
            if (response.body() != null && response.isSuccessful()) {
                Log.d(TAG, "BackgroundWeatherService loadWeather OK");
                Log.d(TAG, "BackgroundWeatherService loadWeather response.body().name = " +
                        response.body().name);
                //делаем запрос о прогнозе погоды и получаем ответ от сервера
                Response<ForecastRequestRestModel> responseForecast = getForecastResponse(currentCity);
                // посылаем интент и ловим его в WeatherFragment
                if (responseForecast.body() != null && responseForecast.isSuccessful()) {
                    Log.d(TAG, "BackgroundWeatherService loadForecast OK");
                    broadcastIntent.putExtra(JAVA_OBJECT_FORECAST, responseForecast.body());
                    broadcastIntent.putExtra(JAVA_OBJECT, response.body());
                    broadcastIntent.putExtra(IS_JSON_NULL, false);
                    broadcastIntent.putExtra(IS_RESPONS_NULL, false);
                    sendBroadcast(broadcastIntent);
                    Log.d(TAG, "Всё хорошо ///////1");
                    //а если не удалось получить ответ для прогноза- посылаем интент для
                    // обработки ошибки ловим его в WeatherFragment
                } else {
                    Log.d(TAG, "BackgroundWeatherService loadWeather NO");
                    broadcastIntent.putExtra(IS_JSON_NULL, true);
                    broadcastIntent.putExtra(IS_RESPONS_NULL, false);
                    sendBroadcast(broadcastIntent);
                    Log.d(TAG, "Нет прогноза ///////2");
                }
                //а если не удалось получить ответ для погоды- город не существует, обычно
                // посылаем интент для обработки ошибки  ловим его в WeatherFragment
            } else {
                Log.d(TAG, "BackgroundWeatherService loadWeather NO");
                broadcastIntent.putExtra(IS_JSON_NULL, true);
                broadcastIntent.putExtra(IS_RESPONS_NULL, false);
                sendBroadcast(broadcastIntent);
                Log.d(TAG, "города нет ///////3");
            }
            //если телефон не может посылать запросы, response=null
        }else {
            Log.d(TAG, "BackgroundWeatherService response = null" );
            broadcastIntent.putExtra(IS_RESPONS_NULL, true);
            sendBroadcast(broadcastIntent);
            Log.d(TAG, " response = null ///////4");
        }
    }

    private Response<ForecastRequestRestModel> getForecastResponse(String currentCity) {
        //если надо получить сразу WeatherRequestRestModel, то надо .execute().body()
        Response<ForecastRequestRestModel> responseForecast = null;
        try {
            if (Locale.getDefault().getLanguage().equals("ru")) {
                Log.d(TAG, "BackgroundWeatherService loadForecastRu");
                responseForecast = OpenWeatherRepo.getSingleton()
                        .getAPI().loadForecastRu(currentCity,
                                "80bb32e4a0db84762bb04ab2bd724646", "metric", "ru")
                        .execute();
            } else {
                Log.d(TAG, "BackgroundWeatherService loadForecastEng");
                responseForecast = OpenWeatherRepo.getSingleton()
                        .getAPI().loadForecastEng(currentCity,
                                "80bb32e4a0db84762bb04ab2bd724646", "metric")
                        .execute();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseForecast;
    }

    private Response<WeatherRequestRestModel> getWeatherResponse(String currentCity) {
        //если надо получить сразу WeatherRequestRestModel, то надо .execute().body()
        Response<WeatherRequestRestModel> response = null;

        try {
            if (Locale.getDefault().getLanguage().equals("ru")) {
                Log.d(TAG, "BackgroundWeatherService loadWeatherRu");
                response = OpenWeatherRepo.getSingleton()
                        .getAPI().loadWeatherRu(currentCity,
                                "80bb32e4a0db84762bb04ab2bd724646", "metric", "ru")
                        .execute();
            } else {
                Log.d(TAG, "BackgroundWeatherService loadWeatherEng");
                response = OpenWeatherRepo.getSingleton()
                        .getAPI().loadWeatherEng(currentCity,
                                "80bb32e4a0db84762bb04ab2bd724646", "metric")
                        .execute();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
}


