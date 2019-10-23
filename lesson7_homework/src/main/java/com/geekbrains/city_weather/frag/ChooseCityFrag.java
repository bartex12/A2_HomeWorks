package com.geekbrains.city_weather.frag;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import com.geekbrains.city_weather.R;
import com.geekbrains.city_weather.adapter.RecyclerViewCityAdapter;
import com.geekbrains.city_weather.dialogs.DialogCityAdd;
import com.geekbrains.city_weather.singltones.CityListLab;
import java.util.ArrayList;
import java.util.Objects;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.geekbrains.city_weather.constants.AppConstants.CURRENT_CITY;
import static com.geekbrains.city_weather.constants.AppConstants.CURRENT_CITY_MARKED;
import static com.geekbrains.city_weather.constants.AppConstants.WEATHER_FRAFMENT_TAG;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class ChooseCityFrag extends Fragment {
    private static final String TAG = "33333";
    private String city = "";
    private boolean isExistWhetherFrag;  // Можно ли расположить рядом фрагмент с погодой
    private RecyclerView recyclerViewMarked; //RecyclerView для списка ранее выбранных городов
    private ArrayList<String> cityMarked = new ArrayList<>(); //список ранее выбранных городов
    private RecyclerViewCityAdapter recyclerViewCityAdapter; //адаптер для RecyclerView

    public ChooseCityFrag() {
        // Required empty public constructor
    }

    public static ChooseCityFrag newInstance() {
        return  new ChooseCityFrag();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_city_choose, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        initRecycledView();
        registerForContextMenu(recyclerViewMarked);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "ChooseCityFrag onActivityCreated");
        // Определение, можно ли будет расположить рядом данные в другом фрагменте
        isExistWhetherFrag = getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
        // Если это не первое создание, то восстановим текущую позицию
        if (savedInstanceState != null) {
            Log.d(TAG, "ChooseCityFrag onActivityCreated savedInstanceState != null");
            this.initRecycledView(); //если не сделать, при повороте теряем список
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "ChooseCityFrag onResume recyclerViewCityAdapter = " + recyclerViewCityAdapter);
        if (recyclerViewCityAdapter!= null){
            recyclerViewCityAdapter.notifyDataSetChanged();
        }
    }

    //проверка - если такой город есть в списке- возвращает false
    //сделан статическим, чтобы можно было использовать в адаптере списка
    public static boolean isNotCityInList(String city, ArrayList<String> cityMarked) {
        for (int i = 0; i < cityMarked.size(); i++) {
            if (cityMarked.get(i).toUpperCase().equals(city.toUpperCase())) {
                return false;
            }
        }
        return true;
    }

    // Сохраним текущий город (вызывается перед выходом из фрагмента)
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "ChooseCityFrag onSaveInstanceState");
        outState.putString(CURRENT_CITY, city);
        outState.putStringArrayList(CURRENT_CITY_MARKED, cityMarked);
        Log.d(TAG, "ChooseCityFrag savedInstanceState cityMarked.size()= " +
                cityMarked.size() + " city = " + city);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "ChooseCityFrag onPause");
    }

    //************************************************************************************
    //Действия по подключению контекстного меню для пунктов списка RecyclerView во фрагменте
    // 1 в onViewCreated фрагмента пишем registerForContextMenu(recyclerViewMarked);
    // 2 делаем метод onContextItemSelected(MenuItem item) как обычно (см ниже)
    // 3 ViewHolder адаптера implements View.OnCreateContextMenuListener и реализуем
    // onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) интерфейса
    // 4 присваиваем слушатель адаптеру во ViewHolder: itemView.setOnCreateContextMenuListener(this);
    // 5  устанавливаем слушатель для долгих нажатий в onBindViewHolder адаптера
    // holder.textView.setOnLongClickListener(new View.OnLongClickListener()
    //*******************************************************************

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        handleMenuItemClick(item);
        return super.onContextItemSelected(item);
    }

    //инициализация View
    private void initViews(View view) {
        recyclerViewMarked = view.findViewById(R.id.recycledViewMarked);
        CheckBox checkBoxWind = view.findViewById(R.id.checkBoxWind);
        checkBoxWind.setChecked(true);
        checkBoxWind.setEnabled(false);
        CheckBox checkBoxPressure = view.findViewById(R.id.checkBoxPressure);
        checkBoxPressure.setChecked(true);
        checkBoxPressure.setEnabled(false);
    }

    //инициализация RecycledView
    private void initRecycledView() {
        Log.d(TAG, "ChooseCityFrag initRecycledView");
        //используем встроенный LinearLayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        //реализуем интерфейс адаптера, в  его методе onCityClick получим имя города и его позицию
        RecyclerViewCityAdapter.OnCityClickListener onCityClickListener =
                new RecyclerViewCityAdapter.OnCityClickListener() {
                    @Override
                    public void onCityClick(String newCity) {
                        Log.d(TAG, "ChooseCityFrag initRecycledView onCityClick");
                        //изменяем город
                        city = newCity;
                        // показываем погоду в городе с учётом ориентации экрана
                        showCityWhetherWithOrientation(city);
                    }
                };
        //передадим адаптеру в конструкторе список выбранных городов и ссылку на интерфейс
        //в принципе, надо через adapter.setOnCityClickListener, но хочу попробовать так
        //понятно, что это  неуниверсально, так как адаптер теперь зависит от конкретного интерфейся
        recyclerViewCityAdapter = new RecyclerViewCityAdapter(CityListLab.getCitysList(),
                onCityClickListener, getActivity());

        recyclerViewMarked.setLayoutManager(layoutManager);
        recyclerViewMarked.setAdapter(recyclerViewCityAdapter);
    }

    //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    private void handleMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_add: {
                DialogFragment dialogFragment = new DialogCityAdd();
                dialogFragment.show(Objects.requireNonNull(getFragmentManager()), "addCity");
                break;
            }
            case R.id.menu_remove: {
                recyclerViewCityAdapter.removeElement();
                break;
            }
            case R.id.menu_clear: {
                recyclerViewCityAdapter.clearList();
                break;
            }
            case R.id.menu_cancel: {
                //TODO
                break;
            }
        }
    }

    // Показать погоду во фрагменте  в портретной ориентации
    private void showCityWhether(String city) {

        // создаем новый фрагмент с текущей позицией для вывода погоды
        WeatherFragment weatherFrag = WeatherFragment.newInstance(city);
        // ... и выполняем транзакцию по замене фрагмента
        FragmentTransaction ft = Objects.requireNonNull(getFragmentManager()).beginTransaction();
        ft.replace(R.id.content_super, weatherFrag, WEATHER_FRAFMENT_TAG);  // замена фрагмента
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);// эффект
        //ft.addToBackStack(null);
        ft.commit();
        Log.d(TAG, "ChooseCityFrag showCityWhether Фрагмент = " +
                getFragmentManager().findFragmentById(R.id.content_super));
    }

    // Показать погоду во фрагменте в альбомной ориентации
    private void showCityWhetherLand(String city) {

        // создаем новый фрагмент с текущей позицией для вывода погоды
        WeatherFragment weatherFrag = WeatherFragment.newInstance(city);
        // ... и выполняем транзакцию по замене фрагмента
        FragmentTransaction ft = Objects.requireNonNull(getFragmentManager()).beginTransaction();
        ft.replace(R.id.content_super_r, weatherFrag, WEATHER_FRAFMENT_TAG);  // замена фрагмента
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);// эффект
        //ft.addToBackStack(null);
        ft.commit();
        Log.d(TAG, "ChooseCityFrag showCityWhetherLand Фрагмент = " +
                getFragmentManager().findFragmentById(R.id.content_super));
    }

    // показываем погоду в городе с учётом ориентации экрана
    private void showCityWhetherWithOrientation(String city) {
        //если альбомная ориентация,то
        if (isExistWhetherFrag) {
            showCityWhetherLand(city);
            //а если портретная, то
        } else {
            showCityWhether(city);
        }
    }


}

