package com.geekbrains.city_weather.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.geekbrains.city_weather.R;
import com.geekbrains.city_weather.singltones.CityListLab;
import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewCityAdapter extends RecyclerView.Adapter<RecyclerViewCityAdapter.ViewHolder>{
    private static final String TAG = "33333";
    private ArrayList<String> data;
    private OnCityClickListener onCityClickListener;
    private Activity activity;
    private long posItem = 0;

    public interface OnCityClickListener {
        void onCityClick(String city);
    }

    public RecyclerViewCityAdapter(ArrayList<String> data,
                                   OnCityClickListener onCityClickListener, Activity activity) {
        if (data != null) {
            this.data = data;
        }else{
            this.data = new ArrayList<>();
        }
        this.onCityClickListener = onCityClickListener;
        this.activity = activity;
    }

    public void addElement(String city) {
        data.add(city);
        notifyDataSetChanged();
    }

    public void removeElement() {
        Log.d(TAG, "RecyclerViewCityAdapter removeElement");
        if (data.size() > 0) {
            data.remove((int) posItem);
            notifyDataSetChanged();
            Log.d(TAG, "RecyclerViewCityAdapter removeElement size" + CityListLab.getCitysList().size());
        }
    }

    public void clearList() {
        Log.d(TAG, "RecyclerViewCityAdapter clearList");
        data.clear();
        notifyDataSetChanged();
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
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_list,
                parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        TextView textView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textViewCity);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            activity.getMenuInflater().inflate(R.menu.context_city_menu, menu);
        }
    }
}