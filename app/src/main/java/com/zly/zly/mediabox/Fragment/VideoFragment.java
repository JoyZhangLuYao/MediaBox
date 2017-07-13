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

import com.anye.greendao.gen.CollectFileInfoDao;
import com.anye.greendao.gen.FileInfoDao;
import com.orhanobut.logger.Logger;
import com.zly.zly.mediabox.Adapter.VideoFileAdapter;
import com.zly.zly.mediabox.MyLibs.MyApplication;
import com.zly.zly.mediabox.MyView.ClearEditText;
import com.zly.zly.mediabox.MyView.MyToast;
import com.zly.zly.mediabox.R;
import com.zly.zly.mediabox.Ui.DeviceActivity;
import com.zly.zly.mediabox.Utils.BufChangeHex;
import com.zly.zly.mediabox.Utils.CharacterParser;
import com.zly.zly.mediabox.Utils.Util;
import com.zly.zly.mediabox.bean.CollectFileInfo;
import com.zly.zly.mediabox.bean.FileInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class VideoFragment extends Fragment implements VideoFileAdapter.onLove, TextWatcher {
    ClearEditText clearEditText;
    ListView videoListView;
    TextView noFiles;
    VideoFileAdapter videoFileAdapter;
    List<FileInfo> list = new ArrayList<>();
    private CharacterParser characterParser;
    MyApplication myApplication;
    private byte[] end;
    private MyVideoBroadcastReceiver myBroadcastReceiver;

    @Override
    public void onDestroy() {
        getContext().unregisterReceiver(myBroadcastReceiver);
        super.onDestroy();
    }

    public VideoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        end = new byte[2];
        end[0] = (byte) 0x44;
        end[1] = (byte) 0xbb;

        myBroadcastReceiver = new MyVideoBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("VIDEO");
        intentFilter.addAction("notify");
        getContext().registerReceiver(myBroadcastReceiver, intentFilter);

        myApplication = ((MyApplication) getActivity().getApplication());
        clearEditText = (ClearEditText) view.findViewById(R.id.clearEditText1);
        videoListView = (ListView) view.findViewById(R.id.video_listview);
        noFiles= (TextView) view.findViewById(R.id.no_files);
        characterParser = CharacterParser.getInstance();
        addData();
        videoFileAdapter = new VideoFileAdapter(getContext(), list, myApplication);
        videoListView.setAdapter(videoFileAdapter);
        videoFileAdapter.setOnLove(this);
        videoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //   videoFileAdapter.setPlayNumber(list.get(i).getFileNumber().intValue());
                //  videoFileAdapter.notifyDataSetChanged();
                byte[] cmd0 = videoFileAdapter.getList().get(i).getBuf();
                cmd0[2] = DeviceActivity.intToByteArray1(0xB0)[0];
                byte[] cmd = new byte[7];
                System.arraycopy(cmd0, 0, cmd, 0, 6);
                cmd[6] = 0;
                Intent intent = new Intent("QQ");
                intent.putExtra("MyData", BufChangeHex.concat(cmd, end));
                getContext().sendBroadcast(intent);

                getContext().sendBroadcast(new Intent("PLAY"));


            }
        });
        clearEditText.addTextChangedListener(this);

        return view;
    }

    private void addData() {

        list = myApplication.getFileInfoDao().loadAll();
        if(list.size()==0){noFiles.setVisibility(View.VISIBLE);}else {noFiles.setVisibility(View.GONE);}

    }

    private void filterData(String filterStr) {
        List<FileInfo> filterDateList = new ArrayList<>();

        if (TextUtils.isEmpty(filterStr)) {
            filterDateList = list;
        } else {
            filterDateList.clear();
            for (FileInfo fileInfo : list) {
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
        videoFileAdapter.setList(filterDateList);
        videoFileAdapter.notifyDataSetChanged();
    }

    //收藏监听
    @Override
    public void love(FileInfo fileInfo, boolean isLove) {
        //   FileInfoDao fileInfoDao = myApplication.getFileInfoDao();
        if (isLove) {/*

                CollectFileInfo info = new CollectFileInfo();
                info.setName(fileInfo.getName());
                info.setFileNumber(fileInfo.getFileNumber());
                info.setType(fileInfo.getType());
                info.setClickMark(fileInfo.getClickMark());
                info.setLove(fileInfo.getLove());
                info.setBuf(fileInfo.getBuf());

                // myApplication.getCollectFileInfoDao().insert(new CollectFileInfo(fileInfo.getName(),fileInfo.getFileNumber(),fileInfo.getType(),fileInfo.getClickMark(),fileInfo.getLove(),fileInfo.getBuf()));
                myApplication.getCollectFileInfoDao().insert(info);


                FileInfo file = fileInfoDao.queryBuilder().where(FileInfoDao.Properties.FileNumber.eq(fileInfo.getFileNumber())).build().unique();
                if (file != null) {
                    file.setLove(true);
                    fileInfoDao.update(file);
                }*/
            byte[] cmd = new byte[10];
            cmd[0] = (byte) 0x55;
            cmd[1] = (byte) 0xaa;
            cmd[2] = (byte) 0xd0;
            cmd[3] = (byte) 0x01;
            cmd[4] = (byte) 0x02;
            cmd[5] = fileInfo.getBuf()[4];
            cmd[6] = fileInfo.getBuf()[5];
            cmd[7] = (byte) 0x01;
            cmd[8] = (byte) 0x44;
            cmd[9] = (byte) 0xbb;
            Intent intent = new Intent("QQ");
            intent.putExtra("MyData", cmd);
            getContext().sendBroadcast(intent);

            //  MyToast.makeToast(getContext(), R.mipmap.love_on, "已收藏视频!", 800);
        } else {/*
                // myApplication.getCollectFileInfoDao().deleteByKey(fileInfo.getFileNumber());
                List<CollectFileInfo> collectList = myApplication.getCollectFileInfoDao().queryBuilder().where(CollectFileInfoDao.Properties.FileNumber.eq(fileInfo.getFileNumber())).list();
                for (CollectFileInfo collectFileInfo : collectList) {
                    myApplication.getCollectFileInfoDao().delete(collectFileInfo);
                }

                FileInfo file = fileInfoDao.queryBuilder().where(FileInfoDao.Properties.FileNumber.eq(fileInfo.getFileNumber())).build().unique();
                if (file != null) {
                    file.setLove(false);
                    fileInfoDao.update(file);
                }

                byte[] cmd = new byte[10];
                cmd[0] = (byte) 0x55;
                cmd[1] = (byte) 0xaa;
                cmd[2] = (byte) 0xd0;
                cmd[3] = (byte) 0x01;
                cmd[4] = (byte) 0x02;
                cmd[5] = fileInfo.getBuf()[4];
                cmd[6] = fileInfo.getBuf()[5];
                cmd[7] = (byte) 0x00;
                cmd[8] = (byte) 0x44;
                cmd[9] = (byte) 0xbb;
                Intent intent = new Intent("QQ");
                intent.putExtra("MyData", cmd);
                getContext().sendBroadcast(intent);

                MyToast.makeToast(getContext(), R.mipmap.love, "已取消收藏视频!", 800);
            */
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

    class MyVideoBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("VIDEO")) {
                int currectNum = intent.getIntExtra("VIDEO", -1);
                Logger.d("当前播放文件号" + currectNum);
                videoFileAdapter.setPlayNumber(currectNum);
                videoFileAdapter.notifyDataSetChanged();



            } else if (intent.getAction().equals("notify")) {
                list = ((MyApplication) getActivity().getApplication()).getFileInfoDao().loadAll();
                videoFileAdapter.setList(list);
                videoFileAdapter.notifyDataSetChanged();
            }


        }
    }
}
