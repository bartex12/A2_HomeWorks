package com.geekbrains.city_weather.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.geekbrains.city_weather.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class WeatherCardAdapterNew extends RecyclerView.Adapter<WeatherCardAdapterNew.CardViewHolder> {
    private ArrayList<DataForecast> dataForecastNew = new ArrayList<>();
    private Context context;

    public WeatherCardAdapterNew(Context context, ArrayList<DataForecast> data) {
        if (data != null) {
            dataForecastNew = data;
        }
        this.context = context;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_forecast_new, parent, false);
        return new WeatherCardAdapterNew.CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherCardAdapterNew.CardViewHolder holder, int position) {
        holder.textViewDescrNew.setText(dataForecastNew.get(position).getDescriptionNew());
        holder.textViewTemperNew.setText(dataForecastNew.get(position).getTempNew());
        holder.textViewDayNew.setText(dataForecastNew.get(position).getDayNew());
        holder.imageViewIcon.setImageDrawable(dataForecastNew.get(position).getIconDraw());
    }

    @Override
    public int getItemCount() {
        return dataForecastNew.size();
    }

    class CardViewHolder extends RecyclerView.ViewHolder {

        TextView textViewDescrNew;
        TextView textViewTemperNew;
        TextView textViewDayNew;
        ImageView imageViewIcon;


        CardViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDescrNew = itemView.findViewById(R.id.textViewDescrNew);
            textViewTemperNew = itemView.findViewById(R.id.textViewTemperNew);
            textViewDayNew = itemView.findViewById(R.id.textViewDayNew);
            imageViewIcon = itemView.findViewById(R.id.imageViewIcon);
        }
    }
}
