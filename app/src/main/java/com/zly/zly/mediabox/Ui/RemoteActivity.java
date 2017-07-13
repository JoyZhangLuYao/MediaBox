package com.zly.zly.mediabox.Ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.zly.zly.mediabox.MyLibs.IBluetooth;
import com.zly.zly.mediabox.MyLibs.MyApplication;
import com.zly.zly.mediabox.R;
import com.zly.zly.mediabox.Utils.BufChangeHex;

public class RemoteActivity extends Activity implements View.OnTouchListener {

    private byte[] head, end;
    private MyApplication myApplication;
    private IBluetooth iBluetooth;

    Button rock, jazz, fashion, classics, normal;
    ImageView back;

    //震动对象
    Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);
        setTranslucentStatus();
        vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);

        head = new byte[2];
        head[0] = (byte) 0x55;
        head[1] = (byte) 0xaa;
        end = new byte[2];
        end[0] = (byte) 0x44;
        end[1] = (byte) 0xbb;

        myApplication = (MyApplication) getApplication();
        iBluetooth = myApplication.getIBluetooth();

        initView();
    }

    private void initView() {
        rock = (Button) findViewById(R.id.rock);
        jazz = (Button) findViewById(R.id.jazz);
        fashion = (Button) findViewById(R.id.fashion);
        classics = (Button) findViewById(R.id.classics);
        normal = (Button) findViewById(R.id.normal);
        back = (ImageView) findViewById(R.id.back);
        rock.setOnTouchListener(this);
        jazz.setOnTouchListener(this);
        fashion.setOnTouchListener(this);
        classics.setOnTouchListener(this);
        normal.setOnTouchListener(this);
        back.setOnTouchListener(this);

    }


    private void sendCmd2(int cmd1, int cmd2, int cmd3) {
        byte[] cmd = new byte[7];
        System.arraycopy(head, 0, cmd, 0, head.length);
        System.arraycopy(BufChangeHex.intToByteArray1(cmd1), 0, cmd, head.length, BufChangeHex.intToByteArray1(cmd1).length);
        System.arraycopy(BufChangeHex.intToByteArray1(cmd2), 0, cmd, head.length + 1, BufChangeHex.intToByteArray1(cmd2).length);
        cmd[4] = (byte) cmd3;
        System.arraycopy(end, 0, cmd, head.length + 3, end.length);
        iBluetooth.sendMyData(cmd);
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                switch (view.getId()) {
                    case R.id.rock:
                        vibrator.vibrate(150);
                        sendCmd2(0xA0, 0xB3, 2);
                        rock.setBackgroundResource(R.mipmap.remote_on);
                        jazz.setBackgroundResource(R.mipmap.remote_off);
                        fashion.setBackgroundResource(R.mipmap.remote_off);
                        classics.setBackgroundResource(R.mipmap.remote_off);
                        normal.setBackgroundResource(R.mipmap.remote_off);


                        break;
                    case R.id.jazz:
                        vibrator.vibrate(150);
                        sendCmd2(0xA0, 0xB3, 4);
                        rock.setBackgroundResource(R.mipmap.remote_off);
                        jazz.setBackgroundResource(R.mipmap.remote_on);
                        fashion.setBackgroundResource(R.mipmap.remote_off);
                        classics.setBackgroundResource(R.mipmap.remote_off);
                        normal.setBackgroundResource(R.mipmap.remote_off);


                        break;
                    case R.id.fashion:
                        vibrator.vibrate(150);
                        sendCmd2(0xA0, 0xB3, 5);
                        rock.setBackgroundResource(R.mipmap.remote_off);
                        jazz.setBackgroundResource(R.mipmap.remote_off);
                        fashion.setBackgroundResource(R.mipmap.remote_on);
                        classics.setBackgroundResource(R.mipmap.remote_off);
                        normal.setBackgroundResource(R.mipmap.remote_off);


                        break;
                    case R.id.classics:
                        vibrator.vibrate(150);
                        sendCmd2(0xA0, 0xB3, 3);
                        rock.setBackgroundResource(R.mipmap.remote_off);
                        jazz.setBackgroundResource(R.mipmap.remote_off);
                        fashion.setBackgroundResource(R.mipmap.remote_off);
                        classics.setBackgroundResource(R.mipmap.remote_on);
                        normal.setBackgroundResource(R.mipmap.remote_off);


                        break;
                    case R.id.normal:
                        vibrator.vibrate(150);
                        sendCmd2(0xA0, 0xB3, 1);
                        rock.setBackgroundResource(R.mipmap.remote_off);
                        jazz.setBackgroundResource(R.mipmap.remote_off);
                        fashion.setBackgroundResource(R.mipmap.remote_off);
                        classics.setBackgroundResource(R.mipmap.remote_off);
                        normal.setBackgroundResource(R.mipmap.remote_on);


                        break;

                }

                break;
            case MotionEvent.ACTION_UP:
                switch (view.getId()) {
                   /* case R.id.rock:
                        rock.setBackgroundResource(R.mipmap.remote_off);

                        break;
                    case R.id.jazz:
                        jazz.setBackgroundResource(R.mipmap.remote_off);

                        break;
                    case R.id.fashion:
                        fashion.setBackgroundResource(R.mipmap.remote_off);

                        break;
                    case R.id.classics:
                        classics.setBackgroundResource(R.mipmap.remote_off);

                        break;
                    case R.id.normal:
                        normal.setBackgroundResource(R.mipmap.remote_off);

                        break;*/
                    case R.id.back:
                        finish();

                        break;
                }
                break;
        }


        return true;
    }

    /**
     * 为xml 的根布局添加android:fitsSystemWindows=”true” 属性<br/>
     */
    protected void setTranslucentStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
        }
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(R.color.mainclior);// 通知栏所需颜色
    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }
}
