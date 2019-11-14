package com.geekbrains.city_weather.frag;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.geekbrains.city_weather.R;
import com.geekbrains.city_weather.adapter.DataForecast;
import com.geekbrains.city_weather.adapter.WeatherCardAdapter;
import com.geekbrains.city_weather.database.DataWeather;
import com.geekbrains.city_weather.database.ForecastTable;
import com.geekbrains.city_weather.database.WeatherDataBaseHelper;
import com.geekbrains.city_weather.database.WeatherTable;
import com.geekbrains.city_weather.services.BackgroundWeatherService;
import com.geekbrains.city_weather.singltones.CityLab;
import com.squareup.picasso.Picasso;

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
import rest.weather_model.WeatherRequestRestModel;
import rest.forecast_model.ForecastRequestRestModel;

import static androidx.preference.PreferenceManager.*;
import static com.geekbrains.city_weather.constants.AppConstants.BROADCAST_WEATHER_ACTION;
import static com.geekbrains.city_weather.constants.AppConstants.CITY_FRAFMENT_TAG;
import static com.geekbrains.city_weather.constants.AppConstants.CURRENT_CITY;
import static com.geekbrains.city_weather.constants.AppConstants.IS_JSON_NULL;
import static com.geekbrains.city_weather.constants.AppConstants.IS_RESPONS_NULL;
import static com.geekbrains.city_weather.constants.AppConstants.JAVA_OBJECT;
import static com.geekbrains.city_weather.constants.AppConstants.JAVA_OBJECT_FORECAST;
import static com.geekbrains.city_weather.constants.AppConstants.LAST_CITY;
import static com.geekbrains.city_weather.constants.AppConstants.WEATHER_FRAFMENT_TAG;

/**
 *  Варианты получения погодных данных в приложении:
 * 1) на уроке A2L1 JSONObject получали в отдельном потоке через методы sdk - HttpURLConnection и т д
 * а обрабатывали в потоке GUI через handler.post(new Runnable()
 *
 * 2) на уроке A2L3 JSONObject получали через сервис, который сам создавал отдельный поток,
 * передавали в интенте широковещательного сообщения в виде строки в место обработки
 * воссоздавали JSONObject из строки и обрабатывали в потоке GUI
 *
 * 3) на уроке A2L5 погодные данные получаем через сервис сразу в виде JAVA объекта
 * с помощью запроса к погодному серверу через библиотеку Retrofit с конвертором GSON,
 * сериализуем ответ и передаём его  в интенте широковещательного сообщения в место обработки,
 * где десериализуем и обрабатывали в потоке GUI
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
    private ImageView imageView;
    //когда сервис BackgroundWeatherService отправляет уведомление о завершении
    //мы его получаем и в  методе onReceive обрабатываем погодные данные
    private ServiceFinishedReceiver receiver = new ServiceFinishedReceiver();

    private SQLiteDatabase database;
    boolean isCityInBase;   // такой город есть в базе7


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
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "WeatherFragment onAttach" );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "WeatherFragment onViewCreated" );

        initDB();
        initViews(view);
        initFonts();
        getActualDataOfCityWeather();
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

    private void initDB(){
        database = new WeatherDataBaseHelper(getActivity()).getWritableDatabase();
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
        imageView = view.findViewById(R.id.imageView);
    }

    private void initFonts() {
        Typeface weatherFont = Typeface.createFromAsset(
                Objects.requireNonNull(getActivity()).getAssets(), "fonts/weather.ttf");
        textViewIcon.setTypeface(weatherFont);
    }

    //если последнее обновление в базе не найдено - идем на погодный сайт, а если
    // последнее обновление было не более 10000 секунд назад - берём данные из базы
    private void getActualDataOfCityWeather(){

        //получаем текущий город из синглтона - куда город попал из Preferences
        String currentCity = CityLab.getCity();
        //получаем список городов из базы
        ArrayList<String> ara = WeatherTable.getAllCitys(database);
        Log.d(TAG, "WeatherFragment getDataOfCityWeather delta = " + ara.toString());
        boolean isCityInDatabase = ara.contains(currentCity);
        Log.d(TAG, "WeatherFragment getDataOfCityWeather isCityInDatabase = " + isCityInDatabase);

        //если текущий город есть в базе
        if (isCityInDatabase){
            //получаем время последнего обновления погоды по этому городу
            long lastUpdateOfCityWeather = WeatherTable.getLastUpdate(database, currentCity);
            long delta = System.currentTimeMillis()/1000 - lastUpdateOfCityWeather;
            Log.d(TAG, "*** WeatherFragment getDataOfCityWeather /1000 = "
                    + System.currentTimeMillis()/1000);
            Log.d(TAG, "*** WeatherFragment getDataOfCityWeather lastUpdateOfCityWeather = "
                    + lastUpdateOfCityWeather);
            Log.d(TAG, "*** WeatherFragment getDataOfCityWeather delta = " + delta);

            //  для отладки время сделать 600 секунд
            //  если прошло больше заданного времени (1 час) с последнего обновления
            if (delta>3600){
                //запускаем сервис, работающий в отдельном потоке, передаём туда текущий город
                //для получения погодных данных
                Intent intent = new Intent(getActivity(), BackgroundWeatherService.class);
                intent.putExtra(CURRENT_CITY, currentCity);
                Objects.requireNonNull(getActivity()).startService(intent);
                //иначе  берём данные из базы
            }else{
                Log.d(TAG, "***********  WeatherFragment getDataOfCityWeather  ************");
                // получаем погодные данные для текущего города
                DataWeather dataWeather = WeatherTable.getOneCityWeatherLine( database, currentCity);
                Log.d(TAG, "WeatherFragment getDataOfCityWeather dataWeather = " + dataWeather);
                cityTextView.setText(dataWeather.getCityName());
                textViewLastUpdate.setText(dataWeather.getLastUpdate());
                textViewWhether.setText(dataWeather.getDescription());
                textViewWind.setText(dataWeather.getWindSpeed());
                textViewPressure.setText(dataWeather.getPressure());
                textViewTemper.setText(dataWeather.getTemperature());
                Drawable drawable = getIconFromIconCod(dataWeather.getIconCod());
                imageView.setImageDrawable(drawable);
            }
        }else {
            //запускаем сервис, работающий в отдельном потоке, передаём туда текущий город
            //для получения погодных данных
            Intent intent = new Intent(getActivity(), BackgroundWeatherService.class);
            intent.putExtra(CURRENT_CITY, currentCity);
            Objects.requireNonNull(getActivity()).startService(intent);
        }

    }

    // Показать погоду во фрагменте в альбомной ориентации
    private void showCityWhetherLand() {

        // создаем новый фрагмент с текущей позицией для вывода погоды
        WeatherFragment weatherFrag = WeatherFragment.newInstance();
        // ... и выполняем транзакцию по замене фрагмента
        FragmentTransaction ft = Objects.requireNonNull(getFragmentManager()).beginTransaction();
        ft.replace(R.id.content_super_r, weatherFrag, WEATHER_FRAFMENT_TAG);  // замена фрагмента
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);// эффект
        ft.commit();
        Log.d(TAG, "WeatherFragment onCityChange Фрагмент = " +
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

    private void renderWeather(WeatherRequestRestModel modelWeather) {

        try {
            setPlaceName(modelWeather.name, modelWeather.sys.country);
            String lastUpdate = setUpdatedText(modelWeather.dt);
            setDescription(modelWeather.weather[0].description);
            String windSpeed = setWind(modelWeather.wind.speed);
            String pressure = setPressure(modelWeather.main.pressure);
            String temperature = setCurrentTemp(modelWeather.main.temp);

            loadImageWithPicasso(modelWeather.weather[0].id,
                    modelWeather.sys.sunrise * 1000,
                    modelWeather.sys.sunset * 1000);

            //добавляем или заменяем погодные данные для города modelWeather.name
            addOrReplaceCityWeather(modelWeather, lastUpdate, windSpeed, pressure, temperature);

            //записываем город в синглтон - делаем его текущим
            CityLab.setCurrentCity(modelWeather.name);

        } catch (Exception exc) {
            exc.printStackTrace();
            Log.e(TAG, "One or more fields not found in the JSON data");
        }
    }

    private void addOrReplaceCityWeather(WeatherRequestRestModel modelWeather, String lastUpdate,
                                         String windSpeed, String pressure, String temperature) {
        //получаем класс DataWeather
        DataWeather dataWeather = new DataWeather(modelWeather.name,
                modelWeather.sys.country,lastUpdate, modelWeather.weather[0].description,
                windSpeed, pressure, temperature, modelWeather.weather[0].icon, modelWeather.dt );
        Log.e(TAG, "addOrReplaceCityWeather modelWeather.dt = " + modelWeather.dt);

        ArrayList<String> ara = WeatherTable.getAllCitys(database);
        isCityInBase = ara.contains(modelWeather.name);
        if (isCityInBase){
            WeatherTable.replaceCityWeather(modelWeather.name, dataWeather, database);
            Log.e(TAG, "addOrReplaceCityWeather isCityInBase = true ");
        }else {
            WeatherTable.addCityWeather(dataWeather, database);
            Log.e(TAG, "addOrReplaceCityWeather isCityInBase = false");
        }
    }

    private void renderForecast(ForecastRequestRestModel modelForecast) {
        dates = getDateArray(modelForecast);
        temperuteres = getTempArray(modelForecast);
        iconArray = getIconsArray(getIdArray(modelForecast));

        addOrReplaceCityForecast(modelForecast);
        //после формирования данных для адаптера инициализируем сам адаптер
        initRecyclerView();
    }

    private void addOrReplaceCityForecast(ForecastRequestRestModel modelForecast){
        //получаем массив объектов класса DataForecast
        DataForecast[] dataForecasts = getDataForecasts();
        if (isCityInBase){
           ForecastTable.replaceCityForecast(modelForecast.city.name, dataForecasts, database);


            Log.e(TAG, "addOrReplaceCityWeather isCityInBase = true ");
        }else {
            ForecastTable.addCityForecast(dataForecasts, database, modelForecast.city.name);
            Log.e(TAG, "addOrReplaceCityWeather isCityInBase = false");
        }
    }

    private void loadImageWithPicasso(int actualId, long sunrise, long sunset){
        String path =getWeatherIconPath(actualId,sunrise,sunset);
        Picasso.get().load(path).into(imageView);
    }

    //получение даты для прогноза на 5 дней
    private String[] getDateArray(ForecastRequestRestModel modelForecast) {
        Log.e(TAG, "getDateArray list.length = " + modelForecast.list.length);
        DateFormat dateFormat = DateFormat.getDateInstance();
        long dateTime;
        String[] dateTimeArray = new String[5];
        for (int i = 0; i < dateTimeArray.length; i++) {
            dateTime = modelForecast.list[7 + 8 * i].dt;
            dateTimeArray[i] = dateFormat.format(new Date(dateTime * 1000));
        }
        Log.e(TAG, "dateTimeArray.length = " + dateTimeArray.length);
        return dateTimeArray;
    }

    //получение температуры для прогноза на 5 дней
    private double[] getTempArray(ForecastRequestRestModel modelForecast){
        Log.e(TAG, "getTempArray list.length = " + modelForecast.list.length);
        double[] temper = new double[5];
        for (int i = 0; i < temper.length; i++) {
            temper[i] =  modelForecast.list[7 + 8*i].main.temp;
        }
        Log.e(TAG, "temper.length = " + temper.length);
        return temper;
    }

    //получение массива id для прогноза на 5 дней
    private int[] getIdArray(ForecastRequestRestModel modelForecast) {
        Log.e(TAG, "getIdArray list.length = " + modelForecast.list.length);
        int[] id = new int[5];
        for (int i = 0; i < id.length; i++) {
            id[i] = modelForecast.list[7 + 8 * i].weather[0].id;
        }
        Log.e(TAG, "id.length = " + id.length);
        return id;
    }

    //загрузка данных в адаптер списка прогноза на 5 дней
    private void  initRecyclerView(){

        DataForecast[] data = getDataForecasts();

        ArrayList<DataForecast> list = new ArrayList<>(data.length);
        list.addAll(Arrays.asList(data));

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false);
        WeatherCardAdapter cardAdapter = new WeatherCardAdapter(getActivity(), list);

        recyclerViewForecast.setLayoutManager(layoutManager);
        recyclerViewForecast.setAdapter(cardAdapter);
    }

    private DataForecast[] getDataForecasts() {
        return new DataForecast[] {
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

    private void setPlaceName(String name, String country) {
        cityTextView.setText(name + ", " + country);
    }

    private String setUpdatedText(long dt) {
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        String updateOn = dateFormat.format(new Date(dt * 1000));
        String updatedText = Objects.requireNonNull(getActivity()).getResources()
                .getString(R.string.lastUpdate) + updateOn;
        textViewLastUpdate.setText(updatedText);
        return updatedText;
    }

    private void setDescription(String description){
        textViewWhether.setText(description);
    }

    private String setWind(float wind){
        String windSpeed = Objects.requireNonNull(getActivity()).getString(R.string.windSpeed);
        String ms = getActivity().getString(R.string.ms);
        String windText = windSpeed + " " + wind + " " + ms;
        textViewWind.setText(windText);
        return windText;
    }

    private String setPressure(float pressure) {
        String press = Objects.requireNonNull(getActivity()).getString(R.string.press);
        String hPa = getActivity().getString(R.string.hPa);
        String pressureText = press + " " + pressure + " " + hPa;
        textViewPressure.setText(pressureText);
        return pressureText;
    }

    private String setCurrentTemp(float temper){
        String currentText = String.format(Locale.getDefault(), "%.1f",temper) + "\u2103";
        textViewTemper.setText(currentText);
        return currentText;
    }

    //получение массива символов иконок  для отображения в пятидневном прогнозе погоды
    private String[] getIconsArray(int[] actualId) {

        String[] icons = new String[5];
        try {
            for (int i = 0; i < icons.length; i++) {
                int id = actualId[i] / 100;

                if (actualId[i] == 800) {
                    icons[i] = getString(R.string.weather_sunny);
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

    //получение изображений иконок в зависимости от кода иконки на сайте openweathermap.org
    // для вывода иконок при обращении к базе данных на устройстве
    private Drawable getIconFromIconCod(String iconCod) {
        Drawable drawable;
        switch (iconCod) {
            case "01d":
            case "01n":
                drawable = Objects.requireNonNull(getActivity())
                        .getResources().getDrawable(R.drawable.sun);
                break;
            case "02d":
            case "02n":
                drawable = Objects.requireNonNull(getActivity())
                        .getResources().getDrawable(R.drawable.partly_cloudy);
                break;
            case "03d":
            case "03n":
                drawable = Objects.requireNonNull(getActivity())
                        .getResources().getDrawable(R.drawable.cloudy);
                break;
            case "04d":
            case "04n":
                drawable = Objects.requireNonNull(getActivity())
                        .getResources().getDrawable(R.drawable.cloudy);
                break;
            case "09d":
            case "09n":
                drawable = Objects.requireNonNull(getActivity())
                        .getResources().getDrawable(R.drawable.rain);
                break;
            case "10d":
            case "10n":
                drawable = Objects.requireNonNull(getActivity())
                        .getResources().getDrawable(R.drawable.little_rain);
                break;
            case "11d":
            case "11n":
                drawable = Objects.requireNonNull(getActivity())
                        .getResources().getDrawable(R.drawable.boom);
                break;
            case "13d":
            case "13n":
                drawable = Objects.requireNonNull(getActivity())
                        .getResources().getDrawable(R.drawable.snow);
                break;
            case "50d":
            case "50n":
                drawable = Objects.requireNonNull(getActivity())
                        .getResources().getDrawable(R.drawable.smog);
                break;
            default:
                drawable = Objects.requireNonNull(getActivity())
                        .getResources().getDrawable(R.drawable.what);
        }

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            imageView.setVisibility(View.GONE);
        }
        return drawable;
    }

    //получение пути к иконкам на сайте openweathermap.org для вывода с Picasso
    private String getWeatherIconPath(int actualId, long sunrise, long sunset) {
        int id = actualId / 100;
        String icon = "";

        if (actualId == 800) {
            long currentTime = new Date().getTime();
            if (currentTime >= sunrise && currentTime < sunset) {
                icon = "http://openweathermap.org/img/wn/01d@2x.png";
            } else {
                icon = "http://openweathermap.org/img/wn/01d@2x.png";
            }
        } else {
            switch (id) {
                case 2: {
                    icon = "http://openweathermap.org/img/wn/11d@2x.png";
                    break;
                }
                case 3: {
                    icon = "http://openweathermap.org/img/wn/09d@2x.png";
                    break;
                }
                case 5: {
                    icon = "http://openweathermap.org/img/wn/10d@2x.png";
                    break;
                }
                case 6: {
                    icon = "http://openweathermap.org/img/wn/13d@2x.png";
                    break;
                }
                case 7: {
                    icon = "http://openweathermap.org/img/wn/50d@2x.png";
                    break;
                }
                case 8: {
                    icon = "http://openweathermap.org/img/wn/04d@2x.png";
                    break;
                }
            }
        }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            imageView.setVisibility(View.GONE);
        }
        return icon;
    }

    private class ServiceFinishedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, final Intent intent) {
            Log.d(TAG, "WeatherFragment ServiceFinishedReceiver onReceive" );
            //переходим в поток GUI
            Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //сначала смотрим, а удалось ли сервису получить JAVA объект
                    boolean is_JSON_null =  intent.getBooleanExtra(IS_JSON_NULL, true);
                    boolean isResponceNull =  intent.getBooleanExtra(IS_RESPONS_NULL, false);
                    //сначала смотрим, ответ от сервера равен null или нет
                    if (isResponceNull){
                        Toast.makeText(getActivity(), getActivity().getResources()
                                        .getString(R.string.tlf_problems),Toast.LENGTH_LONG).show();
                        Log.e(TAG, "ServiceFinishedReceiver: Возникли проблемы " +
                                "с отправкой запроса. Возможно требуется перезагрузка телефона");
                    }else {
                        //если не удалось, то is_JSON_null = true
                        if (is_JSON_null){
                            Toast.makeText(getActivity(), R.string.place_not_found,
                                    Toast.LENGTH_LONG).show();

                            CityLab.setCityDefault();  //делаем текущим город Saint Petersburg

                            if (Objects.requireNonNull(getActivity()).getResources().getConfiguration()
                                    .orientation  == Configuration.ORIENTATION_LANDSCAPE){
                                //показываем фрагмент с погодой с городом по умолчанию
                                showCityWhetherLand();
                                //перегружаем фрагмент со списком для обновления списка
                                //поскольку в базу данных город не заносился, в списке его не будет
                                setChooseCityFrag();
                            }else {
                                //показываем фрагмент со списком
                                setChooseCityFrag();
                            }
                            //если JAVA объект получен, то получаем данные
                        }else {
                            //десериализуем объект WeatherRequestRestModel
                            WeatherRequestRestModel modelWeather = (WeatherRequestRestModel)
                                    Objects.requireNonNull(intent.getExtras())
                                            .getSerializable(JAVA_OBJECT);
                            Log.d(TAG, "WeatherFragment ServiceFinishedReceiver modelWeather =" +
                                    Objects.requireNonNull(modelWeather).dt);

                            //обрабатываем данные и выводим на экран если всё OK, заносим данные в базу
                            renderWeather(modelWeather);

                            ForecastRequestRestModel modelForecast =(ForecastRequestRestModel)
                                    Objects.requireNonNull(intent.getExtras())
                                            .getSerializable(JAVA_OBJECT_FORECAST);
                            Log.d(TAG, "WeatherFragment ServiceFinishedReceiver modelForecast =" +
                                    modelForecast);
                            renderForecast(modelForecast);
                        }
                    }
                }
            });
        }
    }
}
//TODO 1 hPa = 0.75006375541921 mmHg