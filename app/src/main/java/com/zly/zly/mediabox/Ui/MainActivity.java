package com.zly.zly.mediabox.Ui;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.zly.zly.mediabox.Adapter.MainAdapter;
import com.zly.zly.mediabox.Fragment.MusicFragment;
import com.zly.zly.mediabox.Fragment.PhotoFragment;
import com.zly.zly.mediabox.Fragment.LocalFragment;
import com.zly.zly.mediabox.Fragment.VideoFragment;
import com.zly.zly.mediabox.MyLibs.IBluetooth;
import com.zly.zly.mediabox.MyLibs.MyApplication;
import com.zly.zly.mediabox.MyView.MyToast;
import com.zly.zly.mediabox.R;
import com.zly.zly.mediabox.Utils.BufChangeHex;
import com.zly.zly.mediabox.Utils.Util;
import com.zly.zly.mediabox.bean.CollectFileInfo;
import com.zly.zly.mediabox.bean.CollectMusicFileInfo;
import com.zly.zly.mediabox.bean.CollectPhotoFileInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZhangLuyao on 2017/3/28.
 */

public class MainActivity extends FragmentActivity implements View.OnClickListener, View.OnLongClickListener {

   /* static final String[] PERMISSION = new String[]{
            Manifest.permission.READ_CONTACTS,// 写入权限
            Manifest.permission.READ_EXTERNAL_STORAGE,  //读取权限
            Manifest.permission.WRITE_CALL_LOG,        //读取设备信息
    };*/


    Context context;
    MyApplication myApplication;

    TextView localText;
    ViewPager viewPager;
    RadioGroup radioGroup, titRadioGroup;
    RadioButton one, two, three, four;
    MainAdapter mainAdapter;
    ImageView love, remote, battery;
    Button preBtn, playBtn, pauseBtn, nextBtn, volBtn;
    LinearLayout controlLayout;
    RelativeLayout volRl;
    List<Fragment> lists = new ArrayList<>();

    MusicFragment musicFragment;
    PhotoFragment photoFragment;
    LocalFragment localFragment;
    VideoFragment videoFragment;
    private SeekBar seekBar;
    private IBluetooth iBluetooth;


    byte[] head, end;
    private MyBroadcastReceiver myBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTranslucentStatus();
        head = new byte[2];
        head[0] = (byte) 0x55;
        head[1] = (byte) 0xaa;
        end = new byte[2];
        end[0] = (byte) 0x44;
        end[1] = (byte) 0xbb;

        myBroadcastReceiver = new MyBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("QQ");
        intentFilter.addAction("VOL");
        intentFilter.addAction("STATE");
        intentFilter.addAction("PLAY");
        intentFilter.addAction("DISCONNECTED");
        intentFilter.addAction("CHANGEDEVICE");
        intentFilter.addAction("COLLECT");
        intentFilter.addAction("UNCOLLECT");
        intentFilter.addAction("V");
        intentFilter.addAction("M");
        intentFilter.addAction("P");
        registerReceiver(myBroadcastReceiver, intentFilter);
        /**
         * 设置Android6.0的权限申请
         */
        //  setPermissions();


        myApplication = (MyApplication) getApplication();
        context = myApplication.getContxt();
        iBluetooth = myApplication.getIBluetooth();
        initRoot();

        if (myApplication.getuAndTf() == 0) {
            battery.setImageResource(R.mipmap.img_u);
        } else if (myApplication.getuAndTf() == 1) {
            battery.setImageResource(R.mipmap.img_tf);
        }
        battery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSwitchTfAndUWindow(v);
            }
        });

        if (myApplication.getCollectFileInfoDao().loadAll().size() != 0) {

            byte[] cmd = new byte[9];
            cmd[0] = (byte) 0x55;
            cmd[1] = (byte) 0xaa;
            cmd[2] = (byte) 0xf0;
            cmd[3] = (byte) 0x02;
            cmd[4] = (byte) 0x01;
            cmd[5] = (byte) 0x02;
            cmd[6] = (byte) myApplication.getCollectFileInfoDao().loadAll().size();
            cmd[7] = (byte) 0x44;
            cmd[8] = (byte) 0xbb;
            final Intent intent = new Intent("QQ");
            intent.putExtra("MyData", cmd);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    sendBroadcast(intent);
                }
            }, 50);

        }
        if(myApplication.getCollectMusicFileInfoDao().loadAll().size()!=0&&myApplication.getCollectFileInfoDao().loadAll().size()==0){

            myApplication.setSendRootCollectListMark(2);

            byte[] cmd11 = new byte[9];
            cmd11[0] = (byte) 0x55;
            cmd11[1] = (byte) 0xaa;
            cmd11[2] = (byte) 0xf0;
            cmd11[3] = (byte) 0x02;
            cmd11[4] = (byte) 0x01;
            cmd11[5] = (byte) 0x01;
            cmd11[6] = (byte) myApplication.getCollectMusicFileInfoDao().loadAll().size();
            cmd11[7] = (byte) 0x44;
            cmd11[8] = (byte) 0xbb;
            final Intent intent11 = new Intent("QQ");
            intent11.putExtra("MyData", cmd11);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    sendBroadcast(intent11);
                }
            }, 50);
        }
    }

    @Override
    protected void onResume() {

        super.onResume();
        sendBroadcast(new Intent("notify"));

        sendCmd(0xA0, 0xB2);
    }

    private void initRoot() {
        musicFragment = new MusicFragment();
        photoFragment = new PhotoFragment();
        localFragment = new LocalFragment();
        videoFragment = new VideoFragment();


        lists.add(videoFragment);
        lists.add(musicFragment);
        lists.add(photoFragment);
        lists.add(localFragment);


        mainAdapter = new MainAdapter(getSupportFragmentManager(), lists);

        localText = (TextView) findViewById(R.id.local_text);
        viewPager = (ViewPager) findViewById(R.id.main_vp);
        radioGroup = (RadioGroup) findViewById(R.id.main_rg);
        controlLayout = (LinearLayout) findViewById(R.id.control_layout);
        volRl = (RelativeLayout) findViewById(R.id.rl_vol);
        titRadioGroup = (RadioGroup) findViewById(R.id.tit_radiogroup);
        preBtn = (Button) findViewById(R.id.pre_btn);
        playBtn = (Button) findViewById(R.id.play_btn);
        pauseBtn = (Button) findViewById(R.id.pause_btn);
        nextBtn = (Button) findViewById(R.id.next_btn);
        volBtn = (Button) findViewById(R.id.vol_btn);
        love = (ImageView) findViewById(R.id.love);
        love.setTag("1");
        remote = (ImageView) findViewById(R.id.remote);
        battery = (ImageView) findViewById(R.id.battery);

        one = (RadioButton) findViewById(R.id.one);
        two = (RadioButton) findViewById(R.id.two);
        three = (RadioButton) findViewById(R.id.three);
        four = (RadioButton) findViewById(R.id.four);

        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(mainAdapter);
        switch (myApplication.getCurrentMain()) {
            case 1:
                localText.setText(Util.getString(this, R.string.video));
                radioGroup.check(R.id.one);
                viewPager.setCurrentItem(0);
                break;
            case 2:
                localText.setText(Util.getString(this, R.string.music));
                radioGroup.check(R.id.two);
                viewPager.setCurrentItem(1);
                break;
            case 3:
                localText.setText(Util.getString(this, R.string.photo));
                radioGroup.check(R.id.three);
                viewPager.setCurrentItem(2);
                break;
        }

        radioGroup.setOnCheckedChangeListener(MyGBListener);
        viewPager.setOnPageChangeListener(MyViewPagerListener);
        love.setOnClickListener(this);
        love.setOnLongClickListener(this);
        volBtn.setOnClickListener(this);
        preBtn.setOnClickListener(this);
        playBtn.setOnClickListener(this);
        pauseBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        remote.setOnClickListener(this);
    }

    private RadioGroup.OnCheckedChangeListener MyGBListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {


            switch (i) {
                case R.id.one:

                    localText.setText(Util.getString(MainActivity.this, R.string.video));
                    viewPager.setCurrentItem(0);
                    myApplication.setCurrentMain(1);
                    setControlVisibility(1);
                    setTitRadioGroupVisibility(0);
                    love.setVisibility(View.VISIBLE);
                    volRl.setVisibility(View.VISIBLE);
                    if (myApplication.getLoveMark() == 1) {
                        love.setImageResource(R.mipmap.love_play);
                        Log.d("dd", "259");
                        love.setTag("0");
                    } else {
                        love.setImageResource(R.mipmap.love_tit);
                        love.setTag("1");
                    }
                    break;
                case R.id.two:

                    localText.setText(Util.getString(MainActivity.this, R.string.music));
                    viewPager.setCurrentItem(1);
                    myApplication.setCurrentMain(2);
                    setControlVisibility(1);
                    setTitRadioGroupVisibility(0);
                    love.setVisibility(View.VISIBLE);
                    volRl.setVisibility(View.VISIBLE);
                    if (myApplication.getLoveMark() == 2) {
                        love.setImageResource(R.mipmap.love_play);
                        Log.d("dd", "277");
                        love.setTag("0");
                    } else {
                        love.setImageResource(R.mipmap.love_tit);
                        love.setTag("1");
                    }
                    break;
                case R.id.three:

                    localText.setText(Util.getString(MainActivity.this, R.string.photo));
                    viewPager.setCurrentItem(2);
                    myApplication.setCurrentMain(3);
                    setControlVisibility(1);
                    setTitRadioGroupVisibility(0);
                    love.setVisibility(View.GONE);
                    volRl.setVisibility(View.GONE);
                    break;
                case R.id.four:

                    localText.setText(Util.getString(MainActivity.this, R.string.local_music));
                    viewPager.setCurrentItem(3);
                    myApplication.setCurrentMain(4);
                    setControlVisibility(0);
                    setTitRadioGroupVisibility(0);
                    love.setVisibility(View.INVISIBLE);
                    volRl.setVisibility(View.VISIBLE);
                    break;
            }

        }
    };
    private ViewPager.OnPageChangeListener MyViewPagerListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.buingbuing);
            switch (position) {
                case 0:
                    one.startAnimation(animation);
                    radioGroup.check(R.id.one);
                    myApplication.setCurrentMain(1);
                    setControlVisibility(1);
                    setTitRadioGroupVisibility(0);
                    love.setVisibility(View.VISIBLE);
                    localText.setVisibility(View.VISIBLE);
                    break;
                case 1:
                    two.startAnimation(animation);
                    radioGroup.check(R.id.two);
                    myApplication.setCurrentMain(2);
                    setControlVisibility(1);
                    setTitRadioGroupVisibility(0);
                    love.setVisibility(View.VISIBLE);
                    localText.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    three.startAnimation(animation);
                    radioGroup.check(R.id.three);
                    myApplication.setCurrentMain(3);
                    setControlVisibility(1);
                    setTitRadioGroupVisibility(0);
                    love.setVisibility(View.VISIBLE);
                    localText.setVisibility(View.VISIBLE);
                    break;
                case 3:
                    four.startAnimation(animation);
                    radioGroup.check(R.id.four);
                    myApplication.setCurrentMain(4);
                    setControlVisibility(0);
                    setTitRadioGroupVisibility(0);
                    love.setVisibility(View.INVISIBLE);
                    localText.setVisibility(View.VISIBLE);

                    break;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };


    private void setControlVisibility(int v) {
        if (v == 0) {
            controlLayout.setVisibility(View.GONE);
        } else if (v == 1) {
            controlLayout.setVisibility(View.VISIBLE);
        }

    }

    private void setTitRadioGroupVisibility(int v) {
        if (v == 0) {
            titRadioGroup.setVisibility(View.GONE);
        } else if (v == 1) {
            titRadioGroup.setVisibility(View.VISIBLE);
        }

    }

    /*private void setPermissions() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            //Android 6.0申请权限
            ActivityCompat.requestPermissions(this, PERMISSION, 1);
        } else {
            Logger.d("权限申请ok");
        }
    }*/

    private void showVolWindow(View view) {

        // 一个自定义的布局，作为显示的内容
        View contentView = LayoutInflater.from(this).inflate(
                R.layout.vol_layout, null);
        int w = View.MeasureSpec.UNSPECIFIED;
        int h = View.MeasureSpec.UNSPECIFIED;
        //  contentView.measure(w, h);
        // 设置按钮的点击事件
        seekBar = (SeekBar) contentView.findViewById(R.id.seekBar);
        seekBar.setMax(30);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //拖动后处理事件
                MyToast.makeToast(MainActivity.this, -1, Util.getString(MainActivity.this, R.string.the_current_value) + seekBar.getProgress(), 800);
                sendCmd2(0xA0, 0x21, seekBar.getProgress());

            }
        });


        final PopupWindow popupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setTouchable(true);
        popupWindow.setAnimationStyle(R.style.AnimationPreview);

        popupWindow.setTouchInterceptor(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                Log.i("mengdd", "onTouch : ");

                return false;
                // 这里如果返回true的话，touch事件将被拦截
                // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
            }
        });

        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        // 我觉得这里是API的一个bug

        int popupHeight = view.getMeasuredHeight();  //获取测量后的高度
        int[] location = new int[2];

        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.mipmap.vol_bg));
        view.getLocationOnScreen(location);

        // 设置好参数之后再show
        //    popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, (location[0] + view.getWidth() / 2) - popupWidth / 2 , location[1] - popupHeight*2);
        popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, 0, location[1] - popupHeight * 3);

    }
    private void showSwitchTfAndUWindow(View view) {
        // 一个自定义的布局，作为显示的内容
        View contentView = LayoutInflater.from(this).inflate(
                R.layout.switchtfanduwindow_layout, null);
        final PopupWindow popupWindow = new PopupWindow(contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);


        TextView tf_u = (TextView) contentView.findViewById(R.id.tf_u);
        if (myApplication.getuAndTf() == 0) {
            tf_u.setText(getResources().getString(R.string.switch_tf));
        } else if (myApplication.getuAndTf() == 1) {
            tf_u.setText(getResources().getString(R.string.switch_u));
        }


        tf_u.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myApplication.getuAndTf() == 0) {

                    sendCmd2(0xF0, 0x01, 0x02);

                } else if (myApplication.getuAndTf() == 1) {

                    sendCmd2(0xF0, 0x01, 0x03);

                }
                popupWindow.dismiss();
               // MyToast.makeToast(MainActivity.this,-1,Util.getString(MainActivity.this,R.string.switch_device),900);
            }
        });


        popupWindow.setTouchable(true);
        popupWindow.setAnimationStyle(R.style.AnimationPreview);

        popupWindow.setTouchInterceptor(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return false;
                // 这里如果返回true的话，touch事件将被拦截
                // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
            }
        });

        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框

        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.mipmap.switch_u_tf));

        // 设置好参数之后再show
        popupWindow.showAsDropDown(view);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.love:

                if (view.getTag().equals("0")) {
                    love.setImageResource(R.mipmap.love_tit);
                    view.setTag("1");
                    myApplication.setLoveMark(-1);
                    switch (myApplication.getCurrentMain()) {
                        case 1:
                            sendCmd2(0xE0, 2, 0);
                            break;
                        case 2:
                            sendCmd2(0xE0, 1, 0);
                            break;
                        /*case 3:
                            sendCmd2(0xE0, 3, 0);
                            break;*/
                    }

                } else if (view.getTag().equals("1")) {


                    switch (myApplication.getCurrentMain()) {
                        case 1:
                            if (myApplication.getCollectFileInfoDao().loadAll().size() == 0) {
                                MyToast.makeToast(MainActivity.this, -1, Util.getString(MainActivity.this, R.string.no_collection), 1000);
                            } else {
                                sendCmd2(0xE0, 2, 1);
                                myApplication.setLoveMark(1);
                                love.setImageResource(R.mipmap.love_play);
                                Log.d("dd", "490");
                                view.setTag("0");
                            }
                            break;
                        case 2:
                            if (myApplication.getCollectMusicFileInfoDao().loadAll().size() == 0) {
                                MyToast.makeToast(MainActivity.this, -1, Util.getString(MainActivity.this, R.string.no_collection), 1000);
                            } else {
                                sendCmd2(0xE0, 1, 1);
                                myApplication.setLoveMark(2);
                                love.setImageResource(R.mipmap.love_play);
                                Log.d("dd", "501");
                                view.setTag("0");
                            }
                            break;
                        /*case 3:
                            if(myApplication.getCollectPhotoFileInfoDao().loadAll().size()==0){
                                MyToast.makeToast(MainActivity.this,-1,"亲，还没有收藏哦~",1000);
                            }else {sendCmd2(0xE0, 3, 1);
                            myApplication.setLoveMark(3);
                            love.setImageResource(R.mipmap.love_play);
                            view.setTag("0");}
                            break;*/
                    }

                }
                break;
            case R.id.vol_btn:
                showVolWindow(view);
                sendCmd(0xA0, 0xB4);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                sendCmd(0xA0, 0xB4);


                break;
            case R.id.pre_btn:
                sendCmd(0xA0, 0x08);
                break;
            case R.id.next_btn:
                sendCmd(0xA0, 0x09);
                break;
            case R.id.play_btn:
                sendCmd(0xA0, 0x0A);
                playBtn.setVisibility(View.GONE);
                pauseBtn.setVisibility(View.VISIBLE);
                break;
            case R.id.pause_btn:
                sendCmd(0xA0, 0x0B);
                playBtn.setVisibility(View.VISIBLE);
                pauseBtn.setVisibility(View.GONE);
                break;
            case R.id.remote:
                startActivity(new Intent(MainActivity.this, RemoteActivity.class));
                break;

        }
    }

    @Override
    public boolean onLongClick(View view) {
        ((Vibrator) getSystemService(Service.VIBRATOR_SERVICE)).vibrate(100);
        startActivity(new Intent(MainActivity.this, LoveActivity.class));

        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            myApplication.setSearchMark(true);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(myBroadcastReceiver);
        myApplication.setSendCollectListMark(true);

        super.onDestroy();
    }

    private void sendCmd(int cmd1, int cmd2) {
        byte[] cmd = new byte[6];
        System.arraycopy(head, 0, cmd, 0, head.length);
        System.arraycopy(BufChangeHex.intToByteArray1(cmd1), 0, cmd, head.length, BufChangeHex.intToByteArray1(cmd1).length);
        System.arraycopy(BufChangeHex.intToByteArray1(cmd2), 0, cmd, head.length + 1, BufChangeHex.intToByteArray1(cmd2).length);
        System.arraycopy(end, 0, cmd, head.length + 2, end.length);
        iBluetooth.sendMyData(cmd);
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

    private void sendCmd1(int cmd1) {
        byte[] cmd = new byte[5];
        System.arraycopy(head, 0, cmd, 0, head.length);
        System.arraycopy(BufChangeHex.intToByteArray1(cmd1), 0, cmd, head.length, BufChangeHex.intToByteArray1(cmd1).length);
        System.arraycopy(end, 0, cmd, head.length + 1, end.length);
        iBluetooth.sendMyData(cmd);
    }


    class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("QQ")) {
                iBluetooth.sendMyData(intent.getByteArrayExtra("MyData"));
            } else if (intent.getAction().equals("STATE")) {
                int state = intent.getIntExtra("STATE", -1);
                if (state == 0) {
                    playBtn.setVisibility(View.GONE);
                    pauseBtn.setVisibility(View.VISIBLE);
                } else if (state == 1) {
                    playBtn.setVisibility(View.VISIBLE);
                    pauseBtn.setVisibility(View.GONE);
                }

            } else if (intent.getAction().equals("VOL")) {
                if (seekBar != null) {
                    seekBar.setProgress(intent.getIntExtra("VOL", 0));
                }
            } else if (intent.getAction().equals("PLAY")) {
                playBtn.setVisibility(View.GONE);
                pauseBtn.setVisibility(View.VISIBLE);
            } else if (intent.getAction().equals("DISCONNECTED")) {
                finish();
            } else if (intent.getAction().equals("CHANGEDEVICE")) {
                myApplication.setSearchMark(true);
                finish();
            } else if (intent.getAction().equals("COLLECT")) {
                //  love.setImageResource(R.mipmap.love_play);
            } else if (intent.getAction().equals("UNCOLLECT")) {
                love.setImageResource(R.mipmap.love_tit);
                love.setTag("1");
                myApplication.setLoveMark(-1);
            } else if (intent.getAction().equals("V")) {
                localText.setText(Util.getString(MainActivity.this, R.string.video));
                radioGroup.check(R.id.one);
                viewPager.setCurrentItem(0);
            } else if (intent.getAction().equals("M")) {
                localText.setText(Util.getString(MainActivity.this, R.string.music));
                radioGroup.check(R.id.two);
                viewPager.setCurrentItem(1);

            } else if (intent.getAction().equals("P")) {
                localText.setText(Util.getString(MainActivity.this, R.string.photo));
                radioGroup.check(R.id.three);
                viewPager.setCurrentItem(2);

            }


        }
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
