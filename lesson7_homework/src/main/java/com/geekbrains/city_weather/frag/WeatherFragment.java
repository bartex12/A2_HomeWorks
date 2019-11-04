package com.geekbrains.city_weather.frag;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.geekbrains.city_weather.R;
import com.geekbrains.city_weather.adapter.DataForecast;
import com.geekbrains.city_weather.adapter.WeatherCardAdapter;
import com.geekbrains.city_weather.services.BackgroundWeatherService;
import com.geekbrains.city_weather.singltones.CityLab;
import com.geekbrains.city_weather.singltones.CityListLab;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static androidx.preference.PreferenceManager.*;
import static com.geekbrains.city_weather.constants.AppConstants.BROADCAST_WEATHER_ACTION;
import static com.geekbrains.city_weather.constants.AppConstants.CITY_FRAFMENT_TAG;
import static com.geekbrains.city_weather.constants.AppConstants.CURRENT_CITY;
import static com.geekbrains.city_weather.constants.AppConstants.IS_JSON_NULL;
import static com.geekbrains.city_weather.constants.AppConstants.JSON_OBJECT;
import static com.geekbrains.city_weather.constants.AppConstants.JSON_OBJECT_FORECAST;
import static com.geekbrains.city_weather.constants.AppConstants.LAST_CITY;
import static com.geekbrains.city_weather.constants.AppConstants.WEATHER_FRAFMENT_TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class WeatherFragment extends Fragment {
    private static final String TAG = "33333";

    private RecyclerView recyclerViewForecast;
    private TextView cityTextView;
    private TextView textViewLastUpdate;
    private TextView textViewWhether;
    private TextView textViewTemper;
    private TextView textViewWind;
    private TextView textViewPressure;
    private TextView textViewIcon;
    private String[] dates = new String[5];
    private double[] temperuteres = new double[5];
    private String[] iconArray = new String[5];
    private String currentCity;
    private ServiceFinishedReceiver receiver = new ServiceFinishedReceiver();

    public WeatherFragment() {
        // Required empty public constructor
    }

    public static WeatherFragment newInstance() {
        return new WeatherFragment();
    }

    @Override
    @SuppressLint("Recycle")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_whether, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        initFonts();

        //запускаем сервис, работающий в отдельном потоке, передаём туда текущий город
        Intent intent = new Intent(getActivity(), BackgroundWeatherService.class);
        intent.putExtra(CURRENT_CITY, CityLab.getCity());
        Objects.requireNonNull(getActivity()).startService(intent);
        Log.d(TAG, "WeatherFragment onViewCreated" );
    }

    @Override
    public void onStart() {
        Log.d(TAG, "WeatherFragment onStart");
        //регистрируем примник широковещательных сообщений с фильтром BROADCAST_WEATHER_ACTION
        Objects.requireNonNull(getActivity())
                .registerReceiver(receiver, new IntentFilter(BROADCAST_WEATHER_ACTION));
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "WeatherFragment onResume");
        //  !!!!  имя папки в телефоне com.geekbrains.a1l1_helloworld   !!!
        SharedPreferences prefSetting =
                getDefaultSharedPreferences(Objects.requireNonNull(getActivity()));
        //получаем из файла настроек состояние чекбоксов Ключ не менять!
        boolean isShowCheckboxes = prefSetting.getBoolean("showCheckBoxes", true);
        Log.d(TAG, "WeatherFragment onResume isShowCheckboxes = " + isShowCheckboxes);

        // показываем/скрываем данные о ветре и давлении
        showWindAndPressure(isShowCheckboxes);
    }

    @Override
    public void onStop() {
        Log.d(TAG, "WeatherFragment onStop");
        Objects.requireNonNull(getActivity()).unregisterReceiver(receiver);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "WeatherFragment onDestroy");
        SharedPreferences defaultPrefs =
                PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(getActivity()));
        saveLastCity(defaultPrefs);
        super.onDestroy();
    }

    private void saveLastCity(SharedPreferences preferences){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(LAST_CITY, CityLab.getCity());
        editor.apply();
    }

    private void initViews(View view) {
        recyclerViewForecast = view.findViewById(R.id.recyclerViewForecast);
        cityTextView = view.findViewById(R.id.greetingsTextView);
        textViewLastUpdate = view.findViewById(R.id.textViewLastUpdate);
        textViewWhether = view.findViewById(R.id.textViewWhether);
        textViewTemper = view.findViewById(R.id.textViewTemper);
        textViewWind = view.findViewById(R.id.textViewWind);
        textViewPressure = view.findViewById(R.id.textViewPressure);
        textViewIcon = view.findViewById(R.id.textViewIcon);
    }

    private void initFonts() {
        Typeface weatherFont = Typeface.createFromAsset(
                Objects.requireNonNull(getActivity()).getAssets(), "fonts/weather.ttf");
        textViewIcon.setTypeface(weatherFont);
    }

    // Показать погоду во фрагменте в альбомной ориентации
    private void showCityWhetherLand(String city) {

        // создаем новый фрагмент с текущей позицией для вывода погоды
        WeatherFragment weatherFrag = WeatherFragment.newInstance();
        // ... и выполняем транзакцию по замене фрагмента
        FragmentTransaction ft = Objects.requireNonNull(getFragmentManager()).beginTransaction();
        ft.replace(R.id.content_super_r, weatherFrag, WEATHER_FRAFMENT_TAG);  // замена фрагмента
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);// эффект
        //ft.addToBackStack(null);
        ft.commit();
        Log.d(TAG, "MainActivity onCityChange Фрагмент = " +
                getFragmentManager().findFragmentById(R.id.content_super));
    }

    // создаем новый фрагмент со списком ранее выбранных городов
    private void setChooseCityFrag() {
        ChooseCityFrag chooseCityFrag = ChooseCityFrag.newInstance();
        FragmentTransaction ft = Objects.requireNonNull(getActivity())
                .getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_super, chooseCityFrag, CITY_FRAFMENT_TAG);  // замена фрагмента
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);// эффект
        ft.commit();
    }

    /**
     * @param jsonObjectForecast
     * данные для 5 дневного прогноза
     */
    private void renderForecast(JSONObject jsonObjectForecast) {
        try {
            dates = getDateArray(jsonObjectForecast);
            temperuteres = getTempArray(jsonObjectForecast);
            int[] idArray = getIdArray(jsonObjectForecast);
            long sunrise = getSunrise(jsonObjectForecast);
            long sunset = getSunset(jsonObjectForecast);
            iconArray = getIconsArray(idArray, sunrise, sunset);
            //после формирования данных для адаптера инициализируем сам адаптер
            initRecyclerView();

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "Ошибка в renderForecast");
        }
    }

    private void renderWeather(JSONObject jsonObject) {

        try {
            JSONObject details = jsonObject.getJSONArray("weather").getJSONObject(0);
            JSONObject main = jsonObject.getJSONObject("main");
            JSONObject wind = jsonObject.getJSONObject("wind");

            setPlaceName(jsonObject);
            setUpdatedText(jsonObject);
            setDescription(details);
            setWind(wind);
            setPressure(main);
            setCurrentTemp(main);

            setWeatherIcon(details.getInt("id"),
                    jsonObject.getJSONObject("sys").getLong("sunrise") * 1000,
                    jsonObject.getJSONObject("sys").getLong("sunset") * 1000);
        } catch (Exception exc) {
            exc.printStackTrace();
            Log.e(TAG, "One or more fields not found in the JSON data");
        }
    }

    //получение даты для прогноза на 5 дней
    private String[] getDateArray(JSONObject jsonObjectForecast) throws JSONException {
        Log.e(TAG, "getDateArray");
        DateFormat dateFormat = DateFormat.getDateInstance();
        long dateTime;
        String[] dateTimeArray = new String[5];
        for (int i = 0; i < dateTimeArray.length; i++) {
            dateTime = jsonObjectForecast.getJSONArray("list").
                    getJSONObject(7 + 8 * i).getLong("dt");
            dateTimeArray[i] = dateFormat.format(new Date(dateTime * 1000));
        }
        Log.e(TAG, "dateTimeArray.length = " + dateTimeArray.length);
        return dateTimeArray;
    }

    //получение температуры для прогноза на 5 дней
    private double[] getTempArray(JSONObject jsonObjectForecast) throws JSONException {
        Log.e(TAG, "getTempArray");
        double[] temper = new double[5];
        for (int i = 0; i < temper.length; i++) {
            JSONObject list = jsonObjectForecast.getJSONArray("list").getJSONObject(7 + 8 * i);
            temper[i] = list.getJSONObject("main").getDouble("temp");
        }
        Log.e(TAG, "temper.length = " + temper.length);
        return temper;
    }

    //получение массива id для прогноза на 5 дней
    private int[] getIdArray(JSONObject jsonObjectForecast) throws JSONException {
        Log.e(TAG, "getIdArray");
        int[] id = new int[5];
        for (int i = 0; i < id.length; i++) {
            JSONObject list = jsonObjectForecast.getJSONArray("list").getJSONObject(7 + 8 * i);
            id[i] = list.getJSONArray("weather").getJSONObject(0).getInt("id");
            Log.e(TAG, "id[i] = " + id[i]);
        }
        Log.e(TAG, "id.length = " + id.length);
        return id;
    }

    //получение времени  восход для прогноза на 5 дней
    private long getSunrise(JSONObject jsonObjectForecast) throws JSONException {
        Log.e(TAG, "getSunrise");
        long sunrise = jsonObjectForecast.getJSONObject("city").getLong("sunrise");
        Log.e(TAG, "sunrise = " + sunrise);
        return sunrise;
    }

    //получение времени  заката для прогноза на 5 дней
    private long getSunset(JSONObject jsonObjectForecast) throws JSONException {
        Log.e(TAG, "getSunset");
        long sunset = jsonObjectForecast.getJSONObject("city").getLong("sunset");
        Log.e(TAG, "sunset = " + sunset);
        return sunset;
    }

    //загрузка данных в адаптер списка прогноза на 5 дней
    private void  initRecyclerView(){
        //иконки
        DataForecast[] data = new DataForecast[] {
                new DataForecast(dates[0], iconArray[0],
                        String.format(Locale.getDefault(),
                                "%.1f", temperuteres[0]) + "\u2103"),
                new DataForecast(dates[1], iconArray[1],
                        String.format(Locale.getDefault(),
                                "%.1f", temperuteres[1]) + "\u2103"),
                new DataForecast(dates[2], iconArray[2],
                        String.format(Locale.getDefault(),
                                "%.1f", temperuteres[2]) + "\u2103"),
                new DataForecast(dates[3], iconArray[3],
                        String.format(Locale.getDefault(),
                                "%.1f", temperuteres[3]) + "\u2103"),
                new DataForecast(dates[4], iconArray[4],
                        String.format(Locale.getDefault(),
                                "%.1f", temperuteres[4]) + "\u2103")};

        ArrayList<DataForecast> list = new ArrayList<>(data.length);
        list.addAll(Arrays.asList(data));

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false);
        WeatherCardAdapter cardAdapter = new WeatherCardAdapter(getActivity(), list);

        recyclerViewForecast.setLayoutManager(layoutManager);
        recyclerViewForecast.setAdapter(cardAdapter);
    }

    // показываем/скрываем данные о ветре и давлении
    private void showWindAndPressure(boolean isShowCheckboxes) {
        if (isShowCheckboxes) {
            textViewWind.setVisibility(View.VISIBLE);
            textViewPressure.setVisibility(View.VISIBLE);
        } else {
            textViewWind.setVisibility(View.GONE);
            textViewPressure.setVisibility(View.GONE);
        }
    }

    private void setPlaceName(JSONObject jsonObject) throws JSONException {
        String cityText = jsonObject.getString("name").toUpperCase() + ", "
                + jsonObject.getJSONObject("sys").getString("country");
        //выводим строки в текстовых полях
        cityTextView.setText(cityText);
    }

    private void setUpdatedText(JSONObject jsonObject) throws JSONException {
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        String updateOn = dateFormat.format(new Date(jsonObject.getLong("dt") * 1000));
        String updatedText = Objects.requireNonNull(getActivity()).getResources()
                .getString(R.string.lastUpdate) + updateOn;
        textViewLastUpdate.setText(updatedText);
    }

    private void setDescription(JSONObject details) throws JSONException {
        String descriptionText = details.getString("description").toUpperCase();
        textViewWhether.setText(descriptionText);
    }

    private void setWind(JSONObject jsonObject) throws JSONException {
        String wind = jsonObject.getString("speed");
        String windSpeed = Objects.requireNonNull(getActivity()).getString(R.string.windSpeed);
        String ms = getActivity().getString(R.string.ms);
        String windText = windSpeed + " " + wind + " " + ms;
        textViewWind.setText(windText);
    }

    private void setCurrentTemp(JSONObject main) throws JSONException {
        String currentTextText = String.format(Locale.getDefault(), "%.1f",
                main.getDouble("temp")) + "\u2103";
        textViewTemper.setText(currentTextText);
    }

    private void setPressure(JSONObject main) throws JSONException {
        String pressure = main.getString("pressure");
        String press = Objects.requireNonNull(getActivity()).getString(R.string.press);
        String hPa = getActivity().getString(R.string.hPa);
        String pressureText = press + " " + pressure + " " + hPa;
        textViewPressure.setText(pressureText);
    }

    private String[] getIconsArray(int[] actualId, long sunrise, long sunset) {

        String[] icons = new String[5];
        try {
            for (int i = 0; i < icons.length; i++) {
                int id = actualId[i] / 100;

                if (actualId[i] == 800) {
                    long currentTime = new Date().getTime();
                    if (currentTime >= sunrise && currentTime < sunset) {
                        //icon = "\u2600";
                        icons[i] = getString(R.string.weather_sunny);
                    } else {
                        icons[i] = getString(R.string.weather_clear_night);
                    }
                } else {
                    switch (id) {
                        case 2: {
                            icons[i] = getString(R.string.weather_thunder);
                            break;
                        }
                        case 3: {
                            icons[i] = getString(R.string.weather_drizzle);
                            break;
                        }
                        case 5: {
                            icons[i] = getString(R.string.weather_rainy);
                            break;
                        }
                        case 6: {
                            icons[i] = getString(R.string.weather_snowy);
                            break;
                        }
                        case 7: {
                            icons[i] = getString(R.string.weather_foggy);
                            break;
                        }
                        case 8: {
                            //icon = "\u2601";
                            icons[i] = getString(R.string.weather_cloudy);
                            break;
                        }
                    }
                }

            }
            Log.e(TAG, "icons.length = " + icons.length);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        return icons;
    }

    private void setWeatherIcon(int actualId, long sunrise, long sunset) {
        int id = actualId / 100;
        String icon = "";

        if (actualId == 800) {
            long currentTime = new Date().getTime();
            if (currentTime >= sunrise && currentTime < sunset) {
                //icon = "\u2600";
                icon = getString(R.string.weather_sunny);
            } else {
                icon = getString(R.string.weather_clear_night);
            }
        } else {
            switch (id) {
                case 2: {
                    icon = getString(R.string.weather_thunder);
                    break;
                }
                case 3: {
                    icon = getString(R.string.weather_drizzle);
                    break;
                }
                case 5: {
                    icon = getString(R.string.weather_rainy);
                    break;
                }
                case 6: {
                    icon = getString(R.string.weather_snowy);
                    break;
                }
                case 7: {
                    icon = getString(R.string.weather_foggy);
                    break;
                }
                case 8: {
                    //icon = "\u2601";
                    icon = getString(R.string.weather_cloudy);
                    break;
                }
            }
        }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            textViewIcon.setVisibility(View.GONE);
        } else {
            textViewIcon.setText(icon);
        }
    }

    private class ServiceFinishedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, final Intent intent) {
            Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //сначала смотрим, а удалось ли сервису получить JSON объект
                    boolean is_JSON_null =  intent.getBooleanExtra(IS_JSON_NULL, true);
                    //если не удалось, то is_JSON_null = true
                    if (is_JSON_null){
                        String currentCity = intent.getStringExtra(CURRENT_CITY);
                        Toast.makeText(getActivity(), R.string.place_not_found,
                                Toast.LENGTH_LONG).show();
                        Log.e(TAG, "ServiceFinishedReceiver CitysList().size() =" +
                                CityListLab.getCitysList().size());
                        CityListLab.removeSity(currentCity); //удаляем город из списка
                        Log.e(TAG, "ServiceFinishedReceiver CitysList().size() =" +
                                CityListLab.getCitysList().size());
                        CityLab.setCityDefault();  //устанавливаем текущий город Saint Petersburg

                        if (Objects.requireNonNull(getActivity()).getResources().getConfiguration()
                                .orientation  == Configuration.ORIENTATION_LANDSCAPE){
                            //показываем фрагмент с погодой с городом по умолчанию
                            showCityWhetherLand(CityLab.getCity());
                            //перегружаем фрагмент со списком для обновления списка
                            setChooseCityFrag();
                        }else {
                            //показываем фрагмент со списком
                            setChooseCityFrag();
                        }
                        //если JSON объект получен, то переводим строки в JSON объекты и получаем данные
                    }else {
                        String jsonObjectString = intent.getStringExtra(JSON_OBJECT);
                        String jsonObjectForecastString = intent.getStringExtra(JSON_OBJECT_FORECAST);
                        JSONObject jsonObject = null;
                        JSONObject jsonObjectForecast = null;
                        try {
                            jsonObject = new JSONObject(Objects.requireNonNull(jsonObjectString));
                            jsonObjectForecast = new JSONObject(Objects.requireNonNull(jsonObjectForecastString));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        renderWeather(jsonObject);
                        renderForecast(jsonObjectForecast);
                    }
                    Log.d(TAG, "WeatherFragment ServiceFinishedReceiver onReceive" );
                }
            });
        }
    }
}
//1 hPa = 0.75006375541921 mmHg