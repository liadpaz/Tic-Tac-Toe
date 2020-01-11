package com.liadpaz.tic_tac_toe;

import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class HelloDialog extends Dialog {

    HelloDialog(Context context, @NotNull String name) {
        super(context);

        setContentView(R.layout.layout_hello);
        setCancelable(true);

        TextView tv_hello_name = findViewById(R.id.tv_hello_name);
        Button btn_hello = findViewById(R.id.btn_hello);

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

        btn_hello.setOnClickListener(v -> HelloDialog.this.dismiss());
    }
}
