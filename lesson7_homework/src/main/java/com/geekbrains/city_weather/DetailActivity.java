package com.geekbrains.city_weather;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import com.geekbrains.city_weather.dialogs.AboutDialog;
import com.geekbrains.city_weather.frag.WeatherFragment;
import com.geekbrains.city_weather.preferences.SettingsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import java.util.ArrayList;
import java.util.Objects;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import static com.geekbrains.city_weather.constants.AppConstants.CITY_MARKED;
import static com.geekbrains.city_weather.constants.AppConstants.CURRENT_CITY;
import static com.geekbrains.city_weather.constants.AppConstants.WEATHER_FRAFMENT_TAG;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "33333";
    private String currentCity;
    private ArrayList<String> cityMarked;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    finish();
                    return true;
                case R.id.navigation_about:
                    AboutDialog aboutDialog = new AboutDialog();
                    aboutDialog.show(getSupportFragmentManager(),
                            getResources().getString(R.string.dialog));
                    return true;
                case R.id.navigation_settings:
                    Log.d(TAG, "onNavigationItemSelected");
                    Intent intentSettings = new Intent(DetailActivity.this,
                            SettingsActivity.class);
                    startActivity(intentSettings);
                    finish();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        initFab();
        initBottomNavigation();
        getDataFromIntent();

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //после изменения строки списка и переворота экрана надо передать актуальную позицию
            //делать это буду через активность вызовом метода фрагмента
            Log.d(TAG, "DetailActivity onCreate currentCity = " + currentCity);
            Intent intent = new Intent(DetailActivity.this, MainActivity.class);
            intent.putExtra(CURRENT_CITY, currentCity);
            intent.putExtra(CITY_MARKED, cityMarked);
            startActivity(intent);
            // Если устройство перевернули в альбомную ориентацию,
            // то надо эту activity закрыть и убрать из стэка
            finish();
        }

        // Если эта activity запускается первый раз (с каждым новым городом первый раз)
        // то перенаправим параметр фрагменту
        if (savedInstanceState == null) {
            //создаём фрагмент, передавая индекс в аргументы фрагмента
            WeatherFragment details = WeatherFragment.newInstance(currentCity, cityMarked);
            // Добавим фрагмент на activity
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, details, WEATHER_FRAFMENT_TAG)
                    .commit();
        }
    }

    private void initFab() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, getResources().getString(R.string.stub), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void initBottomNavigation() {
        BottomNavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void getDataFromIntent() {
        //получаем название города из интента
        currentCity = Objects.requireNonNull(getIntent()
                .getExtras()).getString(CURRENT_CITY);
        //получаем список ранее выьбранных городов их интента
        cityMarked = getIntent()
                .getStringArrayListExtra(CITY_MARKED);
    }
}
