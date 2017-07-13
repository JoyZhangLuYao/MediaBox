package com.zly.zly.mediabox.Ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.bdsdk.update.BaiDuAutoUpdatePopupwindow;
import com.zly.zly.mediabox.MyLibs.MyApplication;
import com.zly.zly.mediabox.R;

public class WelcomeActivity extends Activity {

    private MyApplication myApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);



        new Handler (){}.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(WelcomeActivity.this,DeviceActivity.class));
                overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);

                finish();

            }
        },2000);
    }
}
