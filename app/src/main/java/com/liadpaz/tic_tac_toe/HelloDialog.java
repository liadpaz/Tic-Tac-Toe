package com.liadpaz.tic_tac_toe;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;

import com.liadpaz.tic_tac_toe.databinding.LayoutHelloBinding;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class HelloDialog extends Dialog {

    HelloDialog(Context context, @NotNull String name) {
        super(context);
        LayoutHelloBinding binding = LayoutHelloBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setCancelable(true);

        TextView tv_hello_name = binding.tvHelloName;
        binding.btnHello.setOnClickListener(v -> HelloDialog.this.dismiss());

        int time = Integer.parseInt(new SimpleDateFormat("HH", Locale.ENGLISH).format(new Date()));
        if (time <= 5) {
            tv_hello_name.setText(String.format("%s %s!", context.getString(R.string.good_night), name));
        } else if (time <= 11) {
            tv_hello_name.setText(String.format("%s %s!", context.getString(R.string.good_morning), name));
        } else if (time <= 17) {
            tv_hello_name.setText(String.format("%s %s!", context.getString(R.string.good_afternoon), name));
        } else {
            tv_hello_name.setText(String.format("%s %s!", context.getString(R.string.good_evening), name));
        }

    }
}
