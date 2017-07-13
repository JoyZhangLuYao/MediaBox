package com.zly.zly.mediabox.Fragment;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.zly.zly.mediabox.Adapter.PhoneMusicAdapter;
import com.zly.zly.mediabox.MyLibs.IBluetooth;
import com.zly.zly.mediabox.MyLibs.MyApplication;
import com.zly.zly.mediabox.MyView.ClearEditText;
import com.zly.zly.mediabox.MyView.MyToast;
import com.zly.zly.mediabox.R;
import com.zly.zly.mediabox.Ui.LoveActivity;
import com.zly.zly.mediabox.Utils.BufChangeHex;
import com.zly.zly.mediabox.Utils.CharacterParser;
import com.zly.zly.mediabox.Utils.MusicLoader;
import com.zly.zly.mediabox.Utils.NatureService;
import com.zly.zly.mediabox.Utils.Util;

import java.util.ArrayList;
import java.util.List;

import static android.media.CamcorderProfile.get;

/**
 * A simple {@link Fragment} subclass.
 */
public class LocalFragment extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener, TextWatcher {
    ListView nativeList;
    Button preBtn;
    static Button playAndPauseBtn;
    Button nextBtn;
    Button volBtn;

    AudioManager audioManager;
    ComponentName mRemoteControlClientReceiverComponent;
    ClearEditText clearEditText;

    private List<MusicLoader.MusicInfo> musicList;

    static PhoneMusicAdapter adapter;
    static int currentMusic = -1;
    static int currentPosition;
    static NatureService.NatureBinder natureBinder;
    ProgressReceiver progressReceiver;
    static long time = System.currentTimeMillis();
    private CharacterParser characterParser;
    private MusicLoader musicLoader;
    private long curentId;
    private MyApplication myApplication;
    byte[] end, head;
    IBluetooth iBluetooth;
    private SeekBar seekBar;


    public LocalFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        registerMyReceiver();


        //获取音频服务
        audioManager = (AudioManager) getContext().getSystemService(getContext().AUDIO_SERVICE);
        //注册接收的Receiver

        mRemoteControlClientReceiverComponent = new ComponentName(
                getContext().getPackageName(), RemoteControlClientReceiver.class.getName());
        //注册MediaButton
        audioManager.registerMediaButtonEventReceiver(mRemoteControlClientReceiverComponent);
        //绑定服务
        Intent intent = new Intent(getContext(), NatureService.class);
        getContext().bindService(intent, serviceConnection, getContext().BIND_AUTO_CREATE);


        characterParser = CharacterParser.getInstance();

        myApplication = ((MyApplication) getActivity().getApplication());
        iBluetooth = myApplication.getIBluetooth();
        head = new byte[2];
        head[0] = (byte) 0x55;
        head[1] = (byte) 0xaa;
        end = new byte[2];
        end[0] = (byte) 0x44;
        end[1] = (byte) 0xbb;


        View view = inflater.inflate(R.layout.fragment_local, container, false);
        nativeList = (ListView) view.findViewById(R.id.native_music_listview);
        preBtn = (Button) view.findViewById(R.id.pre_btn);
        playAndPauseBtn = (Button) view.findViewById(R.id.play_pause_btn);
        nextBtn = (Button) view.findViewById(R.id.next_btn);
        volBtn = (Button) view.findViewById(R.id.vol_btn);
        clearEditText = (ClearEditText) view.findViewById(R.id.clearEditText1);
        clearEditText.addTextChangedListener(this);


        musicLoader = MusicLoader.instance(getContext().getContentResolver());

        musicList = musicLoader.getMusicList();
        adapter = new PhoneMusicAdapter(getContext(), musicList);
        nativeList.setAdapter(adapter);
        nativeList.setOnItemClickListener(this);
        preBtn.setOnClickListener(this);
        playAndPauseBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        volBtn.setOnClickListener(this);
        return view;
    }

    //点播歌曲
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        currentMusic = i;
        natureBinder.startPlay(currentMusic, 0);
        playAndPauseBtn.setBackgroundResource(R.mipmap.pause_on);
        adapter.setClickPosition(i);
        //  adapter.setClickId(clickId);
        adapter.notifyDataSetChanged();

        sendCmd(0xA0, 0x17);
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sendCmd(0xA0, 0x17);


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.pre_btn:
                if (currentMusic < 1) return;
                natureBinder.toPrevious();
                currentMusic--;
                adapter.setClickPosition(currentMusic);
                adapter.notifyDataSetChanged();
                break;
            case R.id.play_pause_btn:
                if (natureBinder.isPlaying()) {
                    natureBinder.stopPlay();
                    playAndPauseBtn.setBackgroundResource(R.mipmap.play_on);
                } else {
                    if (currentMusic == -1) {
                        currentMusic = 1;
                    }
                    natureBinder.startPlay(currentMusic, currentPosition);
                    playAndPauseBtn.setBackgroundResource(R.mipmap.pause_on);
                }
                break;
            case R.id.next_btn:
                if (currentMusic > musicList.size() - 2) return;
                natureBinder.toNext();
                currentMusic++;
                adapter.setClickPosition(currentMusic);
                adapter.notifyDataSetChanged();
                break;
            case R.id.vol_btn:
                sendCmd(0xA0, 0xB4);
                showVolWindow(view);


                break;
        }
    }

    private void showVolWindow(View view) {

        // 一个自定义的布局，作为显示的内容
        View contentView = LayoutInflater.from(getContext()).inflate(
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
                MyToast.makeToast(getContext(),-1,Util.getString(getContext(),R.string.the_current_value)+seekBar.getProgress(),800);
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
        popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, 0, location[1] - popupHeight * 2);

    }

    private void registerMyReceiver() {
        progressReceiver = new ProgressReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NatureService.ACTION_UPDATE_PROGRESS);
        intentFilter.addAction("VOL");
        getContext().registerReceiver(progressReceiver, intentFilter);
    }


    class ProgressReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (NatureService.ACTION_UPDATE_PROGRESS.equals(action)) {
                int progress = intent.getIntExtra(
                        NatureService.ACTION_UPDATE_PROGRESS, 0);
                if (progress > 0) {
                    currentPosition = progress; // Remember the current position
                    // pbDuration.setProgress(progress / 1000);
                }
            } else if (intent.getAction().equals("VOL")) {
                if (seekBar != null) {
                    seekBar.setProgress(intent.getIntExtra("VOL", 0));
                }
            }
        }
    }

    public static class RemoteControlClientReceiver extends BroadcastReceiver {

        public RemoteControlClientReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_MEDIA_BUTTON)) {
                KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                if (event == null)
                    return;

                if (event.getKeyCode() != KeyEvent.KEYCODE_HEADSETHOOK
                        && event.getKeyCode() != KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                        && event.getAction() != KeyEvent.ACTION_DOWN
                        && event.getAction() != KeyEvent.KEYCODE_MEDIA_PLAY
                        && event.getAction() != KeyEvent.KEYCODE_MEDIA_PAUSE)
                    return;

                switch (event.getKeyCode()) {
                    //播放AND暂停
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        Log.d("pppp","KEYCODE_MEDIA_PLAY_PAUSE");
                        if (System.currentTimeMillis() - time > 100) {
                            if (natureBinder.isPlaying()) {
                                natureBinder.stopPlay();
                                playAndPauseBtn.setBackgroundResource(R.mipmap.play_on);
                                //   Toast.makeText(context,"暂停",Toast.LENGTH_SHORT).show();
                            } else {

                                natureBinder.startPlay(currentMusic, currentPosition);
                                playAndPauseBtn.setBackgroundResource(R.mipmap.pause_on);
                                //   Toast.makeText(context,"播放",Toast.LENGTH_SHORT).show();
                            }
                            time = System.currentTimeMillis();
                        }
                        break;

                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                        Log.d("pppp","KEYCODE_MEDIA_PLAY");
                        natureBinder.startPlay(currentMusic, currentPosition);
                        playAndPauseBtn.setBackgroundResource(R.mipmap.pause_on);
                        //播放
                        /*natureBinder.stopPlay();
                        playAndPauseBtn.setBackgroundResource(R.mipmap.pause_on);*/

                        //   Toast.makeText(context,"播放",Toast.LENGTH_SHORT).show();



                        break;
                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                        Log.d("pppp","KEYCODE_MEDIA_PAUSE");
                        natureBinder.stopPlay();
                        playAndPauseBtn.setBackgroundResource(R.mipmap.play_on);

                        //   Toast.makeText(context,"暂停",Toast.LENGTH_SHORT).show();
                        /*natureBinder.startPlay(currentMusic, currentPosition);
                        playAndPauseBtn.setBackgroundResource(R.mipmap.play_on);*/

                        break;
                    case KeyEvent.KEYCODE_MEDIA_STOP:
                        //停止
                        break;

                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        natureBinder.toNext();
                        currentMusic++;
                        adapter.setClickPosition(currentMusic);
                        adapter.notifyDataSetChanged();
                      //  Toast.makeText(context, Util.getString(context,R.string.previous), Toast.LENGTH_SHORT).show();
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        natureBinder.toPrevious();
                        currentMusic--;
                        adapter.setClickPosition(currentMusic);
                        adapter.notifyDataSetChanged();
                     //   Toast.makeText(context, Util.getString(context,R.string.next), Toast.LENGTH_SHORT).show();
                        break;

                }
            }
            abortBroadcast();
        }
    }


    ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            natureBinder = (NatureService.NatureBinder) service;
            if (natureBinder == null) {
                Log.d("natureBinder为空", "natureBinder为空!");
            } else {
                Log.d("natureBinder不为空", "natureBinder不为空!");
            }

        }
    };


    //TODO 
    private void filterData(String filterStr) {
        List<MusicLoader.MusicInfo> filterDateList = new ArrayList<>();

        if (TextUtils.isEmpty(filterStr)) {
            filterDateList = musicList;
        } else {
            filterDateList.clear();
            for (MusicLoader.MusicInfo fileInfo : musicList) {
                String name = fileInfo.getTitle();
                String name1;
                if (name.length() >= filterStr.length()) {
                    name1 = name.substring(0, filterStr.length());
                } else {
                    name1 = name.substring(0, name.length());
                }
                if (name1.indexOf(filterStr.toString()) != -1 ||
                        characterParser.getSelling(name).startsWith(filterStr.toString()) ||
                        characterParser.getSelling(name).startsWith(Util.exChange(filterStr.toString()))) {
                    filterDateList.add(fileInfo);
                }
            }
        }
        adapter.setMusicList(filterDateList);
        adapter.notifyDataSetChanged();
        NatureService.setMusicList(filterDateList);

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


    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        //当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
        filterData(charSequence.toString());
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public void onDestroy() {
        getContext().unregisterReceiver(progressReceiver);
        getContext().unbindService(serviceConnection);
        super.onDestroy();
    }
}
