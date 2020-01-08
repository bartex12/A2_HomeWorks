package com.geekbrains.city_weather;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class HelpActivity extends AppCompatActivity {
    private static final String TAG = "33333";
    TextView tvHelp;
    ImageView left;
    ImageView right;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        ActionBar acBar = getSupportActionBar();
        Objects.requireNonNull(acBar).setTitle("");
        //показать стрелку Назад
        acBar.setDisplayHomeAsUpEnabled(true);
        acBar.setHomeButtonEnabled(true);

        tvHelp = findViewById(R.id.textViewHelpMain);

        InputStream iFile = getResources().openRawResource(R.raw.weather_help);
        StringBuilder strFile = inputStreamToString(iFile);
        tvHelp.setText(strFile);

        left = findViewById(R.id.imageView2);
        left.setImageResource(R.drawable.help_magistr);

        right = findViewById(R.id.imageView3);
        right.setImageResource(R.drawable.help_magistr);
    }

    private StringBuilder inputStreamToString(InputStream iFile) {
        StringBuilder strFull = new StringBuilder();
        String str;
        try {
            // открываем поток для чтения
            InputStreamReader ir = new InputStreamReader(iFile);
            BufferedReader br = new BufferedReader(ir);
            // читаем содержимое
            while ((str = br.readLine()) != null) {
                //Чтобы не было в одну строку, ставим символ новой строки
                strFull.append(str).append("\n");
            }
            //закрываем потоки
            iFile.close();
            ir.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strFull;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_help_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Log.d(TAG, "Домой");
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
