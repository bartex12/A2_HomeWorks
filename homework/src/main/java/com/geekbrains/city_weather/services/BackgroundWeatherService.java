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
import static com.geekbrains.city_weather.constants.AppConstants.IS_JSON_NULL;
import static com.geekbrains.city_weather.constants.AppConstants.IS_RESPONS_NULL;
import static com.geekbrains.city_weather.constants.AppConstants.JAVA_OBJECT;
import static com.geekbrains.city_weather.constants.AppConstants.JAVA_OBJECT_FORECAST;
import static com.geekbrains.city_weather.constants.AppConstants.LATITUDE;
import static com.geekbrains.city_weather.constants.AppConstants.LONGITUDE;

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

        //получаем координаты из интента
        double latitude = Objects.requireNonNull(intent.getExtras()).getDouble(LATITUDE);
        double longitude = intent.getExtras().getDouble(LONGITUDE);
        String lat = String.valueOf(latitude);
        String lon = String.valueOf(longitude);

        Log.d(TAG, "BackgroundWeatherService lat = " + lat + " lon = " + lon);
        // создаём интент широковещательного сообщения с фильтром и ловим его в WeatherFragment
        Intent broadcastIntent = new Intent(BROADCAST_WEATHER_ACTION);
        //делаем запрос о погоде и получаем ответ от сервера
        //если надо получить сразу WeatherRequestRestModel, то надо .execute().body()
        Response<WeatherRequestRestModel> response = getWeatherLatLonResponse(lat, lon);
        Log.d(TAG, "BackgroundWeatherService response = " + response);

        //если телефон не может посылать запросы, response=null, обрабатываем эту ситуацию
        if (response!=null){
            //если удалось получить ответ от сервера делаем запрос прогноза и посылаем интент с ответом
            if (response.body() != null && response.isSuccessful()) {
                Log.d(TAG, "BackgroundWeatherService loadWeather OK");
                Log.d(TAG, "BackgroundWeatherService loadWeather response.body().coordinates = " +
                        " lat = " + response.body().coordinates.lat +
                        " lon = " + response.body().coordinates.lon +
                        " name = " + response.body().name);
                //делаем запрос о прогнозе погоды и получаем ответ от сервера
                Response<ForecastRequestRestModel> responseForecast =
                        getForecastLatLonResponse(lat, lon);

                if (responseForecast.body() != null && responseForecast.isSuccessful()) {
                    Log.d(TAG, "BackgroundWeatherService loadForecast OK");
                    Log.d(TAG, "BackgroundWeatherService loadForecast response.body().coordinates = " +
                            " lat = " + responseForecast.body().city.coord.lat +
                            " lon = " + responseForecast.body().city.coord.lon +
                            " name = " + responseForecast.body().city.name);
                    broadcastIntent.putExtra(JAVA_OBJECT_FORECAST, responseForecast.body());
                    broadcastIntent.putExtra(JAVA_OBJECT, response.body());
                    broadcastIntent.putExtra(IS_JSON_NULL, false);
                    broadcastIntent.putExtra(IS_RESPONS_NULL, false);
                    sendBroadcast(broadcastIntent);
                    Log.d(TAG, "///////1");
                    //а если не удалось получить ответ для прогноза- посылаем интент для
                    // обработки ошибки ловим его в WeatherFragment
                } else {
                    Log.d(TAG, "BackgroundWeatherService loadWeather NO");
                    broadcastIntent.putExtra(IS_JSON_NULL, true);
                    broadcastIntent.putExtra(IS_RESPONS_NULL, false);
                    sendBroadcast(broadcastIntent);
                    Log.d(TAG, "///////2");
                }
                //а если не удалось получить ответ для погоды- город не существует, обычно
                // посылаем интент для обработки ошибки  ловим его в WeatherFragment
            } else {
                Log.d(TAG, "BackgroundWeatherService loadWeather NO");
                broadcastIntent.putExtra(IS_JSON_NULL, true);
                broadcastIntent.putExtra(IS_RESPONS_NULL, false);
                sendBroadcast(broadcastIntent);
                Log.d(TAG, "///////3");
            }
            //если телефон не может посылать запросы, response=null
        }else {
            Log.d(TAG, "BackgroundWeatherService response = null" );
            broadcastIntent.putExtra(IS_RESPONS_NULL, true);
            sendBroadcast(broadcastIntent);
            Log.d(TAG, "///////4");
        }
    }

    private Response<WeatherRequestRestModel> getWeatherLatLonResponse(String latitude, String longitude) {
        //если надо получить сразу WeatherRequestRestModel, то надо .execute().body()
        Response<WeatherRequestRestModel> response = null;

        try {
            if (Locale.getDefault().getLanguage().equals("ru")) {
                response = OpenWeatherRepo.getSingleton()
                        .getAPI().loadWeatherLatLonRu(latitude, longitude,
                                "80bb32e4a0db84762bb04ab2bd724646", "metric", "ru")
                        .execute();
            } else {
                response = OpenWeatherRepo.getSingleton()
                        .getAPI().loadWeatherLatLonEng(latitude, longitude,
                                "80bb32e4a0db84762bb04ab2bd724646", "metric")
                        .execute();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    private Response<ForecastRequestRestModel> getForecastLatLonResponse(String latitude, String longitude) {
        //если надо получить сразу WeatherRequestRestModel, то надо .execute().body()
        Response<ForecastRequestRestModel> responseForecast = null;
        try {
            if (Locale.getDefault().getLanguage().equals("ru")) {
                responseForecast = OpenWeatherRepo.getSingleton()
                        .getAPI().loadForecastLatLonRu(latitude, longitude,
                                "80bb32e4a0db84762bb04ab2bd724646", "metric", "ru")
                        .execute();
            } else {
                responseForecast = OpenWeatherRepo.getSingleton()
                        .getAPI().loadForecastLatLonEng(latitude, longitude,
                                "80bb32e4a0db84762bb04ab2bd724646", "metric")
                        .execute();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseForecast;
    }

}


