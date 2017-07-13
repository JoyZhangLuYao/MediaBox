package com.zly.zly.mediabox.Fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.anye.greendao.gen.CollectPhotoFileInfoDao;
import com.anye.greendao.gen.PhotoFileInfoDao;
import com.orhanobut.logger.Logger;
import com.zly.zly.mediabox.Adapter.PhotoFileAdapter;
import com.zly.zly.mediabox.Adapter.VideoFileAdapter;
import com.zly.zly.mediabox.MyLibs.MyApplication;
import com.zly.zly.mediabox.MyView.ClearEditText;
import com.zly.zly.mediabox.MyView.MyToast;
import com.zly.zly.mediabox.R;
import com.zly.zly.mediabox.Ui.DeviceActivity;
import com.zly.zly.mediabox.Utils.BufChangeHex;
import com.zly.zly.mediabox.Utils.CharacterParser;
import com.zly.zly.mediabox.Utils.Util;
import com.zly.zly.mediabox.bean.CollectPhotoFileInfo;
import com.zly.zly.mediabox.bean.FileInfo;
import com.zly.zly.mediabox.bean.PhotoFileInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class PhotoFragment extends Fragment implements PhotoFileAdapter.onLove, TextWatcher {
    ClearEditText clearEditText;
    ListView videoListView;
    TextView noFiles;
    PhotoFileAdapter fileAdapter;
    List<PhotoFileInfo> list = new ArrayList<>();
    private CharacterParser characterParser;
    byte[] end;
    private MyPhotoBroadcastReceiver myBroadcastReceiver;
    private MyApplication myApplication;

    @Override
    public void onDestroy() {
        getContext().unregisterReceiver(myBroadcastReceiver);
        super.onDestroy();
    }

    public PhotoFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_photo, container, false);
        end = new byte[2];
        end[0] = (byte) 0x44;
        end[1] = (byte) 0xbb;

        myBroadcastReceiver = new MyPhotoBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("PHOTO");
        intentFilter.addAction("notify");
        getContext().registerReceiver(myBroadcastReceiver, intentFilter);


        myApplication = ((MyApplication) getActivity().getApplication());

        clearEditText = (ClearEditText) view.findViewById(R.id.clearEditText1);
        videoListView = (ListView) view.findViewById(R.id.photo_listview);
        noFiles= (TextView) view.findViewById(R.id.no_files);
        characterParser = CharacterParser.getInstance();
        addData();
        fileAdapter = new PhotoFileAdapter(getContext(), list);
        videoListView.setAdapter(fileAdapter);
        fileAdapter.setOnLove(this);
        videoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //  fileAdapter.setPlayNumber(list.get(i).getFileNumber().intValue());
                //  fileAdapter.notifyDataSetChanged();
                byte[] cmd0 = fileAdapter.getList().get(i).getBuf();
                cmd0[2] = DeviceActivity.intToByteArray1(0xB0)[0];
                byte[] cmd = new byte[7];
                System.arraycopy(cmd0, 0, cmd, 0, 6);
                cmd[6] = 0;
                Intent intent = new Intent("QQ");
                intent.putExtra("MyData", BufChangeHex.concat(cmd, end));
                getContext().sendBroadcast(intent);
                getContext().sendBroadcast(new Intent("PLAY"));
                /*try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                getContext().sendBroadcast(intent);*/
            }
        });
        clearEditText.addTextChangedListener(this);


        return view;
    }

    //数据
    private void addData() {
        list = ((MyApplication) getActivity().getApplication()).getPhotoFileInfoDao().loadAll();
        if(list.size()==0){noFiles.setVisibility(View.VISIBLE);}else {noFiles.setVisibility(View.GONE);}
        /*for (int i = 0; i < 40; i++) {
            if (i == 1) {
                list.add(new FileInfo("张某人的照片Item" + i, new Long((long)i), i, false, true));
            } else if (i == 5) {
                list.add(new FileInfo("王某人的照片Item" + i, new Long((long)i), i, false, false));
            } else if (i == 6) {
                list.add(new FileInfo("Z某人的照片Item" + i, new Long((long)i), i, false, true));
            } else {
                list.add(new FileInfo("张某人的的照片Item" + i, new Long((long)i), i, false, false));
            }

        }*/
    }

    //筛选数据
    private void filterData(String filterStr) {
        List<PhotoFileInfo> filterDateList = new ArrayList<>();

        if (TextUtils.isEmpty(filterStr)) {
            filterDateList = list;
        } else {
            filterDateList.clear();
            for (PhotoFileInfo fileInfo : list) {
                String name = fileInfo.getName();
                String name1;
                if (name.length() >= filterStr.length()) {
                    name1 = name.substring(0, filterStr.length());
                } else {
                    name1 = name.substring(0, name.length());
                }
                if (name.indexOf(filterStr.toString()) != -1 ||
                        characterParser.getSelling(name).startsWith(filterStr.toString()) ||
                        characterParser.getSelling(name).startsWith(Util.exChange(filterStr.toString()))) {
                    filterDateList.add(fileInfo);
                }
            }
        }
        fileAdapter.setList(filterDateList);
        fileAdapter.notifyDataSetChanged();
    }

    //收藏监听
    @Override
    public void love(PhotoFileInfo fileInfo, boolean isLove) {
        PhotoFileInfoDao fileInfoDao = myApplication.getPhotoFileInfoDao();
        if (isLove) {
            CollectPhotoFileInfo info = new CollectPhotoFileInfo();
            info.setName(fileInfo.getName());
            info.setFileNumber(fileInfo.getFileNumber());
            info.setType(fileInfo.getType());
            info.setClickMark(fileInfo.getClickMark());
            info.setLove(fileInfo.getLove());
            info.setBuf(fileInfo.getBuf());

            myApplication.getCollectPhotoFileInfoDao().insert(info);

            PhotoFileInfo file = fileInfoDao.queryBuilder().where(PhotoFileInfoDao.Properties.FileNumber.eq(fileInfo.getFileNumber())).build().unique();
            if (file != null) {
                file.setLove(true);
                fileInfoDao.update(file);
            }
            byte[] cmd = new byte[10];
            cmd[0] = (byte) 0x55;
            cmd[1] = (byte) 0xaa;
            cmd[2] = (byte) 0xd0;
            cmd[3] = (byte) 0x01;
            cmd[4] = (byte) 0x03;
            cmd[5] = fileInfo.getBuf()[4];
            cmd[6] = fileInfo.getBuf()[5];
            cmd[7] = (byte) 0x01;
            cmd[8] = (byte) 0x44;
            cmd[9] = (byte) 0xbb;
            Intent intent = new Intent("QQ");
            intent.putExtra("MyData", cmd);
            getContext().sendBroadcast(intent);

         //   MyToast.makeToast(getContext(), R.mipmap.love_on, "已收藏图片!", 800);
        } else {
            // myApplication.getCollectPhotoFileInfoDao().deleteByKey(fileInfo.getFileNumber());
            List<CollectPhotoFileInfo> collectList = myApplication.getCollectPhotoFileInfoDao().queryBuilder().where(CollectPhotoFileInfoDao.Properties.FileNumber.eq(fileInfo.getFileNumber())).list();
            for (CollectPhotoFileInfo collectFileInfo : collectList) {
                myApplication.getCollectPhotoFileInfoDao().delete(collectFileInfo);
            }

            PhotoFileInfo file = fileInfoDao.queryBuilder().where(PhotoFileInfoDao.Properties.FileNumber.eq(fileInfo.getFileNumber())).build().unique();
            if (file != null) {
                file.setLove(false);
                fileInfoDao.update(file);
            }
            byte[] cmd = new byte[10];
            cmd[0] = (byte) 0x55;
            cmd[1] = (byte) 0xaa;
            cmd[2] = (byte) 0xd0;
            cmd[3] = (byte) 0x01;
            cmd[4] = (byte) 0x03;
            cmd[5] = fileInfo.getBuf()[4];
            cmd[6] = fileInfo.getBuf()[5];
            cmd[7] = (byte) 0x00;
            cmd[8] = (byte) 0x44;
            cmd[9] = (byte) 0xbb;
            Intent intent = new Intent("QQ");
            intent.putExtra("MyData", cmd);
            getContext().sendBroadcast(intent);

            MyToast.makeToast(getContext(), R.mipmap.love, "已取消收藏图片!", 800);
        }
    }

    //检索相关监听
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

    class MyPhotoBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("PHOTO")) {
                int currectNum = intent.getIntExtra("PHOTO", -1);
                Logger.d("当前播放文件号" + currectNum);
                fileAdapter.setPlayNumber(currectNum);
                fileAdapter.notifyDataSetChanged();

            } else if (intent.getAction().equals("notify")) {
                list = ((MyApplication) getActivity().getApplication()).getPhotoFileInfoDao().loadAll();
                fileAdapter.setList(list);
                fileAdapter.notifyDataSetChanged();
            }


        }
    }

}
