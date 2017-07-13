package com.zly.zly.mediabox.Ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.anye.greendao.gen.FileInfoDao;
import com.anye.greendao.gen.MusicFileInfoDao;
import com.bdsdk.update.BaiDuAutoUpdatePopupwindow;
import com.jieli.bluetoothcontrol.FilePathItem;
import com.orhanobut.logger.Logger;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.zly.zly.mediabox.Adapter.DeviceAdapter;
import com.zly.zly.mediabox.MyLibs.IBluetooth;
import com.zly.zly.mediabox.MyLibs.MyApplication;
import com.zly.zly.mediabox.MyView.ArcProgress.ArcProgress;
import com.zly.zly.mediabox.MyView.ArcProgress.OnTextCenter;
import com.zly.zly.mediabox.MyView.MyToast;
import com.zly.zly.mediabox.R;
import com.zly.zly.mediabox.Utils.BufChangeHex;
import com.zly.zly.mediabox.Utils.Util;
import com.zly.zly.mediabox.bean.CollectFileInfo;
import com.zly.zly.mediabox.bean.CollectMusicFileInfo;
import com.zly.zly.mediabox.bean.CollectPhotoFileInfo;
import com.zly.zly.mediabox.bean.FileInfo;
import com.zly.zly.mediabox.bean.MusicFileInfo;
import com.zly.zly.mediabox.bean.MyDevice;
import com.zly.zly.mediabox.bean.PhotoFileInfo;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DeviceActivity extends Activity {
    private String[] PERMISSION = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    ImageView search;
    ListView deviceListView;
    ImageView loading;
    TextView hint, loadingWord;
    VideoView vv;

    MyApplication myApplication;
    IBluetooth iBluetooth;

    BluetoothDevice a2dpDevice;
    DeviceAdapter deviceAdapter;

    List<MyDevice> myDevice = new ArrayList<>();
    List<BluetoothDevice> BondedDevices = new ArrayList<>();
    private BluetoothAdapter bluetoothAdapter;

    public boolean isInit;
    private boolean isShow;
    private BluetoothA2dp bluetoothA2dp;
    private AlertDialog dialog;
    byte[] head, end;


    //用来记录文件更新进度变量
    int num = 0;
    int totalNum = 0;
    //进度条
    private ArcProgress arcProgress;

    boolean cd = true;
    private MyDeviceBroadcastReceiver myBroadcastReceiver;
    private boolean isUpData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myApplication = (MyApplication) getApplication();
        //百度更新检查
        BaiDuAutoUpdatePopupwindow baiDuAutoUpdatePopupwindow = new BaiDuAutoUpdatePopupwindow(this);
        baiDuAutoUpdatePopupwindow.startCheck();


        head = new byte[2];
        head[0] = (byte) 0x55;
        head[1] = (byte) 0xaa;
        end = new byte[2];
        end[0] = (byte) 0x44;
        end[1] = (byte) 0xbb;


        setPermissions();
        myBroadcastReceiver = new MyDeviceBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("VIDEO");
        intentFilter.addAction("i");
        registerReceiver(myBroadcastReceiver, intentFilter);


        setContentView(R.layout.activity_device);
        setTranslucentStatus();


        initView();
        deviceAdapter = new DeviceAdapter(myDevice, this);
        deviceListView.setAdapter(deviceAdapter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();  //打开蓝牙，需要BLUETOOTH_ADMIN权限
        }


        iBluetooth = myApplication.getIBluetooth();

        BondedDevices = iBluetooth.getBondedDevices();


        if (BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(BluetoothProfile.A2DP) == BluetoothProfile.STATE_CONNECTED) {
            getConnectBt();

        } else {
            //提示NFC连接
            PackageManager pm = getPackageManager();
            boolean nfc = pm.hasSystemFeature(PackageManager.FEATURE_NFC);
            if (nfc) {
                NfcManager manager = (NfcManager) getSystemService(Context.NFC_SERVICE);
                NfcAdapter adapter = manager.getDefaultAdapter();
                if (adapter != null && adapter.isEnabled()) {
                    // adapter存在，能启用
                    if (getSharedPreferences("nfc", Context.MODE_PRIVATE).getBoolean("nfc", true))
                        showNFCDialog(R.string.nfc1);
                } else {
                    if (getSharedPreferences("nfc", Context.MODE_PRIVATE).getBoolean("nfc", true))
                        showNFCDialog(R.string.nfc2);
                }
            }

        }


        iBluetooth.setOnIDeviceListener(new IBluetooth.IDeviceListener() {
            @Override
            public void onFinish() {

            }

            @Override
            public void onFileList(List<FilePathItem> fileList) {

            }

            @Override
            public void onFilePath(String path) {

            }

            @Override
            public void onDevicePause() {

            }

            @Override
            public void onDevicePlay() {

            }

            @Override
            public void onPlayNumber(int number) {

            }

            @Override
            public void onProgressUpdate(int progress) {

            }

            @Override
            public void onDeviceModeUpdate(int mode) {

            }

            @Override
            public void onMusicTotalTime(int totalTime) {

            }

            @Override
            public void onMyData(byte[] buf) {
                String data = BufChangeHex.encodeHexStr(buf).replace(" ", "");
                if (data.length() > 3 && data.substring(0, 4).contains("55aa")) {
                    Logger.d("原始收到数据----->" + data);


                    String name = "";
                    int fileNumber;
                    int type;
                    boolean clickMark = false;
                    boolean love = false;


                    //列表文件信息
                    if (data.substring(4, 6).equals("0b")) {

                        isUpData = true;

                        //文件名
                        cd = false;
                        byte[] nameBytes = new byte[buf.length - 8];
                        System.arraycopy(buf, 6, nameBytes, 0, buf.length - 8);
                        try {
                            // name = intercept(new String(nameBytes,"UTF-8"));
                            //名字编码方式改为“GBK”
                            name = intercept(new String(nameBytes, "GBK"));

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        //文件号
                        fileNumber = Integer.parseInt(data.substring(8, 12), 16);

                            /*byte[] fileNumberBytes = new byte[2];
                            System.arraycopy(buf, 5, fileNumberBytes, 0, 2);
                            fileNumber = BufChangeHex.bytesToInt(fileNumberBytes, 0);*/
                        Log.d("当前文件号", fileNumber + "");

                        Log.d(num + "当前歌曲名", name);
                        //arcProgress.setProgress(num);
                        num++;
                        if (totalNum != 0) {
                            Message msg = new Message();
                            msg.what = (int) Math.ceil(num * 100 / totalNum);
                            msg.obj = arcProgress;
                            handler.sendMessage(msg);
                        }


                        //文件类型
                        if (data.substring(6, 8).equals("02")) {
                            type = 1;
                            try {
                                myApplication.getFileInfoDao().insert(new FileInfo(name, new Long((long) fileNumber), type, clickMark, love, buf));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            //  myApplication.setMusicList(musicList);
                        } else if (data.substring(6, 8).equals("01")) {
                            type = 2;
                            try {
                                myApplication.getMusicFileInfoDao().insert(new MusicFileInfo(name, new Long((long) fileNumber), type, clickMark, love, buf));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            // myApplication.setVideoList(videoList);
                        } else if (data.substring(6, 8).equals("03")) {
                            type = 3;
                            try {
                                myApplication.getPhotoFileInfoDao().insert(new PhotoFileInfo(name, new Long((long) fileNumber), type, clickMark, love, buf));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            // myApplication.setPhotoList(photoList);
                        }

                    } else if (data.substring(4, 6).equals("0d")) {
                        if (data.substring(6, 8).equals("01")) {
                            byte[] total = new byte[4];
                            byte[] capacity = new byte[5];
                            System.arraycopy(buf, 4, total, 0, 4);
                            System.arraycopy(buf, 8, capacity, 0, 5);
                            try {
                                String totalStr = new String(total, "UTF-8");
                                totalNum = Integer.parseInt(totalStr.trim());
                                if (totalNum == 0) {
                                    // Toast.makeText(DeviceActivity.this, "无媒体文件！", Toast.LENGTH_SHORT).show();
                                    // MyToast.makeToast(DeviceActivity.this, -1, Util.getString(DeviceActivity.this, R.string.no_media_files), 1000);
                                    startActivity(new Intent(DeviceActivity.this, MainActivity.class));

                                    if (dialog != null) {
                                        dialog.dismiss();
                                        isShow = false;
                                    }

                                }
                                Log.d("当前总数-----------", totalNum + "");
                                String capacityStr = new String(capacity, "UTF-8");
                                Log.d("当前总容量-----------", capacityStr);
                                saveUpDataInfo(totalStr, capacityStr);

                            } catch (Exception e) {

                                e.printStackTrace();


                            }
                            // getFileList();
                        } else if (data.substring(6, 8).equals("02")) {
                            if (data.substring(8, 10).equals("00")) {
                                //不需要更新
                                startActivity(new Intent(DeviceActivity.this, MainActivity.class));

                            } else if (data.substring(8, 10).equals("01")) {
                                getFileList();
                            }
                            Logger.d("当前存储设备--->" + data.substring(10, 12));
                            if (data.substring(10, 12).equals("03")) {
                                myApplication.setuAndTf(0);
                            } else if (data.substring(10, 12).equals("02")) {
                                myApplication.setuAndTf(1);
                            }

                        } else if (data.substring(6, 8).equals("03")) {
                            //更换存储设备
                            sendBroadcast(new Intent("CHANGEDEVICE"));
                            isShow = false;
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    //  cd = true;
                                    //  getFileList();

                                    verify(0xC0);

                                }
                            }, 2000);


                        } else if (data.substring(6, 8).equals("04")) {
                            //切换的设备不存在
                            MyToast.makeToast(DeviceActivity.this, -1, Util.getString(DeviceActivity.this, R.string.no_device), 1000);

                        }

                    } else if (data.substring(4, 6).equals("0c")) {
                        //当前播放歌曲上报

                        int currentNum = Integer.parseInt(data.substring(8, 12), 16);

                        //文件类型
                        if (data.substring(6, 8).equals("02")) {
                            Intent intent = new Intent("VIDEO");
                            intent.putExtra("VIDEO", currentNum);
                            sendBroadcast(intent);
                            // sendBroadcast(new Intent("V"));
                            myApplication.setLoveOK(true);

                        } else if (data.substring(6, 8).equals("01")) {

                            Intent intent = new Intent("MUSIC");
                            intent.putExtra("MUSIC", currentNum);
                            sendBroadcast(intent);
                            // sendBroadcast(new Intent("M"));
                            myApplication.setLoveOK(true);

                        } else if (data.substring(6, 8).equals("03")) {

                            Intent intent = new Intent("PHOTO");
                            intent.putExtra("PHOTO", currentNum);
                            sendBroadcast(intent);
                            // sendBroadcast(new Intent("P"));
                        }
                        /*if (data.substring(12, 14).equals("00")) {
                            Intent intent0 = new Intent("UNCOLLECT");
                            intent0.putExtra("UNCOLLECT", currentNum);
                            // sendBroadcast(intent0);

                        } else if (data.substring(12, 14).equals("01")) {
                            //收藏中的歌曲
                            Intent intent = new Intent("COLLECT");
                            intent.putExtra("COLLECT", currentNum);
                            sendBroadcast(intent);

                        }*/
                        //收藏中的歌曲
                        //   if (data.substring(12, 14).equals("01")) {
                        //收藏中的歌曲
                        Intent intent = new Intent("COLLECT");
                        intent.putExtra("COLLECT", currentNum);
                        sendBroadcast(intent);

                        //   }

                    } else if (data.substring(4, 6).equals("0e")) {
                        //当前播放状态上报state
                        int state = (int) buf[4];
                        Intent intent = new Intent("STATE");
                        intent.putExtra("STATE", state);
                        sendBroadcast(intent);

                    } else if (data.substring(4, 6).equals("0a")) {
                        int vol = (int) buf[4];
                        Intent intent = new Intent("VOL");
                        intent.putExtra("VOL", vol);
                        sendBroadcast(intent);
                    } else if (data.substring(4, 6).equals("1a")) {
                        if (data.substring(6, 8).equals("01")) {
                            if (data.substring(8, 10).equals("01")) {
                                //可以发送文件列表了
                                if (myApplication.isSendCollectListMark()) {
                                    sendRootCollectList();
                                } else {
                                    sendCollectList();
                                }

                            } else {
                                Toast.makeText(DeviceActivity.this, "Error", Toast.LENGTH_SHORT).show();
                            }
                        } else if (data.substring(6, 8).equals("02")) {
                            if (data.substring(8, 10).equals("01")) {
                                Toast.makeText(DeviceActivity.this, "OK!", Toast.LENGTH_SHORT).show();
                            } else if (data.substring(8, 10).equals("00")) {
                                //固件出小差未接收完全，重新发送收藏文件
                                sendCollectList();
                            }
                        }
                    } else if (data.substring(4, 6).equals("0f")) {
                        if (data.substring(8, 10).equals("01")) {
                            MyToast.makeToast(DeviceActivity.this, R.mipmap.love_on, Util.getString(DeviceActivity.this, R.string.play_collection_files), 800);
                        } else {
                            MyToast.makeToast(DeviceActivity.this, R.mipmap.love, Util.getString(DeviceActivity.this, R.string.cancel_play_collection_files), 800);
                            //TODO
                            sendBroadcast(new Intent("UNCOLLECT"));
                        }
                    } else if (data.substring(4, 6).equals("1c")) {
                        if (data.substring(12, 14).equals("01")) {
                            //文件号
                            int Number = Integer.parseInt(data.substring(8, 12), 16);
                            if (data.substring(6, 8).equals("02")) {
                                //视频
                                FileInfoDao fileInfoDao = myApplication.getFileInfoDao();
                                FileInfo file = fileInfoDao.queryBuilder().where(FileInfoDao.Properties.FileNumber.eq(Number)).build().unique();


                                CollectFileInfo info = new CollectFileInfo();
                                info.setName(file.getName());
                                info.setFileNumber(file.getFileNumber());
                                info.setType(file.getType());
                                info.setClickMark(file.getClickMark());
                                info.setLove(true);
                                info.setBuf(file.getBuf());

                                myApplication.getCollectFileInfoDao().insert(info);

                                if (file != null) {
                                    file.setLove(true);
                                    fileInfoDao.update(file);
                                }
                                sendBroadcast(new Intent("notify"));

                            } else if (data.substring(6, 8).equals("01")) {
                                //音频
                                MusicFileInfoDao fileInfoDao = myApplication.getMusicFileInfoDao();
                                MusicFileInfo file = fileInfoDao.queryBuilder().where(MusicFileInfoDao.Properties.FileNumber.eq(Number)).build().unique();
                                CollectMusicFileInfo info = new CollectMusicFileInfo();
                                info.setName(file.getName());
                                info.setFileNumber(file.getFileNumber());
                                info.setType(file.getType());
                                info.setClickMark(file.getClickMark());
                                info.setLove(true);
                                info.setBuf(file.getBuf());

                                myApplication.getCollectMusicFileInfoDao().insert(info);


                                if (file != null) {

                                    file.setLove(true);
                                    fileInfoDao.update(file);
                                }
                                sendBroadcast(new Intent("notify"));

                            }
                        } else {
                            MyToast.makeToast(DeviceActivity.this, -1, "Error", 800);
                        }
                    }


                }
            }
        });
        iBluetooth.setOnIDeviceManager(new IBluetooth.IDeviceManager() {
            @Override
            public void onFoundDevice(BluetoothDevice bluetoothDevice) {
                /*if(!myDevice.contains(bluetoothDevice)){
                    myDevice.add(new MyDevice(bluetoothDevice,false));
                }*/
                List<String> add = new ArrayList<String>();
                for (MyDevice device : myDevice) {
                    add.add(device.getBluetoothDevice().getAddress());
                }
                if (!add.contains(bluetoothDevice.getAddress())) {
                    myDevice.add(new MyDevice(bluetoothDevice, false));
                }
                deviceAdapter.setList(myDevice);
                deviceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onConnectSuccess() {


                isInit = true;
                getConnectBt();
                BondedDevices = iBluetooth.getBondedDevices();
                //校验列表是否有变化
                verify(0xC0);
                /*new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //校验列表是否有变化
                        verify(0xC0);
                    }
                }, 600);*/


                //  getFileList();
                //  iBluetooth.sendMyData(cmd);


                //   startActivity(new Intent(DeviceActivity.this, MainActivity.class));


            }

            @Override
            public void onConnectFailed() {

            }

            @Override
            public void onDeviceDisconnected() {

            }
        });


    }

    int i = 0;

    private void sendCollectList() {
        byte tot = 0;
        //发送收藏文件
        switch (myApplication.getCurrentMain()) {
            case 1:
                List<CollectFileInfo> listV = myApplication.getListV();
                tot = (byte) listV.size();
                if (i < listV.size()) {
                    CollectFileInfo collectFileInfo = listV.get(i);
                    byte[] cmd1 = new byte[9];
                    cmd1[0] = (byte) 0x55;
                    cmd1[1] = (byte) 0xaa;
                    cmd1[2] = (byte) 0xd0;
                    cmd1[3] = (byte) 0x03;
                    cmd1[4] = (byte) 0x02;
                    cmd1[5] = collectFileInfo.getBuf()[4];
                    cmd1[6] = collectFileInfo.getBuf()[5];
                    cmd1[7] = (byte) 0x44;
                    cmd1[8] = (byte) 0xbb;
                    Intent intent1 = new Intent("QQ");
                    intent1.putExtra("MyData", cmd1);
                    sendBroadcast(intent1);
                    i++;
                } else {
                    i = 0;
                    byte[] cmd = new byte[9];
                    cmd[0] = (byte) 0x55;
                    cmd[1] = (byte) 0xaa;
                    cmd[2] = (byte) 0xf0;
                    cmd[3] = (byte) 0x02;
                    cmd[4] = (byte) 0x02;
                    cmd[5] = (byte) 0x02;
                    cmd[6] = tot;
                    cmd[7] = (byte) 0x44;
                    cmd[8] = (byte) 0xbb;
                    Intent intent = new Intent("QQ");
                    intent.putExtra("MyData", cmd);
                    sendBroadcast(intent);

                }


                break;
            case 2:
                List<CollectMusicFileInfo> listM = myApplication.getListM();
                tot = (byte) listM.size();
                if (i < listM.size()) {
                    CollectMusicFileInfo collectMusicFileInfo = listM.get(i);
                    byte[] cmd = new byte[9];
                    cmd[0] = (byte) 0x55;
                    cmd[1] = (byte) 0xaa;
                    cmd[2] = (byte) 0xd0;
                    cmd[3] = (byte) 0x03;
                    cmd[4] = (byte) 0x01;
                    cmd[5] = collectMusicFileInfo.getBuf()[4];
                    cmd[6] = collectMusicFileInfo.getBuf()[5];
                    cmd[7] = (byte) 0x44;
                    cmd[8] = (byte) 0xbb;
                    Intent intent = new Intent("QQ");
                    intent.putExtra("MyData", cmd);
                    sendBroadcast(intent);
                    i++;
                } else {
                    i = 0;
                    byte[] cmd = new byte[9];
                    cmd[0] = (byte) 0x55;
                    cmd[1] = (byte) 0xaa;
                    cmd[2] = (byte) 0xf0;
                    cmd[3] = (byte) 0x02;
                    cmd[4] = (byte) 0x02;
                    cmd[5] = (byte) 0x01;
                    cmd[6] = tot;
                    cmd[7] = (byte) 0x44;
                    cmd[8] = (byte) 0xbb;
                    Intent intent = new Intent("QQ");
                    intent.putExtra("MyData", cmd);
                    sendBroadcast(intent);
                }
                break;
            case 3:
                List<CollectPhotoFileInfo> listP = myApplication.getListP();
                tot = (byte) listP.size();
                if (i < listP.size()) {
                    CollectPhotoFileInfo collectPhotoFileInfo = listP.get(i);
                    byte[] cmd = new byte[9];
                    cmd[0] = (byte) 0x55;
                    cmd[1] = (byte) 0xaa;
                    cmd[2] = (byte) 0xd0;
                    cmd[3] = (byte) 0x03;
                    cmd[4] = (byte) 0x03;
                    cmd[5] = collectPhotoFileInfo.getBuf()[4];
                    cmd[6] = collectPhotoFileInfo.getBuf()[5];
                    cmd[7] = (byte) 0x44;
                    cmd[8] = (byte) 0xbb;
                    Intent intent = new Intent("QQ");
                    intent.putExtra("MyData", cmd);
                    sendBroadcast(intent);
                    i++;
                } else {
                    i = 0;
                    byte[] cmd = new byte[9];
                    cmd[0] = (byte) 0x55;
                    cmd[1] = (byte) 0xaa;
                    cmd[2] = (byte) 0xf0;
                    cmd[3] = (byte) 0x02;
                    cmd[4] = (byte) 0x02;
                    cmd[5] = (byte) 0x03;
                    cmd[6] = tot;
                    cmd[7] = (byte) 0x44;
                    cmd[8] = (byte) 0xbb;
                    Intent intent = new Intent("QQ");
                    intent.putExtra("MyData", cmd);
                    sendBroadcast(intent);
                }
                break;
        }
        /*byte[] cmd=new byte[8];
        cmd[0]=(byte)0x55;
        cmd[1]=(byte)0xaa;
        cmd[2]=(byte)0xf0;
        cmd[3]=(byte)0x02;
        cmd[4]=(byte)0x02;
        cmd[5]=tot;
        cmd[6]=(byte)0x44;
        cmd[7]=(byte)0xbb;
        Intent intent = new Intent("QQ");
        intent.putExtra("MyData", cmd);
        sendBroadcast(intent);*/
    }

    int j = 0;

    private void sendRootCollectList() {
        byte tot = 0;
        //发送收藏文件
        switch (myApplication.getSendRootCollectListMark()) {
            case 1:
                List<CollectFileInfo> listV = myApplication.getCollectFileInfoDao().loadAll();
                tot = (byte) listV.size();

                if (myApplication.getCollectFileInfoDao().loadAll().size() != 0) {
                    if (j < listV.size()) {
                        CollectFileInfo collectFileInfo = listV.get(j);
                        byte[] cmd1 = new byte[9];
                        cmd1[0] = (byte) 0x55;
                        cmd1[1] = (byte) 0xaa;
                        cmd1[2] = (byte) 0xd0;
                        cmd1[3] = (byte) 0x03;
                        cmd1[4] = (byte) 0x02;
                        cmd1[5] = collectFileInfo.getBuf()[4];
                        cmd1[6] = collectFileInfo.getBuf()[5];
                        cmd1[7] = (byte) 0x44;
                        cmd1[8] = (byte) 0xbb;
                        Intent intent1 = new Intent("QQ");
                        intent1.putExtra("MyData", cmd1);
                        sendBroadcast(intent1);
                        j++;
                    } else {
                        j = 0;
                        byte[] cmd = new byte[9];
                        cmd[0] = (byte) 0x55;
                        cmd[1] = (byte) 0xaa;
                        cmd[2] = (byte) 0xf0;
                        cmd[3] = (byte) 0x02;
                        cmd[4] = (byte) 0x02;
                        cmd[5] = (byte) 0x02;
                        cmd[6] = tot;
                        cmd[7] = (byte) 0x44;
                        cmd[8] = (byte) 0xbb;
                        Intent intent = new Intent("QQ");
                        intent.putExtra("MyData", cmd);
                        sendBroadcast(intent);
                        myApplication.setSendRootCollectListMark(2);
                        MyToast.makeToast(this, -1, Util.getString(this, R.string.synchronous_video_collection_list_complete), 800);

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


                break;
            case 2:
                List<CollectMusicFileInfo> listM = myApplication.getCollectMusicFileInfoDao().loadAll();
                tot = (byte) listM.size();
                if (j < listM.size()) {
                    CollectMusicFileInfo collectMusicFileInfo = listM.get(j);
                    byte[] cmd2 = new byte[9];
                    cmd2[0] = (byte) 0x55;
                    cmd2[1] = (byte) 0xaa;
                    cmd2[2] = (byte) 0xd0;
                    cmd2[3] = (byte) 0x03;
                    cmd2[4] = (byte) 0x01;
                    cmd2[5] = collectMusicFileInfo.getBuf()[4];
                    cmd2[6] = collectMusicFileInfo.getBuf()[5];
                    cmd2[7] = (byte) 0x44;
                    cmd2[8] = (byte) 0xbb;
                    Intent intent2 = new Intent("QQ");
                    intent2.putExtra("MyData", cmd2);
                    sendBroadcast(intent2);
                    j++;
                } else {
                    j = 0;
                    byte[] cmd22 = new byte[9];
                    cmd22[0] = (byte) 0x55;
                    cmd22[1] = (byte) 0xaa;
                    cmd22[2] = (byte) 0xf0;
                    cmd22[3] = (byte) 0x02;
                    cmd22[4] = (byte) 0x02;
                    cmd22[5] = (byte) 0x01;
                    cmd22[6] = tot;
                    cmd22[7] = (byte) 0x44;
                    cmd22[8] = (byte) 0xbb;
                    Intent intent22 = new Intent("QQ");
                    intent22.putExtra("MyData", cmd22);
                    sendBroadcast(intent22);
                    myApplication.setSendRootCollectListMark(1);
                    myApplication.setSendCollectListMark(false);
                    MyToast.makeToast(this, -1, Util.getString(this, R.string.synchronous_music_collection_list_complete), 800);
                }
                break;

        }
        /*byte[] cmd=new byte[8];
        cmd[0]=(byte)0x55;
        cmd[1]=(byte)0xaa;
        cmd[2]=(byte)0xf0;
        cmd[3]=(byte)0x02;
        cmd[4]=(byte)0x02;
        cmd[5]=tot;
        cmd[6]=(byte)0x44;
        cmd[7]=(byte)0xbb;
        Intent intent = new Intent("QQ");
        intent.putExtra("MyData", cmd);
        sendBroadcast(intent);*/
    }

    private void getFileList() {
        final byte[] cmd = new byte[6];
        System.arraycopy(head, 0, cmd, 0, head.length);
        System.arraycopy(intToByteArray1(0xA0), 0, cmd, head.length, intToByteArray1(0xA0).length);
        System.arraycopy(intToByteArray1(0xB0), 0, cmd, head.length + 1, intToByteArray1(0xB0).length);
        System.arraycopy(end, 0, cmd, head.length + 2, end.length);
        clearDao();
        if (!isShow) showD();
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //   iBluetooth.sendMyData(cmd);
        cd = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (cd) {
                        iBluetooth.sendMyData(cmd);
                        cd = false;
                    }

                }
            }
        }).start();
    }

    private void clearDao() {
        myApplication.getFileInfoDao().deleteAll();
        myApplication.getCollectFileInfoDao().deleteAll();

        myApplication.getMusicFileInfoDao().deleteAll();
        myApplication.getCollectMusicFileInfoDao().deleteAll();

        myApplication.getPhotoFileInfoDao().deleteAll();
        myApplication.getCollectPhotoFileInfoDao().deleteAll();
    }

    private void initView() {
        search = (ImageView) findViewById(R.id.search_device);
        deviceListView = (ListView) findViewById(R.id.dvice_listview);
        loading = (ImageView) findViewById(R.id.loading);
        loadingWord = (TextView) findViewById(R.id.loadingword);
        //    loading.setGifImage(R.mipmap.loading);
        loading.setBackgroundResource(R.drawable.load_anim);
        ((AnimationDrawable) loading.getBackground()).start();
        hint = (TextView) findViewById(R.id.hint);
        vv = (VideoView) findViewById(R.id.vv);


        //setWelcomeWord();

        loading.setVisibility(View.GONE);
        loadingWord.setVisibility(View.GONE);
        //搜索设备
        search.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                cd = true;

                search();


            }
        });
        //点击连接
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // iBluetooth.connect(myDevice.get(i).getBluetoothDevice());
                startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));


            }
        });
    }


    @Override
    protected void onRestart() {
        super.onRestart();

        getConnectBt();
        BondedDevices = iBluetooth.getBondedDevices();

        bluetoothAdapter.cancelDiscovery();

        loading.setVisibility(View.VISIBLE);
        loadingWord.setVisibility(View.VISIBLE);

        new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == 1) {
                    loading.setVisibility(View.GONE);
                    loadingWord.setVisibility(View.GONE);
                }
                return true;
            }
        }).sendEmptyMessageDelayed(1, 3500);


        myDevice.clear();
        if (a2dpDevice != null)
            Logger.d("目前连接的A2dp" + a2dpDevice.getAddress() + a2dpDevice.getName());
        for (BluetoothDevice bondedDevice : BondedDevices) {


            if (bondedDevice != null && a2dpDevice != null && a2dpDevice.getAddress().equals(bondedDevice.getAddress())) {
                myDevice.add(new MyDevice(bondedDevice, true));
            } else {
                myDevice.add(new MyDevice(bondedDevice, false));
            }
        }
        deviceAdapter.setList(myDevice);
        deviceAdapter.notifyDataSetChanged();
        iBluetooth.startDiscovery();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String uri = "android.resource://" + getPackageName() + "/" + R.raw.welcome_amin;
        vv.setVideoURI(Uri.parse((uri)));
        //监听播完了重播
        vv.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                vv.start();
            }
        });

        vv.start();
        if (!myApplication.isSearchMark()) {
            search();
        }
        myApplication.setSearchMark(false);

        if (BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(BluetoothProfile.A2DP) == BluetoothProfile.STATE_CONNECTED) {
            getConnectBt();
            //  iBluetooth.initMyBluetooth(DeviceActivity.this);

        }

    }

    void showNFCDialog(int info) {
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        final View dialogView = LayoutInflater.from(this).inflate(R.layout.nfc_dialog, null);
        dialogView.setBackgroundResource(R.drawable.shape_corner);
        final CheckBox cb = (CheckBox) dialogView.findViewById(R.id.checkBox);
        TextView tv = (TextView) dialogView.findViewById(R.id.textView4);
        Button ct = (Button) dialogView.findViewById(R.id.button);
        tv.setText(info);
        ct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = getSharedPreferences("nfc", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                if (cb.isChecked()) {
                    editor.putBoolean("nfc", false);
                } else {
                    editor.putBoolean("nfc", true);
                }
                editor.commit();
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.setContentView(dialogView);

    }


    private void search() {
        if (BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(BluetoothProfile.A2DP) == BluetoothProfile.STATE_CONNECTED) {
            // getConnectBt();
            if (isInit) iBluetooth.release();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            iBluetooth.initMyBluetooth(DeviceActivity.this);
            isInit = true;

        }


        bluetoothAdapter.cancelDiscovery();
        iBluetooth.startDiscovery();

        loading.setVisibility(View.VISIBLE);
        loadingWord.setVisibility(View.VISIBLE);

        new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == 1) {
                    loading.setVisibility(View.GONE);
                    loadingWord.setVisibility(View.GONE);
                }
                return true;
            }
        }).sendEmptyMessageDelayed(1, 4500);


        myDevice.clear();
        if (a2dpDevice != null)
            Logger.d("目前连接的A2dp" + a2dpDevice.getAddress() + a2dpDevice.getName());
        for (BluetoothDevice bondedDevice : iBluetooth.getBondedDevices()) {


            if (bondedDevice != null && a2dpDevice != null && a2dpDevice.getAddress().equals(bondedDevice.getAddress())) {
                myDevice.add(new MyDevice(bondedDevice, true));
            } else {
                myDevice.add(new MyDevice(bondedDevice, false));
            }
        }
        deviceAdapter.setList(myDevice);
        deviceAdapter.notifyDataSetChanged();
        iBluetooth.startDiscovery();
    }


    //检查已连接的蓝牙设备
    private void getConnectBt() {
        int a2dp = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP);
        int headset = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET);
        int health = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEALTH);
        int flag = -1;
        if (a2dp == BluetoothProfile.STATE_CONNECTED) {
            flag = a2dp;
        } else if (headset == BluetoothProfile.STATE_CONNECTED) {
            flag = headset;
        } else if (health == BluetoothProfile.STATE_CONNECTED) {
            flag = health;
        }
        if (flag != -1) {
            bluetoothAdapter.getProfileProxy(DeviceActivity.this, new BluetoothProfile.ServiceListener() {
                @Override
                public void onServiceDisconnected(int profile) {

                }

                @Override
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    bluetoothA2dp = (BluetoothA2dp) proxy;
                    iBluetooth.setA2dp((BluetoothA2dp) proxy);
                    List<BluetoothDevice> mDevices = proxy.getConnectedDevices();
                    if (mDevices != null && mDevices.size() > 0) {
                        for (BluetoothDevice device : mDevices) {

                            a2dpDevice = device;
                            iBluetooth.setA2dpDevice(device);
                        }
                    } else {
                        a2dpDevice = null;
                    }
                }
            }, flag);
        }
    }

    private void getDisConnect() {
        int a2dp = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP);

        int flag = -1;
        if (a2dp == BluetoothProfile.STATE_CONNECTED) {
            flag = a2dp;
        }
        if (flag != -1) {
            bluetoothAdapter.getProfileProxy(DeviceActivity.this, new BluetoothProfile.ServiceListener() {
                @Override
                public void onServiceDisconnected(int profile) {

                }

                @Override
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    bluetoothA2dp = (BluetoothA2dp) proxy;
                    iBluetooth.setA2dp((BluetoothA2dp) proxy);
                    List<BluetoothDevice> mDevices = proxy.getConnectedDevices();
                    if (mDevices != null && mDevices.size() > 0) {
                        for (BluetoothDevice device : mDevices) {


                            if (bluetoothA2dp != null && device != null) {

                                Class<? extends BluetoothA2dp> clazz = bluetoothA2dp.getClass();
                                try {
                                    clazz.getMethod("disconnect", BluetoothDevice.class).invoke(bluetoothA2dp, device);
                                    bluetoothAdapter.closeProfileProxy(BluetoothProfile.A2DP, bluetoothA2dp);

                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                } catch (NoSuchMethodException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }, flag);
        }
    }

    private void showD() {

        isShow = true;
        View v = getLayoutInflater().inflate(R.layout.arc_layout, null);
        // v.setBackground(new BitmapDrawable(BlurUtil.doBlur(BitmapFactory.decodeResource(getResources(), R.mipmap.main_bg_bu), 1, 45)));
        //   v.setBackgroundColor(Color.parseColor("#3f3d3c"));
        //    v.setBackgroundResource(R.mipmap.updata_bg);
        v.setBackgroundResource(R.drawable.shape_corner);
        arcProgress = (ArcProgress) v.findViewById(R.id.myProgress);
        TextView retry = (TextView) v.findViewById(R.id.retry);
        v.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                isShow = false;
            }
        });
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                num = 0;
                clearDao();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        cd = true;
                    }
                }, 500);
            }
        });
        arcProgress.setOnCenterDraw(new OnTextCenter());
        dialog = new AlertDialog.Builder(this).create();
        //始终不消失
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.setContentView(v);
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        /*WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;*/
        /*params.width = (int)(display.getWidth());
        params.height = (int)(display.getHeight());*/
        WindowManager wm = this.getWindowManager();
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        params.width = (int) (width * 0.8);
        params.height = (int) (height * 0.48);
        dialog.getWindow().setAttributes(params);
        // addProrgress(arcProgress);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) iBluetooth.release();
        System.exit(0);
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(myBroadcastReceiver);

        iBluetooth.release();
        bluetoothAdapter.closeProfileProxy(BluetoothProfile.A2DP, bluetoothA2dp);
        super.onDestroy();
    }


    Handler handler = new Handler() {
        int n = 0;

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ArcProgress progressBar = (ArcProgress) msg.obj;
            progressBar.setProgress(msg.what);
            //  if (num == totalNum && totalNum != 0) {

            if (msg.what == 100 && msg.what != n) {
                isUpData = false;
                //如果没有权限则请求权限
                if(ContextCompat.checkSelfPermission(DeviceActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
                    //第二个参数是数组表明可以一次性获取多个权限，后面的请求码要和回调处理函数的请求码对上
                    ActivityCompat.requestPermissions(DeviceActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }else{
                    startActivity(new Intent(DeviceActivity.this, MainActivity.class));
                }
                root();
                num = 0;
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
                isShow = false;
            }
            n = msg.what;
        }
    };

    public void addProrgress(ArcProgress progressBar) {
        Thread thread = new Thread(new ProgressThread(progressBar));
        thread.start();
    }

    class ProgressThread implements Runnable {
        private ArcProgress progressBar;

        public ProgressThread(ArcProgress progressBar) {
            this.progressBar = progressBar;
        }

        @Override
        public void run() {
            while (true) {
                if (isFinishing()) {
                    break;
                }
                if (totalNum != 0) {
                    Message msg = new Message();
                    msg.what = (int) Math.ceil(num * 100 / totalNum);
                    msg.obj = progressBar;
                    SystemClock.sleep(50);
                    handler.sendMessage(msg);
                }
            }
        }
    }

    private void verify(int cmd1) {
        byte[] cmd = new byte[14];
        System.arraycopy(head, 0, cmd, 0, head.length);
        System.arraycopy(BufChangeHex.intToByteArray1(cmd1), 0, cmd, head.length, BufChangeHex.intToByteArray1(cmd1).length);

        if (getUpDataInfo().get("number").equals("0000")) {
            System.arraycopy(end, 0, cmd, 12, end.length);
        } else {
            if ((!getUpDataInfo().get("number").equals("0000")) && myApplication.getFileInfoDao().loadAll().size() == 0 && myApplication.getMusicFileInfoDao().loadAll().size() == 0 && myApplication.getPhotoFileInfoDao().loadAll().size() == 0) {
                cmd[3] = 0;
                cmd[4] = 0;
                cmd[5] = 0;
                cmd[6] = 0;
                cmd[7] = 0;
                cmd[8] = 0;
                cmd[9] = 0;
                cmd[10] = 0;
                cmd[11] = 0;
                System.arraycopy(end, 0, cmd, 12, end.length);
            } else {
                byte[] number = getUpDataInfo().get("number").getBytes();
                cmd[3] = number[0];
                cmd[4] = number[1];
                cmd[5] = number[2];
                cmd[6] = number[3];
                byte[] capacity = getUpDataInfo().get("capacity").getBytes();
                cmd[7] = capacity[0];
                cmd[8] = capacity[1];
                cmd[9] = capacity[2];
                cmd[10] = capacity[3];
                cmd[11] = capacity[4];
                System.arraycopy(end, 0, cmd, 12, end.length);
            }
        }
        iBluetooth.sendMyData(cmd);
    }

    private void root() {
        byte[] cmd = new byte[6];
        System.arraycopy(head, 0, cmd, 0, head.length);
        System.arraycopy(intToByteArray1(0xA0), 0, cmd, head.length, intToByteArray1(0xA0).length);
        System.arraycopy(intToByteArray1(0xB1), 0, cmd, head.length + 1, intToByteArray1(0xB1).length);
        System.arraycopy(end, 0, cmd, head.length + 2, end.length);

        iBluetooth.sendMyData(cmd);
    }

    private void setPermissions() {


        /*if (ContextCompat.checkSelfPermission(DeviceActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            //Android 6.0申请权限
            ActivityCompat.requestPermissions(this, PERMISSION, 1);
        } else {
            Logger.d("权限申请ok");
        }*/


        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(PERMISSION, 1);
            }
        }*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            for(String permission: PERMISSION){
                int ret = checkSelfPermission(permission);
                if (ret != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(PERMISSION, 1);
                    return;
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:

                break;
        }
    }


    /**
     * 为xml 的根布局添加android:fitsSystemWindows=”true” 属性
     */
    protected void setTranslucentStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
        }
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(R.color.mainclior2);// 通知栏所需颜色
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

    /*private void setWelcomeWord() {
        Date d = new Date();
        if (d.getHours() < 11) {
            welcomeWord.setText(Util.getString(this, R.string.good_morning));
        } else if (d.getHours() < 13) {
            welcomeWord.setText(Util.getString(this, R.string.good_afternoon1));
        } else if (d.getHours() < 18) {
            welcomeWord.setText(Util.getString(this, R.string.good_afternoon2));
        } else if (d.getHours() < 24) {
            welcomeWord.setText(Util.getString(this, R.string.good_evening));
        }
    }*/

    /**
     * 保存更新信息
     */
    private void saveUpDataInfo(String number, String capacity) {
        //获得SharedPreferences对象
        SharedPreferences preferences = getSharedPreferences("zly", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("number", number);
        editor.putString("capacity", capacity);

        editor.commit();
    }

    /**
     * 获取更新信息
     */
    private Map<String, String> getUpDataInfo() {
        Map<String, String> params = new HashMap<String, String>();
        SharedPreferences preferences = getSharedPreferences("zly", Context.MODE_PRIVATE);
        params.put("number", preferences.getString("number", "0000"));
        params.put("capacity", preferences.getString("capacity", "00000"));
        return params;
    }

    public static byte[] intToByteArray1(int i) {
        byte[] result = new byte[2];
        byte[] r = new byte[1];
        result[1] = (byte) ((i >> 8) & 0xFF);
        result[0] = (byte) (i & 0xFF);
        r[0] = result[0];
        return r;
    }

    public static byte[] intToByteArray2(int i) {
        byte[] result = new byte[2];
        result[0] = (byte) ((i >> 8) & 0xFF);
        result[1] = (byte) (i & 0xFF);
        return result;
    }

    class MyDeviceBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("DISCONNECTED")) {
                isShow = false;
                if (isUpData) saveUpDataInfo("0000", "00000");

            } else if (intent.getAction().equals("i")) {
                i = 0;
            }


        }
    }

    //利用正则表达式去除乱码
    public String intercept(String str) throws Exception {

        String regex = "[\u4E00-\u9FA5A-Za-z0-9_`~!@#$%^&*()+=|{}:\\s+\\\\s+;\\\\[\\\\].<>/~！@#￥%……&*（）—— ——+|{}【】‘；：”“’。，、-]";

        Matcher matcher = Pattern.compile(regex).matcher(str);

        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            sb.append(matcher.group());
        }

        return sb.toString();
    }


}

