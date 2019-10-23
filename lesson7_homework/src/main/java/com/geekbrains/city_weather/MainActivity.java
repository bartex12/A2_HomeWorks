package com.geekbrains.city_weather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.geekbrains.city_weather.dialogs.DialogCityAdd;
import com.geekbrains.city_weather.dialogs.DialogCityChange;
import com.geekbrains.city_weather.dialogs.MessageDialog;
import com.geekbrains.city_weather.frag.ChooseCityFrag;
import com.geekbrains.city_weather.frag.WeatherFragment;
import com.geekbrains.city_weather.preferences.SettingsActivity;
import com.geekbrains.city_weather.singltones.CityLab;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import java.util.ArrayList;
import java.util.Objects;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import static com.geekbrains.city_weather.constants.AppConstants.CITY_FRAFMENT_TAG;
import static com.geekbrains.city_weather.constants.AppConstants.WEATHER_FRAFMENT_TAG;


public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, DialogCityAdd.OnCityAddListener,
        DialogCityChange.OnCityChangeListener{

    private static final String TAG = "33333";
    private boolean isShowCheckboxes;
    private DrawerLayout drawer;
    private  ArrayList<String> cityMarked = new ArrayList<>();
    private boolean isExistWhetherFrag;
    private String city_current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        city_current = getResources().getString(R.string.saint_petersburg);
        Log.d(TAG,"MainActivity onCreate city_current = " + city_current);
        initFab();
        initPrefDefault();
        initviews();
        initSingleton();
        // Определение, можно ли будет расположить рядом данные в другом фрагменте
        isExistWhetherFrag = getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
        if (isExistWhetherFrag){
            setChooseCityFrag();
            // создаем новый фрагмент с текущей позицией для вывода погоды
            setWeatherFragmentland(city_current);
        }else {
            setChooseCityFrag();
        }
    }

    private void initSingleton() {
        cityMarked.add(city_current);
        //инициализируем список синглтона значением по умолчанию
        CityLab.getInstance(cityMarked);
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

    private void initPrefDefault() {
        //устанавливаем из настроек значения по умолчанию для первой загрузки
        androidx.preference.PreferenceManager
                .setDefaultValues(this, R.xml.pref_setting, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"MainActivity onResume");
        //получаем настройки из активности настроек
        SharedPreferences prefSetting = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
        //получаем из файла настроек состояние чекбокса
        isShowCheckboxes = prefSetting.getBoolean("showCheckBoxes", true);
        Log.d(TAG,"MainActivity onResume isShowCheckboxes = " + isShowCheckboxes);
        // показываем/скрываем чекбоксы на экране выбора города
       // setCheckboxesInFragment(isShowCheckboxes);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Log.d(TAG, "ListOfSmetasNames onOptionsItemSelected id = " + id);
        switch (id) {
            case R.id.navigation_choose_city:
                showChangecityDialogFragment();
                return true;

            case R.id.navigation_add:
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
        fab.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangecityDialogFragment();
            }
        });
    }

    //диалог сохранения, оформленный как класс с указанием имени файла
    private void showChangecityDialogFragment() {
        DialogFragment dialogFragment = new DialogCityChange();
        dialogFragment.show(getSupportFragmentManager(), "changeCity");
    }

    //диалог сохранения, оформленный как класс с указанием имени файла
    private void showAddcityDialogFragment() {
        DialogFragment dialogFragment = new DialogCityAdd();
        dialogFragment.show(getSupportFragmentManager(), "addCity");
    }

    // показываем/скрываем чекбоксы на экране выбора города
    private void setCheckboxesInFragment(boolean isShowCheckboxes) {
        ChooseCityFrag fr = (ChooseCityFrag) getSupportFragmentManager().
                findFragmentById(R.id.citiesWhether);
        View view = Objects.requireNonNull(fr).getView();
        CheckBox checkBoxWind = Objects.requireNonNull(view).findViewById(R.id.checkBoxWind);
        CheckBox checkBoxPressure = Objects.requireNonNull(view).findViewById(R.id.checkBoxPressure);
        if (isShowCheckboxes) {
            checkBoxWind.setVisibility(View.VISIBLE);
            checkBoxPressure.setVisibility(View.VISIBLE);
        } else {
            checkBoxWind.setVisibility(View.GONE);
            checkBoxPressure.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        int id = menuItem.getItemId();

        if (id == R.id.nav_camera) {
            Log.d(TAG, "MainActivity onNavigationItemSelected nav_camera");
            showChangecityDialogFragment();
        } else if (id == R.id.nav_gallery) {
            Log.d(TAG, "MainActivity onNavigationItemSelected nav_gallery");
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
    public void onCityAdd(String city) {
        Log.d(TAG, "MainActivity onCityAdd city = " + city);
        //добавляем город в список синглтона
        CityLab.addCity(city);
        Log.d(TAG, "MainActivity onCityChange CityLab.size = " + CityLab.getCitysList().size());
    }

    @Override
    public void onCityChange(String city) {
        Log.d(TAG, "MainActivity onCityChange city = " + city);
        //добавляем город в список синглтона
        CityLab.addCity(city);
        Log.d(TAG, "MainActivity onCityChange CityLab.size = " + CityLab.getCitysList().size());

        if (getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE){
            setWeatherFragmentland(city);
        }else {
            setWeatherFragment(city);
        }
    }

    @Override
    public void onBackPressed() {
        boolean isChooseCityFrag = getSupportFragmentManager().findFragmentById(
                R.id.content_super)instanceof WeatherFragment;
        Log.d(TAG, "MainActivity onBackPressed isChooseCityFrag = " + isChooseCityFrag);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (isChooseCityFrag){
            setChooseCityFrag();
        }else{
            super.onBackPressed();
        }
    }

    // создаем новый фрагмент со списком ранее выбранных городов
    private void setChooseCityFrag() {
        ChooseCityFrag chooseCityFrag = ChooseCityFrag.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_super, chooseCityFrag, CITY_FRAFMENT_TAG);  // замена фрагмента
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);// эффект
        ft.commit();
    }

    // создаем новый фрагмент с текущей позицией для вывода погоды
    private void setWeatherFragment(String city) {
        WeatherFragment weatherFrag = WeatherFragment.newInstance(city);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_super, weatherFrag, WEATHER_FRAFMENT_TAG);  // замена фрагмента
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);// эффект
        ft.commit();
    }

    // создаем новый фрагмент с текущей позицией для вывода погоды
    private void setWeatherFragmentland(String city) {
        WeatherFragment weatherFrag = WeatherFragment.newInstance(city);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_super_r, weatherFrag, WEATHER_FRAFMENT_TAG);  // замена фрагмента
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);// эффект
        ft.commit();
    }
}