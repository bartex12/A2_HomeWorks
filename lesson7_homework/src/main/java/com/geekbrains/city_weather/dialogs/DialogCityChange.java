package com.geekbrains.city_weather.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.geekbrains.city_weather.R;
import com.google.android.material.snackbar.Snackbar;
import java.util.Objects;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DialogCityChange extends DialogFragment {

    public DialogCityChange() {
        super();
    }

    private OnCityChangeListener onCityChangeListener;

    public interface OnCityChangeListener{
        void onCityChange(String city);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        onCityChangeListener = (OnCityChangeListener)context;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_choose_city, null);
        final EditText etCity = view.findViewById(R.id.editTextCity);
        final Button buttonOk = view.findViewById(R.id.buttonOk);
        etCity.requestFocus();
        etCity.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(view);
        builder.setTitle(R.string.show_city);
        builder.setIcon(R.drawable.ic_my_location_red_24dp);

        //действия при нажатии кнопки OK
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = etCity.getText().toString();
                if (city.trim().isEmpty()) {
                    Snackbar.make(v.getRootView(),
                            Objects.requireNonNull(getActivity()).getString(R.string.inputCitiName),
                            Snackbar.LENGTH_SHORT).show();
                } else {
                    onCityChangeListener.onCityChange(city);
                    Objects.requireNonNull(getDialog()).dismiss();  //закрывает только диалог
                }
            }
        });

        return builder.create();
    }
}
