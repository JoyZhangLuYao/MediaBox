package com.zly.zly.mediabox.MyLibs;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.jieli.bluetoothcontrol.BluetoothControl;
import com.jieli.bluetoothcontrol.DataTypeChangeHelper;
import com.jieli.bluetoothcontrol.FilePathItem;
import com.jieli.bluetoothcontrol.Flags;
import com.orhanobut.logger.Logger;
import com.zly.zly.mediabox.Utils.BufChangeHex;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by ZhangLuyao on 2017/3/10.
 */

public class IBluetooth {

    private BluetoothControl mBluetoothControl;
    int NO_FLUSH = 0;
    private static BluetoothAdapter mBluetoothAdapter;
    private static IBluetooth iBluetooth = null;
    FilePathItem publicFolderItem;
    private static Context context;
    private IDeviceManager iDeviceManager;
    private IDeviceListener iDeviceListener;
    byte[] newHead = new byte[15];
    byte[] head = new byte[2];
    byte[] cmd_11 = intToByteArray1(0x0b);
    byte[] cmd_12 = intToByteArray1(0x0c);
    byte[] cmd_16 = intToByteArray1(0x10);

    private int myBluetoothPlayNomPlayNO;
    private int myBluetoothPlayStatus;
    private int prePlayStatus;
    private int prePlayNo;

    private BluetoothDevice A2dpDevice;
    private BluetoothA2dp a2dp;

    public BluetoothDevice getA2dpDevice() {
        return A2dpDevice;
    }

    public void setA2dpDevice(BluetoothDevice a2dpDevice) {
        A2dpDevice = a2dpDevice;
    }

    public BluetoothA2dp getA2dp() {
        return a2dp;
    }

    public void setA2dp(BluetoothA2dp a2dp) {
        this.a2dp = a2dp;
    }

    public synchronized static IBluetooth getInstance(Context context) {

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        IBluetooth.context = context;
        if (iBluetooth == null) {
            iBluetooth = new IBluetooth();
        }

        return iBluetooth;
    }

    private IBluetooth() {
    }


    /**
     * 蓝牙连接部分
     */
    //扫描蓝牙
    public void startDiscovery() {
        register();

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //   ((Activity) context).requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                mBluetoothAdapter.startDiscovery();
            } else {
                mBluetoothAdapter.startDiscovery();
            }
        } else {
            mBluetoothAdapter.startDiscovery();
        }*/
        mBluetoothAdapter.startDiscovery();

        new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == 1) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                return true;
            }
        }).sendEmptyMessageDelayed(1, 6000);
    }

    //获取绑定设备
    public List<BluetoothDevice> getBondedDevices() {
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        List<BluetoothDevice> bondList = new ArrayList<>(devices);
        return bondList;
    }

    public void connect(BluetoothDevice device) {
        mBluetoothAdapter.cancelDiscovery();
        switch (device.getBondState()) {
            case BluetoothDevice.BOND_NONE:
                try {
                    Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                    createBondMethod.invoke(device);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case BluetoothDevice.BOND_BONDED:
                try {
                    // connect2(device);
                    if (BluetoothAdapter.getDefaultAdapter().getProfileConnectionState(BluetoothProfile.A2DP) != BluetoothProfile.STATE_CONNECTED) {
                        connectA2DP(device);
                    }
                    Thread.sleep(200);
                    initMyBluetooth(context);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }


    private void connectA2DP(BluetoothDevice device) {
        if (mBluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP) != BluetoothProfile.STATE_CONNECTED) {
            //在listener中完成A2DP服务的调用
            mBluetoothAdapter.getProfileProxy(context, new connServListener(device), BluetoothProfile.A2DP);
        }
    }

    public void disconnectA2DP() {
        if (a2dp != null && A2dpDevice != null) {

            Class<? extends BluetoothA2dp> clazz = a2dp.getClass();
            try {
                clazz.getMethod("disconnect", BluetoothDevice.class).invoke(a2dp, A2dpDevice);
                mBluetoothAdapter.closeProfileProxy(BluetoothProfile.A2DP, a2dp);


            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }


    public class connServListener implements BluetoothProfile.ServiceListener {

        private BluetoothDevice device;

        public connServListener(BluetoothDevice device) {
            this.device = device;
            A2dpDevice = device;
        }

        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            //use reflect method to get the Hide method "connect" in BluetoothA2DP
            a2dp = (BluetoothA2dp) proxy;
            //a2dp.isA2dpPlaying(mBTDevInThread);
            Class<? extends BluetoothA2dp> clazz = a2dp.getClass();


            Method method_Connect;
            //  Method method_disConnect;
            //通过BluetoothA2DP隐藏的connect(BluetoothDevice btDev)函数，打开btDev的A2DP服务
            try {

                          /*
                           * 1.Reflect this method
                             public boolean connect(BluetoothDevice device);
                           *
                           * 2.function definition
                             getMethod(String methodName, Class <?>... paramType)
                           */
                //1.这步相当于定义函数
                method_Connect = clazz.getMethod("connect", BluetoothDevice.class);

                //invoke(object receiver,object... args)
                //2.这步相当于调用函数,invoke需要传入args：BluetoothDevice的实例
                method_Connect.invoke(a2dp, device);


                //   method_disConnect = clazz.getMethod("disconnect", BluetoothDevice.class);
                //    method_disConnect.invoke(a2dp,device);

            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
            // TODO Auto-generated method stub
        }
    }

    //注册广播
    public void register() {
        IntentFilter filter = new IntentFilter();
        //扫描action
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);

        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        context.registerReceiver(MYBroadcast, filter);
    }

    /**
     * 广播监听
     */
    BroadcastReceiver MYBroadcast = new BroadcastReceiver() {
        public static final String TAG = "MyBuletooth";

        @Override
        public void onReceive(final Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {

            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {

            } else if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (iDeviceManager != null) {
                    iDeviceManager.onFoundDevice(device);
                }
                Log.d(TAG, "onReceive: " + device.getAddress() + "\n" + device.getName() + "\n" + device.getUuids());

            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);


                Log.d(TAG, "ACTION_BOND_STATE_CHANGED==" + device.getBondState());
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDING://正在配对
                        Log.d(TAG, "正在配对......");
                        break;
                    case BluetoothDevice.BOND_BONDED://配对结束
                        Log.d(TAG, "完成配对");
                        connectA2DP(device);
                        /*try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }*/

                      new Handler().postDelayed(new Runnable() {
                          @Override
                          public void run() {
                              initMyBluetooth(context);
                          }
                      },300) ;

                        break;
                    case BluetoothDevice.BOND_NONE://取消配对/未配对
                        Log.d(TAG, "取消配对");
                    default:
                        break;
                }
            } else if (action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {
                Log.d(TAG, "ACTION_CONNECTION_STATE_CHANGED");


            } else if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
                Log.d(TAG, "ACTION_ACL_CONNECTED");


            } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                Log.d(TAG, "ACTION_ACL_DISCONNECTED");


            } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)) {
                Log.d(TAG, "ACTION_ACL_DISCONNECT_REQUESTED");

            }
        }
    };

    //断开连接
    public void disConnect(String add) {

        if (mBluetoothControl != null) {
            mBluetoothControl.diconnectDevice(add);
            mBluetoothControl.unregisterBroadcoastReceiver();
            mBluetoothControl.release();
        }
        context.unregisterReceiver(MYBroadcast);


        disconnectA2DP();
    }


    public void initMyBluetooth(Context context) {

        head[0] = (byte) 0xfe;
        head[1] = (byte) 0xfe;
        newHead[0] = 74;
        newHead[1] = 76;
        newHead[2] = 66;
        newHead[3] = 84;
        newHead[4] = (byte) Integer.parseInt("00", 16);
        newHead[5] = (byte) Integer.parseInt("00", 16);
        newHead[6] = (byte) Integer.parseInt("00", 16);
        newHead[7] = (byte) Integer.parseInt("04", 16);
        newHead[8] = (byte) Integer.parseInt("00", 16);
        newHead[9] = (byte) Integer.parseInt("00", 16);
        newHead[10] = (byte) Integer.parseInt("00", 16);
        newHead[11] = (byte) Integer.parseInt("00", 16);
        newHead[12] = (byte) Integer.parseInt("00", 16);
        newHead[13] = (byte) Integer.parseInt("03", 16);
        newHead[14] = (byte) Integer.parseInt("0F", 16);


        mBluetoothControl = getBluetoothControl(context);
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Toast.makeText(context, "没有蓝牙设备，请到设置连接蓝牙！", Toast.LENGTH_LONG).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                //enableBluetoothAdapter();
            } else {
                mBluetoothControl.init();
            }
        }
        //  mBluetoothControl.initBluetoothDevice();
        mBluetoothControl.setVersionIdString("JIELI-APP-ID");

        new Thread(new Runnable() {
            @Override
            public void run() {
                BluetoothDevice bd = null;
                Boolean mark = true;
                while (mark) {
                    if (bd == null && mBluetoothControl != null) {

                        bd = mBluetoothControl.getAudioConnectedDevice();



                    } else {

                        String add = bd.getAddress();
                        mBluetoothControl.tryToConnectBluetoothSocket(add);
                        mBluetoothControl.setMsgHandler(myHandler);
                        mBluetoothControl.setSocketReciveDataHandler(myHandler1);
                        mark = false;
                    }
                }
            }
        }).start();

    }

    private Handler myHandler = new Handler() {


        @Override
        public void handleMessage(Message msg) {


            switch (msg.what) {

                case Flags.MESSAGE_UPDATE_FILELIST:
                    //文件索引
                    FilePathItem folderItem = (FilePathItem) msg.obj;
                    publicFolderItem = folderItem;
                    String showCompleteName = mBluetoothControl.requestFileRootPathsStringArray(folderItem);
                    // 获取文件目录内容
                    List<FilePathItem> lists = mBluetoothControl.requestShowFileList(folderItem);
                    if (iDeviceListener != null) {
                        iDeviceListener.onFileList(lists);
                        iDeviceListener.onFilePath(showCompleteName);
                    }
                    if (msg.arg2 == 1) {
                        iDeviceListener.onFinish();
                    }
                    break;
                //当前进度
                case Flags.MESSAGE_CURRENT_TIME:
                    if (iDeviceListener != null) {
                        iDeviceListener.onProgressUpdate(msg.arg1);
                    }
                    break;
                //总时间
                case Flags.MESSAGE_TOTAL_TIME:

                    if (iDeviceListener != null) {
                        iDeviceListener.onMusicTotalTime(msg.arg1);
                    }
                    break;
                case Flags.MESSAGE_RFCOMM_CONNECTED:
                    //发送版本匹配命令（必要）
                    Log.d("----------", "Socket连接成功！");
                    mBluetoothControl.setEnableIdPair(true);
                    mBluetoothControl.tryToSendControlCmd(Flags.CONTROL_DEVICE_AND_APP_VERSION);

                    break;
                case Flags.MESSAGE_VERSION_ID_SUCCESS_PAIRED:

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("QQ", "版本匹配成功！！");

                            try {

                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            if (mBluetoothControl.getPlayModeInfoList() == null) {
                                mBluetoothControl.tryToSendControlCmd(Flags.CONTROL_FLAG_GET_MODE);

                                try {
                                    Thread.sleep(300);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if (mBluetoothControl.getPlayModeInfoList() != null) {
                                    //  context.sendBroadcast(new Intent(Mark.INIT_SUCCESS));
                                    if (iDeviceManager != null) {
                                        try {
                                            Thread.sleep(500);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        iDeviceManager.onConnectSuccess();
                                    }
                                }

                            }
                        }
                    }).start();

                    /*try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
                    final Thread thread = new Thread(new Runnable() {


                        @Override
                        public void run() {
                            while (true) {

                                myBluetoothPlayStatus = iBluetooth.getPlayStatus();
                                myBluetoothPlayNomPlayNO = iBluetooth.getPlayNumbr();
                                if (myBluetoothPlayNomPlayNO != prePlayNo) {
                                    //播放文件改变
                                    Log.d("播放文件改变为---", "" + myBluetoothPlayNomPlayNO);
                                    if (iDeviceListener != null)
                                        iDeviceListener.onPlayNumber(myBluetoothPlayNomPlayNO);
                                }
                                if (myBluetoothPlayStatus != prePlayStatus) {

                                    if (myBluetoothPlayStatus == 0) {
                                        Log.d("播放状态改变为---", "播放");
                                        if (iDeviceListener != null) iDeviceListener.onDevicePlay();
                                    } else if (myBluetoothPlayStatus == 1) {
                                        Log.d("播放状态改变为---", "暂停");
                                        if (iDeviceListener != null)
                                            iDeviceListener.onDevicePause();
                                    }
                                }
                                prePlayStatus = myBluetoothPlayStatus;
                                prePlayNo = myBluetoothPlayNomPlayNO;
                                try {
                                    Thread.sleep(80);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    });
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            thread.start();
                        }
                    },3000);

                    break;

                case Flags.MESSAGE_VERSION_ID_FAILED_PAIRED:
                    Log.d("QQ", "版本匹配失败！！");
                    if (iDeviceManager != null) {
                        iDeviceManager.onConnectFailed();
                    }

                    break;
                case Flags.MESSAGE_UPDATE_MODE:
                    //当前硬件所处的模式
                    if (iDeviceListener != null) {
                        iDeviceListener.onDeviceModeUpdate(msg.arg1);
                    }
                    /*if (msg.arg1 == 0) {
                        setDeviceMode(1);
                    }*/

                    break;
                //断开连接回调！！
                case Flags.MESSAGE_RFCOMM_DISCONNECTED:
                    if (iDeviceManager != null) {
                        iDeviceManager.onDeviceDisconnected();
                    }
                    Log.d("QQ", "设备已断开，请重新连接！");
                    mBluetoothControl.release();
                    context.sendBroadcast(new Intent("DISCONNECTED"));
                    break;


            }
        }

    };

    private Handler myHandler1 = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Flags.SOCKET_SOURCE_DATA://0x7777
                    byte[] data0 = (byte[]) msg.obj;
                    /*if (data0.length < 15) return;
                    byte[] data = new byte[data0.length - 15];
                    System.arraycopy(data0, 15, data, 0, data0.length - 15);
                    if (iDeviceListener != null) {
                        iDeviceListener.onMyData(data);
                    }*/
                    if (iDeviceListener != null) {
                        iDeviceListener.onMyData(data0);
                    }

                default:

            }
        }
    };

    public int getPlayMode() {
        byte b = mBluetoothControl.getCurrentPlayMode();
        int playMode = -1;
        if ((b & (1 << 0)) == 1) {
            playMode = 0;//全设备循环
        } else if (((b & (1 << 1)) >> 1) == 1) {
            playMode = 1;//单设备循环
        } else if (((b & (1 << 2)) >> 2) == 1) {
            playMode = 2;//单曲循环
        } else if (((b & (1 << 3)) >> 3) == 1) {
            playMode = 3;//随机
        } else if (((b & (1 << 4)) >> 4) == 1) {
            playMode = 4;//文件夹循环
        }
        return playMode;
    }

    //设置模式
    void setDeviceMode(int i) {
        byte[] param = new byte[1];
        param[0] = (byte) i;//当param[0] = 2时，进入手机音乐模式
        if (!mBluetoothControl.tryToSendControlCmd(Flags.CONTROL_FLAG_MODE, param[0], null)) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mBluetoothControl.tryToSendControlCmd(Flags.CONTROL_FLAG_MODE, param[0], null);
        }
    }

    //获取根目录
    void getRootFile() {
        mBluetoothControl.createBrowserBond();
        mBluetoothControl.resetStoreCacheList();
        FilePathItem rootItem = FilePathItem.getDefaultRootItem();
        rootItem.request_paths_intArray = new ArrayList<Integer>();
        rootItem.request_paths_strArray = new ArrayList<String>();
        rootItem.request_paths_intArray.add(0, rootItem.ROOT_CLUSTER);
        rootItem.request_paths_strArray.add(0, rootItem.ROOT_NAME);
        send2RemoteDevice(rootItem, rootItem.request_paths_intArray, 0);
    }

    //刷新加载更多
    public void LoadMore() {
        publicFolderItem.request_paths_intArray = new ArrayList<Integer>(publicFolderItem.request_paths_intArray);
        publicFolderItem.request_paths_strArray = new ArrayList<String>(publicFolderItem.request_paths_strArray);
        send2RemoteDevice(publicFolderItem, publicFolderItem.request_paths_intArray, NO_FLUSH);

    }

    //获下一级目录
    void getSubFile(FilePathItem info) {
        //TODO more than 9 level  for ui dialog
        if (info.paths_intArray.size() >= 9) {
            Log.e("error", "can not go in more than 9 level ");
            return;
        }

        info.request_paths_intArray = new ArrayList<Integer>(info.paths_intArray);
        info.request_paths_strArray = new ArrayList<String>(info.paths_stringArray);
        info.request_paths_intArray.add(info.cluster);
        info.request_paths_strArray.add(info.mShowName);
        send2RemoteDevice(info, info.request_paths_intArray, NO_FLUSH);
    }

    //返回上一层级
    void getFatherFile() {

        //TODO less than ROOT level  for ui dialog
        if (publicFolderItem.cluster == FilePathItem.ROOT_CLUSTER) {
            Log.e("error", "can not go in more than 9 level ");
            return;
        }

        if (publicFolderItem != null)
            mBluetoothControl.requestFileItem(publicFolderItem.father_cluster);

    }

    //这一坨代码是获取当前时间和总时间
    void getTimeRequest() {
        byte[] allData = new byte[22];
        byte[] allRealData = new byte[16];
        byte[] jlid = DataTypeChangeHelper.StringToByte(Flags.JLID);
        System.arraycopy(jlid, 0, allRealData, 0, jlid.length);
        byte[] jlidValue = new byte[8];
        byte[] curt = DataTypeChangeHelper.StringToByte(Flags.CURT);
        int myCurtLength = DataTypeChangeHelper.byteArrayToInt(curt);
        byte[] curtBigLength = DataTypeChangeHelper.little_intToByte(myCurtLength, 4);
        byte[] tott = DataTypeChangeHelper.StringToByte(Flags.TOTT);
        int myTottLength = DataTypeChangeHelper.byteArrayToInt(tott);
        byte[] tottBigLength = DataTypeChangeHelper.little_intToByte(myTottLength, 4);
        System.arraycopy(curt, 0, jlidValue, 0, curtBigLength.length);
        System.arraycopy(tott, 0, jlidValue, 4, tottBigLength.length);
        byte[] jlidLength = DataTypeChangeHelper.int2byte(jlidValue.length);
        int myJlidLength = DataTypeChangeHelper.byteArrayToInt(jlidLength);
        byte[] jlidBigLength = DataTypeChangeHelper.little_intToByte(myJlidLength, 4);
        System.arraycopy(jlidBigLength, 0, allRealData, 4, jlid.length);
        System.arraycopy(jlidValue, 0, allRealData, 8, jlidValue.length);
        byte[] packageFlag = new byte[2];
        packageFlag[0] = (byte) 0xFF;
        packageFlag[1] = (byte) 0xFF;
        byte[] packageFLagBig = DataTypeChangeHelper.little_intToByte(DataTypeChangeHelper.toInt(packageFlag), 2);
        System.arraycopy(packageFLagBig, 0, allData, 0, packageFLagBig.length); //Package_FLAG
        byte[] crc = DataTypeChangeHelper.intToByte(DataTypeChangeHelper.CRC16_Check(allRealData, 2));
        byte[] crcBig = DataTypeChangeHelper.little_intToByte(DataTypeChangeHelper.toInt(crc), 2);
        System.arraycopy(crcBig, 0, allData, 2, crcBig.length); //CRC
        byte[] packageLengthBig = DataTypeChangeHelper.little_intToByte(allRealData.length, 2);
        System.arraycopy(packageLengthBig, 0, allData, 4, packageLengthBig.length); //Package_lengtth(不包含头6个byte)
        System.arraycopy(allRealData, 0, allData, 6, allRealData.length);
        byte[] param = new byte[1];
        param[0] = 0;
        //发送CURT<当前时间>和TOTT<总时间>的请求
        mBluetoothControl.tryToSendEQControlCmd(Flags.CONTROL_FLAG_OP, Flags.CONTROL_MODE_DEVICE, Flags.CONTROL_FLAG_REQUEST, param, allData);
    }

    //播放或者暂停
    void playOrPause() {
        if (mBluetoothControl.getDevicePlayStatus() == 0) { //播放
            byte[] param = new byte[1];
            param[0] = Flags.CONTROL_FLAG_DEVICE_PAUSE;
            mBluetoothControl.tryToSendControlCmd(Flags.CONTROL_FLAG_OP, Flags.CONTROL_MODE_DEVICE, Flags.CONTROL_FLAG_DEVICE, param); //暂停
        } else if (mBluetoothControl.getDevicePlayStatus() == 1) { //暂停
            byte[] param = new byte[1];
            param[0] = Flags.CONTROL_FLAG_DEVICE_PLAY;
            mBluetoothControl.tryToSendControlCmd(Flags.CONTROL_FLAG_OP, Flags.CONTROL_MODE_DEVICE, Flags.CONTROL_FLAG_DEVICE, param); //暂停
        }
    }

    //上一曲
    void playPrevious() {
        byte[] paramPrev = new byte[1];
        paramPrev[0] = Flags.CONTROL_FLAG_DEVICE_PREV;
        mBluetoothControl.tryToSendControlCmd(Flags.CONTROL_FLAG_OP, Flags.CONTROL_MODE_DEVICE, Flags.CONTROL_FLAG_MY_DEVICE, paramPrev); //上一首歌曲
    }

    //下一曲
    void playNext() {
        byte[] paramNext = new byte[1];
        paramNext[0] = Flags.CONTROL_FLAG_DEVICE_NEXT;
        mBluetoothControl.tryToSendControlCmd(Flags.CONTROL_FLAG_OP, Flags.CONTROL_MODE_DEVICE, Flags.CONTROL_FLAG_MY_DEVICE, paramNext); //下一首歌曲
    }

    //播放模式
    void setMode(int i) {
        //循环模式 22单曲，23随机，24文件夹循环

        mBluetoothControl.socketWrite(sendMyData(head, cmd_16, i, 0, 0));
        /*byte[] param = new byte[1];
        param[0] = (byte) (1 << (byte) i);
        mBluetoothControl.tryToSendControlCmd(Flags.CONTROL_FLAG_OP, Flags.CONTROL_MODE_DEVICE, Flags.CONTROL_MODE_CHOOSE_MODE, param);*/
    }


    //点播文件
    void play(int folderIndex, int musicIndex) {
        mBluetoothControl.socketWrite(sendMyData(head, cmd_12, folderIndex, musicIndex + 1, 0));
    }

    //获取硬件播放的状态(0播放，1暂停)
    int getPlayStatus() {
        int status = -1;
        status = mBluetoothControl.getDevicePlayStatus();
        return status;
    }

    //获取当前播放文件号
    int getPlayNumbr() {
        return mBluetoothControl.getClusNum();
    }

    BluetoothControl getBluetoothControl(Context context) {
        if (mBluetoothControl == null) {
            mBluetoothControl = new BluetoothControl(context);

        }
        return mBluetoothControl;
    }

    private void send2RemoteDevice(FilePathItem item, List<Integer> mIntArray, int flush_flag) {
        //参数3为文件一次刷新文件数量，默认为10个
        mBluetoothControl.requestFileNames(item, mIntArray, 10, flush_flag, item.had_FileOrFolder_index + 1);

    }

    //命令发送
    private byte[] sendMyData(byte[] head, byte[] cmd, int p1, int p2, int p3) {
        byte[] send = new byte[15];
        System.arraycopy(head, 0, send, 0, head.length);
        System.arraycopy(cmd, 0, send, head.length, cmd.length);
        System.arraycopy(intToByteArray4(p1), 0, send, head.length + cmd.length, intToByteArray4(p1).length);
        System.arraycopy(intToByteArray4(p2), 0, send, head.length + cmd.length + intToByteArray4(p1).length, intToByteArray4(p2).length);
        System.arraycopy(intToByteArray4(p3), 0, send, head.length + cmd.length + intToByteArray4(p1).length + intToByteArray4(p2).length, intToByteArray4(p3).length);
        return concat(newHead, send);
    }

    //发送自定义数据
    public void sendMyData(byte[] buf) {
        //   mBluetoothControl.socketWrite(concat(newHead, buf));


        mBluetoothControl.socketWrite(buf);
        Logger.d("自定义数据发送-->" + BufChangeHex.encodeHexStr(buf));
    }


    private byte[] concat(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    private byte[] intToByteArray4(int i) {
        byte[] result = new byte[4];
        result[3] = (byte) ((i >> 24) & 0xFF);
        result[2] = (byte) ((i >> 16) & 0xFF);
        result[1] = (byte) ((i >> 8) & 0xFF);
        result[0] = (byte) (i & 0xFF);
        return result;
    }

    private byte[] intToByteArray1(int i) {
        byte[] result = new byte[2];
        byte[] r = new byte[1];
        result[1] = (byte) ((i >> 8) & 0xFF);
        result[0] = (byte) (i & 0xFF);
        r[0] = result[0];
        return r;
    }


    static class Mark {
        public static final String INIT_SUCCESS = "init_success";
        public static final String INIT_FAILED = "init_failed";
        public static final String DISCONNECTED = "disconnected";
        public static final String FILEMARK = "filemark";
        public static final String MODE = "mode";
        public static final String CURRENT_TIME = "current_time";
        public static final String TOTAL_TIME = "total_time";
        public static final String MY_DATA = "my_data";
        public static final String REPORTED_PREV = "reported_prev";
        public static final String REPORTED_NEXT = "reported_next";
        public static final String REPORTED_FILENO = "reported_no";

    }

    public interface IDeviceManager {

        void onFoundDevice(BluetoothDevice bluetoothDevice);

        void onConnectSuccess();

        void onConnectFailed();

        // void onDeviceConnected();
        void onDeviceDisconnected();

    }

    public interface IDeviceListener {

        void onFinish();

        void onFileList(List<FilePathItem> fileList);

        void onFilePath(String path);

        void onDevicePause();

        void onDevicePlay();

        void onPlayNumber(int number);

        void onProgressUpdate(int progress);

        void onDeviceModeUpdate(int mode);

        void onMusicTotalTime(int totalTime);

        void onMyData(byte[] buf);

    }

    //释放对象
    public void release() {
        if (mBluetoothControl != null) mBluetoothControl.release();
    }

    public void setOnIDeviceManager(IDeviceManager iDeviceManager) {
        this.iDeviceManager = iDeviceManager;
    }

    public void setOnIDeviceListener(IDeviceListener iDeviceListener) {
        this.iDeviceListener = iDeviceListener;
    }
}
