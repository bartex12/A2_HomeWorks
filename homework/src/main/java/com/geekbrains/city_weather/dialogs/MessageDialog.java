package com.geekbrains.city_weather.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.geekbrains.city_weather.R;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import static com.geekbrains.city_weather.constants.AppConstants.MESSAGE;

public class MessageDialog extends DialogFragment {

    public static MessageDialog newInstance(String message) {
        Bundle args = new Bundle();
        args.putString(MESSAGE, message);
        MessageDialog fragment = new MessageDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = Objects.requireNonNull(getActivity()).getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_message, null);

        final TextView tvMessage = view.findViewById(R.id.textViewMessage);
        tvMessage.setText(Objects.requireNonNull(getArguments()).getString(MESSAGE));

        final Button buttonOk = view.findViewById(R.id.buttonOkMessage);
        builder.setView(view);

        //действия при нажатии кнопки OK
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return builder.create();
    }
}
