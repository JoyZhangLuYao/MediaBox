package com.zly.zly.mediabox.Fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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

import com.anye.greendao.gen.CollectMusicFileInfoDao;
import com.anye.greendao.gen.MusicFileInfoDao;
import com.orhanobut.logger.Logger;
import com.zly.zly.mediabox.Adapter.MusicFileAdapter;
import com.zly.zly.mediabox.Adapter.VideoFileAdapter;
import com.zly.zly.mediabox.MyLibs.MyApplication;
import com.zly.zly.mediabox.MyView.ClearEditText;
import com.zly.zly.mediabox.MyView.MyToast;
import com.zly.zly.mediabox.R;
import com.zly.zly.mediabox.Ui.DeviceActivity;
import com.zly.zly.mediabox.Utils.BufChangeHex;
import com.zly.zly.mediabox.Utils.CharacterParser;
import com.zly.zly.mediabox.Utils.Util;
import com.zly.zly.mediabox.bean.CollectMusicFileInfo;
import com.zly.zly.mediabox.bean.FileInfo;
import com.zly.zly.mediabox.bean.MusicFileInfo;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusicFragment extends Fragment implements MusicFileAdapter.onLove, TextWatcher {
    ClearEditText clearEditText;
    ListView videoListView;
    TextView noFiles;
    MusicFileAdapter fileAdapter;
    List<MusicFileInfo> list = new ArrayList<>();
    private CharacterParser characterParser;
    private byte[] end;
    private MyMusicBroadcastReceiver myBroadcastReceiver;
    private MyApplication myApplication;

    @Override
    public void onDestroy() {
        getContext().unregisterReceiver(myBroadcastReceiver);
        super.onDestroy();
    }

    public MusicFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_music, container, false);
        end = new byte[2];
        end[0] = (byte) 0x44;
        end[1] = (byte) 0xbb;

        myBroadcastReceiver = new MyMusicBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("MUSIC");
        intentFilter.addAction("notify");
        getContext().registerReceiver(myBroadcastReceiver, intentFilter);

        myApplication = ((MyApplication) getActivity().getApplication());

        clearEditText = (ClearEditText) view.findViewById(R.id.clearEditText1);
        videoListView = (ListView) view.findViewById(R.id.music_listview);
        characterParser = CharacterParser.getInstance();
        noFiles= (TextView) view.findViewById(R.id.no_files);
        addData();
        fileAdapter = new MusicFileAdapter(getContext(), list, myApplication);
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

    private void addData() {

        list = myApplication.getMusicFileInfoDao().loadAll();
        if(list.size()==0){noFiles.setVisibility(View.VISIBLE);}else {noFiles.setVisibility(View.GONE);}

    }


    //筛选数据
    private void filterData(String filterStr) {
        List<MusicFileInfo> filterDateList = new ArrayList<>();

        if (TextUtils.isEmpty(filterStr)) {
            filterDateList = list;
        } else {
            filterDateList.clear();
            for (MusicFileInfo fileInfo : list) {
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
    public void love(MusicFileInfo fileInfo, boolean isLove) {

        // MusicFileInfoDao fileInfoDao = myApplication.getMusicFileInfoDao();
        if (isLove) {/*
                CollectMusicFileInfo info = new CollectMusicFileInfo();
                info.setName(fileInfo.getName());
                info.setFileNumber(fileInfo.getFileNumber());
                info.setType(fileInfo.getType());
                info.setClickMark(fileInfo.getClickMark());
                info.setLove(fileInfo.getLove());
                info.setBuf(fileInfo.getBuf());

                //  myApplication.getCollectMusicFileInfoDao().insert(new CollectMusicFileInfo(fileInfo.getName(),fileInfo.getFileNumber(),fileInfo.getType(),fileInfo.getClickMark(),fileInfo.getLove(),fileInfo.getBuf()));
                myApplication.getCollectMusicFileInfoDao().insert(info);

                MusicFileInfo file = fileInfoDao.queryBuilder().where(MusicFileInfoDao.Properties.FileNumber.eq(fileInfo.getFileNumber())).build().unique();
                if (file != null) {
                    file.setLove(true);
                    fileInfoDao.update(file);
                }*/
            byte[] cmd = new byte[10];
            cmd[0] = (byte) 0x55;
            cmd[1] = (byte) 0xaa;
            cmd[2] = (byte) 0xd0;
            cmd[3] = (byte) 0x01;
            cmd[4] = (byte) 0x01;
            cmd[5] = fileInfo.getBuf()[4];
            cmd[6] = fileInfo.getBuf()[5];
            cmd[7] = (byte) 0x01;
            cmd[8] = (byte) 0x44;
            cmd[9] = (byte) 0xbb;
            Intent intent = new Intent("QQ");
            intent.putExtra("MyData", cmd);
            getContext().sendBroadcast(intent);


            //  MyToast.makeToast(getContext(), R.mipmap.love_on, "已收藏音乐!", 800);

        } else {/*
                //myApplication.getCollectMusicFileInfoDao().deleteByKey(fileInfo.getFileNumber());

                List<CollectMusicFileInfo> collectList = myApplication.getCollectMusicFileInfoDao().queryBuilder().where(CollectMusicFileInfoDao.Properties.FileNumber.eq(fileInfo.getFileNumber())).list();
                for (CollectMusicFileInfo collectFileInfo : collectList) {
                    myApplication.getCollectMusicFileInfoDao().delete(collectFileInfo);
                }

                MusicFileInfo file = fileInfoDao.queryBuilder().where(MusicFileInfoDao.Properties.FileNumber.eq(fileInfo.getFileNumber())).build().unique();
                if (file != null) {
                    file.setLove(false);
                    fileInfoDao.update(file);
                }
                byte[] cmd = new byte[10];
                cmd[0] = (byte) 0x55;
                cmd[1] = (byte) 0xaa;
                cmd[2] = (byte) 0xd0;
                cmd[3] = (byte) 0x01;
                cmd[4] = (byte) 0x01;
                cmd[5] = fileInfo.getBuf()[4];
                cmd[6] = fileInfo.getBuf()[5];
                cmd[7] = (byte) 0x00;
                cmd[8] = (byte) 0x44;
                cmd[9] = (byte) 0xbb;
                Intent intent = new Intent("QQ");
                intent.putExtra("MyData", cmd);
                getContext().sendBroadcast(intent);

                MyToast.makeToast(getContext(), R.mipmap.love, "已取消收藏音乐!", 800);
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

    class MyMusicBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("MUSIC")) {
                int currectNum = intent.getIntExtra("MUSIC", -1);
                Logger.d("当前播放文件号" + currectNum);
                fileAdapter.setPlayNumber(currectNum);
                fileAdapter.notifyDataSetChanged();

            } else if (intent.getAction().equals("notify")) {
                list = ((MyApplication) getActivity().getApplication()).getMusicFileInfoDao().loadAll();
                fileAdapter.setList(list);
                fileAdapter.notifyDataSetChanged();
            }


        }
    }

}
