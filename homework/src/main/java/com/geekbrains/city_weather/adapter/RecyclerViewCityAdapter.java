package com.geekbrains.city_weather.adapter;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.geekbrains.city_weather.R;
import com.geekbrains.city_weather.database.WeatherTable;
import com.geekbrains.city_weather.singltones.CityListLab;
import java.util.ArrayList;
import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static com.geekbrains.city_weather.constants.AppConstants.DEFAULT_CITY;

public class RecyclerViewCityAdapter extends RecyclerView.Adapter<RecyclerViewCityAdapter.ViewHolder>{
    private static final String TAG = "33333";
    private ArrayList<String> data;
    private SQLiteDatabase database;
    private OnCityClickListener onCityClickListener;
    private int posItem = 0;
    private Context context;

    public interface OnCityClickListener {
        void onCityClick(String city);
    }

    public RecyclerViewCityAdapter(SQLiteDatabase database) {
        this.database = database;
        data = WeatherTable.getAllCitys(database);

        if (data != null) {
            if (data.size() == 0){
                data.add(DEFAULT_CITY);
            }
        }else{
            this.data = new ArrayList<>();
        }
        Log.d(TAG, "RecyclerViewCityAdapter конструктор data = " + data);
    }

    public void setOnCityClickListener(OnCityClickListener onCityClickListener){
        this.onCityClickListener =onCityClickListener;
    }

    public void addElement(String city) {
        Log.d(TAG, "RecyclerViewCityAdapter addElement");
        if (isNotCityInList(city)){
            data.add(city);
            Collections.sort(data);
            notifyDataSetChanged();
        }else {
            Toast.makeText(context, context.getResources()
                    .getString(R.string.is_in_list),Toast.LENGTH_SHORT).show();
        }
    }

    public void removeElement() {
        Log.d(TAG, "RecyclerViewCityAdapter removeElement");
        if (data.size() > 0) {
            data.remove(posItem);
            notifyDataSetChanged();
        }
    }

    private boolean isNotCityInList(String city) {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).toUpperCase().equals(city.toUpperCase())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.textView.setText(data.get(position));
        holder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = data.get(position);
                //вызываем метод интерфейса onCityClick и передаём в него название города
                //метод сработает у всех подписанных на него - у нас  в ChooseCityFrag
                Log.d(TAG, "RecyclerViewCityAdapter onBindViewHolder city =  " + city);
                onCityClickListener.onCityClick(city);
            }
        });
        // устанавливаем слушатель долгих нажатий на списке для вызова контекстного меню
        holder.textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                posItem = position;
                return false;
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_list,
                parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder  {
        TextView textView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textViewCity);
        }
    }
}
