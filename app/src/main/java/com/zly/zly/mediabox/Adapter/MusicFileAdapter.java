package com.zly.zly.mediabox.Adapter;

import android.app.Application;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zly.zly.mediabox.MyLibs.MyApplication;
import com.zly.zly.mediabox.MyView.MyToast;
import com.zly.zly.mediabox.R;
import com.zly.zly.mediabox.bean.FileInfo;
import com.zly.zly.mediabox.bean.MusicFileInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tim on 2017/3/31.
 */

public class MusicFileAdapter extends BaseAdapter {
    Context context;
    MyApplication myApplication;
    LayoutInflater layoutInflater;
    List<MusicFileInfo> list = new ArrayList<>();
    static onLove onLove;
    int playNumber = -1;

    public MusicFileAdapter(Context context, List<MusicFileInfo> list, MyApplication myApplication) {
        this.context = context;
        this.myApplication = myApplication;
        this.list = list;
        this.layoutInflater = LayoutInflater.from(context);
    }

    public List<MusicFileInfo> getList() {
        return list;
    }

    public void setList(List<MusicFileInfo> list) {
        this.list = list;
    }

    public int getPlayNumber() {
        return playNumber;
    }

    public void setPlayNumber(int playNumber) {
        this.playNumber = playNumber;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public MusicFileInfo getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public MusicFileAdapter.onLove getOnLove() {
        return onLove;
    }

    public void setOnLove(MusicFileAdapter.onLove onLove) {
        this.onLove = onLove;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final ViewHolder viewHolder;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.file_item, viewGroup, false);
            viewHolder = new ViewHolder((ImageView) view.findViewById(R.id.play_mark), (TextView) view.findViewById(R.id.name), (ImageView) view.findViewById(R.id.love), (RelativeLayout) view.findViewById(R.id.play_bg));
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        final MusicFileInfo fileInfo = list.get(i);
        viewHolder.name.setText(fileInfo.getName());
        if (fileInfo.getClickMark() || playNumber == fileInfo.getFileNumber()) {
            viewHolder.playMark.setVisibility(View.VISIBLE);
            viewHolder.bg.setBackgroundColor(context.getResources().getColor(R.color.play_bg));
        } else {
            viewHolder.playMark.setVisibility(View.INVISIBLE);
            viewHolder.bg.setBackgroundColor(context.getResources().getColor(R.color.color_w));
        }
        if (fileInfo.getLove()) {
           // viewHolder.love.setBackgroundResource(R.mipmap.uncollect);
            viewHolder.love.setImageResource(R.mipmap.uncollect);
            viewHolder.love.setTag(R.mipmap.love_on);
        } else {
           // viewHolder.love.setBackgroundResource(R.mipmap.collect);
            viewHolder.love.setImageResource(R.mipmap.collect);
            viewHolder.love.setTag(R.mipmap.love);
        }
        viewHolder.love.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onLove != null) {
                    if (myApplication.isLoveOK()) {

                        if ((int) viewHolder.love.getTag() == R.mipmap.love_on) {
                            //viewHolder.love.setBackgroundResource(R.mipmap.love);
                            // viewHolder.love.setTag(R.mipmap.love);
                            // MusicFileAdapter.onLove.love(fileInfo, false);
                        } else if ((int) viewHolder.love.getTag() == R.mipmap.love) {
                            // viewHolder.love.setBackgroundResource(R.mipmap.love_on);
                            viewHolder.love.setTag(R.mipmap.love_on);
                            MusicFileAdapter.onLove.love(fileInfo, true);
                        }

                    } else {
                        MyToast.makeToast(context, -1, "当前设备模式不支持此操作", 1000);
                    }
                }
            }
        });


        return view;
    }

    class ViewHolder {
        public ViewHolder(ImageView playMark, TextView name, ImageView love, RelativeLayout bg) {
            this.playMark = playMark;
            this.name = name;
            this.love = love;
            this.bg = bg;
        }

        ImageView playMark;
        TextView name;
        ImageView love;
        RelativeLayout bg;
    }

    public interface onLove {
        void love(MusicFileInfo fileInfo, boolean isLove);
    }
}
