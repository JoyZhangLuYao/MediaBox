package com.zly.zly.mediabox.Ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.anye.greendao.gen.FileInfoDao;
import com.anye.greendao.gen.MusicFileInfoDao;
import com.anye.greendao.gen.PhotoFileInfoDao;
import com.orhanobut.logger.Logger;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.zly.zly.mediabox.Adapter.DragListAdapter;
import com.zly.zly.mediabox.MyLibs.IBluetooth;
import com.zly.zly.mediabox.MyLibs.MyApplication;
import com.zly.zly.mediabox.MyView.DragListView;
import com.zly.zly.mediabox.MyView.MyToast;
import com.zly.zly.mediabox.R;
import com.zly.zly.mediabox.Utils.BufChangeHex;
import com.zly.zly.mediabox.Utils.Util;
import com.zly.zly.mediabox.bean.CollectFileInfo;
import com.zly.zly.mediabox.bean.CollectMusicFileInfo;
import com.zly.zly.mediabox.bean.CollectPhotoFileInfo;
import com.zly.zly.mediabox.bean.FileInfo;
import com.zly.zly.mediabox.bean.MusicFileInfo;
import com.zly.zly.mediabox.bean.PhotoFileInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LoveActivity extends Activity implements View.OnClickListener {
    TextView edit;
    ImageView back;
    ListView loveList;
    Button preBtn, playBtn, pauseBtn, nextBtn, volBtn;
    DragListView dragListView;
    DragListAdapter dragListAdapter;
    MyApplication myApplication;

    private List<FileInfo> mData = new ArrayList<>();
    private SeekBar seekBar;
    private byte[] head, end;
    private IBluetooth iBluetooth;
    List<FileInfo> mDataOld;
    MyLoveBroadcastReceiver receiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        head = new byte[2];
        head[0] = (byte) 0x55;
        head[1] = (byte) 0xaa;
        end = new byte[2];
        end[0] = (byte) 0x44;
        end[1] = (byte) 0xbb;
        setContentView(R.layout.activity_love);
        setTranslucentStatus();
        receiver = new MyLoveBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("VOL");
        intentFilter.addAction("STATE");
        intentFilter.addAction("PLAY");
        intentFilter.addAction("DISCONNECTED");
        intentFilter.addAction("CHANGEDEVICE");
        intentFilter.addAction("COLLECT");
        registerReceiver(receiver, intentFilter);


        myApplication = (MyApplication) getApplication();
        iBluetooth = myApplication.getIBluetooth();
        edit = (TextView) findViewById(R.id.edit);
        back = (ImageView) findViewById(R.id.back);
        loveList = (ListView) findViewById(R.id.love_list);
        preBtn = (Button) findViewById(R.id.pre_btn);
        playBtn = (Button) findViewById(R.id.play_love_btn);
        pauseBtn = (Button) findViewById(R.id.pause_love_btn);
        nextBtn = (Button) findViewById(R.id.next_btn);
        volBtn = (Button) findViewById(R.id.vol_btn);
        dragListView = (DragListView) findViewById(R.id.love_list);
        preBtn.setOnClickListener(this);
        playBtn.setOnClickListener(this);
        pauseBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        volBtn.setOnClickListener(this);
        initData();
        dragListAdapter = new DragListAdapter(this, mData, myApplication.getCurrentMain(), myApplication);
        dragListView.setAdapter(dragListAdapter);


        dragListView.setDragEndListener(new DragListView.DragEndListener() {

            @Override
            public void onDragEnd(List<FileInfo> list) {
                Logger.d(list.toString());
                dragListAdapter.setmDataList(list);

            }
        });
        dragListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                List<FileInfo> datas = dragListAdapter.getmDataList();
                dragListAdapter.upDateItem(new FileInfo(datas.get(position).getName(), datas.get(position).getFileNumber(), datas.get(position).getType(), true, datas.get(position).getLove(), datas.get(position).getBuf()));
                dragListAdapter.notifyDataSetChanged();
                byte[] cmd0 = datas.get(position).getBuf();
                cmd0[2] = DeviceActivity.intToByteArray1(0xB0)[0];
                byte[] cmd = new byte[7];
                System.arraycopy(cmd0, 0, cmd, 0, 6);
                cmd[6] = 1;
                Intent intent = new Intent("QQ");
                intent.putExtra("MyData", BufChangeHex.concat(cmd, end));
                sendBroadcast(intent);
                playBtn.setVisibility(View.GONE);
                pauseBtn.setVisibility(View.VISIBLE);

            }
        });
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edit.getText().equals(Util.getString(LoveActivity.this,R.string.edit))) {
                    dragListAdapter.setEdit(true);
                    dragListAdapter.notifyDataSetChanged();
                    edit.setText(Util.getString(LoveActivity.this,R.string.complete));
                    mDataOld = dragListAdapter.getFinalList();
                    sendBroadcast(new Intent("i"));

                } else {
                    dragListAdapter.setEdit(false);
                    dragListAdapter.notifyDataSetChanged();
                    edit.setText(Util.getString(LoveActivity.this,R.string.edit));
                    List<FileInfo> mData0 = dragListAdapter.getFinalList();
                    byte tot = 0;
                    byte type = 0;

                    if (myApplication.getCurrentMain() == 1) {

                        myApplication.getCollectFileInfoDao().deleteAll();

                        for (FileInfo fileInfo : mData0) {
                            CollectFileInfo info = new CollectFileInfo();
                            info.setName(fileInfo.getName());
                            info.setFileNumber(fileInfo.getFileNumber());
                            info.setType(fileInfo.getType());
                            info.setClickMark(fileInfo.getClickMark());
                            info.setLove(fileInfo.getLove());
                            info.setBuf(fileInfo.getBuf());
                            // myApplication.getCollectFileInfoDao().insert(new CollectFileInfo(fileInfo.getName(),fileInfo.getFileNumber(),fileInfo.getType(),fileInfo.getClickMark(),fileInfo.getLove(),fileInfo.getBuf()));
                            myApplication.getCollectFileInfoDao().insert(info);

                        }
                        tot = (byte) myApplication.getCollectFileInfoDao().loadAll().size();
                        type = 0x02;


                    } else if (myApplication.getCurrentMain() == 2) {
                        myApplication.getCollectMusicFileInfoDao().deleteAll();
                        for (FileInfo fileInfo : mData0) {
                            CollectMusicFileInfo info = new CollectMusicFileInfo();
                            info.setName(fileInfo.getName());
                            info.setFileNumber(fileInfo.getFileNumber());
                            info.setType(fileInfo.getType());
                            info.setClickMark(fileInfo.getClickMark());
                            info.setLove(fileInfo.getLove());
                            info.setBuf(fileInfo.getBuf());
                            //  myApplication.getCollectMusicFileInfoDao().insert(new CollectMusicFileInfo(fileInfo.getName(),fileInfo.getFileNumber(),fileInfo.getType(),fileInfo.getClickMark(),fileInfo.getLove(),fileInfo.getBuf()));
                            myApplication.getCollectMusicFileInfoDao().insert(info);

                        }
                        tot = (byte) myApplication.getCollectMusicFileInfoDao().loadAll().size();
                        type = 0x01;

                    } else if (myApplication.getCurrentMain() == 3) {
                        myApplication.getCollectPhotoFileInfoDao().deleteAll();
                        for (FileInfo fileInfo : mData0) {
                            CollectPhotoFileInfo info = new CollectPhotoFileInfo();
                            info.setName(fileInfo.getName());
                            info.setFileNumber(fileInfo.getFileNumber());
                            info.setType(fileInfo.getType());
                            info.setClickMark(fileInfo.getClickMark());
                            info.setLove(fileInfo.getLove());
                            info.setBuf(fileInfo.getBuf());
                            //  myApplication.getCollectPhotoFileInfoDao().insert(new CollectPhotoFileInfo(fileInfo.getName(),fileInfo.getFileNumber(),fileInfo.getType(),fileInfo.getClickMark(),fileInfo.getLove(),fileInfo.getBuf()));
                            myApplication.getCollectPhotoFileInfoDao().insert(info);
                        }
                        tot = (byte) myApplication.getCollectPhotoFileInfoDao().loadAll().size();
                        type = 0x03;

                    }
                    List<CollectFileInfo> listV = myApplication.getCollectFileInfoDao().loadAll();
                    myApplication.setListV(listV);
                    List<CollectMusicFileInfo> listM = myApplication.getCollectMusicFileInfoDao().loadAll();
                    myApplication.setListM(listM);
                    List<CollectPhotoFileInfo> listP = myApplication.getCollectPhotoFileInfoDao().loadAll();
                    myApplication.setListP(listP);

                    byte[] cmd = new byte[9];
                    cmd[0] = (byte) 0x55;
                    cmd[1] = (byte) 0xaa;
                    cmd[2] = (byte) 0xf0;
                    cmd[3] = (byte) 0x02;
                    cmd[4] = (byte) 0x01;
                    cmd[5] = type;
                    cmd[6] = tot;
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
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


    }


    public void initData() {
        // 数据结果
        // mData = new ArrayList<FileInfo>();
        if ((myApplication.getCurrentMain() == 1)) {
            List<CollectFileInfo> mData0 = myApplication.getCollectFileInfoDao().loadAll();
            if (mData0 == null) return;
            for (CollectFileInfo collectFileInfo : mData0) {
                mData.add(new FileInfo(collectFileInfo.getName(), collectFileInfo.getFileNumber(), collectFileInfo.getType(), collectFileInfo.getClickMark(), collectFileInfo.getLove(), collectFileInfo.getBuf()));
            }
            // mData = myApplication.getCollectVideoList();
            Logger.d("收藏的列表---" + mData.toString());
        } else if (myApplication.getCurrentMain() == 2) {

            List<CollectMusicFileInfo> mData0 = myApplication.getCollectMusicFileInfoDao().loadAll();
            if (mData0 == null) return;
            for (CollectMusicFileInfo musicFileInfo : mData0) {
                mData.add(new FileInfo(musicFileInfo.getName(), musicFileInfo.getFileNumber(), musicFileInfo.getType(), musicFileInfo.getClickMark(), musicFileInfo.getLove(), musicFileInfo.getBuf()));
            }

            // mData = myApplication.getCollectMusicList();
        } else if (myApplication.getCurrentMain() == 3) {
            List<CollectPhotoFileInfo> mData0 = myApplication.getCollectPhotoFileInfoDao().loadAll();
            if (mData0 == null) return;
            for (CollectPhotoFileInfo collectPhotoFileInfo : mData0) {
                mData.add(new FileInfo(collectPhotoFileInfo.getName(), collectPhotoFileInfo.getFileNumber(), collectPhotoFileInfo.getType(), collectPhotoFileInfo.getClickMark(), collectPhotoFileInfo.getLove(), collectPhotoFileInfo.getBuf()));
            }

            // mData = myApplication.getCollectPhotoList();
        }

    }

    private void showVolWindow(View view) {

        // 一个自定义的布局，作为显示的内容
        View contentView = LayoutInflater.from(this).inflate(
                R.layout.vol_layout, null);
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
               // Toast.makeText(LoveActivity.this, "当前值" + seekBar.getProgress() + "", Toast.LENGTH_SHORT).show();
                MyToast.makeToast(LoveActivity.this,-1,Util.getString(LoveActivity.this,R.string.the_current_value)+seekBar.getProgress(),800);
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
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupWidth = view.getMeasuredWidth();    //  获取测量后的宽度
        int popupHeight = view.getMeasuredHeight();  //获取测量后的高度
        int[] location = new int[2];

        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.mipmap.vol_bg));
        view.getLocationOnScreen(location);

        // 设置好参数之后再show
        //    popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, (location[0] + view.getWidth() / 2) - popupWidth / 2 , location[1] - popupHeight*2);
        popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, 0, location[1] - popupHeight * 2);

    }

    private void sendCmd(int cmd1, int cmd2) {
        byte[] cmd = new byte[6];
        System.arraycopy(head, 0, cmd, 0, head.length);
        System.arraycopy(BufChangeHex.intToByteArray1(cmd1), 0, cmd, head.length, BufChangeHex.intToByteArray1(cmd1).length);
        System.arraycopy(BufChangeHex.intToByteArray1(cmd2), 0, cmd, head.length + 1, BufChangeHex.intToByteArray1(cmd2).length);
        System.arraycopy(end, 0, cmd, head.length + 2, end.length);
        iBluetooth.sendMyData(cmd);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.pre_btn:
                sendCmd(0xA0, 0x08);
                break;
            case R.id.play_love_btn:
                sendCmd(0xA0, 0x0a);
                playBtn.setVisibility(View.GONE);
                pauseBtn.setVisibility(View.VISIBLE);
                break;
            case R.id.pause_love_btn:
                sendCmd(0xA0, 0x0b);
                playBtn.setVisibility(View.VISIBLE);
                pauseBtn.setVisibility(View.GONE);
                break;
            case R.id.next_btn:
                sendCmd(0xA0, 0x09);
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

    class MyLoveBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("STATE")) {
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
                List<FileInfo> datas = dragListAdapter.getmDataList();
                for (int i=0;i<datas.size();i++) {
                    FileInfo data=datas.get(i);
                    if (data.getFileNumber().intValue() == intent.getIntExtra("COLLECT", -1)) {
                        dragListAdapter.upDateItem(new FileInfo(data.getName(), data.getFileNumber(), data.getType(), true, data.getLove(), data.getBuf()));
                        dragListAdapter.notifyDataSetChanged();
                    }
                }

            }
        }
    }
}
