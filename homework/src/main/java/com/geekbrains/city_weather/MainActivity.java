package com.geekbrains.city_weather;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.geekbrains.city_weather.database.WeatherDataBaseHelper;
import com.geekbrains.city_weather.dialogs.DialogCityAdd;
import com.geekbrains.city_weather.dialogs.DialogCityChange;
import com.geekbrains.city_weather.dialogs.MessageDialog;
import com.geekbrains.city_weather.frag.ChooseCityFrag;
import com.geekbrains.city_weather.frag.WeatherFragment;
import com.geekbrains.city_weather.preferences.SettingsActivity;
import com.geekbrains.city_weather.singltones.CityLab;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import static com.geekbrains.city_weather.constants.AppConstants.CITY_FRAFMENT_TAG;
import static com.geekbrains.city_weather.constants.AppConstants.LAST_CITY;
import static com.geekbrains.city_weather.constants.AppConstants.WEATHER_FRAFMENT_TAG;


public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "33333";
    private DrawerLayout drawer;
    private boolean doubleBackToExitPressedOnce;
    SQLiteDatabase database;
    LocationManager mLocManager = null;
    boolean permissionsGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "MainActivity onCreate");

        // если разрешений на определение местоположения нет, запрашиваем их,
        // а если есть - получаем для местоположения устройства город с кодом страны
        // и пишем в Preferences, затем читаем в onResume() в методе initSingletons()
        //если разрешения НЕ ДАНЫ пользователем, выводим Toast и продолжаем работу,
        // используя в качестве текущего города последний запомненный в Preferences/или дефолтный/
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        } else {
            //работаем с местоположением устройства
            getMyLocationCity();
        }

        initDB();
        initFab();
        initPrefs();
        initviews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "MainActivity onResume");

        initSingletons();
        doOrientationBasedActions();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "MainActivity onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //сохранение списка делаем в onStop - при нажатии на среднюю кнопку телефона onStop->onResume
        Log.d(TAG, "MainActivity onDestroy");
        //закрываем базу данных
        database.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Log.d(TAG, "MainActivity onOptionsItemSelected id = " + id);
        switch (id) {
            case R.id.navigation_choose_city:
                setChooseCityFrag();
                showChangecityDialogFragment();
                return true;

            case R.id.navigation_add:
                setChooseCityFrag();
                showAddcityDialogFragment();
                return true;

            case R.id.navigation_about:
                showMessageDialogFfagment(getResources().getString(R.string.aboutAppMessage));
                return true;

            case R.id.navigation_settings:
                Log.d(TAG, "OptionsItem = navigation_settings");
                showSettingsActivity();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getMyLocationCity() {
        // получаем экземпляр LocationManager
        mLocManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //получаем местонахождение
        @SuppressLint("MissingPermission") final Location loc = Objects.requireNonNull(mLocManager)
                .getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        //получаем из местоположения город с кодом страны
        String cityWithCountryCod = getCityWithCountryCod(Objects.requireNonNull(loc));
        //пишем найденный город с кодом страны  в preferences,
        // чтобы прочитать в initSingletons() и сделать текущим
        saveMyLocation(cityWithCountryCod);

        Log.d(TAG, "MainActivity onResume  Местоположение: " + cityWithCountryCod +
                " Широта = " + loc.getLatitude() + "  Долгота = " + loc.getLongitude()
        );
    }

    //получаем из местоположения город с кодом страны
    private String getCityWithCountryCod(Location loc) {
        String cityWithCountryCod = null;
        Geocoder geo = new Geocoder(getBaseContext(), Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geo.getFromLocation(loc.getLatitude(),
                    loc.getLongitude(), 1);
            if (addresses.size() > 0) {
                String cityName = addresses.get(0).getLocality();
                String countryCod = addresses.get(0).getCountryCode();
                //получаем город с кодом страны
                cityWithCountryCod = cityName + ", " + countryCod;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cityWithCountryCod;
    }

    public void initDB() {
        database = new WeatherDataBaseHelper(this).getWritableDatabase();
    }

    private void initSingletons() {
        Log.d(TAG, "MainActivity initSingletons");
        //  !!!!  имя папки в телефоне com.geekbrains.a1l1_helloworld   !!!
        SharedPreferences prefSetting = PreferenceManager.getDefaultSharedPreferences(this);
        //это последний запомненный город из Preferences
        String cityCurrent = prefSetting.getString(LAST_CITY,
                getResources().getString(R.string.saint_petersburg));
        //инициализируем значение  синглтона CityLab последним городом из Preferences
        CityLab.getInstance(cityCurrent);
    }

    //действия с фрагментами в зависимости от ориентации телефона
    private void doOrientationBasedActions() {
        Log.d(TAG, "MainActivity doOrientationBasedActions");
        //если альбомная ориентация - можно будет расположить рядом данные в другом фрагменте
        if (isLandscape()) {
            setChooseCityFrag();
            setWeatherFragment(R.id.content_super_r);
            //а если портретная
        } else {
            setWeatherFragment(R.id.content_super);
        }
    }

    // Определение, можно ли будет расположить рядом данные в другом фрагменте
    private boolean isLandscape() {
        return getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
    }

    private void initviews() {

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view_main);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);
    }

    private void initPrefs() {
        //устанавливаем из настроек значения по умолчанию для первой загрузки
        //  !!!!  имя папки в телефоне com.geekbrains.a1l1_helloworld   !!!
        PreferenceManager.setDefaultValues(this, R.xml.pref_setting, false);
    }

    private void showMessageDialogFfagment(String message) {
        DialogFragment dialogMessage = MessageDialog.newInstance(message);
        dialogMessage.show(getSupportFragmentManager(), "dialogMessage");
    }

    private void showSettingsActivity() {
        Intent intentSettings = new Intent(this, SettingsActivity.class);
        startActivity(intentSettings);
    }

    private void initFab() {
        FloatingActionButton fab = findViewById(R.id.fab_main);
        if (isLandscape()) {
            fab.hide();
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setChooseCityFrag();
                showChangecityDialogFragment();
            }
        });
    }

    private void showChangecityDialogFragment() {
        DialogFragment dialogFragment = new DialogCityChange();
        dialogFragment.show(getSupportFragmentManager(), "changeCity");
    }

    private void showAddcityDialogFragment() {
        DialogFragment dialogFragment = new DialogCityAdd();
        dialogFragment.show(getSupportFragmentManager(), "addCity");
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        int id = menuItem.getItemId();

        if (id == R.id.nav_camera) {
            Log.d(TAG, "MainActivity onNavigationItemSelected nav_camera");
            setChooseCityFrag();
            showChangecityDialogFragment();
        } else if (id == R.id.nav_gallery) {
            Log.d(TAG, "MainActivity onNavigationItemSelected nav_gallery");
            setChooseCityFrag();
            showAddcityDialogFragment();
        } else if (id == R.id.nav_help) {
            Log.d(TAG, "MainActivity onNavigationItemSelected nav_help");
            showMessageDialogFfagment(getResources().getString(R.string.willBeHelp));
        } else if (id == R.id.nav_manage) {
            Log.d(TAG, "MainActivity onNavigationItemSelected nav_tools");
            showSettingsActivity();
        } else if (id == R.id.nav_share) {
            Log.d(TAG, "MainActivity onNavigationItemSelected nav_share");
            showMessageDialogFfagment(getResources().getString(R.string.willBeLink));
        } else if (id == R.id.nav_send) {
            Log.d(TAG, "MainActivity onNavigationItemSelected nav_send");
            showMessageDialogFfagment(getResources().getString(R.string.willBeEstimate));
        }
        // Выделяем выбранный пункт меню в шторке
        menuItem.setChecked(true);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    @Override
    public void onBackPressed() {

        //если фрагмент - это WeatherFragment то isChooseCityFrag = true
        boolean isChooseCityFrag = getSupportFragmentManager().findFragmentById(
                R.id.content_super) instanceof WeatherFragment;
        Log.d(TAG, "MainActivity onBackPressed isChooseCityFrag = " + isChooseCityFrag);

        //если шторка открыта- закрываем
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            //если isChooseCityFrag==true то меняем фрагмент на ChooseCityFrag
        } else if (isChooseCityFrag) {
            setChooseCityFrag();
            //иначе мы в ChooseCityFrag и выходим из программы при повторном
            //нажатии в течение 2 секунд
            // http://qaru.site/questions/30293/clicking-the-back-button-twice-to-exit-an-activity
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Snackbar.make(findViewById(android.R.id.content),
                    Objects.requireNonNull(this).getString(R.string.forExit),
                    Snackbar.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    // создаем новый фрагмент со списком ранее выбранных городов
    //TODO возможно сделать его статическим если использовать во фрагменте
    private void setChooseCityFrag() {
        Log.d(TAG, "MainActivity setChooseCityFrag");
        ChooseCityFrag chooseCityFrag = ChooseCityFrag.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_super, chooseCityFrag, CITY_FRAFMENT_TAG);  // замена фрагмента
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);// эффект
        ft.commit();
    }

    // создаем новый фрагмент с текущей позицией города  для вывода погоды
    //TODO возможно сделать его статическим если использовать во фрагменте
    private void setWeatherFragment(int container_id) {
        Log.d(TAG, "MainActivity setWeatherFragment");
        WeatherFragment weatherFrag = WeatherFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(container_id, weatherFrag, WEATHER_FRAFMENT_TAG);  // замена фрагмента
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);// эффект
        ft.commit();
    }

    //замена первых символов в словах на заглавные буквы
    //https://ru.stackoverflow.com/questions/612500/Сделать-заглавным-первый-символ-в-каждом-слове
    public static String toUpperCaseForFirstLetter(String text) {
        StringBuilder builder = new StringBuilder(text);
        //выставляем первый символ заглавным, если это буква
        if (Character.isAlphabetic(text.codePointAt(0)))
            builder.setCharAt(0, Character.toUpperCase(text.charAt(0)));
        //крутимся в цикле, и меняем буквы, перед которыми пробел на заглавные
        for (int i = 1; i < text.length(); i++)
            if (Character.isAlphabetic(text.charAt(i)) && Character.isSpaceChar(text.charAt(i - 1)))
                builder.setCharAt(i, Character.toUpperCase(text.charAt(i)));
        return builder.toString();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 100) {
            permissionsGranted = (grantResults.length > 1
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED)
                    && (grantResults[0] == PackageManager.PERMISSION_GRANTED);
            if (permissionsGranted) {
                Toast.makeText(this, getResources().getString(R.string.permission),
                        Toast.LENGTH_SHORT).show();
                recreate();
            } else {
                Toast.makeText(this,
                        getResources().getString(R.string.notPermission),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveMyLocation(String cityWithCountryCod) {
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(Objects.requireNonNull(this));
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(LAST_CITY, cityWithCountryCod);
        editor.apply();
    }
}
