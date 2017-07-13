package com.zly.zly.mediabox.MyView;

import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zly.zly.mediabox.R;

/**
 * Created by ZhangLuyao on 2017/5/25.
 */

public class MyToast {
    private static Toast toast;

    public static void makeToast(Context context, int img, String s, int time){
        toast=Toast.makeText(context,"",Toast.LENGTH_SHORT);
        LayoutInflater inflater=LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.my_toast, null);
        view.setBackgroundResource(R.drawable.shape_corner);
        if(img!=-1){((ImageView)view.findViewById(R.id.img)).setImageResource(img);}else {
            ((ImageView)view.findViewById(R.id.img)).setVisibility(View.GONE);
        }
        ((TextView)view.findViewById(R.id.text)).setText(s);
        toast.setGravity(Gravity.CENTER, 0, 300);
        toast.setView(view);
        toast.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        },time);
    }
}
